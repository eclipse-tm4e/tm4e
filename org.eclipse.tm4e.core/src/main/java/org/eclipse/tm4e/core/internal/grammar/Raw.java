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
 * Pierre-Yves B. - Issue #221 NullPointerException when retrieving fileTypes
 */
package org.eclipse.tm4e.core.internal.grammar;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.types.IRawCaptures;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;
import org.eclipse.tm4e.core.internal.types.IRawRepository;
import org.eclipse.tm4e.core.internal.types.IRawRule;

/**
 * Raw
 */
public final class Raw extends HashMap<@Nullable String, @Nullable Object>
		implements IRawRepository, IRawRule, IRawGrammar, IRawCaptures {

	public static final String DOLLAR_SELF = "$self";
	public static final String DOLLAR_BASE = "$base";

	private static final String APPLY_END_PATTERN_LAST = "applyEndPatternLast";
	private static final String BEGIN = "begin";
	private static final String BEGIN_CAPTURES = "beginCaptures";
	private static final String CAPTURES = "captures";
	private static final String CONTENT_NAME = "contentName";
	private static final String END = "end";
	private static final String END_CAPTURES = "endCaptures";
	private static final String FILE_TYPES = "fileTypes";
	private static final String FIRST_LINE_MATCH = "firstLineMatch";
	private static final String ID = "id";
	private static final String INCLUDE = "include";
	private static final String INJECTIONS = "injections";
	private static final String INJECTION_SELECTOR = "injectionSelector";
	private static final String MATCH = "match";
	private static final String NAME = "name";
	private static final String PATTERNS = "patterns";
	private static final String REPOSITORY = "repository";
	private static final String SCOPE_NAME = "scopeName";
	private static final String WHILE = "while";
	private static final String WHILE_CAPTURES = "whileCaptures";

	private static final long serialVersionUID = 1L;

	@Nullable
	private transient List<String> fileTypes;

	private Object getSafe(@Nullable final Object key) {
		@SuppressWarnings("unlikely-arg-type")
		final var obj = get(key);
		if (obj == null) {
			throw new IllegalArgumentException("Key '" + key + "' does not exit found");
		}
		return obj;
	}

	@Override
	@Nullable
	public IRawRule getProp(final String name) {
		return (IRawRule) get(name);
	}

	@Override
	public IRawRule getBase() {
		return (IRawRule) getSafe(DOLLAR_BASE);
	}

	@Override
	public void setBase(final IRawRule base) {
		super.put(DOLLAR_BASE, base);
	}

	@Override
	public IRawRule getSelf() {
		return (IRawRule) getSafe(DOLLAR_SELF);
	}

	@Override
	public void setSelf(final IRawRule self) {
		super.put(DOLLAR_SELF, self);
	}

	@Nullable
	@Override
	public Integer getId() {
		return (Integer) get(ID);
	}

	@Override
	public void setId(@Nullable final Integer id) {
		super.put(ID, id);
	}

	@Nullable
	@Override
	public String getName() {
		return (String) get(NAME);
	}

	@Override
	public void setName(@Nullable final String name) {
		super.put(NAME, name);
	}

	@Nullable
	@Override
	public String getContentName() {
		return (String) get(CONTENT_NAME);
	}

	@Nullable
	@Override
	public String getMatch() {
		return (String) get(MATCH);
	}

	@Nullable
	@Override
	public IRawCaptures getCaptures() {
		updateCaptures(CAPTURES);
		return (IRawCaptures) get(CAPTURES);
	}

	public void updateCaptures(final String name) {
		final Object captures = get(name);
		if (captures instanceof List) {
			final Raw rawCaptures = new Raw();
			int i = 0;
			for (final Object capture : (List<?>) captures) {
				i++;
				rawCaptures.put(i + "", capture);
			}
			super.put(name, rawCaptures);
		}
	}

	@Nullable
	@Override
	public String getBegin() {
		return (String) get(BEGIN);
	}

	@Nullable
	@Override
	public String getWhile() {
		return (String) get(WHILE);
	}

	@Nullable
	@Override
	public String getInclude() {
		return (String) get(INCLUDE);
	}

	@Override
	public void setInclude(@Nullable final String include) {
		super.put(INCLUDE, include);
	}

	@Nullable
	@Override
	public IRawCaptures getBeginCaptures() {
		updateCaptures(BEGIN_CAPTURES);
		return (IRawCaptures) get(BEGIN_CAPTURES);
	}

	@Override
	public void setBeginCaptures(@Nullable final IRawCaptures beginCaptures) {
		super.put(BEGIN_CAPTURES, beginCaptures);
	}

	@Nullable
	@Override
	public String getEnd() {
		return (String) get(END);
	}

	@Nullable
	@Override
	public IRawCaptures getEndCaptures() {
		updateCaptures(END_CAPTURES);
		return (IRawCaptures) get(END_CAPTURES);
	}

	@Nullable
	@Override
	public IRawCaptures getWhileCaptures() {
		updateCaptures(WHILE_CAPTURES);
		return (IRawCaptures) get(WHILE_CAPTURES);
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public Collection<IRawRule> getPatterns() {
		return (Collection<IRawRule>) get(PATTERNS);
	}

	@Override
	public void setPatterns(final @Nullable Collection<IRawRule> patterns) {
		super.put(PATTERNS, patterns);
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, IRawRule> getInjections() {
		return (Map<String, IRawRule>) get(INJECTIONS);
	}

	@Nullable
	@Override
	public String getInjectionSelector() {
		return (String) get(INJECTION_SELECTOR);
	}

	@Nullable
	@Override
	public IRawRepository getRepository() {
		return (IRawRepository) get(REPOSITORY);
	}

	@Override
	public IRawRepository getRepositorySafe() {
		return (IRawRepository) getSafe(REPOSITORY);
	}

	@Override
	public void setRepository(@Nullable final IRawRepository repository) {
		super.put(REPOSITORY, repository);
	}

	@Override
	public boolean isApplyEndPatternLast() {
		final Object applyEndPatternLast = get(APPLY_END_PATTERN_LAST);
		if (applyEndPatternLast == null) {
			return false;
		}
		if (applyEndPatternLast instanceof Boolean) {
			return (Boolean) applyEndPatternLast;
		}
		if (applyEndPatternLast instanceof Integer) {
			return ((Integer) applyEndPatternLast).equals(1);
		}
		return false;
	}

	@Override
	public String getScopeName() {
		return (String) getSafe(SCOPE_NAME);
	}

	@Override
	public Collection<String> getFileTypes() {
		List<String> result = fileTypes;
		if (result == null) {
			result = new ArrayList<>();
			final Collection<@NonNull ?> unparsedFileTypes = (Collection<@NonNull ?>) get(FILE_TYPES);
			if (unparsedFileTypes != null) {
				for (final Object o : unparsedFileTypes) {
					String str = o.toString();
					// #202
					if (str.startsWith(".")) {
						str = str.substring(1);
					}
					result.add(str);
				}
			}
			fileTypes = result;
		}
		return result;
	}

	@Override
	@Nullable
	public String getFirstLineMatch() {
		return (String) get(FIRST_LINE_MATCH);
	}

	@Override
	public IRawRule getCapture(final String captureId) {
		return (IRawRule) getSafe(captureId);
	}

	@Override
	public Iterator<String> iterator() {
		final Iterator<String> it = castNonNull(super.keySet().iterator());
		return castNonNull(it);
	}

	@Override
	public Raw clone() {
		final var clone = (Raw) clone(this);
		return castNonNull(clone);
	}

	@Nullable
	private Object clone(@Nullable final Object value) {
		if (value instanceof Raw) {
			final Raw rowToClone = (Raw) value;
			final Raw raw = new Raw();
			for (final var entry : rowToClone.entrySet()) {
				raw.put(entry.getKey(), clone(entry.getValue()));
			}
			return raw;
		}
		if (value instanceof List) {
			return ((List<?>) value).stream().map(this::clone).collect(Collectors.toList());
		}
		return value;
	}

	@Nullable
	@Override
	public Object put(@Nullable final String key, @Nullable final Object value) {
		if (FILE_TYPES.equals(key))
			fileTypes = null;

		return super.put(key, value);
	}

	@Override
	@SuppressWarnings("unlikely-arg-type")
	public void putAll(@Nullable final Map<? extends @Nullable String, ? extends @Nullable Object> m) {
		if (m != null && m.containsKey(FILE_TYPES))
			fileTypes = null;
		super.putAll(m);
	}
}