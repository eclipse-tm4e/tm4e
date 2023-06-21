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
package org.eclipse.tm4e.ui.tests.support.command;

public abstract class Command implements ICommand {

	private final String name;
	private String styleRanges;
	private boolean done;

	protected Command(final String name) {
		this.name = name;
		this.done = false;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setStyleRanges(final String styleRanges) {
		this.styleRanges = styleRanges;
	}

	@Override
	public String getStyleRanges() {
		return styleRanges;
	}

	public void execute() {
		if (!done) {
			doExecute();
			done = true;
		}
	}

	protected abstract void doExecute();

	public abstract Integer getLineTo();

	public static String toText(final String text) {
		final var newText = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			switch (c) {
				case '\n':
					newText.append("\\n");
					break;
				case '\r':
					newText.append("\\r");
					break;
				case '"':
					newText.append("\\\"");
					break;
				default:
					newText.append(c);
			}
		}
		return newText.toString();
	}

}
