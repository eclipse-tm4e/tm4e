/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.core.model;

import static org.eclipse.tm4e.core.registry.IGrammarSource.fromResource;
import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.tm4e.core.Data;
import org.eclipse.tm4e.core.internal.grammar.StateStack;
import org.eclipse.tm4e.core.model.ITMModel.BackgroundTokenizationState;
import org.eclipse.tm4e.core.registry.Registry;
import org.junit.jupiter.api.Test;

class TMModelTest {

	@Test
	void testTokenizeWithTimeout() {
		final var grammar = new Registry().addGrammar(fromResource(Data.class, "TypeScript.tmLanguage.json"));

		final var textLines = """
				function addNumbers(a: number, b: number) { // 1
					return a + b;                           // 2
				}                                           // 3
				const sum = addNumbers(10, 15);             // 4
				console.log('Sum is: ' + sum);              // 5
			""".split("\\r?\\n");
		assertEquals(5, textLines.length);

		final var tmModel = new TMModel(textLines.length) {
			@Override
			public String getLineText(final int lineIndex) throws Exception {
				return textLines[lineIndex];
			}
		};

		try {
			tmModel.setGrammar(grammar);

			assertEquals(BackgroundTokenizationState.COMPLETED, tmModel.getBackgroundTokenizationState());

			// adding a listener will spawn the TokenizerThread
			tmModel.addModelTokensChangedListener(event -> {
			});

			boolean tokenizationWasSeenInProgress = false;
			boolean tokenizationCompleted = false;
			while (!tokenizationCompleted) {
				switch (tmModel.getBackgroundTokenizationState()) {
					case IN_PROGRESS:
						tokenizationWasSeenInProgress = true;
						break;
					case COMPLETED:
						if (tokenizationWasSeenInProgress)
							tokenizationCompleted = true;
						break;
				}
				Thread.yield();
			}

			assertEquals(BackgroundTokenizationState.COMPLETED, tmModel.getBackgroundTokenizationState());

			for (int i = 1; i < textLines.length; i++) {
				assertNotEquals(StateStack.NULL, tmModel.lines.get(i).startState, "Line " + i + " is expected to be up-to-date");
			}
			for (int i = 0; i < textLines.length; i++) {
				assertNotNull(tmModel.lines.get(i).tokens, "Line " + i + " is expected to be up-to-date");
			}
		} finally {
			tmModel.dispose();
		}
	}
}
