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
package org.eclipse.tm4e.core.internal.grammar;

import static java.lang.System.Logger.Level.*;
import static org.eclipse.tm4e.core.internal.utils.MoreCollections.*;
import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.oniguruma.OnigCaptureIndex;
import org.eclipse.tm4e.core.internal.oniguruma.OnigNextMatchResult;
import org.eclipse.tm4e.core.internal.oniguruma.OnigString;
import org.eclipse.tm4e.core.internal.rule.BeginEndRule;
import org.eclipse.tm4e.core.internal.rule.BeginWhileRule;
import org.eclipse.tm4e.core.internal.rule.CaptureRule;
import org.eclipse.tm4e.core.internal.rule.CompiledRule;
import org.eclipse.tm4e.core.internal.rule.MatchRule;
import org.eclipse.tm4e.core.internal.rule.Rule;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/grammar.ts#L1028">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/grammar.ts</a>
 */
final class LineTokenizer {

	private static final Logger LOGGER = System.getLogger(LineTokenizer.class.getName());

	private interface IMatchResult {
		OnigCaptureIndex[] getCaptureIndices();

		int getMatchedRuleId();
	}

	private interface IMatchInjectionsResult extends IMatchResult {
		boolean isPriorityMatch();
	}

	private static final class WhileStack {

		private final StackElement stack;
		private final BeginWhileRule rule;

		private WhileStack(final StackElement stack, final BeginWhileRule rule) {
			this.stack = stack;
			this.rule = rule;
		}
	}

	private static final class WhileCheckResult {

		private final StackElement stack;
		private final int linePos;
		private final int anchorPosition;
		private final boolean isFirstLine;

		private WhileCheckResult(final StackElement stack, final int linePos, final int anchorPosition,
				final boolean isFirstLine) {
			this.stack = stack;
			this.linePos = linePos;
			this.anchorPosition = anchorPosition;
			this.isFirstLine = isFirstLine;
		}
	}

	private final Grammar grammar;
	private final OnigString lineText;
	private boolean isFirstLine;
	private int linePos;
	private StackElement stack;
	private final LineTokens lineTokens;
	private int anchorPosition = -1;
	private boolean stop;

	private LineTokenizer(final Grammar grammar, final OnigString lineText, final boolean isFirstLine,
			final int linePos, final StackElement stack, final LineTokens lineTokens) {
		this.grammar = grammar;
		this.lineText = lineText;
		this.isFirstLine = isFirstLine;
		this.linePos = linePos;
		this.stack = stack;
		this.lineTokens = lineTokens;
	}

	private StackElement scan(boolean checkWhileConditions) {
		stop = false;

		if (checkWhileConditions) {
			final var whileCheckResult = checkWhileConditions(grammar, lineText, isFirstLine, linePos, stack,
					lineTokens);
			stack = whileCheckResult.stack;
			linePos = whileCheckResult.linePos;
			isFirstLine = whileCheckResult.isFirstLine;
			anchorPosition = whileCheckResult.anchorPosition;
		}

		while (!stop) {
			scanNext(); // potentially modifies linePos && anchorPosition
		}

		return stack;
	}

