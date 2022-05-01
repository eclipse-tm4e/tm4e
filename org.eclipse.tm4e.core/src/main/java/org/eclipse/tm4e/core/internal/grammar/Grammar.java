/**
 * Copyright (c) 2015-2017 Angelo ZERR.
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
 * - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 * - Fabio Zadrozny <fabiofz@gmail.com> - Not adding '\n' on tokenize if it already finished with '\n'
 */
package org.eclipse.tm4e.core.internal.grammar;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult2;
import org.eclipse.tm4e.core.grammar.StackElement;
import org.eclipse.tm4e.core.internal.matcher.Matcher;
import org.eclipse.tm4e.core.internal.oniguruma.OnigString;
import org.eclipse.tm4e.core.internal.registry.IGrammarRepository;
import org.eclipse.tm4e.core.internal.rule.IRuleFactoryHelper;
import org.eclipse.tm4e.core.internal.rule.Rule;
import org.eclipse.tm4e.core.internal.rule.RuleFactory;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;
import org.eclipse.tm4e.core.internal.types.IRawRepository;
import org.eclipse.tm4e.core.internal.types.IRawRule;
import org.eclipse.tm4e.core.theme.IThemeProvider;

/**
 * TextMate grammar implementation.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/grammar.ts#L459">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/grammar.ts</a>
 */
public final class Grammar implements IGrammar, IRuleFactoryHelper {

	private static final Logger LOGGER = System.getLogger(RuleFactory.class.getName());

	private final String scopeName;

	private int rootId = -1;
	private int lastRuleId = 0;
	private final Map<Integer, @Nullable Rule> ruleId2desc = new HashMap<>();
	private final Map<String, IRawGrammar> includedGrammars = new HashMap<>();
	private final IGrammarRepository grammarRepository;
	private final IRawGrammar grammar;

	@Nullable
	private List<Injection> injections;
	private final ScopeMetadataProvider scopeMetadataProvider;
	private final List<TokenTypeMatcher> tokenTypeMatchers = new ArrayList<>();

	@Nullable
	private final BalancedBracketSelectors balancedBracketSelectors;

	public Grammar(
			final String scopeName,
			final IRawGrammar grammar,
			final int initialLanguage,
			@Nullable final Map<String, Integer> embeddedLanguages,
			@Nullable Map<String, Integer> tokenTypes,
			@Nullable BalancedBracketSelectors balancedBracketSelectors,
			final IGrammarRepository grammarRepository,
			final IThemeProvider themeProvider) {
		this.scopeName = scopeName;
		this.scopeMetadataProvider = new ScopeMetadataProvider(initialLanguage, themeProvider, embeddedLanguages);
		this.balancedBracketSelectors = balancedBracketSelectors;
		this.grammarRepository = grammarRepository;
		this.grammar = initGrammar(grammar, null);

		if (tokenTypes != null) {
			for (final var selector : tokenTypes.keySet()) {
				for (final var matcher : Matcher.createMatchers(selector)) {
					tokenTypeMatchers.add(new TokenTypeMatcher() {
						@Override
						public int getType() {
							return tokenTypes.get(selector);
						}

						@Override
						public Matcher<List<String>> getMatcher() {
							return matcher.matcher;
						}
					});
				}
			}
		}
	}

	public void onDidChangeTheme() {
		this.scopeMetadataProvider.onDidChangeTheme();
	}

	ScopeMetadata getMetadataForScope(final String scope) {
		return this.scopeMetadataProvider.getMetadataForScope(scope);
	}

	private void collectInjections(final List<Injection> result, final String selector, final IRawRule rule,
			final IRuleFactoryHelper ruleFactoryHelper, final IRawGrammar grammar) {
		final var matchers = Matcher.createMatchers(selector);
		final int ruleId = RuleFactory.getCompiledRuleId(rule, ruleFactoryHelper, this.grammar.getRepositorySafe());

		for (final var matcher : matchers) {
			result.add(new Injection(selector, matcher.matcher, ruleId, grammar, matcher.priority));
		}
	}

	private List<Injection> collectInjections() {
		final var grammarRepository = new IGrammarRepository() {
			@Override
			public @Nullable IRawGrammar lookup(final String scopeName) {
				if (Objects.equals(scopeName, Grammar.this.scopeName)) {
					return Grammar.this.grammar;
				}
				return getExternalGrammar(scopeName, null);
			}

			@Override
			public @Nullable Collection<String> injections(final String targetScope) {
				return Grammar.this.grammarRepository.injections(targetScope);
			}
		};

		final var dependencyProcessor = new ScopeDependencyProcessor(grammarRepository, this.scopeName);
		// TODO: uncomment below to visit all scopes
		// while (dependencyProcessor.queue.length > 0) {
		// dependencyProcessor.processQueue();
		// }

		final var result = new ArrayList<Injection>();

		dependencyProcessor.seenFullScopeRequests.forEach((scopeName) -> {
			final var grammar = grammarRepository.lookup(scopeName);
			if (grammar == null) {
				return;
			}

			// add injections from the current grammar
			final var rawInjections = grammar.getInjections();
			if (rawInjections != null) {
				for (final var e : rawInjections.entrySet()) {
					collectInjections(result, e.getKey(), e.getValue(), this, grammar);
				}
			}

			// add injection grammars contributed for the current scope
			final var injectionScopeNames = this.grammarRepository.injections(scopeName);
			if (injectionScopeNames != null) {
				injectionScopeNames.forEach(injectionScopeName -> {
					final var injectionGrammar = Grammar.this.getExternalGrammar(injectionScopeName, null);
					if (injectionGrammar != null) {
						final var selector = injectionGrammar.getInjectionSelector();
						if (selector != null) {
							collectInjections(result, selector, (IRawRule) injectionGrammar, this,
									injectionGrammar);
						}
					}
				});
			}
		});

		Collections.sort(result, (i1, i2) -> i1.priority - i2.priority); // sort by priority

		return result;
	}

