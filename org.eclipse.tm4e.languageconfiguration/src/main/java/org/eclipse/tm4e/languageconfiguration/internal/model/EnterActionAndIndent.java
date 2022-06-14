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
package org.eclipse.tm4e.languageconfiguration.internal.model;

public final class EnterActionAndIndent {

	public final EnterAction enterAction;
	public final String indentation;

	public EnterActionAndIndent(final EnterAction enterAction, final String indentation) {
		this.enterAction = enterAction;
		this.indentation = indentation;
	}
}
