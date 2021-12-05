/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
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
 *  - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 *  - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.internal.rule;

public class IRegExpSourceAnchorCache {

	public final String A0_G0;
	public final String A0_G1;
	public final String A1_G0;
	public final String A1_G1;

	public IRegExpSourceAnchorCache(String A0_G0, String A0_G1, String A1_G0, String A1_G1) {
		this.A0_G0 = A0_G0;
		this.A0_G1 = A0_G1;
		this.A1_G0 = A1_G0;
		this.A1_G1 = A1_G1;
	}
}
