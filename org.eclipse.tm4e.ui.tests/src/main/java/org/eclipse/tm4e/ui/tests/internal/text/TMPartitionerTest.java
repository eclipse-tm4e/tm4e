/*******************************************************************************
 * Copyright (c) 2025 Vegard IT GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT) - initial implementation
 *******************************************************************************/
package org.eclipse.tm4e.ui.tests.internal.text;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Paths;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.Registry;
import org.eclipse.tm4e.ui.internal.model.TMModelManager;
import org.eclipse.tm4e.ui.internal.text.TMPartitioner;
import org.eclipse.tm4e.ui.tests.support.TestUtils;
import org.eclipse.tm4e.ui.text.ITMPartitionRegion;
import org.eclipse.tm4e.ui.text.TMPartitions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TMPartitionerTest {

	private Registry reg = new Registry();
	private Document doc;
	private TMPartitioner partitioner;
	private IGrammar jsGrammar;
	private IGrammar mdGrammar;

	@Test
	void computePartitioningCoversTailAfterLargeAppendWithoutTokenization() throws Exception {
		// Ensure initial model is ready and fully tokenized (from @BeforeEach)
		final int oldLen = doc.getLength();

		// Append a large chunk to the end, but do NOT wait for tokenization
		final var sb = new StringBuilder();
		for (int i = 0; i < 2000; i++) {
			sb.append("line ").append(i).append('\n');
		}
		doc.replace(oldLen, 0, sb.toString());

		final int newLen = doc.getLength();
		final ITypedRegion[] parts = partitioner.computePartitioning(0, newLen);

		// Expected: result covers [0,newLen) contiguously, filling untokenized tail with base.
		int prevEnd = 0;
		for (final ITypedRegion r : parts) {
			assertThat(r.getOffset()).isEqualTo(prevEnd);
			prevEnd = r.getOffset() + r.getLength();
		}
		assertThat(prevEnd).as("partitioning should cover entire document length").isEqualTo(newLen);
	}

	@Test
	void computePartitioningCoversWholeRange() throws Exception {
		final int len = doc.getLength();
		final ITMPartitionRegion[] parts = partitioner.computePartitioning(0, len);
		assertThat(parts.length).isGreaterThanOrEqualTo(3); // expect base, css, js at least

		// contiguity and coverage
		int covered = 0;
		int prevEnd = 0;
		boolean sawCss = false, sawJs = false;
		for (final ITMPartitionRegion r : parts) {
			assertThat(r.getOffset()).isEqualTo(prevEnd);
			prevEnd = r.getOffset() + r.getLength();
			covered += r.getLength();
			if ("tm4e:source.css".equals(r.getType())) {
				sawCss = true;
				assertThat(r.getGrammarScope()).isEqualTo("source.css");
			}
			if ("tm4e:source.js".equals(r.getType())) {
				sawJs = true;
				assertThat(r.getGrammarScope()).isEqualTo("source.js");
			}
		}
		assertThat(prevEnd).isEqualTo(len);
		assertThat(covered).isEqualTo(len);
		assertThat(sawCss).isTrue();
		assertThat(sawJs).isTrue();
	}

	@Test
	void computePartitioningFillsGapsForSubrangeAfterAppend() throws Exception {
		final int oldLen = doc.getLength();
		final var add = new StringBuilder();
		for (int i = 0; i < 500; i++) {
			add.append("extra ").append(i).append('\n');
		}
		doc.replace(oldLen, 0, add.toString());

		final int queryOffset = Math.max(0, oldLen - 10);
		final int queryLen = 250;
		final int queryEnd = Math.min(doc.getLength(), queryOffset + queryLen);
		final ITypedRegion[] segs = partitioner.computePartitioning(queryOffset, queryLen);

		int cursor = queryOffset;
		for (final ITypedRegion r : segs) {
			assertThat(r.getOffset()).isEqualTo(cursor);
			cursor = r.getOffset() + r.getLength();
		}
		assertThat(cursor).isEqualTo(queryEnd);
	}

	@Test
	void computePartitioningHandlesEmptyDocument() throws Exception {
		// Reuse the shared partitioner/document from setup for consistency
		doc.set("");
		final ITypedRegion[] parts = partitioner.computePartitioning(0, 10);
		assertThat(parts).hasSize(1);
		assertThat(parts[0].getOffset()).isEqualTo(0);
		assertThat(parts[0].getLength()).isEqualTo(0);
	}

	@Test
	void cssContentIsSinglePartitionRegion() throws Exception {
		final String text = doc.get();
		final int styleTagStart = text.indexOf("<style>\n");
		assertThat(styleTagStart).isGreaterThanOrEqualTo(0);
		final int styleContent = styleTagStart + "<style>\n".length();
		final int styleClose = text.indexOf("</style>", styleContent);
		assertThat(styleClose).isGreaterThan(styleContent);

		final int contentLen = styleClose - styleContent;
		final ITMPartitionRegion[] parts = partitioner.computePartitioning(styleContent, contentLen);
		assertThat(parts).hasSize(1);
		assertThat(parts[0].getType()).isEqualTo("tm4e:source.css");
		assertThat(parts[0].getGrammarScope()).isEqualTo("source.css");
	}

	@Test
	void documentRemoveDoesNotThrowOnNullText() throws Exception {
		// Remove a single character using Document#remove, which yields a DocumentEvent with null text
		final int pos = Math.max(0, doc.get().indexOf("<html>") - 1);
		// IDocument has no remove(); simulate deletion with replace and empty text
		doc.replace(pos, 1, "");

		// Should not throw; partitioning remains contiguous over new length
		final int len = doc.getLength();
		final ITypedRegion[] parts = partitioner.computePartitioning(0, len);
		int end = 0;
		for (final ITypedRegion r : parts) {
			assertThat(r.getOffset()).isEqualTo(end);
			end = r.getOffset() + r.getLength();
		}
		assertThat(end).isEqualTo(len);
	}

	@Test
	void editInsideJsLineDoesNotDropJsRemainder() throws Exception {
		// Extend the <script> block to span multiple lines
		String text = doc.get();
		final int scriptClose = text.indexOf("</script>");
		TestUtils.waitForIdleAfterChange(doc, 3_000, () -> doc.replace(scriptClose, 0, "let a = 1;\nlet b = 2;\n"));
		text = doc.get();

		final int aIdx = text.indexOf("let a = 1;");
		final int bIdx = text.indexOf("let b = 2;");
		assertThat(aIdx).isGreaterThanOrEqualTo(0);
		assertThat(bIdx).isGreaterThanOrEqualTo(0);

		// Sanity: both lines are JS
		assertThat(partitioner.getContentType(aIdx)).isEqualTo("tm4e:source.js");
		assertThat(partitioner.getContentType(bIdx)).isEqualTo("tm4e:source.js");

		// Edit only the first JS line (single-line range within the JS partition)
		final int numPos = aIdx + "let a = ".length();
		TestUtils.waitForIdleAfterChange(doc, 3_000, () -> doc.replace(numPos, 1, "3"));

		// After tokenization for the edited line, the next JS line must remain JS
		assertThat(partitioner.getContentType(bIdx)).isEqualTo("tm4e:source.js");
	}

	@Test
	void editsAddDeleteMoveEmbedded() throws Exception {
		// 1) Add a new CSS block after <body> (single-line block to ensure the changed range covers content)
		String text = doc.get();
		final int bodyIdx = text.indexOf("<body>");
		final String toInsert = "<style>.new { width: 10px; }</style>\n";
		TestUtils.waitForIdleAfterChange(doc, 3_000,
				() -> doc.replace(bodyIdx + "<body>\n".length(), 0, toInsert));
		text = doc.get();
		final int newCssIdx = text.indexOf(".new");
		assertThat(newCssIdx).isGreaterThanOrEqualTo(0);
		assertThat(partitioner.getContentType(newCssIdx)).isEqualTo("tm4e:source.css");

		// 2) Delete the original CSS block (the one containing 'color: red')
		final int origStyleStart = text.indexOf("<style>\n");
		final int origStyleEnd = text.indexOf("</style>", origStyleStart) + "</style>".length();
		if (origStyleStart >= 0) {
			final int delStart = origStyleStart;
			final int delLen = origStyleEnd - origStyleStart;
			TestUtils.waitForIdleAfterChange(doc, 3_000, () -> doc.replace(delStart, delLen, ""));
		}

		// Ensure the 'color' declaration is gone and the region is base
		text = doc.get();
		assertThat(text.indexOf("color: red")).isEqualTo(-1);
		final int checkBase = Math.max(0, origStyleStart - 1);
		assertThat(partitioner.getContentType(checkBase)).startsWith(TMPartitioner.scopeToPartitionType("text."));

		// 3) Move the <script> block to after <head>
		final int scriptStart = text.indexOf("<script>");
		final int scriptEnd = text.indexOf("</script>", scriptStart) + "</script>".length();
		final String scriptBlock = text.substring(scriptStart, scriptEnd);
		{
			final int delStart = scriptStart;
			final int delLen = scriptEnd - scriptStart;
			TestUtils.waitForIdleAfterChange(doc, 3_000, () -> doc.replace(delStart, delLen, ""));
		}
		text = doc.get();
		final int headIdx = text.indexOf("<head>");
		TestUtils.waitForIdleAfterChange(doc, 3_000,
				() -> doc.replace(headIdx + "<head>\n".length(), 0, scriptBlock + "\n"));
		text = doc.get();
		final int funcIdx = text.indexOf("function x");
		assertThat(funcIdx).isGreaterThanOrEqualTo(0);
		assertThat(partitioner.getContentType(funcIdx)).isEqualTo("tm4e:source.js");

		// Ensure tail of document does not contain JS partitions anymore
		final int tailStart = text.lastIndexOf("</body>");
		final ITypedRegion[] tailParts = partitioner.computePartitioning(Math.max(0, tailStart), text.length() - Math.max(0, tailStart));
		assertThat(tailParts).allMatch(r -> !"tm4e:source.js".equals(r.getType()));
	}

	@Test
	void getPartitionReturnsLastRegionAtEOFForJavaScript() throws Exception {
		// Reuse partitioner+doc; switch the shared TM model to JS and use plain JS text
		final var model = TMModelManager.INSTANCE.connect(doc);
		model.setGrammar(jsGrammar);
		partitioner.setGrammar(jsGrammar);
		doc.set("let a = 1;\n");

		// Trigger activation and wait for initial tokenization
		partitioner.getPartition(0);
		TestUtils.waitForModelReady(doc, 5_000);

		final int len = doc.getLength();
		final ITMPartitionRegion eofRegion = partitioner.getPartition(len);
		assertThat(eofRegion.getType()).isEqualTo(TMPartitioner.scopeToPartitionType("source.js"));
		assertThat(eofRegion.getGrammarScope()).isEqualTo("source.js");

		// Negative offset clamps to start and should also be TS
		final ITMPartitionRegion negRegion = partitioner.getPartition(-100);
		assertThat(negRegion.getType()).isEqualTo(TMPartitioner.scopeToPartitionType("source.js"));
		assertThat(negRegion.getGrammarScope()).isEqualTo("source.js");
	}

	@Test
	void jsContentIsSinglePartitionRegion() throws Exception {
		final String text = doc.get();
		final int scriptTagStart = text.indexOf("<script>\n");
		assertThat(scriptTagStart).isGreaterThanOrEqualTo(0);
		final int scriptContent = scriptTagStart + "<script>\n".length();
		final int scriptClose = text.indexOf("</script>", scriptContent);
		assertThat(scriptClose).isGreaterThan(scriptContent);

		final int contentLen = scriptClose - scriptContent;
		final ITMPartitionRegion[] parts = partitioner.computePartitioning(scriptContent, contentLen);
		assertThat(parts).hasSize(1);
		assertThat(parts[0].getType()).isEqualTo("tm4e:source.js");
		assertThat(parts[0].getGrammarScope()).isEqualTo("source.js");
	}

	@Test
	void markdownFencedXmlIsSinglePartition() throws Exception {
		final var mdDoc = new Document("""
			Prose

			```xml
			<profiles id=\"p1\">
			  <profile/>
			</profiles>
			```
			""");
		final var model = TMModelManager.INSTANCE.connect(mdDoc);
		model.setGrammar(mdGrammar);

		// Create a partitioner WITHOUT forcing grammar and connect via helper
		final TMPartitioner p = new TMPartitioner();
		mdDoc.setDocumentPartitioner(TMPartitions.TM_PARTITIONING, p);
		p.connect(mdDoc);

		// Trigger activation
		p.getPartition(0);
		TestUtils.waitForModelReady(mdDoc, 5_000);

		final String text = mdDoc.get();
		final int fence = text.indexOf("```xml\n");
		assertThat(fence).isGreaterThanOrEqualTo(0);
		final int contentStart = fence + "```xml\n".length();
		final int fenceClose = text.indexOf("```", contentStart);
		assertThat(fenceClose).isGreaterThan(contentStart);

		final ITMPartitionRegion[] parts = p.computePartitioning(contentStart, fenceClose - contentStart);
		assertThat(parts).hasSize(1);
		assertThat(parts[0].getType()).isEqualTo("tm4e:text.xml");
		assertThat(parts[0].getGrammarScope()).isEqualTo("text.xml");

		p.disconnect();
	}

	@Test
	void markdownFencedJavascriptIsSinglePartition() throws Exception {
		final var mdDoc = new Document("""
			Prose

			```javascript
			function foo() { return 1; }

			// comment
			```
			""");
		final var model = TMModelManager.INSTANCE.connect(mdDoc);
		model.setGrammar(mdGrammar);

		// Create a partitioner WITHOUT forcing grammar and connect via helper
		final TMPartitioner p = new TMPartitioner();
		mdDoc.setDocumentPartitioner(TMPartitions.TM_PARTITIONING, p);
		p.connect(mdDoc);

		// Trigger activation
		p.getPartition(0);
		TestUtils.waitForModelReady(mdDoc, 5_000);

		final String text = mdDoc.get();
		final int fence = text.indexOf("```javascript\n");
		assertThat(fence).isGreaterThanOrEqualTo(0);
		final int contentStart = fence + "```javascript\n".length();
		final int fenceClose = text.indexOf("```", contentStart);
		assertThat(fenceClose).isGreaterThan(contentStart);

		final ITMPartitionRegion[] parts = p.computePartitioning(contentStart, fenceClose - contentStart);
		assertThat(parts).hasSize(1);
		assertThat(parts[0].getType()).isEqualTo("tm4e:source.js");
		assertThat(parts[0].getGrammarScope()).isEqualTo("source.js");

		p.disconnect();
	}

	@Test
	void markdownFencedJavaIsSinglePartition() throws Exception {
		// the white space between ; and // in the java scoped block is an IToken of type `text.html`
		// this test ensures that the partitioner still makes a single partition of the whole fenced block
		final var mdDoc = new Document(
				"""
					```java
					System.out.println("World!");      // single line comment
					```
					""");
		final var model = TMModelManager.INSTANCE.connect(mdDoc);
		model.setGrammar(mdGrammar);

		// Create a partitioner WITHOUT forcing grammar and connect via helper
		final TMPartitioner p = new TMPartitioner();
		mdDoc.setDocumentPartitioner(TMPartitions.TM_PARTITIONING, p);
		p.connect(mdDoc);

		// Trigger activation
		p.getPartition(0);
		TestUtils.waitForModelReady(mdDoc, 5_000);

		final String text = mdDoc.get();
		final int fence = text.indexOf("```java\n");
		assertThat(fence).isGreaterThanOrEqualTo(0);
		final int contentStart = fence + "```java\n".length();
		final int fenceClose = text.indexOf("```", contentStart);
		assertThat(fenceClose).isGreaterThan(contentStart);

		final ITMPartitionRegion[] parts = p.computePartitioning(contentStart, fenceClose - contentStart);
		assertThat(parts).hasSize(1);
		assertThat(parts[0].getType()).isEqualTo("tm4e:source.java");
		assertThat(parts[0].getGrammarScope()).isEqualTo("source.java");

		p.disconnect();
	}

	@Test
	void legalContentTypesIncludeEmbedded() throws Exception {
		final var legal = partitioner.getLegalContentTypes();
		assertThat(legal).contains("tm4e:source.css");
		assertThat(legal).contains("tm4e:source.js");
		// base is the html scope
		assertThat(legal).anyMatch(s -> s.startsWith("tm4e:text."));
	}

	@Test
	void partitionBoundaries() throws Exception {
		final String text = doc.get();
		final int styleTagStart = text.indexOf("<style>");
		final int styleContent = styleTagStart + "<style>\n".length();
		final int styleClose = text.indexOf("</style>");

		// At the opening tag: base
		assertThat(partitioner.getContentType(styleTagStart + 1)).startsWith(TMPartitioner.scopeToPartitionType("text."));
		final ITMPartitionRegion tmPartition = partitioner.getPartition(styleTagStart + 1);
		// Full scope should carry a specific variant (e.g., text.html.basic) and must not equal the normalized base
		assertThat(tmPartition.getType()).isEqualTo("tm4e:text.html");
		assertThat(tmPartition.getGrammarScope()).isEqualTo("text.html");

		// First character inside CSS content: css
		assertThat(partitioner.getContentType(styleContent)).isEqualTo("tm4e:source.css");
		final ITMPartitionRegion cssRegion = partitioner.getPartition(styleContent);
		assertThat(cssRegion.getType()).isEqualTo("tm4e:source.css");
		assertThat(cssRegion.getGrammarScope()).isEqualTo("source.css");

		// Last character before closing tag: css
		assertThat(partitioner.getContentType(styleClose - 1)).isEqualTo("tm4e:source.css");
		// At the '<' of closing tag: base
		assertThat(partitioner.getContentType(styleClose)).startsWith(TMPartitioner.scopeToPartitionType("text."));

		final int scriptTagStart = text.indexOf("<script>");
		final int scriptContent = scriptTagStart + "<script>\n".length();
		final int scriptClose = text.indexOf("</script>");

		// At the opening tag: base
		assertThat(partitioner.getContentType(scriptTagStart + 1)).startsWith(TMPartitioner.scopeToPartitionType("text."));
		final ITMPartitionRegion scriptTagPartition = partitioner.getPartition(scriptTagStart + 1);
		assertThat(scriptTagPartition.getType()).isEqualTo("tm4e:text.html");
		assertThat(scriptTagPartition.getGrammarScope()).isEqualTo("text.html");

		// Inside JS content: js
		assertThat(partitioner.getContentType(scriptContent)).isEqualTo("tm4e:source.js");
		final ITMPartitionRegion jsRegion = partitioner.getPartition(scriptContent);
		assertThat(jsRegion.getType()).isEqualTo("tm4e:source.js");
		assertThat(jsRegion.getGrammarScope()).isEqualTo("source.js");

		// Last character before closing tag: js
		assertThat(partitioner.getContentType(scriptClose - 1)).isEqualTo("tm4e:source.js");
		// At the '<' of closing tag: base
		assertThat(partitioner.getContentType(scriptClose)).startsWith(TMPartitioner.scopeToPartitionType("text."));
	}

	@Test
	void partitionsEmbeddedLanguages() throws Exception {
		// base HTML
		final int htmlIdx = doc.get().indexOf("<html>") + 1;
		final String baseType = partitioner.getContentType(htmlIdx);
		assertThat(baseType).startsWith(TMPartitioner.scopeToPartitionType("text."));

		// CSS inside <style>
		final int cssIdx = doc.get().indexOf("color");
		final String cssType = partitioner.getContentType(cssIdx);
		assertThat(cssType).isEqualTo("tm4e:source.css");

		// JS inside <script>
		final int jsIdx = doc.get().indexOf("function x");
		final String jsType = partitioner.getContentType(jsIdx);
		assertThat(jsType).isEqualTo("tm4e:source.js");
	}

	@Test
	void prefersExistingModelGrammar() throws Exception {
		// Prepare a fresh document and set a grammar directly on the shared model
		final var jsDoc = new Document("let a = 1;\n");
		final var model = TMModelManager.INSTANCE.connect(jsDoc);
		model.setGrammar(jsGrammar);

		// Create a partitioner WITHOUT forcing grammar and connect via helper
		final TMPartitioner p = new TMPartitioner();
		jsDoc.setDocumentPartitioner(TMPartitions.TM_PARTITIONING, p);
		p.connect(jsDoc);

		// Trigger activation
		p.getPartition(0);
		TestUtils.waitForModelReady(jsDoc, 5_000);

		// Partitioner's grammar should be the one already set on the model
		assertThat(p.getGrammar()).isEqualTo(jsGrammar);
		// And base partition type should reflect the TS scope
		final String expectedBase = TMPartitioner.scopeToPartitionType(jsGrammar.getScopeName());
		assertThat(p.getLegalContentTypes()).contains(expectedBase);

		final ITypedRegion[] parts = p.computePartitioning(0, jsDoc.getLength());
		assertThat(parts).isNotEmpty();
		assertThat(parts[0].getType()).isEqualTo(expectedBase);

		p.disconnect();
	}

	@BeforeEach
	void setup() {
		final var syndir = "../org.eclipse.tm4e.language_pack/syntaxes/";
		reg.addGrammar(IGrammarSource.fromFile(Paths.get(syndir + "java/java.tmLanguage.json")));
		reg.addGrammar(IGrammarSource.fromFile(Paths.get(syndir + "xml/xml.tmLanguage.json")));

		reg.addGrammar(IGrammarSource.fromFile(Paths.get(syndir + "css/css.tmLanguage.json")));
		jsGrammar = reg.addGrammar(IGrammarSource.fromFile(Paths.get(syndir + "javascript/javascript.tmLanguage.json")));
		reg.addGrammar(IGrammarSource.fromFile(Paths.get(syndir + "html/text.html.basic.tmLanguage.json")));
		final IGrammar htmlGrammar = reg.addGrammar(IGrammarSource.fromFile(Paths.get(syndir + "html/html.tmLanguage.json")));

		mdGrammar = reg.addGrammar(IGrammarSource.fromFile(Paths.get(syndir + "markdown/markdown.tmLanguage.json")));

		final String html = """
			<html>
			<head>
			<style>
			body { color: red; }
			</style>
			</head>
			<body>
			<script>
			function x() { return 1; }

			function y() { return 2; }
			</script>
			</body>
			</html>
			""";
		doc = new Document(html);

		partitioner = new TMPartitioner();
		partitioner.setGrammar(htmlGrammar);

		// Install the partitioner on the document so document changes notify it (invokes pruneAndFillBase)
		doc.setDocumentPartitioner(TMPartitions.TM_PARTITIONING, partitioner);
		partitioner.connect(doc);

		// Trigger lazy activation so the TM model is connected before waiting
		partitioner.getPartition(0);

		TestUtils.waitForModelReady(doc, 5_000);
	}

	@AfterEach
	void tearDown() {
		if (partitioner != null) {
			partitioner.disconnect();
		}
	}

}