	List<Injection> getInjections() {
		var injections = this.injections;
		if (injections == null) {
			injections = this.injections = this.collectInjections();

			if (LOGGER.isLoggable(Level.TRACE) && !injections.isEmpty()) {
				LOGGER.log(Level.TRACE,
						"Grammar " + scopeName + " contains the following injections:");
				for (final var injection : injections) {
					LOGGER.log(Level.TRACE, "  - " + injection.debugSelector);
				}
			}
		}
		return injections;
	}

	@Override
	public <T extends @NonNull Rule> T registerRule(final IntFunction<T> factory) {
		final int id = ++this.lastRuleId;
		final @Nullable T result = factory.apply(id);
		this.ruleId2desc.put(id, result);
		return result;
	}

	@Override
	public Rule getRule(final int patternId) {
		final var rule = this.ruleId2desc.get(patternId);
		if (rule == null) {
			throw new IndexOutOfBoundsException(
					"No rule with index " + patternId + " found. Possible values: 0.." + this.ruleId2desc.size());
		}
		return rule;
	}

	@Override
	@Nullable
	public IRawGrammar getExternalGrammar(final String scopeName, @Nullable final IRawRepository repository) {
		if (this.includedGrammars.containsKey(scopeName)) {
			return this.includedGrammars.get(scopeName);
		}

		final IRawGrammar rawIncludedGrammar = this.grammarRepository.lookup(scopeName);
		if (rawIncludedGrammar != null) {
			this.includedGrammars.put(scopeName,
					initGrammar(rawIncludedGrammar, repository != null ? repository.getBase() : null));
			return this.includedGrammars.get(scopeName);
		}
		return null;
	}

	private IRawGrammar initGrammar(IRawGrammar grammar, @Nullable final IRawRule base) {
		grammar = grammar.clone();
		var repo = grammar.getRepository();
		if (repo == null) {
			repo = new Raw();
			((IRawRule) grammar).setRepository(repo);
		}
		final var self = new Raw();
		self.setPatterns(grammar.getPatterns());
		self.setName(grammar.getScopeName());
		repo.setSelf(self);
		repo.setBase(base != null ? base : self);
		return grammar;
	}

	@Override
	public ITokenizeLineResult tokenizeLine(final String lineText) {
		return tokenizeLine(lineText, null);
	}

	@Override
	public ITokenizeLineResult tokenizeLine(final String lineText, @Nullable final StackElement prevState) {
		return tokenize(lineText, prevState, false);
	}

	@Override
	public ITokenizeLineResult2 tokenizeLine2(final String lineText) {
		return tokenizeLine2(lineText, null);
	}

	@Override
	public ITokenizeLineResult2 tokenizeLine2(final String lineText, @Nullable final StackElement prevState) {
		return tokenize(lineText, prevState, true);
	}

	@SuppressWarnings("unchecked")
	private <T> T tokenize(String lineText, @Nullable StackElement prevState, final boolean emitBinaryTokens) {
		if (this.rootId == -1) {
			this.rootId = RuleFactory.getCompiledRuleId(this.grammar.getRepositorySafe().getSelf(), this,
					this.grammar.getRepositorySafe());
		}

		boolean isFirstLine;
		if (prevState == null || prevState.equals(StackElement.NULL)) {
			isFirstLine = true;
			final var rawDefaultMetadata = this.scopeMetadataProvider.getDefaultMetadata();
			final var themeData = rawDefaultMetadata.themeData;
			final var defaultTheme = themeData == null ? null : themeData.get(0);
			final int defaultMetadata = StackElementMetadata.set(0, rawDefaultMetadata.languageId,
					rawDefaultMetadata.tokenType, null, defaultTheme.fontStyle, defaultTheme.foreground,
					defaultTheme.background);

			final var rootRule = castNonNull(this.getRule(this.rootId));
			final var rootScopeName = rootRule.getName(null, null);
			final var rawRootMetadata = this.scopeMetadataProvider.getMetadataForScope(rootScopeName);
			final int rootMetadata = ScopeListElement.mergeMetadata(defaultMetadata, null, rawRootMetadata);

			final ScopeListElement scopeList = new ScopeListElement(null,
					rootScopeName == null ? "unknown" : rootScopeName, rootMetadata);

			prevState = new StackElement(null, this.rootId, -1, -1, false, null, scopeList, scopeList);
		} else {
			isFirstLine = false;
			prevState.reset();
		}

		if (lineText.isEmpty() || lineText.charAt(lineText.length() - 1) != '\n') {
			// Only add \n if the passed lineText didn't have it.
			lineText += '\n';
		}
		final var onigLineText = OnigString.of(lineText);
		final int lineLength = lineText.length();
		final var lineTokens = new LineTokens(emitBinaryTokens, lineText, tokenTypeMatchers, balancedBracketSelectors);
		final var nextState = LineTokenizer.tokenizeString(this, onigLineText, isFirstLine, 0, prevState, lineTokens);

		if (emitBinaryTokens) {
			return (T) new TokenizeLineResult2(lineTokens.getBinaryResult(nextState, lineLength), nextState);
		}
		return (T) new TokenizeLineResult(lineTokens.getResult(nextState, lineLength), nextState);
	}

	@Override
	@Nullable
	public String getName() {
		return grammar.getName();
	}

	@Override
	public String getScopeName() {
		return scopeName;
	}

	@Override
	public Collection<String> getFileTypes() {
		return grammar.getFileTypes();
	}
}
