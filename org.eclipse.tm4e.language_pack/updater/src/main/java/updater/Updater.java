/**
 * Copyright (c) 2023 Sebastian Thomschke and contributors.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * based on https://github.com/sebthom/extra-syntax-highlighting-eclipse-plugin/blob/main/plugin/updater
 */
package updater;

import static updater.utils.Git.gitSparseCheckout;
import static updater.utils.Log.*;
import static updater.utils.ObjectMappers.YAML;
import static updater.utils.Strings.*;
import static updater.utils.Sys.*;
import static updater.utils.Validation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JacksonException;

import updater.utils.Git.GitCheckoutConfig;
import updater.utils.Git.GitCheckoutState;
import updater.utils.Log.WithToString;
import updater.utils.Strings;
import updater.utils.Sys;

/**
 * @author Sebastian Thomschke
 */
public class Updater {

	/**
	 * model for update-syntaxes-config.yaml
	 */
	record Config( //
			@JsonProperty(required = true) String contentTypePrefix, //
			@JsonProperty(required = true) String contentTypePriority, //
			@JsonProperty(required = true) Map<String, Source> sources, //
			@JsonProperty(required = true) Targets targets //
	) {
		record Targets( //
				@JsonProperty(required = true) String sourceReposCacheDir, //
				@JsonProperty(required = true) String pluginXml, //
				@JsonProperty(required = true) String readmeMd, //
				@JsonProperty(required = true) String stateFile, //
				@JsonProperty(required = true) String syntaxesDir) {
		}

		static class Language extends WithToString {
			public boolean update = true;
			public String label;
			public String scopeName;
			public String grammar;
			public String langcfg;
			public String example;
			public List<String> fileExtensions;
			public List<String> fileNames;
			public List<String> filePatterns;
		}

		static class LanguageIgnoreable extends Language {
			public @JsonProperty("ignored") String ignoredReason;
		}

		static class InlineGrammar extends WithToString {
			public boolean update = true;
			public String grammar;
		}

		static class InlineGrammarIgnoreable extends InlineGrammar {
			public @JsonProperty("ignored") String ignoredReason;
		}

		@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
		@JsonSubTypes({ //
			@JsonSubTypes.Type(value = CustomSource.class, name = "custom"), //
			@JsonSubTypes.Type(value = VSCodeSingleExtensionSource.class, name = "vscode-extension"), //
			@JsonSubTypes.Type(value = VSCodeMultiExtensionsSource.class, name = "vscode-extensions") //
		})
		abstract static class Source extends WithToString {
			public @JsonProperty(required = true) GitCheckoutConfig github;
			public boolean licenseDownload = true;
		}

		static class CustomSource extends Source {
			public @JsonProperty(required = true) Map<String /*langId*/, Language> languages;
			public Map<String /*scopeName*/, InlineGrammar> inlineGrammars = Collections.emptyMap();
		}

		static class VSCodeSingleExtensionSource extends Source {
			public Map<String /*langId*/, LanguageIgnoreable> languages = Collections.emptyMap();
			public Map<String /*scopeName*/, InlineGrammarIgnoreable> inlineGrammars = Collections.emptyMap();
		}

		static class VSCodeMultiExtensionsSource extends Source {
			static class Extension extends WithToString {
				public @JsonProperty("ignored") String ignoredReason;
				public Map<String /*langId*/, LanguageIgnoreable> languages = Collections.emptyMap();
				public Map<String /*scopeName*/, InlineGrammarIgnoreable> inlineGrammars = Collections.emptyMap();
			}

			public boolean includeAllByDefault = false;
			public Map<String, Extension> extensions = Collections.emptyMap();
		}
	}

	/**
	 * model for update-syntaxes-state.yaml
	 */
	record State(SortedMap<String, ExtensionState> extensions) {
		static class ExtensionState extends WithToString {
			public GitCheckoutState github;
			public SortedMap<String /* langId */, LanguageState> languages = new TreeMap<>();
			public @JsonProperty("inline-grammars") SortedSet<String /* scopeName */> inlineGrammarScopeNames = new TreeSet<>();
		}

		static class LanguageState extends WithToString {
			public String label;
			public String scopeName;
			public String upstreamURL;
			public SortedSet<String> fileExtensions;
			public SortedSet<String> fileNames;
			public SortedSet<String> filePatterns;
		}
	}