	private void scanNext() {
		LOGGER.log(TRACE, () -> "@@scanNext: |" + lineText.string.replace("\n", "\\n").substring(linePos) + '|');

		final IMatchResult r = matchRuleOrInjections(grammar, lineText, isFirstLine, linePos, stack, anchorPosition);

		if (r == null) {
			LOGGER.log(TRACE, " no more matches.");
			// No match
			lineTokens.produce(stack, lineText.bytesCount);
			stop = true;
			return;
		}

		final OnigCaptureIndex[] captureIndices = r.getCaptureIndices();
		final int matchedRuleId = r.getMatchedRuleId();

		final boolean hasAdvanced = captureIndices.length > 0 && captureIndices[0].end > linePos;

		if (matchedRuleId == -1) {
			// We matched the `end` for this rule => pop it
			final BeginEndRule poppedRule = (BeginEndRule) stack.getRule(grammar);

			/*
			 * if (logger.isEnabled()) { logger.log("  popping " + poppedRule.debugName +
			 * " - " + poppedRule.debugEndRegExp); }
			 */

			lineTokens.produce(stack, captureIndices[0].start);
			stack = stack.setContentNameScopesList(stack.nameScopesList);
			handleCaptures(grammar, lineText, isFirstLine, stack, lineTokens, poppedRule.endCaptures, captureIndices);
			lineTokens.produce(stack, captureIndices[0].end);

			// pop
			final var popped = stack;
			stack = castNonNull(stack.pop());
			anchorPosition = popped.getAnchorPos();

			if (!hasAdvanced && popped.getEnterPos() == linePos) {
				// Grammar pushed & popped a rule without advancing
				LOGGER.log(INFO,
						"[1] - Grammar is in an endless loop - Grammar pushed & popped a rule without advancing");
				// See https://github.com/Microsoft/vscode-textmate/issues/12
				// Let's assume this was a mistake by the grammar author and the
				// intent was to continue in this state
				stack = popped;

				lineTokens.produce(stack, lineText.bytesCount);
				stop = true;
				return;
			}
		} else if (captureIndices.length > 0) {
			// We matched a rule!
			final Rule rule = grammar.getRule(matchedRuleId);

			lineTokens.produce(stack, captureIndices[0].start);

			final StackElement beforePush = stack;
			// push it on the stack rule
			final String scopeName = rule.getName(lineText.string, captureIndices);
			final ScopeListElement nameScopesList = stack.contentNameScopesList.push(grammar, scopeName);
			stack = stack.push(matchedRuleId, linePos, anchorPosition,
					captureIndices[0].end == lineText.bytesCount, null, nameScopesList, nameScopesList);

			if (rule instanceof BeginEndRule) {
				final BeginEndRule pushedRule = (BeginEndRule) rule;

				// if (IN_DEBUG_MODE) {
				// console.log(' pushing ' + pushedRule.debugName + ' - ' +
				// pushedRule.debugBeginRegExp);
				// }

				handleCaptures(grammar, lineText, isFirstLine, stack, lineTokens, pushedRule.beginCaptures,
						captureIndices);
				lineTokens.produce(stack, captureIndices[0].end);
				anchorPosition = captureIndices[0].end;

				final String contentName = pushedRule.getContentName(lineText.string, captureIndices);
				final ScopeListElement contentNameScopesList = nameScopesList.push(grammar, contentName);
				stack = stack.setContentNameScopesList(contentNameScopesList);

				if (pushedRule.endHasBackReferences) {
					stack = stack.setEndRule(
							pushedRule.getEndWithResolvedBackReferences(lineText.string, captureIndices));
				}

				if (!hasAdvanced && beforePush.hasSameRuleAs(stack)) {
					// Grammar pushed the same rule without advancing
					LOGGER.log(INFO,
							"[2] - Grammar is in an endless loop - Grammar pushed the same rule without advancing");
					stack = castNonNull(stack.pop());
					lineTokens.produce(stack, lineText.bytesCount);
					stop = true;
					return;
				}
			} else if (rule instanceof BeginWhileRule) {
				final BeginWhileRule pushedRule = (BeginWhileRule) rule;
				// if (IN_DEBUG_MODE) {
				// console.log(' pushing ' + pushedRule.debugName);
				// }

				handleCaptures(grammar, lineText, isFirstLine, stack, lineTokens, pushedRule.beginCaptures,
						captureIndices);
				lineTokens.produce(stack, captureIndices[0].end);
				anchorPosition = captureIndices[0].end;

				final String contentName = pushedRule.getContentName(lineText.string, captureIndices);
				final ScopeListElement contentNameScopesList = nameScopesList.push(grammar, contentName);
				stack = stack.setContentNameScopesList(contentNameScopesList);

				if (pushedRule.whileHasBackReferences) {
					stack = stack.setEndRule(
							pushedRule.getWhileWithResolvedBackReferences(lineText.string, captureIndices));
				}

				if (!hasAdvanced && beforePush.hasSameRuleAs(stack)) {
					// Grammar pushed the same rule without advancing
					LOGGER.log(INFO,
							"[3] - Grammar is in an endless loop - Grammar pushed the same rule without advancing");
					stack = castNonNull(stack.pop());
					lineTokens.produce(stack, lineText.bytesCount);
					stop = true;
					return;
				}
			} else {
				final MatchRule matchingRule = (MatchRule) rule;
				// if (IN_DEBUG_MODE) {
				// console.log(' matched ' + matchingRule.debugName + ' - ' +
				// matchingRule.debugMatchRegExp);
				// }

				handleCaptures(grammar, lineText, isFirstLine, stack, lineTokens, matchingRule.captures,
						captureIndices);
				lineTokens.produce(stack, captureIndices[0].end);

				// pop rule immediately since it is a MatchRule
				stack = castNonNull(stack.pop());

				if (!hasAdvanced) {
					// Grammar is not advancing, nor is it pushing/popping
					LOGGER.log(INFO,
							"[4] - Grammar is in an endless loop - Grammar is not advancing, nor is it pushing/popping");
					stack = stack.safePop();
					lineTokens.produce(stack, lineText.bytesCount);
					stop = true;
					return;
				}
			}
		}

		if (captureIndices.length > 0 && captureIndices[0].end > linePos) {
			// Advance stream
			linePos = captureIndices[0].end;
			isFirstLine = false;
		}
	}

