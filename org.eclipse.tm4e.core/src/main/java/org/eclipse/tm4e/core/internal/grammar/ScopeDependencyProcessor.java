/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
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
 * - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 * - Sebastian Thomschke - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.internal.grammar;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.TMException;
import org.eclipse.tm4e.core.internal.registry.IGrammarRepository;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;
import org.eclipse.tm4e.core.internal.types.IRawRepository;
import org.eclipse.tm4e.core.internal.types.IRawRule;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/grammar.ts#L161">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/grammar.ts</a>
 */
class ScopeDependencyProcessor {

	private abstract static class ScopeDependency {
		final String scopeName;

		ScopeDependency(final String scopeName) {
			this.scopeName = scopeName;
		}
	}

	private static final class FullScopeDependency extends ScopeDependency {
		FullScopeDependency(final String scopeName) {
			super(scopeName);
		}
	}

	private static final class PartialScopeDependency extends ScopeDependency {
		final String include;

		PartialScopeDependency(final String scopeName, final String include) {
			super(scopeName);
			this.include = include;
		}

		String toKey() {
			return this.scopeName + '#' + this.include;
		}
	}

	private static class ScopeDependencyCollector {

		final List<FullScopeDependency> full = new ArrayList<>();
		final List<PartialScopeDependency> partial = new ArrayList<>();

		final Set<IRawRule> visitedRule = new HashSet<>();
		private final Set<String> seenFull = new HashSet<>();
		private final Set<String> seenPartial = new HashSet<>();

		void add(final ScopeDependency dep) {
			if (dep instanceof FullScopeDependency) {
				final var fdep = (FullScopeDependency) dep;
				if (!this.seenFull.contains(fdep.scopeName)) {
					this.seenFull.add(fdep.scopeName);
					this.full.add(fdep);
				}
			} else {
				final var pdep = (PartialScopeDependency) dep;
				if (!this.seenPartial.contains(pdep.toKey())) {
					this.seenPartial.add(pdep.toKey());
					this.partial.add(pdep);
				}
			}
		}
	}

	final Set<String> seenFullScopeRequests = new HashSet<>();
	final Set<String> seenPartialScopeRequests = new HashSet<>();
	List<ScopeDependency> queue = new ArrayList<>();

	public final IGrammarRepository repo;
	public final String initialScopeName;

	ScopeDependencyProcessor(final IGrammarRepository repo, final String initialScopeName) {
		this.repo = repo;
		this.initialScopeName = initialScopeName;
		this.seenFullScopeRequests.add(initialScopeName);
		this.queue.add(new FullScopeDependency(initialScopeName));
	}

	public void processQueue() {
		final var q = queue;
		queue = new ArrayList<>();

		final var deps = new ScopeDependencyCollector();
		for (final var dep : q) {
			collectDependenciesForDep(this.repo, this.initialScopeName, deps, dep);
		}

		for (final var dep : deps.full) {
			if (this.seenFullScopeRequests.contains(dep.scopeName)) {
				// already processed
				continue;
			}
			this.seenFullScopeRequests.add(dep.scopeName);
			this.queue.add(dep);
		}

		for (final var dep : deps.partial) {
			if (this.seenFullScopeRequests.contains(dep.scopeName)) {
				// already processed in full
				continue;
			}
			if (this.seenPartialScopeRequests.contains(dep.toKey())) {
				// already processed
				continue;
			}
			this.seenPartialScopeRequests.add(dep.toKey());
			this.queue.add(dep);
		}
	}

