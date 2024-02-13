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

import org.eclipse.jdt.annotation.Nullable;

public class RGB {

	public static @Nullable RGB fromHex(final @Nullable String hex) {
		if (hex == null || hex.isBlank())
			return null;

		final var offset = hex.startsWith("#") ? 1 : 0;
		final int r = Integer.parseInt(hex.substring(offset + 0, offset + 2), 16);
		final int g = Integer.parseInt(hex.substring(offset + 2, offset + 4), 16);
		final int b = Integer.parseInt(hex.substring(offset + 4, offset + 6), 16);

		return new RGB(r, g, b);
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
