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
package org.eclipse.tm4e.languageconfiguration.internal.utils;

public class TabSpacesInfo {

	private final int tabSize;
	private final boolean insertSpaces;

	public TabSpacesInfo(int tabSize, boolean insertSpaces) {
		this.tabSize = tabSize;
		this.insertSpaces = insertSpaces;
	}
	
	public int getTabSize() {
		return tabSize;
	}
	
	public boolean isInsertSpaces() {
		return insertSpaces;
	}
}
