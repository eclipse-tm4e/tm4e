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
import java.util.Comparator;
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

import updater.Updater.Config.Source;
import updater.Updater.State.ExtensionState;
import updater.Updater.State.InlineGrammarState;
import updater.Updater.State.LanguageState;
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
			@JsonProperty(required = true) String contentBaseType, //
			@JsonProperty(required = true) String contentTypePrefix, //
			@JsonProperty(required = true) String contentTypePriority, //
			@JsonProperty(required = true) Map<String, Source> sources, //
			@JsonProperty(required = true) Targets targets) {

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
			public List<String> injectTo;
			public List<String> fileExtensions;
			public List<String> fileNames;
			public List<String> filePatterns;

			public String contentBaseType;
			public String contentDescriber;
		}

		static class LanguageIgnoreable extends Language {
			public @JsonProperty("ignored") String ignoredReason;
		}

		static class InlineGrammar extends WithToString {
			public boolean update = true;
			public String grammar;
			public List<String> injectTo;
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
			/** list of build commands */
			public List<String> build = Collections.emptyList();
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

		public /* @Nullable */ Language findSourceLanguageConfig(final String extId, final String langId) {
			final Source source = sources.get(extId);
			if (source instanceof final CustomSource src)
				return src.languages.get(langId);
			if (source instanceof final VSCodeSingleExtensionSource src)
				return src.languages.get(langId);

			for (final var source1 : sources.values())
				if (source1 instanceof final VSCodeMultiExtensionsSource src) {
					final var ext = src.extensions.get(extId);
					if (ext != null)
						return ext.languages.get(langId);
				}
			return null;
		}
	}

	/**
	 * model for update-syntaxes-state.yaml
	 */
	record State(SortedMap<String, ExtensionState> extensions) {
		static class ExtensionState extends WithToString {
			public GitCheckoutState github;
			public SortedMap<String /* langId */, LanguageState> languages = new TreeMap<>();
			public @JsonProperty("inline-grammars") SortedSet<InlineGrammarState> inlineGrammars = new TreeSet<>();
		}

		static class InlineGrammarState extends WithToString implements Comparable<InlineGrammarState> {
			public String scopeName;
			public SortedSet<String> injectTo;

			@Override
			public int compareTo(final InlineGrammarState o) {
				return scopeName.compareTo(o.scopeName);
			}
		}

		static class LanguageState extends WithToString {
			public String label;
			public String scopeName;
			public String upstreamURL;
			public SortedSet<String> injectTo;
			public SortedSet<String> fileExtensions;
			public SortedSet<String> fileNames;
			public SortedSet<String> filePatterns;
		}
	}

	/**
	 * model for VSCode extension package.json
	 */
	record VsCodeExtensionPackageJson( //
			// https://code.visualstudio.com/api/references/extension-manifest
			@JsonProperty(required = true) String name, //
			String icon, //
			String license, //
			String version, //
			@JsonProperty(required = true) Contributions contributes) {

		record Contributions( //
				@JsonProperty(required = true) List<Grammar> grammars, //
				@JsonProperty(required = true) List<Language> languages) {

			record Grammar( //
					String language, //
					@JsonProperty(required = true) String scopeName, //
					@JsonProperty(required = true) String path, //
					List<String> injectTo) {
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
			final String sourceId = sourceEntry.getKey();
			final Source source = sourceEntry.getValue();

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

			final Path sourceRepoDir = sourceReposCacheDir.resolve(sourceId);
			final var gitCheckoutState = gitSparseCheckout(sourceRepoDir, source.github);

			switch (source) {
				case final Config.CustomSource src -> {
					final var extensionState = new State.ExtensionState();
					extensionState.github = gitCheckoutState;
					state.extensions.put(sourceId, extensionState);
					new CustomSourceHandler(sourceId, src, sourceRepoDir, syntaxesDir, extensionState).handle();
				}
				case final Config.VSCodeSingleExtensionSource src -> {
					final var extensionState = new State.ExtensionState();
					extensionState.github = gitCheckoutState;
					state.extensions.put(sourceId, extensionState);
					for (final String buildCommand : src.build) {
						Sys.execVerbose(sourceRepoDir, (Sys.IS_WINDOWS ? "cmd /c " : "") + buildCommand);
					}
					new VSCodeSingleExtensionSourceHandler(sourceId, src, sourceRepoDir, syntaxesDir, extensionState).handle();
				}
				case final Config.VSCodeMultiExtensionsSource src -> new VSCodeMultiExtensionsSourceHandler(sourceId, src, sourceRepoDir,
						gitCheckoutState, syntaxesDir, state.extensions).handle();
				default -> {
				}
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

	private String getIconFileName(final Path syntaxDir, final String langId) {
		final String landIdSanitized = sanitizeFilename(langId);
		return Files.exists(syntaxDir.resolve(landIdSanitized + ".icon.png"))
				? landIdSanitized + ".icon.png"
				: Files.exists(syntaxDir.resolve("icon.png"))
						? "icon.png"
						: null;
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
				final var iconFileName = getIconFileName(syntaxDir, langId);

				final var templateVars = new HashMap<String, Object>();

				templateVars.put("ext_id", extId);
				templateVars.put("lang_id", langId);
				templateVars.put("label", langState.label);
				templateVars.put("icon_filename", iconFileName);
				templateVars.put("file_associations", Arrays.asList( //
						isEmpty(langState.fileExtensions) ? null
								: "file-extensions=\""
										+ join(langState.fileExtensions.stream().map(Strings::removeLeadingDot).distinct().sorted(), ", ")
										+ "\"", //
						isEmpty(langState.fileNames) ? null : "file-names=\"" + join(langState.fileNames, ", ") + "\"", //
						isEmpty(langState.filePatterns) ? null : "file-patterns=\"" + join(langState.filePatterns, ", ") + "\"" //
				).stream().filter(Objects::nonNull).collect(Collectors.joining("<br />")));

				templateVars.put("repo_name", extState.github.repo);
				templateVars.put("repo_ref", extState.github.ref);
				templateVars.put("repo_path", extState.github.path == null ? "" : extState.github.path);
				templateVars.put("commit", extState.github.commit);

				templateVars.put("upstream_url",
						isURL(langState.upstreamURL) ? " <a href=\"" + langState.upstreamURL + "\">[upstream]</a>" : "");

				aboutLines.put(langState.label, render("updater/about.html.peb",
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
			final String extId = ext.getKey();
			final ExtensionState extState = ext.getValue();
			final Path syntaxDir = syntaxesDir.resolve(extId);

			for (final var lang : extState.languages.entrySet()) {
				final String langId = lang.getKey();
				final LanguageState langState = lang.getValue();
				logInfo("Rendering entry [" + extId + "/" + langId + "]...");

				final String landIdSanitized = sanitizeFilename(langId);

				final Path grammarFile = findFirstFile(syntaxDir, //
						f -> f.matches(Pattern.quote(landIdSanitized) + "[.]tmLanguage[.](yaml|json|plist)")).get();

				final String iconFileName = getIconFileName(syntaxDir, langId);

				final var exampleFile = findFirstFile(syntaxDir, //
						f -> f.matches(Pattern.quote(landIdSanitized) + "[.]example[.].*"));

				final var srcLangCfg = config.findSourceLanguageConfig(extId, langId);
				final var contentBaseType = srcLangCfg == null ? "" : stripToEmpty(srcLangCfg.contentBaseType);
				final var contentTypeDescriber = srcLangCfg == null ? "" : stripToEmpty(srcLangCfg.contentDescriber);

				final var langCfgFile = syntaxDir.resolve(langId + ".language-configuration.json");
				final var templateVars = new HashMap<String, Object>();
				templateVars.put("ext_id", extId);
				templateVars.put("lang_id", langId);
				templateVars.put("label", langState.label);
				templateVars.put("content_base_type", contentBaseType.isEmpty() ? config.contentBaseType : contentBaseType);
				templateVars.put("content_type_describer", contentTypeDescriber);
				templateVars.put("content_type_priority", config.contentTypePriority);
				templateVars.put("content_type_id", config.contentTypePrefix + "." + langId);
				templateVars.put("scope_name", langState.scopeName);
				templateVars.put("grammar_filename", grammarFile.getFileName());
				templateVars.put("language_configuration_filename",
						Files.exists(langCfgFile) ? langCfgFile.getFileName().toString() : null);
				templateVars.put("icon_filename", iconFileName);
				templateVars.put("example_filename", exampleFile.isPresent() ? exampleFile.get().getFileName().toString() : null);
				templateVars.put("inject_to", langState.injectTo);
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
				final String fileAssociations = Arrays.asList( //
						fileExtensions.isEmpty() ? null : "file-extensions=\"" + join(fileExtensions, ",") + "\"", //
						fileNames.isEmpty() ? null : "file-names=\"" + join(fileNames, ",") + "\"", //
						filePatterns.isEmpty() ? null : "file-patterns=\"" + join(filePatterns, ",") + "\"" //
				).stream().filter(Objects::nonNull).collect(Collectors.joining(" "));

				templateVars.put("file_associations", fileAssociations.isBlank() && contentTypeDescriber.isEmpty()
						? "file-names=\"WORKAROUND_SO_THAT_THIS_CONTENTTYPE_IS_NOT_ASSOCIATED_WITH_TXT_FILES_SEE_ISSUE_703\""
						: fileAssociations);

				pluginLines.append(render("updater/plugin.grammar.xml.peb", templateVars));
			}

			if (!extState.inlineGrammars.isEmpty()) {

				final var inlineGrammars = new ArrayList<Map<String, Object>>();
				for (final InlineGrammarState inlineGrammar : extState.inlineGrammars) {
					final Path grammarFile = findFirstFile(syntaxDir, //
							f -> f.matches(Pattern.quote(inlineGrammar.scopeName) + "[.]tmLanguage[.](yaml|json|plist)")).get();

					final var entry = new HashMap<String, Object>();
					entry.put("scope_name", inlineGrammar.scopeName);
					entry.put("inject_to", inlineGrammar.injectTo);
					entry.put("grammar_filename", grammarFile.getFileName().toString());
					inlineGrammars.add(entry);
				}

				final var templateVars = new HashMap<String, Object>();
				templateVars.put("ext_id", extId);
				templateVars.put("inlineGrammars", inlineGrammars);

				pluginLines.append(render("updater/plugin.inline-grammar.xml.peb", templateVars));
			}
		}

		final var pluginXML = Path.of(config.targets.pluginXml).toAbsolutePath().normalize();
		logInfo("Saving [" + pluginXML + "]...");
		Files.writeString(pluginXML, //
				normalizeNewlines( //
						replaceSubstringBetween(Files.readString(pluginXML), //
								"<!-- START-GENERATED -->", "<!-- END-GENERATED -->", //
								"\n" + indent(2, pluginLines.toString()) + "\n\n  ") //
				));
	}

	private void updateReadmeMD() throws IOException {
		logHeader("Updating [README.md.]...");

		final var languages = new ArrayList<Map<String, Object>>();

		for (final var extEntry : state.extensions.entrySet()) {
			final String extId = extEntry.getKey();
			final ExtensionState extState = extEntry.getValue();

			for (final var langEntry : extState.languages.entrySet()) {
				final String langId = langEntry.getKey();
				final LanguageState langState = langEntry.getValue();

				if (isEmpty(langState.fileExtensions) && isEmpty(langState.fileNames) && isEmpty(langState.filePatterns)) {
					continue;
				}

				final Path syntaxDir = syntaxesDir.resolve(extId);
				final String iconFileName = getIconFileName(syntaxDir, langId);

				final var langMap = new HashMap<String, Object>();
				langMap.put("ext_id", extId);
				langMap.put("label", langState.label);
				langMap.put("icon_filename", iconFileName);
				langMap.put("file_associations", Arrays.asList( //
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
						.replace("*", "\\*"));
				langMap.put("repo_ref", extState.github.ref);
				langMap.put("repo_name", extState.github.repo);
				langMap.put("repo_path", extState.github.path == null ? "" : extState.github.path);
				langMap.put("commit", extState.github.commit);
				langMap.put("upstream_url", isURL(langState.upstreamURL) ? " [[upstream]](" + langState.upstreamURL + ")" : "");
				languages.add(langMap);
			}
		}

		languages.sort(Comparator.comparing(l -> (String) l.get("label"), String.CASE_INSENSITIVE_ORDER));

		final var readmeMD = Path.of(config.targets.readmeMd).toAbsolutePath().normalize();
		logInfo("Saving [" + readmeMD + "]...");
		Files.writeString(readmeMD, //
				normalizeNewlines( //
						replaceSubstringBetween(Files.readString(readmeMD), //
								"<!-- START-GENERATED -->", "<!-- END-GENERATED -->", //
								"\n" + render("updater/readme.md.peb", Map.of("languages", languages)) + "\n") //
				));
	}

}
