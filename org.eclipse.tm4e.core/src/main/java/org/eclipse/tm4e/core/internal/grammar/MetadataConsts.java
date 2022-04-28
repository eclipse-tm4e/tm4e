/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.internal.grammar;

/**
 * Helpers to manage the "collapsed" metadata of an entire StackElement stack.
 * The following assumptions have been made:
 * - languageId < 256 => needs 8 bits
 * - unique color count < 512 => needs 9 bits
 *
 * The binary format is:
 * - -------------------------------------------
 * 3322 2222 2222 1111 1111 1100 0000 0000
 * 1098 7654 3210 9876 5432 1098 7654 3210
 * - -------------------------------------------
 * xxxx xxxx xxxx xxxx xxxx xxxx xxxx xxxx
 * bbbb bbbb bfff ffff ffFF FFTT LLLL LLLL
 * - -------------------------------------------
 * - L = LanguageId (8 bits)
 * - T = StandardTokenType (2 bits)
 * - F = FontStyle (4 bits)
 * - f = foreground color (9 bits)
 * - b = background color (9 bits)
 */
final class MetadataConsts {

	/**
	 * content should be accessed statically
	 */
	private MetadataConsts() {
	}

	static final int LANGUAGEID_MASK = 0b00000000000000000000000011111111;
	static final int TOKEN_TYPE_MASK = 0b00000000000000000000001100000000;
	static final int FONT_STYLE_MASK = 0b00000000000000000011110000000000;
	static final int FOREGROUND_MASK = 0b00000000011111111100000000000000;
	static final int BACKGROUND_MASK = 0b11111111100000000000000000000000;

	static final int LANGUAGEID_OFFSET = 0;
	static final int TOKEN_TYPE_OFFSET = 8;
	static final int FONT_STYLE_OFFSET = 10;
	static final int FOREGROUND_OFFSET = 14;
	static final int BACKGROUND_OFFSET = 23;
}
