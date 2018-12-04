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
package org.eclipse.tm4e.core.registry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.eclipse.tm4e.core.theme.IRawTheme;

public interface IRegistryOptions {

	public static final IRegistryOptions DEFAULT_LOCATOR = new IRegistryOptions() {

		@Override
		public String getFilePath(String scopeName) {
			return null;
		}
		
		@Override
		public InputStream getInputStream(String scopeName) {
			return null;
		}

		@Override
		public Collection<String> getInjections(String scopeName) {
			return null;
		}
		
	};
	
	default IRawTheme getTheme() {
		return null;
	}
	
	String getFilePath(String scopeName);

	InputStream getInputStream(String scopeName) throws IOException;
	
	Collection<String> getInjections(String scopeName);
	
}
