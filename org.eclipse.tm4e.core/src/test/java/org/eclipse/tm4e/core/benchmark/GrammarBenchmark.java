/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.core.benchmark;

import java.util.Arrays;

import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.IStateStack;
import org.eclipse.tm4e.core.internal.utils.ResourceUtils;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.Registry;

public final class GrammarBenchmark implements Runnable {

	private static final int WARMUP_ROUNDS = 3;
	private static final int BENCHMARK_ROUNDS = 3;
	private static final int OPS_PER_BENCHMARK_ROUND = 50;

	public static void main(final String... args) throws Exception {
		BenchmarkRunner.run(WARMUP_ROUNDS, BENCHMARK_ROUNDS, OPS_PER_BENCHMARK_ROUND, new GrammarBenchmark());
	}

	final IGrammar grammar;
	final String[] sourceCode;

	GrammarBenchmark() throws Exception {
		/*
		 * load the grammar
		 */
		grammar = new Registry().addGrammar(IGrammarSource.fromResource(GrammarBenchmark.class, "GrammarBenchmark.Java.tmLanguage.json"));

		/*
		 * load the file to be parsed
		 */
		try (var sourceFileReader = ResourceUtils.getResourceReader(GrammarBenchmark.class, "GrammarBenchmark.JavaFile.txt")) {
			sourceCode = sourceFileReader
					.lines()
					.toArray(String[]::new);
		}
		System.out.println(String.format("Source Code chars: %,d", Arrays.stream(sourceCode).mapToInt(String::length).sum()));
		System.out.println(String.format("Source Code lines: %,d", sourceCode.length));
	}

	/**
	 * Tokenize all lines of the pre-loaded source file
	 */
	@Override
	public void run() {
		IStateStack state = null;
		for (final var line : sourceCode) {
			state = grammar.tokenizeLine(line, state, null).getRuleStack();
		}
	}
}
