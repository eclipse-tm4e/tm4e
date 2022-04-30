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
 */
package org.eclipse.tm4e.core.internal.rule;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import static java.lang.System.Logger.Level.*;

import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.grammar.Raw;
import org.eclipse.tm4e.core.internal.types.IRawCaptures;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;
import org.eclipse.tm4e.core.internal.types.IRawRepository;
import org.eclipse.tm4e.core.internal.types.IRawRule;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/rule.ts#L691">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/rule.ts</a>
 */
public final class RuleFactory {

	private static final Logger LOGGER = System.getLogger(RuleFactory.class.getName());

	private static CaptureRule createCaptureRule(final IRuleFactoryHelper helper, @Nullable final String name,
			@Nullable final String contentName, @Nullable final Integer retokenizeCapturedWithRuleId) {
		return helper.registerRule(id -> new CaptureRule(id, name, contentName, retokenizeCapturedWithRuleId));
	}

	public static int getCompiledRuleId(final IRawRule desc, final IRuleFactoryHelper helper,
			final IRawRepository repository) {
		if (desc.getId() == null) {
			helper.registerRule(ruleId -> {
				desc.setId(ruleId);

				final var ruleMatch = desc.getMatch();
				if (ruleMatch != null) {
					return new MatchRule(ruleId, desc.getName(), ruleMatch,
							compileCaptures(desc.getCaptures(), helper, repository));
				}

				final var begin = desc.getBegin();
				if (begin == null) {
					final var repository1 = desc.getRepository() == null
							? repository
							: IRawRepository.merge(repository, desc.getRepository());
					var patterns = desc.getPatterns();
					if (patterns == null && desc.getInclude() != null) {
						final var includeRule = new Raw();
						includeRule.setInclude(desc.getInclude());
						patterns = List.of(includeRule);
					}
					return new IncludeOnlyRule(
							/* desc.$vscodeTextmateLocation, */
							ruleId,
							desc.getName(),
							desc.getContentName(),
							compilePatterns(patterns, helper, repository1));
				}

				final String ruleWhile = desc.getWhile();
				if (ruleWhile != null) {
					return new BeginWhileRule(
							/* desc.$vscodeTextmateLocation, */
							ruleId,
							desc.getName(),
							desc.getContentName(),
							begin, compileCaptures(
									desc.getBeginCaptures() != null ? desc.getBeginCaptures() : desc.getCaptures(),
									helper, repository),
							ruleWhile, compileCaptures(
									desc.getWhileCaptures() != null ? desc.getWhileCaptures() : desc.getCaptures(),
									helper, repository),
							compilePatterns(desc.getPatterns(), helper, repository));
				}

				return new BeginEndRule(
						/* desc.$vscodeTextmateLocation, */
						ruleId,
						desc.getName(),
						desc.getContentName(),
						begin, compileCaptures(
								desc.getBeginCaptures() != null ? desc.getBeginCaptures() : desc.getCaptures(),
								helper, repository),
						desc.getEnd(), compileCaptures(
								desc.getEndCaptures() != null ? desc.getEndCaptures() : desc.getCaptures(), helper,
								repository),
						desc.isApplyEndPatternLast(),
						compilePatterns(desc.getPatterns(), helper, repository));
			});
		}
		return castNonNull(desc.getId());
	}

	private static List<@Nullable CaptureRule> compileCaptures(@Nullable final IRawCaptures captures,
			final IRuleFactoryHelper helper, final IRawRepository repository) {
		if (captures == null) {
			return Collections.emptyList();
		}

		// Find the maximum capture id
		int maximumCaptureId = 0;
		for (final String captureId : captures) {
			final int numericCaptureId = parseInt(captureId, 10);
			if (numericCaptureId > maximumCaptureId) {
				maximumCaptureId = numericCaptureId;
			}
		}

		// Initialize result
		final var r = new ArrayList<@Nullable CaptureRule>();
		for (int i = 0; i <= maximumCaptureId; i++) {
			r.add(null);
		}

		// Fill out result
		for (String captureId : captures) {
			final int numericCaptureId = parseInt(captureId, 10);
			final IRawRule rule = captures.getCapture(captureId);
			final Integer retokenizeCapturedWithRuleId = rule.getPatterns() == null
					? null
					: getCompiledRuleId(captures.getCapture(captureId), helper, repository);
			r.set(numericCaptureId, createCaptureRule(helper, rule.getName(), rule.getContentName(),
					retokenizeCapturedWithRuleId));
		}
		return r;
	}