	@Nullable
	private IMatchResult matchRule(final Grammar grammar, final OnigString lineText, final boolean isFirstLine,
			final int linePos, final StackElement stack, final int anchorPosition) {
		final Rule rule = stack.getRule(grammar);
		final CompiledRule ruleScanner = rule.compileAG(grammar, stack.endRule, isFirstLine, linePos == anchorPosition);

		final OnigNextMatchResult r = ruleScanner.scanner.findNextMatchSync(lineText, linePos);

		if (r != null) {
			return new IMatchResult() {

				@Override
				public int getMatchedRuleId() {
					return ruleScanner.rules[r.getIndex()];
				}

				@Override
				public OnigCaptureIndex[] getCaptureIndices() {
					return r.getCaptureIndices();
				}
			};
		}
		return null;
	}

	@Nullable
	private IMatchResult matchRuleOrInjections(final Grammar grammar, final OnigString lineText,
			final boolean isFirstLine,
			final int linePos, final StackElement stack, final int anchorPosition) {
		// Look for normal grammar rule
		final IMatchResult matchResult = matchRule(grammar, lineText, isFirstLine, linePos, stack, anchorPosition);

		// Look for injected rules
		final List<Injection> injections = grammar.getInjections();
		if (injections.isEmpty()) {
			// No injections whatsoever => early return
			return matchResult;
		}

		final IMatchInjectionsResult injectionResult = matchInjections(injections, grammar, lineText, isFirstLine,
				linePos,
				stack, anchorPosition);
		if (injectionResult == null) {
			// No injections matched => early return
			return matchResult;
		}

		if (matchResult == null) {
			// Only injections matched => early return
			return injectionResult;
		}

		// Decide if `matchResult` or `injectionResult` should win
		final int matchResultScore = matchResult.getCaptureIndices()[0].start;
		final int injectionResultScore = injectionResult.getCaptureIndices()[0].start;

		if (injectionResultScore < matchResultScore
				|| (injectionResult.isPriorityMatch() && injectionResultScore == matchResultScore)) {
			// injection won!
			return injectionResult;
		}

		return matchResult;
	}

