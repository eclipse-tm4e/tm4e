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

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IStateStack;
import org.eclipse.tm4e.core.internal.rule.IRuleRegistry;
import org.eclipse.tm4e.core.internal.rule.Rule;
import org.eclipse.tm4e.core.internal.rule.RuleId;

/**
 * Represents a "pushed" state on the stack (as a linked list element).
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/grammar.ts#L1347">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/grammar.ts</a>
 */
public final class StateStack implements IStateStack {

	public static final StateStack NULL = new StateStack(null, RuleId.NO_RULE, 0, 0, false, null,
		new AttributedScopeStack(null, "", 0), new AttributedScopeStack(null, "", 0));

	/**
	 * The position on the current line where this state was pushed.
	 * This is relevant only while tokenizing a line, to detect endless loops.
	 * Its value is meaningless across lines.
	 */
	private int enterPosition;

	/**
	 * The captured anchor position when this stack element was pushed.
	 * This is relevant only while tokenizing a line, to restore the anchor position when popping.
	 * Its value is meaningless across lines.
	 */
	private int anchorPos;

	/**
	 * The previous state on the stack (or null for the root state).
	 */
	@Nullable
	final StateStack parent;

	/**
	 * The depth of the stack.
	 */
	final int depth;

	/**
	 * The state (rule) that this element represents.
	 */
	final RuleId ruleId;

	/**
	 * The state has entered and captured \n. This means that the next line should have an anchorPosition of 0.
	 */
	final boolean beginRuleCapturedEOL;

	/**
	 * The "pop" (end) condition for this state in case that it was dynamically generated through captured text.
	 */
	@Nullable
	final String endRule;

	/**
	 * The list of scopes containing the "name" for this state.
	 */
	final AttributedScopeStack nameScopesList;

	/**
	 * The list of scopes containing the "contentName" (besides "name") for this state.
	 * This list **must** contain as an element `scopeName`.
	 */
	final AttributedScopeStack contentNameScopesList;

	StateStack(
		@Nullable final StateStack parent,
		final RuleId ruleId,
		final int enterPos,
		final int anchorPos,
		final boolean beginRuleCapturedEOL,
		@Nullable final String endRule,
		final AttributedScopeStack nameScopesList,
		final AttributedScopeStack contentNameScopesList) {
		this.parent = parent;
		depth = this.parent != null ? this.parent.depth + 1 : 1;
		this.ruleId = ruleId;
		enterPosition = enterPos;
		this.anchorPos = anchorPos;
		this.beginRuleCapturedEOL = beginRuleCapturedEOL;
		this.endRule = endRule;
		this.nameScopesList = nameScopesList;
		this.contentNameScopesList = contentNameScopesList;
	}

	/**
	 * A structural equals check. Does not take into account `scopes`.
	 */
	private static boolean structuralEquals(@Nullable StateStack a, @Nullable StateStack b) {
		do {
			if (a == b) {
				return true;
			}

			if (a == null && b == null) {
				// End of list reached for both
				return true;
			}

			if (a == null || b == null) {
				// End of list reached only for one
				return false;
			}

			if (a.depth != b.depth || !Objects.equals(a.ruleId, b.ruleId) || !Objects.equals(a.endRule, b.endRule)) {
				return false;
			}

			// Go to previous pair
			a = a.parent;
			b = b.parent;
		} while (true);
	}

	@SuppressWarnings("null")
	private static boolean equals(@Nullable final StateStack a, @Nullable final StateStack b) {
		if (a == b) {
			return true;
		}
		if (!structuralEquals(a, b)) {
			return false;
		}
		return a.contentNameScopesList.equals(b.contentNameScopesList);
	}

	@Override
	public boolean equals(@Nullable final Object other) {
		if (other == null || other.getClass() != StateStack.class) {
			return false;
		}
		return equals(this, (StateStack) other);
	}

	@Override
	public int getDepth() {
		return depth;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hash(endRule, parent, contentNameScopesList, ruleId);
		result = prime * result + depth;
		return result;
	}

	void reset() {
		StateStack el = this;
		while (el != null) {
			el.enterPosition = -1;
			el.anchorPos = -1;
			el = el.parent;
		}
	}

	@Nullable
	StateStack pop() {
		return parent;
	}

	StateStack safePop() {
		if (parent != null)
			return parent;
		return this;
	}

	StateStack push(final RuleId ruleId,
		final int enterPos,
		final int anchorPos,
		final boolean beginRuleCapturedEOL,
		@Nullable final String endRule,
		final AttributedScopeStack nameScopesList,
		final AttributedScopeStack contentNameScopesList) {
		return new StateStack(this,
			ruleId,
			enterPos,
			anchorPos,
			beginRuleCapturedEOL,
			endRule,
			nameScopesList,
			contentNameScopesList);
	}

	int getAnchorPos() {
		return anchorPos;
	}

	int getEnterPos() {
		return enterPosition;
	}

	Rule getRule(final IRuleRegistry grammar) {
		return grammar.getRule(ruleId);
	}

	private void appendString(final List<String> res) {
		if (parent != null) {
			parent.appendString(res);
		}
		// , TODO-${this.nameScopesList}, TODO-${this.contentNameScopesList})`;
		res.add("(" + ruleId + ")");
	}

	@Override
	public String toString() {
		final var r = new ArrayList<String>();
		appendString(r);
		return '[' + String.join(", ", r) + ']';
	}

	StateStack withContentNameScopesList(final AttributedScopeStack contentNameScopesList) {
		if (this.contentNameScopesList.equals(contentNameScopesList)) {
			return this;
		}
		return castNonNull(this.parent).push(this.ruleId,
			this.enterPosition,
			this.anchorPos,
			this.beginRuleCapturedEOL,
			this.endRule,
			this.nameScopesList,
			contentNameScopesList);
	}

	StateStack withEndRule(final String endRule) {
		if (this.endRule != null && this.endRule.equals(endRule)) {
			return this;
		}
		return new StateStack(this.parent,
			this.ruleId,
			this.enterPosition,
			this.anchorPos,
			this.beginRuleCapturedEOL,
			endRule,
			this.nameScopesList,
			this.contentNameScopesList);
	}

	boolean hasSameRuleAs(final StateStack other) {
		var el = this;
		while (el != null && el.enterPosition == other.enterPosition) {
			if (el.ruleId == other.ruleId) {
				return true;
			}
			el = el.parent;
		}
		return false;
	}
}
