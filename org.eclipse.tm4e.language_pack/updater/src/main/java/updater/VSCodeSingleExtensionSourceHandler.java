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

import static updater.utils.Log.logInfo;
import static updater.utils.ObjectMappers.JSON;
import static updater.utils.Validation.*;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import updater.Updater.Config;
import updater.Updater.State.ExtensionState;
import updater.Updater.State.LanguageState;
import updater.Updater.VsCodeExtensionPackageJson;
import updater.Updater.VsCodeExtensionPackageJson.Contributions;

/**
 * @author Sebastian Thomschke
 */
class VSCodeSingleExtensionSourceHandler extends AbstractSourceHandler<Config.VSCodeSingleExtensionSource> {

	final ExtensionState state;

	VSCodeSingleExtensionSourceHandler(final String sourceId, final Config.VSCodeSingleExtensionSource source, final Path sourceRepoDir,
			final Path targetSyntaxDir, final ExtensionState state) {
		super(sourceId, source, sourceRepoDir, targetSyntaxDir);
		this.state = state;
	}

	@Override
	void handle() throws IOException {
		final var pkgJson = JSON.readValue(sourceExtensionDir.resolve("package.json").toFile(), VsCodeExtensionPackageJson.class);
		assertArgNotEmpty("package.json/contributes/grammars", pkgJson.contributes().grammars());
		assertArgNotEmpty("package.json/contributes/languages", pkgJson.contributes().languages());

		final var pkgJsonLangs = new TreeMap<String /*langId*/, Contributions.Language>();
		for (final var lang : pkgJson.contributes().languages()) {
			pkgJsonLangs.put(lang.id(), lang);
		}
		final var pkgJsonLangGrammars = new TreeMap<String /*langId*/, Contributions.Grammar>();
		final var pkgJsonInlineGrammars = new TreeMap<String /*scopeName*/, Contributions.Grammar>();
		for (final var grammar : pkgJson.contributes().grammars()) {
			if (grammar.language() != null) {
				if (pkgJsonLangs.containsKey(grammar.language())) {
					pkgJsonLangGrammars.put(grammar.language(), grammar);
				} else {
					logInfo("[WARNING] Ignoring " + grammar + " which references undefined language.");
				}
			} else {
				pkgJsonInlineGrammars.put(grammar.scopeName(), grammar);
			}
		}

		if (!source.languages.isEmpty()) {
			logInfo("Validating language configuration overrides...", false);
			for (final Entry<String, Config.LanguageIgnoreable> langOverrides : source.languages.entrySet()) {
				final var langId = langOverrides.getKey();
				if (!pkgJsonLangs.containsKey(langId)) {
					logInfo("FAILED", true, false);
					throw new IllegalArgumentException(
							"No language with id [" + langId + "] found at [package.json/contributes/languages]");
				}
			}
			logInfo("OK", true, false);
		}

		if (!source.inlineGrammars.isEmpty()) {
			logInfo("Validating inline grammars overrides...", false);
			for (final Entry<String, Config.InlineGrammarIgnoreable> inlineGrammarOverrides : source.inlineGrammars.entrySet()) {
				final var scopeName = inlineGrammarOverrides.getKey();
				if (!pkgJsonInlineGrammars.containsKey(scopeName)) {
					logInfo("FAILED", true, false);
					throw new IllegalArgumentException("No inline grammar with scopeName [" + scopeName
							+ "] found at [package.json/contributes/grammars]");
				}
			}
			logInfo("OK", true, false);
		}

		final var targetSyntaxDir = targetSyntaxesDir.resolve(sourceId);
		Files.createDirectories(targetSyntaxDir);

		downloadLicenseFile(targetSyntaxDir);

		if (!isBlank(pkgJson.icon())) {
			final var targetIcon = targetSyntaxDir.resolve("icon.png");
			logInfo("Copying file [icon.png]...");
			final var sourceIcon = ImageIO.read(sourceExtensionDir.resolve(pkgJson.icon()).toFile());
			ImageIO.write(resizeImage(sourceIcon, 16, 16), "png", targetIcon.toFile());
			ImageIO.write(resizeImage(sourceIcon, 32, 32), "png", targetSyntaxDir.resolve("icon@2x.png").toFile());
		}

		for (final Entry<String, Contributions.Language> lang : pkgJsonLangs.entrySet()) {
			final var langId = lang.getKey();
			final var langCfg = lang.getValue();
			final var langOverrides = defaultIfNull(source.languages.get(langId), Config.LanguageIgnoreable::new);
			if (!isBlank(langOverrides.ignoredReason) && !"false".equals(langOverrides.ignoredReason)) {
				logInfo("Ignoring language contribution [" + langId + "] as per user config"
						+ ("true".equals(langOverrides.ignoredReason) ? "."
								: ": " + langOverrides.ignoredReason));
				continue;
			}

			final var grammarCfg = pkgJsonLangGrammars.get(langId);
			final var grammarPath = !isBlank(langOverrides.grammar) //
					? langOverrides.grammar //
					: grammarCfg != null //
							? grammarCfg.path() //
							: null;
			if (grammarPath == null) {
				logInfo("[WARNING] Ignoring language contribution [" + langId + "] as no grammar is provided.");
				continue;
			}

			final var ctx = new DownloadContext(langId, langOverrides.update, targetSyntaxDir);
			final var grammarFile = downloadTextMateGrammarFile(ctx, grammarPath);

			final var langcfgPath = !isBlank(langOverrides.langcfg) //
					? langOverrides.langcfg //
					: langCfg.configuration();
			downloadLangConfigurationJSONFile(ctx, langcfgPath);

			downloadExampleFile(ctx, langOverrides.example);

			if (langCfg.icon() != null && !isBlank(langCfg.icon().light())) {
				final var targetIcon = ctx.targetDir().resolve(langId + ".png");
				if (ctx.updateExistingFiles() || !Files.exists(targetIcon)) {
					logInfo("Copying image [" + langCfg.icon().light() + "] -> [" + targetIcon.getFileName() + "]...", false);
					try {
						final var sourceIcon = ImageIO.read(sourceExtensionDir.resolve(langCfg.icon().light()).toFile());
						ImageIO.write(resizeImage(sourceIcon, 16, 16), "png", targetIcon.toFile());
						ImageIO.write(resizeImage(sourceIcon, 32, 32), "png", ctx.targetDir().resolve(langId + "@2x.png").toFile());
						logInfo(" OK", true, false);
					} catch (final Exception ex) {
						logInfo(" ERROR [" + ex.getMessage().replace("\n", " | ") + "]", true, false);
					}
				}
			}

			final var langState = new LanguageState();
			langState.label = !isBlank(langOverrides.label) //
					? langOverrides.label //
					: isEmpty(langCfg.aliases()) ? langId : langCfg.aliases().get(0);
			langState.scopeName = !isBlank(langOverrides.scopeName) //
					? langOverrides.scopeName //
					: grammarCfg == null ? null : grammarCfg.scopeName();
			langState.fileExtensions = !isEmpty(langOverrides.fileExtensions) //
					? new TreeSet<>(langOverrides.fileExtensions) //
					: langCfg.fileExtensions() == null ? null : new TreeSet<>(langCfg.fileExtensions());
			langState.fileNames = !isEmpty(langOverrides.fileNames) //
					? new TreeSet<>(langOverrides.fileNames) //
					: langCfg.fileNames() == null ? null : new TreeSet<>(langCfg.fileNames());
			langState.filePatterns = !isEmpty(langOverrides.filePatterns) //
					? new TreeSet<>(langOverrides.filePatterns) //
					: langCfg.filePatterns() == null ? null : new TreeSet<>(langCfg.filePatterns());
			langState.upstreamURL = getUpstreamUrlFromGrammarFile(grammarFile);
			state.languages.put(langId, langState);
		}

		for (final Entry<String, Contributions.Grammar> inlineGrammar : pkgJsonInlineGrammars.entrySet()) {
			final var scopeName = inlineGrammar.getKey();
			final var grammarOverrides = defaultIfNull(source.inlineGrammars.get(scopeName), Config.InlineGrammarIgnoreable::new);

			if (!isBlank(grammarOverrides.ignoredReason) && !"false".equals(grammarOverrides.ignoredReason)) {
				logInfo("Ignoring inline grammar contribution [" + scopeName + "] as per user config"
						+ ("true".equals(grammarOverrides.ignoredReason) ? "." : ": " + grammarOverrides.ignoredReason));
				continue;
			}
			final var grammarCfg = inlineGrammar.getValue();
			final var grammarPath = isBlank(grammarOverrides.grammar) ? grammarCfg.path() : grammarOverrides.grammar;
			final var ctx = new DownloadContext(scopeName, grammarOverrides.update, targetSyntaxDir);
			downloadTextMateGrammarFile(ctx, grammarPath);
			state.inlineGrammarScopeNames.add(scopeName);
		}
	}

	BufferedImage resizeImage(final BufferedImage originalImage, final int targetWidth, final int targetHeight) {
		final var resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = resizedImage.createGraphics();

		// use RenderingHints to improve image quality
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
		g2d.dispose();

		return resizedImage;
	}
}
