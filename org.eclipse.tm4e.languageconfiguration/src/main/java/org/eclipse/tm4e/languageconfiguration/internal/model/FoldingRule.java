/**
 * Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.model;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/d79132281222cdab77abeacca1af700e34c2f30b/src/vs/editor/common/languages/languageConfiguration.ts#L139">
 *      github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/languageConfiguration.ts#L139</a>
 */
public final class FoldingRule {

	public final boolean offSide;
	public final String markersStart;
	public final String markersEnd;

	public FoldingRule(final boolean offSide, final String markersStart, final String markersEnd) {
		this.offSide = offSide;
		this.markersStart = markersStart;
		this.markersEnd = markersEnd;
	}
}