	private static int parseInt(final String string, final int base) {
		try {
			return Integer.parseInt(string, base);
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	private static CompilePatternsResult compilePatterns(@Nullable final Collection<IRawRule> patterns,
			final IRuleFactoryHelper helper, final IRawRepository repository) {
		if (patterns == null) {
			return new CompilePatternsResult(new int[0], false);
		}

		final var r = new ArrayList<Integer>();
		for (final IRawRule pattern : patterns) {
			int patternId = -1;
			final var patternInclude = pattern.getInclude();
			if (patternInclude == null) {
				patternId = getCompiledRuleId(pattern, helper, repository);
			} else {
				if (patternInclude.charAt(0) == '#') {
					// Local include found in `repository`
					final IRawRule localIncludedRule = repository.getProp(patternInclude.substring(1));
					if (localIncludedRule != null) {
						patternId = getCompiledRuleId(localIncludedRule, helper, repository);
					} else if (LOGGER.isLoggable(DEBUG)) {
						LOGGER.log(DEBUG, "CANNOT find rule for scopeName: %s, I am: %s",
								patternInclude, repository.getBase().getName());
					}
				} else if (patternInclude.equals(Raw.DOLLAR_BASE)) { // Special include also found in `repository`
					patternId = getCompiledRuleId(repository.getBase(), helper, repository);
				} else if (patternInclude.equals(Raw.DOLLAR_SELF)) { // Special include also found in `repository`
					patternId = getCompiledRuleId(repository.getSelf(), helper, repository);
				} else {
					final String externalGrammarName;
					final String externalGrammarInclude;
					int sharpIndex = patternInclude.indexOf('#');
					if (sharpIndex >= 0) {
						externalGrammarName = patternInclude.substring(0, sharpIndex);
						externalGrammarInclude = patternInclude.substring(sharpIndex + 1);
					} else {
						externalGrammarName = patternInclude;
						externalGrammarInclude = null;
					}

					// External include
					final IRawGrammar externalGrammar = helper.getExternalGrammar(externalGrammarName, repository);
					if (externalGrammar != null) {
						final var externalGrammarRepo = externalGrammar.getRepositorySafe();
						if (externalGrammarInclude != null) {
							final IRawRule externalIncludedRule = externalGrammarRepo.getProp(externalGrammarInclude);
							if (externalIncludedRule != null) {
								patternId = getCompiledRuleId(externalIncludedRule, helper, externalGrammarRepo);
							} else if (LOGGER.isLoggable(DEBUG)) {
								LOGGER.log(DEBUG, "CANNOT find rule for scopeName: %s, I am: %s",
										patternInclude, repository.getBase().getName());
							}
						} else {
							patternId = getCompiledRuleId(externalGrammarRepo.getSelf(), helper, externalGrammarRepo);
						}
					} else if (LOGGER.isLoggable(DEBUG)) {
						LOGGER.log(DEBUG, "CANNOT find grammar for scopeName: %s, I am: %s",
								patternInclude, repository.getBase().getName());
					}
				}
			}

			if (patternId != -1) {
				Rule rule;
				try {
					rule = helper.getRule(patternId);
				} catch (IndexOutOfBoundsException ex) {
					rule = null;
					if (patternInclude != null) {
						// TODO currently happens if an include rule references another not yet parsed rule
					} else {
						// should never happen
						ex.printStackTrace();
					}
				}
				boolean skipRule = false;

				if (rule instanceof IncludeOnlyRule) {
					final var ior = (IncludeOnlyRule) rule;
					if (ior.hasMissingPatterns && ior.patterns.length == 0) {
						skipRule = true;
					}
				} else if (rule instanceof BeginEndRule) {
					final var ber = (BeginEndRule) rule;
					if (ber.hasMissingPatterns && ber.patterns.length == 0) {
						skipRule = true;
					}
				} else if (rule instanceof BeginWhileRule) {
					final var bwr = (BeginWhileRule) rule;
					if (bwr.hasMissingPatterns && bwr.patterns.length == 0) {
						skipRule = true;
					}
				}

				if (skipRule) {
					LOGGER.log(DEBUG, "REMOVING RULE ENTIRELY DUE TO EMPTY PATTERNS THAT ARE MISSING");
					continue;
				}

				r.add(patternId);
			}
		}

		return new CompilePatternsResult(r.stream().mapToInt(Integer::intValue).toArray(), patterns.size() != r.size());
	}
}
