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

import static updater.utils.Log.*;
import static updater.utils.ObjectMappers.JSON;
import static updater.utils.Validation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import updater.Updater.Config;
import updater.Updater.Config.VSCodeSingleExtensionSource;
import updater.Updater.State;
import updater.Updater.State.ExtensionState;
import updater.Updater.VsCodeExtensionPackageJson;
import updater.utils.Git.GitCheckoutState;

/**
 * @author Sebastian Thomschke
 */
class VSCodeMultiExtensionsSourceHandler extends AbstractSourceHandler<Config.VSCodeMultiExtensionsSource> {

	final Map<String, ExtensionState> extensionStates;
	final GitCheckoutState gitCheckoutState;

	VSCodeMultiExtensionsSourceHandler(final String sourceId, final Config.VSCodeMultiExtensionsSource source, final Path sourceRepoDir,
			final GitCheckoutState gitCheckoutState, final Path targetSyntaxDir, final Map<String, ExtensionState> extensionStates) {
		super(sourceId, source, sourceRepoDir, targetSyntaxDir);
		this.extensionStates = extensionStates;
		this.gitCheckoutState = gitCheckoutState;
	}

	@Override
	void handle() throws IOException {

		logInfo("Locating valid VSCode grammar extensions...");
		final Map<String, VsCodeExtensionPackageJson> pkgJsonByExtId = new HashMap<>();
		final Map<String, Path> pkgJsonPathByExtId = new HashMap<>();
		try (var l = withLogIndented(); var files = Files.list(sourceExtensionDir)) {
			for (final var dir : files.filter(Files::isDirectory).toList()) {
				final var pkgJSONPath = dir.resolve("package.json");
				if (!Files.exists(pkgJSONPath)) {
					logInfo("Ignoring extension directory [" + dir.getFileName() + "] - no package.json found");
					continue;
				}

				try {
					final var pkgJSON = JSON.readValue(pkgJSONPath.toFile(), VsCodeExtensionPackageJson.class);
					pkgJsonPathByExtId.put(pkgJSON.name(), pkgJSONPath);
					pkgJsonByExtId.put(pkgJSON.name(), pkgJSON);
				} catch (final Exception ex) {
					logInfo("Ignoring extension directory [" + dir.getFileName() + "] - no valid grammar configuration in package.json");
				}
			}
		}

		logInfo("Validating extension overrides...");
		if (!source.extensions.isEmpty()) {
			for (final var ext : source.extensions.entrySet()) {
				final var extId = ext.getKey();
				if (!pkgJsonByExtId.containsKey(extId))
					throw new IllegalArgumentException("No valid extension named [" + extId + "] found in source repo, "
							+ "as specified at [update-syntaxes-config.yaml/sources/" + sourceId + "/extensions/" + extId + "]");
			}
		}

		if (!source.includeAllByDefault && isEmpty(source.extensions))
			throw new IllegalArgumentException("Extensions to include must be declared at [update-syntaxes-config.yaml/sources/" + sourceId
					+ "/extensions/...]");

		try (var l = withLogIndented()) {
			var i = 0;
			for (final var extId : new TreeSet<>(pkgJsonByExtId.keySet())) {
				i++;
				logHeader("[" + i + "/" + pkgJsonByExtId.size() + "] " //
						+ "Processing [" + extId + "] " //
						+ "(" + VSCodeSingleExtensionSource.class.getSimpleName() + ")");

				final var extOverrides = source.extensions.get(extId);
				if (extOverrides != null && !isBlank(extOverrides.ignoredReason) && !"false".equals(extOverrides.ignoredReason)) {
					logInfo("Ignoring available extension [" + extId + "] as per user config"
							+ ("true".equals(extOverrides.ignoredReason) ? "."
									: ": " + extOverrides.ignoredReason));
					continue;
				}

				if (!source.includeAllByDefault && extOverrides == null) {
					logInfo("Ignoring available extension [" + extId + "] as per user config.");
					continue;
				}

				final var extFolder = pkgJsonPathByExtId.get(extId).getParent().getFileName().toString();

				final var extensionState = new State.ExtensionState();
				extensionState.github = new GitCheckoutState( //
						gitCheckoutState.repo, //
						isBlank(source.github.path) ? extFolder : source.github.path + "/" + extFolder, //
						gitCheckoutState.ref, //
						gitCheckoutState.commit);
				extensionStates.put(extId, extensionState);

				final var extSource = new Config.VSCodeSingleExtensionSource();
				extSource.github = extensionState.github;
				extSource.licenseDownload = source.licenseDownload;
				extSource.languages = extOverrides != null ? extOverrides.languages : Collections.emptyMap();

				new VSCodeSingleExtensionSourceHandler(extId, extSource, sourceRepoDir, targetSyntaxesDir, extensionState).handle();
			}
		}
	}
}