	@Nullable
	private IMatchInjectionsResult matchInjections(final List<Injection> injections, final Grammar grammar,
			final OnigString lineText,
			final boolean isFirstLine, final int linePos, final StackElement stack, final int anchorPosition) {
		// The lower the better
		int bestMatchRating = Integer.MAX_VALUE;
		OnigCaptureIndex[] bestMatchCaptureIndices = null;
		int bestMatchRuleId = -1;
		int bestMatchResultPriority = 0;

		final List<String> scopes = stack.contentNameScopesList.generateScopes();

		for (final Injection injection : injections) {
			if (!injection.matches(scopes)) {
				// injection selector doesn't match stack
				continue;
			}

			final CompiledRule ruleScanner = grammar.getRule(injection.ruleId).compileAG(grammar, null, isFirstLine,
					linePos == anchorPosition);
			final OnigNextMatchResult matchResult = ruleScanner.scanner.findNextMatchSync(lineText, linePos);

			if (matchResult == null) {
				continue;
			}

			if (LOGGER.isLoggable(Level.TRACE)) {
				LOGGER.log(Level.TRACE, "  matched injection: " + injection.debugSelector);
				LOGGER.log(Level.TRACE, debugCompiledRuleToString(ruleScanner));
			}
			final int matchRating = matchResult.getCaptureIndices()[0].start;

			if (matchRating > bestMatchRating) {
				// Injections are sorted by priority, so the previous injection had a better or
				// equal priority
				continue;
			}

			bestMatchRating = matchRating;
			bestMatchCaptureIndices = matchResult.getCaptureIndices();
			bestMatchRuleId = ruleScanner.rules[matchResult.getIndex()];
			bestMatchResultPriority = injection.priority;

			if (bestMatchRating == linePos) {
				// No more need to look at the rest of the injections
				break;
			}
		}

		if (bestMatchCaptureIndices != null) {
			final int matchedRuleId = bestMatchRuleId;
			final OnigCaptureIndex[] matchCaptureIndices = bestMatchCaptureIndices;
			final boolean matchResultPriority = bestMatchResultPriority == -1;
			return new IMatchInjectionsResult() {

				@Override
				public int getMatchedRuleId() {
					return matchedRuleId;
				}

				@Override
				public OnigCaptureIndex[] getCaptureIndices() {
					return matchCaptureIndices;
				}

				@Override
				public boolean isPriorityMatch() {
					return matchResultPriority;
				}
			};
		}

		return null;
	}

	private void handleCaptures(final Grammar grammar, final OnigString lineText, final boolean isFirstLine,
			final StackElement stack,
			final LineTokens lineTokens, final List<@Nullable CaptureRule> captures,
			final OnigCaptureIndex[] captureIndices) {
		if (captures.isEmpty()) {
			return;
		}

		final int len = Math.min(captures.size(), captureIndices.length);
		final List<LocalStackElement> localStack = new ArrayList<>();
		final int maxEnd = captureIndices[0].end;
		OnigCaptureIndex captureIndex;

		for (int i = 0; i < len; i++) {
			final CaptureRule captureRule = captures.get(i);
			if (captureRule == null) {
				// Not interested
				continue;
			}

			captureIndex = captureIndices[i];

			if (captureIndex.getLength() == 0) {
				// Nothing really captured
				continue;
			}

			if (captureIndex.start > maxEnd) {
				// Capture going beyond consumed string
				break;
			}

			// pop captures while needed
			while (!localStack.isEmpty() && getLastElement(localStack).endPos <= captureIndex.start) {
				// pop!
				lineTokens.produceFromScopes(getLastElement(localStack).scopes, getLastElement(localStack).endPos);
				removeLastElement(localStack);
			}

			if (!localStack.isEmpty()) {
				lineTokens.produceFromScopes(getLastElement(localStack).scopes, captureIndex.start);
			} else {
				lineTokens.produce(stack, captureIndex.start);
			}

			final var retokenizeCapturedWithRuleId = captureRule.retokenizeCapturedWithRuleId;
			if (retokenizeCapturedWithRuleId != null) {
				// the capture requires additional matching
				final String scopeName = captureRule.getName(lineText.string, captureIndices);
				final ScopeListElement nameScopesList = stack.contentNameScopesList.push(grammar, scopeName);
				final String contentName = captureRule.getContentName(lineText.string, captureIndices);
				final ScopeListElement contentNameScopesList = nameScopesList.push(grammar, contentName);

				// the capture requires additional matching
				final StackElement stackClone = stack.push(retokenizeCapturedWithRuleId, captureIndex.start, -1, false,
						null, nameScopesList, contentNameScopesList);
				final var onigSubStr = OnigString.of(lineText.string.substring(0, captureIndex.end));
				tokenizeString(grammar, onigSubStr, (isFirstLine && captureIndex.start == 0),
						captureIndex.start, stackClone, lineTokens, false);
				continue;
			}

			// push
			final String captureRuleScopeName = captureRule.getName(lineText.string, captureIndices);
			if (captureRuleScopeName != null) {
				// push
				final ScopeListElement base = localStack.isEmpty()
						? stack.contentNameScopesList
						: getLastElement(localStack).scopes;
				final ScopeListElement captureRuleScopesList = base.push(grammar, captureRuleScopeName);
				localStack.add(new LocalStackElement(captureRuleScopesList, captureIndex.end));
			}
		}

		while (!localStack.isEmpty()) {
			// pop!
			lineTokens.produceFromScopes(getLastElement(localStack).scopes, getLastElement(localStack).endPos);
			removeLastElement(localStack);
		}
	}

