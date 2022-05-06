/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.internal.theme.css;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.theme.IStyle;
import org.eclipse.tm4e.core.theme.RGB;
import org.w3c.css.sac.SelectorList;

public class CSSStyle implements IStyle {

	private final SelectorList selector;

	@Nullable
	private RGB color;

	@Nullable
	private RGB backgroundColor;

	private boolean bold;
	private boolean italic;
	private boolean underline;
	private boolean strikeThrough;

	public CSSStyle(final SelectorList selector) {
		this.selector = selector;
	}

	public void setColor(final RGB color) {
		this.color = color;
	}

	@Nullable
	@Override
	public RGB getColor() {
		return color;
	}

	@Nullable
	@Override
	public RGB getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(final RGB backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public SelectorList getSelectorList() {
		return selector;
	}

	public void setBold(final boolean bold) {
		this.bold = bold;
	}

	@Override
	public boolean isBold() {
		return bold;
	}

	public void setItalic(final boolean italic) {
		this.italic = italic;
	}

	@Override
	public boolean isItalic() {
		return italic;
	}

	@Override
	public boolean isUnderline() {
		return underline;
	}

	public void setUnderline(final boolean underline) {
		this.underline = underline;
	}

	@Override
	public boolean isStrikeThrough() {
		return strikeThrough;
	}

	public void setStrikeThrough(final boolean strikeThrough) {
		this.strikeThrough = strikeThrough;
	}

}
