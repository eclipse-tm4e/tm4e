/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * - Sebastian Thomschke (Vegard IT) - add methods hashCode/equals, fromHex(String)
 */
package org.eclipse.tm4e.core.theme;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

public class RGB {

	private static final Logger LOGGER = System.getLogger(RGB.class.getName());

	public static @Nullable RGB fromHex(final @Nullable String hex) {
		if (hex == null || hex.isBlank())
			return null;

		final var offset = hex.startsWith("#") ? 1 : 0;
		final int digitLength = hex.length() - offset;

		final String r, g, b;
		if (digitLength == 3) {
			// Expand 3-digit format: #RGB -> #RRGGBB
			final String r0 = hex.substring(offset + 0, offset + 1);
			final String g0 = hex.substring(offset + 1, offset + 2);
			final String b0 = hex.substring(offset + 2, offset + 3);
			r = r0 + r0;
			g = g0 + g0;
			b = b0 + b0;
		} else if (digitLength == 6) {
			r = hex.substring(offset + 0, offset + 2);
			g = hex.substring(offset + 2, offset + 4);
			b = hex.substring(offset + 4, offset + 6);
		} else {
			LOGGER.log(Level.WARNING, "Invalid hex color string '" + hex +
					"': expected format '#RGB' (3 hex digits) or '#RRGGBB' (6 hex digits)");
			return null;
		}

		return new RGB( //
				Integer.parseInt(r, 16), //
				Integer.parseInt(g, 16), //
				Integer.parseInt(b, 16));
	}

	public final int red;
	public final int green;
	public final int blue;

	public RGB(final int red, final int green, final int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	@Override
	public String toString() {
		return "RGB(" + red + "," + green + "," + blue + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + blue;
		result = prime * result + green;
		result = prime * result + red;
		return result;
	}

	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final RGB other = (RGB) obj;
		if (blue != other.blue)
			return false;
		if (green != other.green)
			return false;
		if (red != other.red)
			return false;
		return true;
	}
}