	/**
	 * model for VSCode extension package.json
	 */
	record VsCodeExtensionPackageJson( //
			@JsonProperty(required = true) String name, //
			String icon, //
			String license, //
			String version, //
			@JsonProperty(required = true) Contributions contributes //
	) {
		record Contributions( //
				@JsonProperty(required = true) List<Grammar> grammars, //
				@JsonProperty(required = true) List<Language> languages //
		) {
			record Grammar( //
					String language, //
					@JsonProperty(required = true) String scopeName, //
					@JsonProperty(required = true) String path) {
			}

			record Language( //
					@JsonProperty(required = true) String id, //
					List<String> aliases, //
					@JsonProperty("extensions") List<String> fileExtensions, //
					@JsonProperty("filenames") List<String> fileNames, //
					@JsonProperty("filenamePatterns") List<String> filePatterns, //
					Icon icon, //
					String configuration) {
				record Icon(String light, String dark) {
				}
			}
		}
	}

	private static final Path CONFIG_FILE = Path.of("updater-config.yaml").toAbsolutePath().normalize();

	public static void main(final String... args) throws Exception {
		try {
			new Updater().run(args.length == 0 ? null : args[0]);
		} catch (final JacksonException ex) {
			logError(ex);
			System.exit(1);
		}
	}

	private final Config config;
	private final State state;
	private final Path stateFile;
	private final Path sourceReposCacheDir;
	private final Path syntaxesDir;

	public Updater() throws Exception {
		logInfo("Loading [" + CONFIG_FILE + "]...");
		config = YAML.readValue(CONFIG_FILE.toFile(), Config.class);
		try (var l = withLogIndented()) {
			logInfo("-> " + config.sources.size() + " source repos defined");
		}

		syntaxesDir = toAbsolutePath(CONFIG_FILE.getParent(), Path.of(config.targets.syntaxesDir));
		logInfo("Syntaxes Folder: [" + syntaxesDir + "]");

		sourceReposCacheDir = toAbsolutePath( //
				CONFIG_FILE.getParent(), //
				Path.of(config.targets.sourceReposCacheDir.replace("{TEMP_DIR}", Sys.TEMP_DIR.toString())));
		logInfo("Source Repos Cache Dir: [" + sourceReposCacheDir + "]");

		stateFile = toAbsolutePath(CONFIG_FILE.getParent(), Path.of(config.targets.stateFile));
		if (Files.exists(stateFile)) {
			logInfo("Loading [" + stateFile + "]...");
			state = YAML.readValue(stateFile.toFile(), State.class);
		} else {
			state = new State(new TreeMap<>());
		}
	}

	private void downloadFiles(final String sourceIdToUpdate) throws IOException, InterruptedException {
		int i = 0;
		for (final var sourceEntry : config.sources.entrySet()) {
			i++;
			final var sourceId = sourceEntry.getKey();
			final var source = sourceEntry.getValue();

			if (sourceIdToUpdate == null) {
				logHeader("[" + i + "/" + config.sources.size() + "] " //
						+ "Processing [" + sourceId + "] " //
						+ "(" + source.getClass().getSimpleName() + ")");
			} else {
				if (!sourceId.equals(sourceIdToUpdate)) {
					continue;
				}
				logHeader("[1/1] " //
						+ "Processing [" + sourceId + "] " //
						+ "(" + source.getClass().getSimpleName() + ")");
			}

			final var sourceRepoPath = sourceReposCacheDir.resolve(sourceId);
			final var gitCheckoutState = gitSparseCheckout(sourceRepoPath, source.github);

			if (source instanceof final Config.CustomSource src) {
				final var extensionState = new State.ExtensionState();
				extensionState.github = gitCheckoutState;
				state.extensions.put(sourceId, extensionState);
				new CustomSourceHandler(sourceId, src, sourceRepoPath, syntaxesDir, extensionState).handle();
			} else if (source instanceof final Config.VSCodeSingleExtensionSource src) {
				final var extensionState = new State.ExtensionState();
				extensionState.github = gitCheckoutState;
				state.extensions.put(sourceId, extensionState);
				new VSCodeSingleExtensionSourceHandler(sourceId, src, sourceRepoPath, syntaxesDir, extensionState).handle();
			} else if (source instanceof final Config.VSCodeMultiExtensionsSource src) {
				new VSCodeMultiExtensionsSourceHandler(sourceId, src, sourceRepoPath, gitCheckoutState, syntaxesDir, state.extensions)
						.handle();
			}

			logInfo("Saving state to [" + stateFile + "]");
			try (var writer = Files.newBufferedWriter(stateFile)) {
				writer.write("# AUTO-GENERATED FILE - Do not edit manually; changes will be lost.");
				writer.newLine();
				YAML.writeValue(writer, state);
			}
		}
	}

