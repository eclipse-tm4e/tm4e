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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntFunction;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult2;
import org.eclipse.tm4e.core.grammar.Injection;
import org.eclipse.tm4e.core.grammar.StackElement;
import org.eclipse.tm4e.core.internal.grammar.parser.Raw;
import org.eclipse.tm4e.core.internal.grammars.IGrammarRepository;
import org.eclipse.tm4e.core.internal.matcher.Matcher;
import org.eclipse.tm4e.core.internal.matcher.MatcherWithPriority;
import org.eclipse.tm4e.core.internal.oniguruma.OnigString;
import org.eclipse.tm4e.core.internal.rule.IRuleFactoryHelper;
import org.eclipse.tm4e.core.internal.rule.Rule;
import org.eclipse.tm4e.core.internal.rule.RuleFactory;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;
import org.eclipse.tm4e.core.internal.types.IRawRepository;
import org.eclipse.tm4e.core.internal.types.IRawRule;
import org.eclipse.tm4e.core.theme.IThemeProvider;
import org.eclipse.tm4e.core.theme.ThemeTrieElementRule;

/**
 * TextMate grammar implementation.
 *
 * @see <a href="https://github.com/Microsoft/vscode-textmate/blob/master/src/grammar.ts">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/grammar.ts</a>
 *
 */
public final class Grammar implements IGrammar, IRuleFactoryHelper {

	private int rootId = -1;
	private int lastRuleId = 0;
	private final Map<Integer, Rule> ruleId2desc = new HashMap<>();
	private final Map<String, IRawGrammar> includedGrammars = new HashMap<>();
	private final IGrammarRepository grammarRepository;
	private final IRawGrammar rawGrammar;
	private List<Injection> injections;
	private final ScopeMetadataProvider scopeMetadataProvider;

	public Grammar(IRawGrammar grammar, int initialLanguage, Map<String, Integer> embeddedLanguages,
			IGrammarRepository grammarRepository, IThemeProvider themeProvider) {
		this.scopeMetadataProvider = new ScopeMetadataProvider(initialLanguage, themeProvider, embeddedLanguages);
		this.grammarRepository = grammarRepository;
		this.rawGrammar = initGrammar(grammar, null);
	}

	public void onDidChangeTheme() {
		this.scopeMetadataProvider.onDidChangeTheme();
	}

	ScopeMetadata getMetadataForScope(String scope) {
		return this.scopeMetadataProvider.getMetadataForScope(scope);
	}

	List<Injection> getInjections() {
		if (this.injections == null) {
			this.injections = new ArrayList<>();
			// add injections from the current grammar
			Map<String, IRawRule> rawInjections = this.rawGrammar.getInjections();
			if (rawInjections != null) {
				for (Entry<String, IRawRule> injection : rawInjections.entrySet()) {
					String expression = injection.getKey();
					IRawRule rule = injection.getValue();
					collectInjections(this.injections, expression, rule, this, this.rawGrammar);
				}
			}

			// add injection grammars contributed for the current scope
			if (this.grammarRepository != null) {
				Collection<String> injectionScopeNames = this.grammarRepository
						.injections(this.rawGrammar.getScopeName());
				if (injectionScopeNames != null) {
					injectionScopeNames.forEach(injectionScopeName -> {
						IRawGrammar injectionGrammar = this.getExternalGrammar(injectionScopeName);
						if (injectionGrammar != null) {
							String selector = injectionGrammar.getInjectionSelector();
							if (selector != null) {
								collectInjections(this.injections, selector, (IRawRule) injectionGrammar, this,
										injectionGrammar);
							}
						}
					});
				}
			}
			Collections.sort(this.injections, (i1, i2) -> i1.priority - i2.priority); // sort by priority
		}
		if (this.injections.isEmpty()) {
			return this.injections;
		}
		return this.injections;
	}

	private void collectInjections(List<Injection> result, String selector, IRawRule rule,
			IRuleFactoryHelper ruleFactoryHelper, IRawGrammar grammar) {
		Collection<MatcherWithPriority<List<String>>> matchers = Matcher.createMatchers(selector);
		int ruleId = RuleFactory.getCompiledRuleId(rule, ruleFactoryHelper, grammar.getRepository());

		for (MatcherWithPriority<List<String>> matcher : matchers) {
			result.add(new Injection(matcher.matcher, ruleId, grammar, matcher.priority));
		}
	}

	@Override
	public <T extends @NonNull Rule> T registerRule(IntFunction<T> factory) {
		int id = (++this.lastRuleId);
		T result = factory.apply(id);
		this.ruleId2desc.put(id, result);
		return result;
	}

