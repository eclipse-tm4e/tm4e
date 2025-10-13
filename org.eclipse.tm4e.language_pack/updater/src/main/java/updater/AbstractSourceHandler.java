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

import static updater.utils.ObjectMappers.*;
import static updater.utils.Sys.*;
import static updater.utils.Validation.isURL;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.UnaryOperator;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import updater.Updater.Config.Source;

/**
 * @author Sebastian Thomschke
 */
abstract class AbstractSourceHandler<T extends Source> {

	final String sourceId;
	final T source;
	final Path sourceRepoDir;
	final Path sourceExtensionDir;
	final Path targetSyntaxesDir;

	AbstractSourceHandler(final String sourceId, final T source, final Path sourceRepoDir, final Path targetSyntaxesDir) {
		this.sourceId = sourceId;
		this.source = source;
		this.sourceRepoDir = sourceRepoDir;
		sourceExtensionDir = source.github.path == null ? sourceRepoDir : sourceRepoDir.resolve(source.github.path);
		this.targetSyntaxesDir = targetSyntaxesDir;
	}

	record DownloadContext(String targetNamePrefix, boolean updateExistingFiles, Path targetDir) {
	}

	void downloadExampleFile(final DownloadContext ctx, final String examplePathOrURL) throws IOException {
		if (examplePathOrURL != null) {
			if (isURL(examplePathOrURL)) {
				final var sourceURL = URI.create(examplePathOrURL).toURL();
				final var targetFile = ctx.targetDir.resolve(ctx.targetNamePrefix + ".example." + getFileExtension(sourceURL.getPath()));
				if (ctx.updateExistingFiles || !Files.exists(targetFile)) {
					downloadFile(sourceURL, targetFile);
				}
			} else {
				final var sourceFile = sourceExtensionDir.resolve(examplePathOrURL).normalize();
				final var targetFile = ctx.targetDir.resolve(ctx.targetNamePrefix + ".example." + getFileExtension(sourceFile));
				if (ctx.updateExistingFiles || !Files.exists(targetFile)) {
					copyFile(sourceFile, targetFile);
				}
			}
		}
	}

	void downloadLangConfigurationJSONFile(final DownloadContext ctx, final String langCfgPathOrURL) throws IOException {
		if (langCfgPathOrURL != null) {
			if (isURL(langCfgPathOrURL)) {
				final var targetFile = ctx.targetDir.resolve(ctx.targetNamePrefix + ".language-configuration.json");
				if (ctx.updateExistingFiles || !Files.exists(targetFile)) {
				   downloadFile(URI.create(langCfgPathOrURL).toURL(), targetFile);
				}
			} else {
				final var targetFile = ctx.targetDir.resolve(ctx.targetNamePrefix + ".language-configuration.json");
				if (ctx.updateExistingFiles || !Files.exists(targetFile)) {
					copyFile(sourceExtensionDir.resolve(langCfgPathOrURL).normalize(), targetFile);
				}
			}
		}
	}

	void downloadLicenseFile(final Path targetDir) throws IOException {
		if (source.licenseDownload) {
			var srcLicenseFile = findFirstFile(sourceExtensionDir, //
					name -> "license".equalsIgnoreCase(name) //
							|| "license.md".equalsIgnoreCase(name) //
							|| "license.txt".equalsIgnoreCase(name));
			if (srcLicenseFile.isEmpty() && !sourceRepoDir.equals(sourceExtensionDir)) {
				srcLicenseFile = findFirstFile(sourceRepoDir, //
						name -> "license".equalsIgnoreCase(name) //
								|| "license.md".equalsIgnoreCase(name) //
								|| "license.txt".equalsIgnoreCase(name));
			}
			if (srcLicenseFile.isEmpty())
				throw new IllegalStateException("License file not found for source [" + sourceId + "]");
			copyFile(srcLicenseFile.get(), targetDir.resolve("LICENSE.txt"));
		}
	}

	/**
	 * @return grammarFile
	 */
	Path downloadTextMateGrammarFile(final DownloadContext ctx, final String grammarPathOrURL) throws IOException {
		final UnaryOperator<String> mapFileExt = fileExt -> switch (fileExt) {
			case "json" -> ".tmLanguage.json";
			case "yml", "yaml", "yaml-tmlanguage" -> ".tmLanguage.yaml";
			case "plist", "xml", "tmlanguage" -> ".tmLanguage.plist";
			default -> ".tmLanguage.plist";
		};

		if (isURL(grammarPathOrURL)) {
			final var sourceURL = URI.create(grammarPathOrURL).toURL();
			final var targetFile = ctx.targetDir.resolve(ctx.targetNamePrefix + mapFileExt.apply(getFileExtension(sourceURL.getPath())
					.toLowerCase()));
			if (ctx.updateExistingFiles || !Files.exists(targetFile)) {
				downloadFile(sourceURL, targetFile);
			}
			return targetFile;
		}

		final var sourceFile = sourceExtensionDir.resolve(grammarPathOrURL).normalize();
		final var targetFile = ctx.targetDir.resolve(ctx.targetNamePrefix + mapFileExt.apply(getFileExtension(sourceFile).toLowerCase()));
		if (ctx.updateExistingFiles || !Files.exists(targetFile)) {
			copyFile(sourceFile, targetFile);
		}
		return targetFile;
	}

	String getUpstreamUrlFromGrammarFile(final Path textMateGrammarFile) throws IOException {
		return switch (getFileExtension(textMateGrammarFile.getFileName()).toLowerCase()) {
			case "json" -> {
				final Map<String, Object> grammar = JSON.readValue(textMateGrammarFile.toFile(), Map.class);
				final var version = grammar.getOrDefault("version", "").toString();
				yield isURL(version) ? version : null;
			}
			case "yml", "yaml", "yaml-tmlanguage" -> {
				final Map<String, Object> grammar = YAML.readValue(textMateGrammarFile.toFile(), Map.class);
				final var version = grammar.getOrDefault("version", "").toString();
				yield isURL(version) ? version : null;
			}
			case "plist", "xml", "tmlanguage" -> {
				try {
					final var domFactory = DocumentBuilderFactory.newInstance();
					domFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
					domFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
					domFactory.setCoalescing(true);
					domFactory.setIgnoringComments(true);
					domFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
					domFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
					final var doc = domFactory.newDocumentBuilder().parse(textMateGrammarFile.toFile());
					final var versionNode = (Node) XPathFactory.newInstance().newXPath() //
							.compile("//key[text()='version']/following-sibling::string") //
							.evaluate(doc, XPathConstants.NODE);

					if (versionNode != null) {
						yield versionNode.getTextContent().trim();
					}
					yield null;
				} catch (final IOException ex) {
					throw ex;
				} catch (final SAXException | ParserConfigurationException | XPathExpressionException ex) {
					throw new IOException(ex);
				}
			}
			default -> null;
		};
	}

	abstract void handle() throws IOException;
}