	/**
	 * @param sourceId the source to get updates from or null to get updates from all sources
	 */
	public void run(final String sourceId) throws Exception {
		downloadFiles(sourceId);
		updatePluginXML();
		updateReadmeMD();
		updateAboutHTML();
		logHeader("** DONE **");
	}

	private void updateAboutHTML() throws IOException {
		logHeader("Updating [about.html.]...");

		final var aboutLines = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		for (final var ext : state.extensions.entrySet()) {
			final var extId = ext.getKey();
			final var extState = ext.getValue();
			for (final var lang : extState.languages.entrySet()) {
				final var langId = lang.getKey();
				final var langState = lang.getValue();

				if (isEmpty(langState.fileExtensions) && isEmpty(langState.fileNames) && isEmpty(langState.filePatterns)) {
					continue;
				}

				final var syntaxDir = syntaxesDir.resolve(extId);
				final var iconFileName = Files.exists(syntaxDir.resolve(langId + ".png")) ? langId + ".png"
						: Files.exists(syntaxDir.resolve("icon.png")) ? "icon.png" : null;

				final var templateVars = new HashMap<String, Object>();

				templateVars.put("label", langState.label);
				templateVars.put("icon", iconFileName == null ? ""
						: " <img src=\"syntaxes/" + extId + "/" + iconFileName + "\" width=16/>");

				templateVars.put("file_associations", Arrays.asList( //
						isEmpty(langState.fileExtensions) ? null
								: "file-extensions=\""
										+ join(langState.fileExtensions.stream().map(Strings::removeLeadingDot).distinct().sorted(), ", ")
										+ "\"", //
						isEmpty(langState.fileNames) ? null : "file-names=\"" + join(langState.fileNames, ", ") + "\"", //
						isEmpty(langState.filePatterns) ? null : "file-patterns=\"" + join(langState.filePatterns, ", ") + "\"" //
				).stream().filter(Objects::nonNull).collect(Collectors.joining("<br />")).replace("*", "\\*"));

				templateVars.put("repo_name", extState.github.repo);
				templateVars.put("repo_ref", extState.github.ref);
				templateVars.put("repo_path", extState.github.path == null ? "" : extState.github.path);
				templateVars.put("commit", extState.github.commit);

				templateVars.put("upstream_url",
						isURL(langState.upstreamURL) ? " <a href=\"" + langState.upstreamURL + "\">[upstream]</a>" : "");

				aboutLines.put(langState.label, render(
						"""
							<tr>
							  <td>{label}{icon}</td>
							  <td>{file_associations}</td>
							  <td><a href="https://github.com/{repo_name}/tree/{commit}/{repo_path}">{repo_ref}@{repo_name}</a>{upstream_url}
							</tr>""",
						templateVars));
			}
		}

		final var aboutHtml = Path.of(config.targets.readmeMd).getParent().resolve("about.html").toAbsolutePath().normalize();
		logInfo("Saving [" + aboutHtml + "]...");
		Files.writeString(aboutHtml, replaceSubstringBetween(Files.readString(aboutHtml), //
				"<!-- START-GENERATED -->", "<!-- END-GENERATED -->", //
				"\n" + join(aboutLines.values(), "\n") + "\n"));
	}

