/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
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
 *  - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 *  - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.grammar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tm4e.core.internal.grammar.ScopeListElement;
import org.eclipse.tm4e.core.internal.rule.IRuleRegistry;
import org.eclipse.tm4e.core.internal.rule.Rule;

/**
 * Represents a "pushed" state on the stack (as a linked list element).
 *
 * @see https://github.com/Microsoft/vscode-textmate/blob/master/src/grammar.ts
 *
 */
public class StackElement {
	
	//_stackElementBrand: void;

	public static final StackElement NULL = new StackElement(null, 0, 0, null, null, null);

	/**
	 * The position on the current line where this state was pushed.
	 * This is relevant only while tokenizing a line, to detect endless loops.
	 * Its value is meaningless across lines.
	 */
	private int _enterPos;

	/**
	 * The previous state on the stack (or null for the root state).
	 */
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
	 * The "pop" (end) condition for this state in case that it was dynamically generated through captured text.
	 */
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

	public StackElement(StackElement parent, int ruleId, int enterPos, String endRule, ScopeListElement nameScopesList, ScopeListElement contentNameScopesList) {
		this.parent = parent;
		this.depth = (this.parent != null ? this.parent.depth + 1 : 1);
		this.ruleId = ruleId;
		this._enterPos = enterPos;
		this.endRule = endRule;
		this.nameScopesList = nameScopesList;
		this.contentNameScopesList = contentNameScopesList;
	}

	/**
	 * A structural equals check. Does not take into account `scopes`.
	 */
	private static boolean _structuralEquals(StackElement a, StackElement b) {
		do {
			if (a == b) {
				return true;
			}

			if (a.depth != b.depth || a.ruleId != b.ruleId || a.endRule != b.endRule) {
				return false;
			}

			// Go to previous pair
			a = a.parent;
			b = b.parent;

			if (a == null && b == null) {
				// End of list reached for both
				return true;
			}

			if (a == null || b == null) {
				// End of list reached only for one
				return false;
			}

		} while (true);
	}
 
	private static boolean _equals(StackElement a, StackElement b) {
		if (a == b) {
			return true;
		}
		if (!_structuralEquals(a, b)) {
			return false;
		}
		return a.contentNameScopesList.equals(b.contentNameScopesList);
	}

	public StackElement clone() {
		return this;
	}	
	
	public boolean equals(StackElement other) {
		if (other == null) {
			return false;
		}
		return StackElement._equals(this, other);
	}

	private static void _reset(StackElement el) {
		while (el != null) {
			el._enterPos = -1;
			el = el.parent;
		}
	}

	public void reset() {
		StackElement._reset(this);
	}

	public StackElement pop() {
		return this.parent;
	}

	public StackElement safePop() {
		if (this.parent != null) {
			return this.parent;
		}
		return this;
	}
	
	public StackElement push(int ruleId, int enterPos, String endRule, ScopeListElement nameScopesList, ScopeListElement contentNameScopesList) {
		return new StackElement(this, ruleId, enterPos, endRule, nameScopesList, contentNameScopesList);
	}

	public int getEnterPos() {
		return this._enterPos;
	}

	public Rule getRule(IRuleRegistry grammar) {
		return grammar.getRule(this.ruleId);
	}

	private void _writeString(List<String> res) {
		if (this.parent != null) {
			this.parent._writeString(res);
		}
		String s = "(" + this.ruleId + ")"; //, TODO-${this.nameScopesList}, TODO-${this.contentNameScopesList})`;
		res.add(s);
	}

	public String toString() {
		List<String> r = new ArrayList<>();
		this._writeString(r);
		return '[' + String.join(", ", r) + ']';
	}

	public StackElement setContentNameScopesList(ScopeListElement contentNameScopesList) {
		if (this.contentNameScopesList.equals(contentNameScopesList)) {
			return this;
		}
		return this.parent.push(this.ruleId, this._enterPos, this.endRule, this.nameScopesList, contentNameScopesList);
	}

	public StackElement setEndRule(String endRule) {
		if (this.endRule != null && this.endRule.equals(endRule)) {
			return this;
		}
		return new StackElement(this.parent, this.ruleId, this._enterPos, endRule, this.nameScopesList, this.contentNameScopesList);
	}

	public boolean hasSameRuleAs(StackElement other) {
		return this.ruleId == other.ruleId;
	}}
