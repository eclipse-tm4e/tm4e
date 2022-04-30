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
package org.eclipse.tm4e.core.grammar;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.grammar.ScopeListElement;
import org.eclipse.tm4e.core.internal.rule.IRuleRegistry;
import org.eclipse.tm4e.core.internal.rule.Rule;

/**
 * Represents a "pushed" state on the stack (as a linked list element).
 *
 * @see <a href="https://github.com/Microsoft/vscode-textmate/blob/master/src/grammar.ts">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/grammar.ts</a>
 *
 */
public class StackElement {

	public static final StackElement NULL = new StackElement(null, 0, 0, 0, false, null,
			new ScopeListElement(null, "", 0), new ScopeListElement(null, "", 0));

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
	public final StackElement parent;

	/**
	 * The depth of the stack.
	 */
	public final int depth;

	/**
	 * The state (rule) that this element represents.
	 */
	public final int ruleId;

	/**
	 * The state has entered and captured \n. This means that the next line should have an anchorPosition of 0.
	 */
	public final boolean beginRuleCapturedEOL;

	/**
	 * The "pop" (end) condition for this state in case that it was dynamically generated through captured text.
	 */
	@Nullable
	public final String endRule;

	/**
	 * The list of scopes containing the "name" for this state.
	 */

	public final ScopeListElement nameScopesList;

	/**
	 * The list of scopes containing the "contentName" (besides "name") for this state.
	 * This list **must** contain as an element `scopeName`.
	 */
	public final ScopeListElement contentNameScopesList;

	public StackElement(
			@Nullable final StackElement parent,
			final int ruleId,
			final int enterPos,
			final int anchorPos,
			final boolean beginRuleCapturedEOL,
			@Nullable final String endRule,
			final ScopeListElement nameScopesList,
			final ScopeListElement contentNameScopesList) {
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
	private static boolean structuralEquals(@Nullable StackElement a, @Nullable StackElement b) {
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

			if (a.depth != b.depth || a.ruleId != b.ruleId || !Objects.equals(a.endRule, b.endRule)) {
				return false;
			}

			// Go to previous pair
			a = a.parent;
			b = b.parent;
		} while (true);
	}

	@SuppressWarnings("null")
	private static boolean equals(@Nullable final StackElement a, @Nullable final StackElement b) {
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
		if (other == null || other.getClass() != StackElement.class) {
			return false;
		}
		return equals(this, (StackElement) other);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hash(endRule, parent, contentNameScopesList);
		result = prime * result + depth;
		result = prime * result + ruleId;
		return result;
	}

	public void reset() {
		StackElement el = this;
		while (el != null) {
			el.enterPosition = -1;
			el.anchorPos = -1;
			el = el.parent;
		}
	}

	@Nullable
	public StackElement pop() {
		return parent;
	}

	public StackElement safePop() {
		if (parent != null)
			return parent;
		return this;
	}

	public StackElement push(final int ruleId,
			final int enterPos,
			final int anchorPos,
			final boolean beginRuleCapturedEOL,
			@Nullable final String endRule,
			final ScopeListElement nameScopesList,
			final ScopeListElement contentNameScopesList) {
		return new StackElement(this,
				ruleId,
				enterPos,
				anchorPos,
				beginRuleCapturedEOL,
				endRule,
				nameScopesList,
				contentNameScopesList);
	}

	public int getAnchorPos() {
		return anchorPos;
	}

	public int getEnterPos() {
		return enterPosition;
	}

	public Rule getRule(final IRuleRegistry grammar) {
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

	public StackElement setContentNameScopesList(final ScopeListElement contentNameScopesList) {
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

	public StackElement setEndRule(final String endRule) {
		if (this.endRule != null && this.endRule.equals(endRule)) {
			return this;
		}
		return new StackElement(this.parent,
				this.ruleId,
				this.enterPosition,
				this.anchorPos,
				this.beginRuleCapturedEOL,
				endRule,
				this.nameScopesList,
				this.contentNameScopesList);
	}

	public boolean hasSameRuleAs(final StackElement other) {
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
