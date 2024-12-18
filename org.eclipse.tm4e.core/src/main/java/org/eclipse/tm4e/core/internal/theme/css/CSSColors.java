/**
 * Copyright (c) 2024 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT) - initial implementation
 */
package org.eclipse.tm4e.core.internal.theme.css;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.theme.RGB;

public final class CSSColors {
	private static final Map<String, RGB> NAMED_COLORS = new HashMap<>();
	static {
		NAMED_COLORS.put("aliceblue", new RGB(240, 248, 255));
		NAMED_COLORS.put("antiquewhite", new RGB(250, 235, 215));
		NAMED_COLORS.put("aqua", new RGB(0, 255, 255));
		NAMED_COLORS.put("aquamarine", new RGB(127, 255, 212));
		NAMED_COLORS.put("azure", new RGB(240, 255, 255));
		NAMED_COLORS.put("beige", new RGB(245, 245, 220));
		NAMED_COLORS.put("bisque", new RGB(255, 228, 196));
		NAMED_COLORS.put("black", new RGB(0, 0, 0));
		NAMED_COLORS.put("blanchedalmond", new RGB(255, 235, 205));
		NAMED_COLORS.put("blue", new RGB(0, 0, 255));
		NAMED_COLORS.put("blueviolet", new RGB(138, 43, 226));
		NAMED_COLORS.put("brown", new RGB(165, 42, 42));
		NAMED_COLORS.put("burlywood", new RGB(222, 184, 135));
		NAMED_COLORS.put("cadetblue", new RGB(95, 158, 160));
		NAMED_COLORS.put("chartreuse", new RGB(127, 255, 0));
		NAMED_COLORS.put("chocolate", new RGB(210, 105, 30));
		NAMED_COLORS.put("coral", new RGB(255, 127, 80));
		NAMED_COLORS.put("cornflowerblue", new RGB(100, 149, 237));
		NAMED_COLORS.put("cornsilk", new RGB(255, 248, 220));
		NAMED_COLORS.put("crimson", new RGB(220, 20, 60));
		NAMED_COLORS.put("cyan", new RGB(0, 255, 255));
		NAMED_COLORS.put("darkblue", new RGB(0, 0, 139));
		NAMED_COLORS.put("darkcyan", new RGB(0, 139, 139));
		NAMED_COLORS.put("darkgoldenrod", new RGB(184, 134, 11));
		NAMED_COLORS.put("darkgray", new RGB(169, 169, 169));
		NAMED_COLORS.put("darkgrey", new RGB(169, 169, 169));
		NAMED_COLORS.put("darkgreen", new RGB(0, 100, 0));
		NAMED_COLORS.put("darkkhaki", new RGB(189, 183, 107));
		NAMED_COLORS.put("darkmagenta", new RGB(139, 0, 139));
		NAMED_COLORS.put("darkolivegreen", new RGB(85, 107, 47));
		NAMED_COLORS.put("darkorange", new RGB(255, 140, 0));
		NAMED_COLORS.put("darkorchid", new RGB(153, 50, 204));
		NAMED_COLORS.put("darkred", new RGB(139, 0, 0));
		NAMED_COLORS.put("darksalmon", new RGB(233, 150, 122));
		NAMED_COLORS.put("darkseagreen", new RGB(143, 188, 143));
		NAMED_COLORS.put("darkslateblue", new RGB(72, 61, 139));
		NAMED_COLORS.put("darkslategray", new RGB(47, 79, 79));
		NAMED_COLORS.put("darkslategrey", new RGB(47, 79, 79));
		NAMED_COLORS.put("darkturquoise", new RGB(0, 206, 209));
		NAMED_COLORS.put("darkviolet", new RGB(148, 0, 211));
		NAMED_COLORS.put("deeppink", new RGB(255, 20, 147));
		NAMED_COLORS.put("deepskyblue", new RGB(0, 191, 255));
		NAMED_COLORS.put("dimgray", new RGB(105, 105, 105));
		NAMED_COLORS.put("dimgrey", new RGB(105, 105, 105));
		NAMED_COLORS.put("dodgerblue", new RGB(30, 144, 255));
		NAMED_COLORS.put("firebrick", new RGB(178, 34, 34));
		NAMED_COLORS.put("floralwhite", new RGB(255, 250, 240));
		NAMED_COLORS.put("forestgreen", new RGB(34, 139, 34));
		NAMED_COLORS.put("fuchsia", new RGB(255, 0, 255));
		NAMED_COLORS.put("gainsboro", new RGB(220, 220, 220));
		NAMED_COLORS.put("ghostwhite", new RGB(248, 248, 255));
		NAMED_COLORS.put("gold", new RGB(255, 215, 0));
		NAMED_COLORS.put("goldenrod", new RGB(218, 165, 32));
		NAMED_COLORS.put("gray", new RGB(128, 128, 128));
		NAMED_COLORS.put("grey", new RGB(128, 128, 128));
		NAMED_COLORS.put("green", new RGB(0, 128, 0));
		NAMED_COLORS.put("greenyellow", new RGB(173, 255, 47));
		NAMED_COLORS.put("honeydew", new RGB(240, 255, 240));
		NAMED_COLORS.put("hotpink", new RGB(255, 105, 180));
		NAMED_COLORS.put("indianred", new RGB(205, 92, 92));
		NAMED_COLORS.put("indigo", new RGB(75, 0, 130));
		NAMED_COLORS.put("ivory", new RGB(255, 255, 240));
		NAMED_COLORS.put("khaki", new RGB(240, 230, 140));
		NAMED_COLORS.put("lavender", new RGB(230, 230, 250));
		NAMED_COLORS.put("lavenderblush", new RGB(255, 240, 245));
		NAMED_COLORS.put("lawngreen", new RGB(124, 252, 0));
		NAMED_COLORS.put("lemonchiffon", new RGB(255, 250, 205));
		NAMED_COLORS.put("lightblue", new RGB(173, 216, 230));
		NAMED_COLORS.put("lightcoral", new RGB(240, 128, 128));
		NAMED_COLORS.put("lightcyan", new RGB(224, 255, 255));
		NAMED_COLORS.put("lightgoldenrodyellow", new RGB(250, 250, 210));
		NAMED_COLORS.put("lightgray", new RGB(211, 211, 211));
		NAMED_COLORS.put("lightgrey", new RGB(211, 211, 211));
		NAMED_COLORS.put("lightgreen", new RGB(144, 238, 144));
		NAMED_COLORS.put("lightpink", new RGB(255, 182, 193));
		NAMED_COLORS.put("lightsalmon", new RGB(255, 160, 122));
		NAMED_COLORS.put("lightseagreen", new RGB(32, 178, 170));
		NAMED_COLORS.put("lightskyblue", new RGB(135, 206, 250));
		NAMED_COLORS.put("lightslategray", new RGB(119, 136, 153));
		NAMED_COLORS.put("lightslategrey", new RGB(119, 136, 153));
		NAMED_COLORS.put("lightsteelblue", new RGB(176, 196, 222));
		NAMED_COLORS.put("lightyellow", new RGB(255, 255, 224));
		NAMED_COLORS.put("lime", new RGB(0, 255, 0));
		NAMED_COLORS.put("limegreen", new RGB(50, 205, 50));
		NAMED_COLORS.put("linen", new RGB(250, 240, 230));
		NAMED_COLORS.put("magenta", new RGB(255, 0, 255));
		NAMED_COLORS.put("maroon", new RGB(128, 0, 0));
		NAMED_COLORS.put("mediumaquamarine", new RGB(102, 205, 170));
		NAMED_COLORS.put("mediumblue", new RGB(0, 0, 205));
		NAMED_COLORS.put("mediumorchid", new RGB(186, 85, 211));
		NAMED_COLORS.put("mediumpurple", new RGB(147, 112, 219));
		NAMED_COLORS.put("mediumseagreen", new RGB(60, 179, 113));
		NAMED_COLORS.put("mediumslateblue", new RGB(123, 104, 238));
		NAMED_COLORS.put("mediumspringgreen", new RGB(0, 250, 154));
		NAMED_COLORS.put("mediumturquoise", new RGB(72, 209, 204));
		NAMED_COLORS.put("mediumvioletred", new RGB(199, 21, 133));
		NAMED_COLORS.put("midnightblue", new RGB(25, 25, 112));
		NAMED_COLORS.put("mintcream", new RGB(245, 255, 250));
		NAMED_COLORS.put("mistyrose", new RGB(255, 228, 225));
		NAMED_COLORS.put("moccasin", new RGB(255, 228, 181));
		NAMED_COLORS.put("navajowhite", new RGB(255, 222, 173));
		NAMED_COLORS.put("navy", new RGB(0, 0, 128));
		NAMED_COLORS.put("oldlace", new RGB(253, 245, 230));
		NAMED_COLORS.put("olive", new RGB(128, 128, 0));
		NAMED_COLORS.put("olivedrab", new RGB(107, 142, 35));
		NAMED_COLORS.put("orange", new RGB(255, 165, 0));
		NAMED_COLORS.put("orangered", new RGB(255, 69, 0));
		NAMED_COLORS.put("orchid", new RGB(218, 112, 214));
		NAMED_COLORS.put("palegoldenrod", new RGB(238, 232, 170));
		NAMED_COLORS.put("palegreen", new RGB(152, 251, 152));
		NAMED_COLORS.put("paleturquoise", new RGB(175, 238, 238));
		NAMED_COLORS.put("palevioletred", new RGB(219, 112, 147));
		NAMED_COLORS.put("papayawhip", new RGB(255, 239, 213));
		NAMED_COLORS.put("peachpuff", new RGB(255, 218, 185));
		NAMED_COLORS.put("peru", new RGB(205, 133, 63));
		NAMED_COLORS.put("pink", new RGB(255, 192, 203));
		NAMED_COLORS.put("plum", new RGB(221, 160, 221));
		NAMED_COLORS.put("powderblue", new RGB(176, 224, 230));
		NAMED_COLORS.put("purple", new RGB(128, 0, 128));
		NAMED_COLORS.put("red", new RGB(255, 0, 0));
		NAMED_COLORS.put("rosybrown", new RGB(188, 143, 143));
		NAMED_COLORS.put("royalblue", new RGB(65, 105, 225));
		NAMED_COLORS.put("saddlebrown", new RGB(139, 69, 19));
		NAMED_COLORS.put("salmon", new RGB(250, 128, 114));
		NAMED_COLORS.put("sandybrown", new RGB(244, 164, 96));
		NAMED_COLORS.put("seagreen", new RGB(46, 139, 87));
		NAMED_COLORS.put("seashell", new RGB(255, 245, 238));
		NAMED_COLORS.put("sienna", new RGB(160, 82, 45));
		NAMED_COLORS.put("silver", new RGB(192, 192, 192));
		NAMED_COLORS.put("skyblue", new RGB(135, 206, 235));
		NAMED_COLORS.put("slateblue", new RGB(106, 90, 205));
		NAMED_COLORS.put("slategray", new RGB(112, 128, 144));
		NAMED_COLORS.put("slategrey", new RGB(112, 128, 144));
		NAMED_COLORS.put("snow", new RGB(255, 250, 250));
		NAMED_COLORS.put("springgreen", new RGB(0, 255, 127));
		NAMED_COLORS.put("steelblue", new RGB(70, 130, 180));
		NAMED_COLORS.put("tan", new RGB(210, 180, 140));
		NAMED_COLORS.put("teal", new RGB(0, 128, 128));
		NAMED_COLORS.put("thistle", new RGB(216, 191, 216));
		NAMED_COLORS.put("tomato", new RGB(255, 99, 71));
		NAMED_COLORS.put("turquoise", new RGB(64, 224, 208));
		NAMED_COLORS.put("violet", new RGB(238, 130, 238));
		NAMED_COLORS.put("wheat", new RGB(245, 222, 179));
		NAMED_COLORS.put("white", new RGB(255, 255, 255));
		NAMED_COLORS.put("whitesmoke", new RGB(245, 245, 245));
		NAMED_COLORS.put("yellow", new RGB(255, 255, 0));
		NAMED_COLORS.put("yellowgreen", new RGB(154, 205, 50));
	}

	/**
	 * @see <a href="https://www.w3.org/wiki/CSS/Properties/color/keywords">w3.org/wiki/CSS/Properties/color/keywords</a>
	 */
	public static @Nullable RGB getByName(final String name) {
		return NAMED_COLORS.get(name);
	}

	private CSSColors() {
	}
}
