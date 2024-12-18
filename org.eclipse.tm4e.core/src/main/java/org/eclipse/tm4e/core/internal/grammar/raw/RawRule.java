/**
 * Copyright (c) 2015-2019 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.internal.grammar.raw;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.parser.PropertySettable;
import org.eclipse.tm4e.core.internal.rule.RuleId;

public class RawRule extends PropertySettable.HashMap<@Nullable Object> implements IRawRule {

	private static final String APPLY_END_PATTERN_LAST = "applyEndPatternLast";
	private static final String BEGIN = "begin";
	static final String BEGIN_CAPTURES = "beginCaptures";
	static final String CAPTURES = "captures";
	private static final String CONTENT_NAME = "contentName";
	private static final String END = "end";
	static final String END_CAPTURES = "endCaptures";
	private static final String ID = "id";
	private static final String INCLUDE = "include";
	private static final String MATCH = "match";
	private static final String NAME = "name";
	private static final String PATTERNS = "patterns";
	static final String REPOSITORY = "repository";
	private static final String WHILE = "while";
	static final String WHILE_CAPTURES = "whileCaptures";

	private static final long serialVersionUID = 1L;

	@Override
	public @Nullable RuleId getId() {
		return (RuleId) get(ID);
	}

	@Override
	public void setId(final RuleId id) {
		super.put(ID, id);
	}

	@Override
	public @Nullable String getName() {
		return (String) get(NAME);
	}

	public RawRule setName(final String name) {
		super.put(NAME, name);
		return this;
	}

	@Override
	public @Nullable String getContentName() {
		return (String) get(CONTENT_NAME);
	}

	@Override
	public @Nullable String getMatch() {
		return (String) get(MATCH);
	}

	@Override
	public @Nullable IRawCaptures getCaptures() {
		updateCaptures(CAPTURES);
		return (IRawCaptures) get(CAPTURES);
	}

	private void updateCaptures(final String name) {
		final Object captures = get(name);
		if (captures instanceof final List<?> capturesList) {
			final var rawCaptures = new RawRule();
			int i = 0;
			for (final var capture : capturesList) {
				i++;
				rawCaptures.put(Integer.toString(i), capture);
			}
			super.put(name, rawCaptures);
		}
	}

	@Override
	public @Nullable String getBegin() {
		return (String) get(BEGIN);
	}

	@Override
	public @Nullable String getWhile() {
		return (String) get(WHILE);
	}

	@Override
	public @Nullable String getInclude() {
		return (String) get(INCLUDE);
	}

	public RawRule setInclude(final @Nullable String include) {
		super.put(INCLUDE, include);
		return this;
	}

	@Override
	public @Nullable IRawCaptures getBeginCaptures() {
		updateCaptures(BEGIN_CAPTURES);
		return (IRawCaptures) get(BEGIN_CAPTURES);
	}

	@Override
	public @Nullable String getEnd() {
		return (String) get(END);
	}

	@Override
	public @Nullable IRawCaptures getEndCaptures() {
		updateCaptures(END_CAPTURES);
		return (IRawCaptures) get(END_CAPTURES);
	}

	@Override
	public @Nullable IRawCaptures getWhileCaptures() {
		updateCaptures(WHILE_CAPTURES);
		return (IRawCaptures) get(WHILE_CAPTURES);
	}

	@Override
	@SuppressWarnings("unchecked")
	public @Nullable Collection<IRawRule> getPatterns() {
		return (Collection<IRawRule>) get(PATTERNS);
	}

	public RawRule setPatterns(final @Nullable Collection<IRawRule> patterns) {
		super.put(PATTERNS, patterns);
		return this;
	}

	@Override
	public @Nullable IRawRepository getRepository() {
		return (IRawRepository) get(REPOSITORY);
	}

	@Override
	public boolean isApplyEndPatternLast() {
		final Object applyEndPatternLast = get(APPLY_END_PATTERN_LAST);
		if (applyEndPatternLast == null) {
			return false;
		}
		if (applyEndPatternLast instanceof final Boolean asBool) {
			return asBool;
		}
		if (applyEndPatternLast instanceof final Integer asInt) {
			return asInt == 1;
		}
		return false;
	}
}