	/**
	 * Walk the stack from bottom to top, and check each while condition in this
	 * order. If any fails, cut off the entire stack above the failed while
	 * condition. While conditions may also advance the linePosition.
	 */
	private WhileCheckResult checkWhileConditions(final Grammar grammar, final OnigString lineText, boolean isFirstLine,
			int linePos, StackElement stack, final LineTokens lineTokens) {
		int currentanchorPosition = stack.beginRuleCapturedEOL ? 0 : -1;
		final List<WhileStack> whileRules = new ArrayList<>();
		for (StackElement node = stack; node != null; node = node.pop()) {
			final Rule nodeRule = node.getRule(grammar);
			if (nodeRule instanceof BeginWhileRule) {
				whileRules.add(new WhileStack(node, (BeginWhileRule) nodeRule));
			}
		}

		for (int i = whileRules.size() - 1; i >= 0; i--) {
			final var whileRule = whileRules.get(i);
			final var ruleScanner = whileRule.rule.compileWhileAG(whileRule.stack.endRule, isFirstLine,
					currentanchorPosition == linePos);
			final var r = ruleScanner.scanner.findNextMatchSync(lineText, linePos);
			if (LOGGER.isLoggable(TRACE)) {
				LOGGER.log(TRACE, "  scanning for while rule");
				LOGGER.log(TRACE, debugCompiledRuleToString(ruleScanner));
			}

			if (r != null) {
				final int matchedRuleId = ruleScanner.rules[r.getIndex()];
				if (matchedRuleId != -2) {
					// we shouldn't end up here
					stack = castNonNull(whileRule.stack.pop());
					break;
				}
				if (r.getCaptureIndices().length > 0) {
					lineTokens.produce(whileRule.stack, r.getCaptureIndices()[0].start);
					handleCaptures(grammar, lineText, isFirstLine, whileRule.stack, lineTokens,
							whileRule.rule.whileCaptures, r.getCaptureIndices());
					lineTokens.produce(whileRule.stack, r.getCaptureIndices()[0].end);
					currentanchorPosition = r.getCaptureIndices()[0].end;
					if (r.getCaptureIndices()[0].end > linePos) {
						linePos = r.getCaptureIndices()[0].end;
						isFirstLine = false;
					}
				}
			} else {
				stack = castNonNull(whileRule.stack.pop());
				break;
			}
		}

		return new WhileCheckResult(stack, linePos, currentanchorPosition, isFirstLine);
	}

	static StackElement tokenizeString(final Grammar grammar, final OnigString lineText, final boolean isFirstLine,
			final int linePos, final StackElement stack, final LineTokens lineTokens,
			final boolean checkWhileConditions) {
		return new LineTokenizer(grammar, lineText, isFirstLine, linePos, stack, lineTokens).scan(checkWhileConditions);
	}

	static String debugCompiledRuleToString(CompiledRule ruleScanner) {
		final var r = new ArrayList<String>();
		for (int i = 0, l = ruleScanner.rules.length; i < l; i++) {
			r.add("   - " + ruleScanner.rules[i] + ": " + ruleScanner.debugRegExps.get(i));
		}
		return String.join(System.lineSeparator(), r);
	}
}