	private void updatePluginXML() throws IOException {
		logHeader("Updating [plugin.xml]...");
		final var pluginLines = new StringBuilder();
		for (final var ext : state.extensions.entrySet()) {
			final var extId = ext.getKey();
			final var extCfg = ext.getValue();

			final var syntaxDir = syntaxesDir.resolve(extId);

			for (final var lang : extCfg.languages.entrySet()) {
				final var langId = lang.getKey();
				final var langState = lang.getValue();

				logInfo("Rendering entry [" + extId + "/" + langId + "]...");

				final var grammarFile = findFirstFile(syntaxDir, //
						f -> f.matches(Pattern.quote(langId) + "[.]tmLanguage[.](yaml|json|plist)"));

				final var iconFileName = Files.exists(syntaxDir.resolve(langId + ".png")) ? langId + ".png"
						: Files.exists(syntaxDir.resolve("icon.png")) ? "icon.png" : null;

				final var exampleFile = findFirstFile(syntaxDir, //
						f -> f.matches(Pattern.quote(langId) + "[.]example[.].*"));

				final var templateVars = new HashMap<String, Object>();
				templateVars.put("ext_id", extId);
				templateVars.put("lang_id", langId);
				templateVars.put("label", langState.label);
				templateVars.put("content_type_prefix", config.contentTypePrefix);
				templateVars.put("content_type_priority", config.contentTypePriority);
				templateVars.put("content_type_id", config.contentTypePrefix + "." + langId);
				templateVars.put("scope_name", langState.scopeName);
				templateVars.put("grammar_filename", grammarFile.get().getFileName());
				templateVars.put("icon_filename", iconFileName);
				templateVars.put("example_filename", exampleFile.isPresent() ? exampleFile.get().getFileName() : null);

				List<String> fileExtensions = new ArrayList<>();
				List<String> fileNames = new ArrayList<>();
				List<String> filePatterns = new ArrayList<>();

				if (langState.fileExtensions != null) {
					fileExtensions.addAll(langState.fileExtensions.stream() //
							.map(Strings::removeLeadingDot) //
							.filter(e -> !e.contains(".")) // ignore extensions with dot, e.g. html.jinja2
							.toList());

					filePatterns.addAll(langState.fileExtensions.stream() //
							.map(Strings::removeLeadingDot) //
							.filter(e -> e.contains(".")) // select extensions with dot, e.g. html.jinja2
							.map(e -> "*." + e) // convert them to a pattern, e.g. "*.html.jinja2"
							.toList());
				}

				if (langState.fileNames != null) {
					fileNames.addAll(langState.fileNames);
				}

				if (langState.filePatterns != null) {
					filePatterns.addAll(langState.filePatterns);
				}

				fileExtensions = fileExtensions.stream().distinct().sorted().toList();
				fileNames = fileNames.stream().distinct().sorted().toList();
				filePatterns = filePatterns.stream().distinct().sorted().toList();

				templateVars.put("file_associations", Arrays.asList( //
						fileExtensions.isEmpty() ? null : "file-extensions=\"" + join(fileExtensions, ",") + "\"", //
						fileNames.isEmpty() ? null : "file-names=\"" + join(fileNames, ",") + "\"", //
						filePatterns.isEmpty() ? null : "file-patterns=\"" + join(filePatterns, ",") + "\"" //
				).stream().filter(Objects::nonNull).collect(Collectors.joining(" ")));

				pluginLines.append(render(
						"""
							\n
							<!-- ======================================== -->
							<!-- {ext_id}/{lang_id}: {label} -->
							<!-- ======================================== -->
							<extension point="org.eclipse.core.contenttype.contentTypes">
							  <content-type id="{content_type_id}" name="{label}" base-type="{content_type_prefix}.basetype" priority="{content_type_priority}"
							                {file_associations} />
							</extension>
							<extension point="org.eclipse.tm4e.registry.grammars">
							  <grammar scopeName="{scope_name}" path="syntaxes/{ext_id}/{grammar_filename}" />
							  <scopeNameContentTypeBinding scopeName="{scope_name}" contentTypeId="{content_type_id}" />
							</extension>""",
						templateVars));

				if (Files.exists(syntaxDir.resolve(langId + ".language-configuration.json"))) {
					pluginLines.append(render(
							"""
								\n
								<extension point="org.eclipse.tm4e.languageconfiguration.languageConfigurations">
								  <languageConfiguration contentTypeId="{content_type_id}" path="syntaxes/{ext_id}/{lang_id}.language-configuration.json" />
								</extension>""",
							templateVars));
				}

				if (iconFileName != null) {
					pluginLines.append(render("""
						\n
						<extension point="org.eclipse.ui.genericeditor.icons">
						  <icon contentType="{content_type_id}" icon="syntaxes/{ext_id}/{icon_filename}"/>
						</extension>""", templateVars));
				}

				if (exampleFile.isPresent()) {
					pluginLines.append(render("""
						\n
						<extension point="org.eclipse.tm4e.ui.snippets">
						  <snippet name="{label} Example" path="syntaxes/{ext_id}/{example_filename}" scopeName="{scope_name}" />
						</extension>""", templateVars));
				}
			}

			if (!extCfg.inlineGrammarScopeNames.isEmpty()) {

				pluginLines.append(render("""
					\n
					<!-- ======================================== -->
					<!-- {ext_id}: Inline Grammars -->
					<!-- ======================================== -->
					<extension point="org.eclipse.tm4e.registry.grammars">
					""", templateVars -> templateVars.put("ext_id", extId)));

				for (final var inlineGrammarScopeName : extCfg.inlineGrammarScopeNames) {

					final var grammarFile = findFirstFile(syntaxDir, //
							f -> f.matches(Pattern.quote(inlineGrammarScopeName) + "[.]tmLanguage[.](yaml|json|plist)"));

					pluginLines.append(render("""
						  <grammar scopeName="{scope_name}" path="syntaxes/{ext_id}/{grammar_filename}" />
						""", templateVars -> {
						templateVars.put("ext_id", extId);
						templateVars.put("scope_name", inlineGrammarScopeName);
						templateVars.put("grammar_filename", grammarFile.get().getFileName());
					}));
				}
				pluginLines.append("</extension>");
			}
		}

		final var pluginXML = Path.of(config.targets.pluginXml).toAbsolutePath().normalize();
		logInfo("Saving [" + pluginXML + "]...");
		Files.writeString(pluginXML, //
				replaceSubstringBetween(Files.readString(pluginXML), //
						"<!-- START-GENERATED -->", "<!-- END-GENERATED -->", //
						indent(2, pluginLines.toString()) + "\n\n  ") //
		);
	}

