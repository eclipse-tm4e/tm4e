/**
 * Copyright (c) 2025 Vegard IT GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Sebastian Thomschke (Vegard IT) - initial implementation
 */
package org.eclipse.tm4e.registry.internal;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.IRegistryOptions;
import org.eclipse.tm4e.core.registry.Registry;

/**
 * Extension of {@link Registry} that automatically reloads grammars when their source files change.
 * <p>
 * Once a grammar has been loaded, subsequent queries will trigger a filesystem timestamp check.
 * If the underlying grammar file has been modified since the last load, it is reloaded before returning.
 * </p>
 * <p>
 * Reload checks are throttled to occur at most once per configured interval (5 seconds) to avoid excessive filesystem access.
 * </p>
 */
public class ReloadingRegistry extends Registry {

	/** Interval for re-checking grammar changes, in nanoseconds (5 seconds). */
	private static final long RECHECK_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(5);

	/**
	 * Holds per‐grammar caching state.
	 *
	 * @param isLoaded whether the grammar was ever successfully loaded
	 * @param modifiedAt the last‐known file.lastModified() (milliseconds since epoch)
	 * @param lastCheckedAt the System.nanoTime() timestamp when we last compared file.modified()
	 */
	private static record GrammarCacheState(boolean isLoaded, long modifiedAt, long lastCheckedAt) {
	}

	private final Map<String /* scopeName */, GrammarCacheState> grammarCacheStates = new HashMap<>();

	public ReloadingRegistry() {
	}

	public ReloadingRegistry(final IRegistryOptions options) {
		super(options);
	}

	@Override
	public @Nullable IGrammar grammarForScopeName(final String scopeName) {
		final GrammarCacheState state = grammarCacheStates.get(scopeName);
		if (state == null)
			return null;

		_loadSingleGrammar(scopeName); // re-loads grammar if required
		return super.grammarForScopeName(scopeName);
	}

	@Override
	protected boolean _loadSingleGrammar(final String scopeName) {
		final long nowNanos = System.nanoTime();

		return castNonNull(grammarCacheStates.compute(scopeName, (key, oldState) -> {
			// Grammar was never loaded perform initial load
			if (oldState == null) {
				final boolean isLoaded = _doLoadSingleGrammar(key);
				final IGrammarSource source = _grammarSourceForScopeName(key);
				final long lastModified = Math.max(source == null ? 0 : source.getLastModified(), 0);
				return new GrammarCacheState(isLoaded, lastModified, nowNanos);
			}

			// Skip re-check if within interval
			if (nowNanos - oldState.lastCheckedAt() < RECHECK_INTERVAL_NANOS) {
				return oldState;
			}

			final IGrammarSource source = _grammarSourceForScopeName(key);
			if (source == null) {
				// No source available: update lastCheckedAt only
				return new GrammarCacheState(oldState.isLoaded(), oldState.modifiedAt(), nowNanos);
			}

			final long lastModified = source.getLastModified();
			if (lastModified > oldState.modifiedAt()) {
				// Reload grammar
				final boolean isLoaded = _doLoadSingleGrammar(key);
				return new GrammarCacheState(isLoaded, lastModified, nowNanos);
			}

			// No change: update lastCheckedAt only
			return new GrammarCacheState(oldState.isLoaded(), oldState.modifiedAt(), nowNanos);
		})).isLoaded();
	}
}