	void collectDependenciesForDep(
			final IGrammarRepository repo,
			final String initialScopeName,
			final ScopeDependencyCollector result,
			final ScopeDependency dep) {
		final var grammar = repo.lookup(dep.scopeName);
		if (grammar == null) {
			if (dep.scopeName.equals(initialScopeName)) {
				throw new TMException("No grammar provided for <" + initialScopeName + ">");
			}
			return;
		}

		final var initialGrammar = repo.lookup(initialScopeName);
		if (dep instanceof FullScopeDependency) {
			collectDependencies(result, castNonNull(initialGrammar), grammar);
		} else {
			final var pdep = (PartialScopeDependency) dep;
			collectSpecificDependencies(result, castNonNull(initialGrammar), grammar, pdep.include, null);
		}

		final var injections = repo.injections(dep.scopeName);
		if (injections != null) {
			for (final var injection : injections) {
				result.add(new FullScopeDependency(injection));
			}
		}
	}

	/**
	 * Collect a specific dependency from the grammar's repository
	 */
	void collectSpecificDependencies(final ScopeDependencyCollector result, final IRawGrammar baseGrammar,
			final IRawGrammar selfGrammar,
			final String include, @Nullable IRawRepository repository) {
		if (repository == null && selfGrammar.isRepositorySet()) {
			repository = selfGrammar.getRepository();
		}
		if (repository != null) {
			final var rule = repository.getRule(include);
			if (rule != null) {
				extractIncludedScopesInPatterns(result, baseGrammar, selfGrammar, List.of(rule), repository);
			}
		}
	}

	/**
	 * Collects the list of all external included scopes in `grammar`.
	 */
	void collectDependencies(final ScopeDependencyCollector result, final IRawGrammar baseGrammar,
			final IRawGrammar selfGrammar) {
		final var patterns = selfGrammar.getPatterns();
		if (patterns != null) {
			extractIncludedScopesInPatterns(result, baseGrammar, selfGrammar, patterns, selfGrammar.getRepository());
		}
		final var injections = selfGrammar.getInjections();
		if (injections != null) {
			extractIncludedScopesInPatterns(result, baseGrammar, selfGrammar, injections.values(),
					selfGrammar.getRepository());
		}
	}

	/**
	 * Fill in `result` all external included scopes in `patterns`
	 */
	void extractIncludedScopesInPatterns(
			final ScopeDependencyCollector result,
			final IRawGrammar baseGrammar,
			final IRawGrammar selfGrammar,
			final Collection<IRawRule> patterns,
			@Nullable final IRawRepository repository) {
		for (final var pattern : patterns) {
			if (result.visitedRule.contains(pattern)) {
				continue;
			}
			result.visitedRule.add(pattern);

			final var patternRepository = pattern.getRepository() == null
					? repository
					: IRawRepository.merge(repository, pattern.getRepository());
			final var patternPatterns = pattern.getPatterns();
			if (patternPatterns != null) {
				extractIncludedScopesInPatterns(result, baseGrammar, selfGrammar, patternPatterns, patternRepository);
			}

			final var include = pattern.getInclude();
			if (include == null) {
				continue;
			}

			if (include.equals(RawRepository.DOLLAR_BASE) || include.equals(baseGrammar.getScopeName())) {
				collectDependencies(result, baseGrammar, baseGrammar);
			} else if (include.equals(RawRepository.DOLLAR_SELF) || include.equals(selfGrammar.getScopeName())) {
				collectDependencies(result, baseGrammar, selfGrammar);
			} else if (include.charAt(0) == '#') {
				collectSpecificDependencies(result, baseGrammar, selfGrammar, include.substring(1), patternRepository);
			} else {
				final var sharpIndex = include.indexOf('#');
				if (sharpIndex >= 0) {
					final var scopeName = include.substring(0, sharpIndex);
					final var includedName = include.substring(sharpIndex + 1);
					if (scopeName.equals(baseGrammar.getScopeName())) {
						collectSpecificDependencies(result, baseGrammar, baseGrammar, includedName, patternRepository);
					} else if (scopeName.equals(selfGrammar.getScopeName())) {
						collectSpecificDependencies(result, baseGrammar, selfGrammar, includedName, patternRepository);
					} else {
						result.add(new PartialScopeDependency(scopeName, include.substring(sharpIndex + 1)));
					}
				} else {
					result.add(new FullScopeDependency(include));
				}
			}
		}
	}
}