	private void updateReadmeMD() throws IOException {
		logHeader("Updating [README.md.]...");

		final var readmeLines = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		for (final var ext : state.extensions.entrySet()) {
			final var extId = ext.getKey();
			final var extState = ext.getValue();
			for (final var lang : extState.languages.entrySet()) {
				final var langId = lang.getKey();
				final var langState = lang.getValue();

				if (isEmpty(langState.fileExtensions) && isEmpty(langState.fileNames) && isEmpty(langState.filePatterns)) {
					continue;
				}

				final var syntaxDir = syntaxesDir.resolve(extId);
				final var iconFileName = Files.exists(syntaxDir.resolve(langId + ".png")) ? langId + ".png"
						: Files.exists(syntaxDir.resolve("icon.png")) ? "icon.png" : null;

				final var templateVars = new HashMap<String, Object>();

				templateVars.put("label", langState.label);
				templateVars.put("icon", iconFileName == null ? ""
						: " <img src=\"syntaxes/" + extId + "/" + iconFileName + "\" width=16/>");

				templateVars.put("file_associations", //
						Arrays.asList( //
								isEmpty(langState.fileExtensions) ? null
										: "file-extensions=\""
												+ join(langState.fileExtensions.stream().map(Strings::removeLeadingDot).distinct().sorted(),
														", ")
												+ "\"", //
								isEmpty(langState.fileNames) ? null : "file-names=\"" + join(langState.fileNames, ", ") + "\"", //
								isEmpty(langState.filePatterns) ? null : "file-patterns=\"" + join(langState.filePatterns, ", ") + "\"" //
						).stream() //
								.filter(Objects::nonNull) //
								.collect(Collectors.joining("<br />")) //
								.replace("*", "\\\\*"));

				templateVars.put("repo_name", extState.github.repo);
				templateVars.put("repo_ref", extState.github.ref);
				templateVars.put("repo_path", extState.github.path == null ? "" : extState.github.path);
				templateVars.put("commit", extState.github.commit);

				templateVars.put("upstream_url", isURL(langState.upstreamURL) ? " [[upstream]](" + langState.upstreamURL + ")" : "");

				readmeLines.put(langState.label, render(
						"""
							| {label}{icon} | {file_associations} | [{repo_ref}@{repo_name}](https://github.com/{repo_name}/tree/{commit}/{repo_path}){upstream_url}
							""",
						templateVars));
			}
		}

		final var readmeMD = Path.of(config.targets.readmeMd).toAbsolutePath().normalize();
		logInfo("Saving [" + readmeMD + "]...");
		Files.writeString(readmeMD, //
				replaceSubstringBetween(Files.readString(readmeMD), //
						"<!-- START-GENERATED -->", "<!-- END-GENERATED -->", //
						"""
							\n
							| Language/Format | File Associations | Source
							|:--------------- |:----------------- |:------ |
							""" + join(readmeLines.values(), "") + "\n") //
		);
	}

}
