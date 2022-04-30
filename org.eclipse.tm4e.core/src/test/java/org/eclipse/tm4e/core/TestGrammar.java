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
package org.eclipse.tm4e.core;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.eclipse.tm4e.core.registry.Registry;

public class TestGrammar {

	public static void main(String[] args) throws Exception {

		Registry registry = new Registry();
		IGrammar grammar = castNonNull(registry.loadGrammarFromPathSync("Angular2TypeScript.tmLanguage",
				TestGrammar.class.getResourceAsStream("Angular2TypeScript.tmLanguage")));

		ITokenizeLineResult result = grammar.tokenizeLine("/** **/");
		for (int i = 0; i < result.getTokens().length; i++) {
			System.err.println(result.getTokens()[i]);
		}
	}
}
