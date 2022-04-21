/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/Microsoft/vscode-textmate/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 * - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 * - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.internal.rule;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.tm4e.core.internal.oniguruma.OnigCaptureIndex;

/**
 *
 * @see <a href="https://github.com/Microsoft/vscode-textmate/blob/master/src/rule.ts">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/rule.ts</a>
 *
 */
final class RegExpSource {

	private static final Pattern HAS_BACK_REFERENCES = Pattern.compile("\\\\(\\d+)");
	private static final Pattern BACK_REFERENCING_END = Pattern.compile("\\\\(\\d+)");
	private static final Pattern REGEXP_CHARACTERS = Pattern
			.compile("[\\-\\\\\\{\\}\\*\\+\\?\\|\\^\\$\\.\\,\\[\\]\\(\\)\\#\\s]");

	private final int ruleId;
	private boolean _hasAnchor;
	private final boolean _hasBackReferences;
	private RegExpSourceAnchorCache anchorCache;
	private String source;

	RegExpSource(String regExpSource, int ruleId) {
		this(regExpSource, ruleId, true);
	}

	private RegExpSource(String regExpSource, int ruleId, boolean handleAnchors) {
		if (handleAnchors) {
			this._handleAnchors(regExpSource);
		} else {
			this.source = regExpSource;
			this._hasAnchor = false;
		}

		if (this._hasAnchor) {
			this.anchorCache = this._buildAnchorCache();
		}

		this.ruleId = ruleId;
		this._hasBackReferences = HAS_BACK_REFERENCES.matcher(this.source).find();

		// console.log('input: ' + regExpSource + ' => ' + this.source + ', ' +
		// this.hasAnchor);
	}

	@Override
	protected RegExpSource clone() {
		return new RegExpSource(this.source, this.ruleId, true);
	}

	void setSource(String newSource) {
		if (this.source.equals(newSource)) {
			return;
		}
		this.source = newSource;

		if (this._hasAnchor) {
			this.anchorCache = this._buildAnchorCache();
		}
	}

	private void _handleAnchors(String regExpSource) {
		if (regExpSource != null) {
			int len = regExpSource.length();
			char ch;
			char nextCh;
			int lastPushedPos = 0;
			final StringBuilder output = new StringBuilder();

			boolean hasAnchor = false;
			for (int pos = 0; pos < len; pos++) {
				ch = regExpSource.charAt(pos);

				if (ch == '\\') {
					if (pos + 1 < len) {
						nextCh = regExpSource.charAt(pos + 1);
						if (nextCh == 'z') {
							output.append(regExpSource.substring(lastPushedPos, pos));
							output.append("$(?!\\n)(?<!\\n)");
							lastPushedPos = pos + 2;
						} else if (nextCh == 'A' || nextCh == 'G') {
							hasAnchor = true;
						}
						pos++;
					}
				}
			}

			this._hasAnchor = hasAnchor;
			if (lastPushedPos == 0) {
				// No \z hit
				this.source = regExpSource;
			} else {
				output.append(regExpSource.substring(lastPushedPos, len));
				this.source = output.toString(); // join('');
			}
		} else {
			this._hasAnchor = false;
			this.source = regExpSource;
		}
	}

	String resolveBackReferences(String lineText, OnigCaptureIndex[] captureIndices) {
		try {
		List<String> capturedValues = Arrays.stream(captureIndices)
				.map(capture -> lineText.substring(capture.getStart(), capture.getEnd())).collect(Collectors.toList());
		Matcher m = BACK_REFERENCING_END.matcher(this.source);
		final StringBuilder sb = new StringBuilder();
		while (m.find()) {
			String g1 = m.group();
			int index = Integer.parseInt(g1.substring(1));
			String replacement = escapeRegExpCharacters(capturedValues.size() > index ? capturedValues.get(index) : "");
			m.appendReplacement(sb, replacement);
		}
		m.appendTail(sb);
		return sb.toString();
		}
		catch(Throwable e) {
			//e.printStackTrace();
		}

		return lineText;
	}

	private String escapeRegExpCharacters(String value) {
		Matcher m = REGEXP_CHARACTERS.matcher(value);
		final StringBuilder sb = new StringBuilder();
		while (m.find()) {
			m.appendReplacement(sb, "\\\\\\\\" + m.group());
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private RegExpSourceAnchorCache _buildAnchorCache() {

		// Collection<String> A0_G0_result=new ArrayList<Character>();
		// Collection<String> A0_G1_result=new ArrayList<String>();
		// Collection<String> A1_G0_result=new ArrayList<String>();
		// Collection<String> A1_G1_result=new ArrayList<String>();

		StringBuilder A0_G0_result = new StringBuilder();
		StringBuilder A0_G1_result = new StringBuilder();
		StringBuilder A1_G0_result = new StringBuilder();
		StringBuilder A1_G1_result = new StringBuilder();
		int pos;
		int len;
		char ch;
		char nextCh;

		for (pos = 0, len = this.source.length(); pos < len; pos++) {
			ch = this.source.charAt(pos);
			A0_G0_result.append(ch);
			A0_G1_result.append(ch);
			A1_G0_result.append(ch);
			A1_G1_result.append(ch);

			if (ch == '\\') {
				if (pos + 1 < len) {
					nextCh = this.source.charAt(pos + 1);
					if (nextCh == 'A') {
						A0_G0_result.append('\uFFFF');
						A0_G1_result.append('\uFFFF');
						A1_G0_result.append('A');
						A1_G1_result.append('A');
					} else if (nextCh == 'G') {
						A0_G0_result.append('\uFFFF');
						A0_G1_result.append('G');
						A1_G0_result.append('\uFFFF');
						A1_G1_result.append('G');
					} else {
						A0_G0_result.append(nextCh);
						A0_G1_result.append(nextCh);
						A1_G0_result.append(nextCh);
						A1_G1_result.append(nextCh);
					}
					pos++;
				}
			}
		}

		return new RegExpSourceAnchorCache(A0_G0_result.toString(), A0_G1_result.toString(), A1_G0_result.toString(),
				A1_G1_result.toString()
		// StringUtils.join(A0_G0_result, ""),
		// StringUtils.join(A0_G1_result, ""),
		// StringUtils.join(A1_G0_result, ""),
		// StringUtils.join(A1_G1_result, "")
		);
	}

	String resolveAnchors(boolean allowA, boolean allowG) {
		if (!this._hasAnchor) {
			return this.source;
		}

		if (allowA) {
			if (allowG) {
				return this.anchorCache.A1_G1;
			}
			return this.anchorCache.A1_G0;
		}
		if (allowG) {
			return this.anchorCache.A0_G1;
		}
		return this.anchorCache.A0_G0;
	}

	boolean hasAnchor() {
		return this._hasAnchor;
	}

	String getSource() {
		return this.source;
	}

	Integer getRuleId() {
		return this.ruleId;
	}

	boolean hasBackReferences() {
		return this._hasBackReferences;
	}

}