	@Override
	public Rule getRule(int patternId) {
		return this.ruleId2desc.get(patternId);
	}

	private IRawGrammar getExternalGrammar(String scopeName) {
		return getExternalGrammar(scopeName, null);
	}

	@Override
	public IRawGrammar getExternalGrammar(String scopeName, @Nullable IRawRepository repository) {
		if (this.includedGrammars.containsKey(scopeName)) {
			return this.includedGrammars.get(scopeName);
		} else if (this.grammarRepository != null) {
			IRawGrammar rawIncludedGrammar = this.grammarRepository.lookup(scopeName);
			if (rawIncludedGrammar != null) {
				this.includedGrammars.put(scopeName,
						initGrammar(rawIncludedGrammar, repository != null ? repository.getBase() : null));
				return this.includedGrammars.get(scopeName);
			}
		}
		return null;
	}

	private IRawGrammar initGrammar(IRawGrammar grammar, IRawRule base) {
		grammar = grammar.clone();
		if (grammar.getRepository() == null) {
			((Raw) grammar).setRepository(new Raw());
		}
		Raw self = new Raw();
		self.setPatterns(grammar.getPatterns());
		self.setName(grammar.getScopeName());
		grammar.getRepository().setSelf(self);
		if (base != null) {
			grammar.getRepository().setBase(base);
		} else {
			grammar.getRepository().setBase(grammar.getRepository().getSelf());
		}
		return grammar;
	}

	@Override
	public ITokenizeLineResult tokenizeLine(String lineText) {
		return tokenizeLine(lineText, null);
	}

	@Override
	public ITokenizeLineResult tokenizeLine(String lineText, StackElement prevState) {
		return tokenize(lineText, prevState, false);
	}

	@Override
	public ITokenizeLineResult2 tokenizeLine2(String lineText) {
		return tokenizeLine2(lineText, null);
	}

	@Override
	public ITokenizeLineResult2 tokenizeLine2(String lineText, StackElement prevState) {
		return tokenize(lineText, prevState, true);
	}

	@SuppressWarnings("unchecked")
	private <T> T tokenize(String lineText, StackElement prevState, boolean emitBinaryTokens) {
		if (this.rootId == -1) {
			this.rootId = RuleFactory.getCompiledRuleId(this.rawGrammar.getRepository().getSelf(), this,
					this.rawGrammar.getRepository());
		}

		boolean isFirstLine;
		if (prevState == null || prevState.equals(StackElement.NULL)) {
			isFirstLine = true;
			ScopeMetadata rawDefaultMetadata = this.scopeMetadataProvider.getDefaultMetadata();
			ThemeTrieElementRule defaultTheme = rawDefaultMetadata.themeData.get(0);
			int defaultMetadata = StackElementMetadata.set(0, rawDefaultMetadata.languageId,
					rawDefaultMetadata.tokenType, defaultTheme.fontStyle, defaultTheme.foreground,
					defaultTheme.background);

			String rootScopeName = this.getRule(this.rootId).getName(null, null);
			ScopeMetadata rawRootMetadata = this.scopeMetadataProvider.getMetadataForScope(rootScopeName);
			int rootMetadata = ScopeListElement.mergeMetadata(defaultMetadata, null, rawRootMetadata);

			ScopeListElement scopeList = new ScopeListElement(null, rootScopeName, rootMetadata);

			prevState = new StackElement(null, this.rootId, -1, null, scopeList, scopeList);
		} else {
			isFirstLine = false;
			prevState.reset();
		}

		if (lineText.isEmpty() || lineText.charAt(lineText.length() - 1) != '\n') {
			// Only add \n if the passed lineText didn't have it.
			lineText += '\n';
		}
		OnigString onigLineText = OnigString.of(lineText);
		int lineLength = lineText.length();
		LineTokens lineTokens = new LineTokens(emitBinaryTokens, lineText);
		StackElement nextState = LineTokenizer.tokenizeString(this, onigLineText, isFirstLine, 0, prevState,
				lineTokens);

		if (emitBinaryTokens) {
			return (T) new TokenizeLineResult2(lineTokens.getBinaryResult(nextState, lineLength), nextState);
		}
		return (T) new TokenizeLineResult(lineTokens.getResult(nextState, lineLength), nextState);
	}

	@Override
	public String getName() {
		return rawGrammar.getName();
	}

	@Override
	public String getScopeName() {
		return rawGrammar.getScopeName();
	}

	@Override
	public Collection<String> getFileTypes() {
		return rawGrammar.getFileTypes();
	}

}
