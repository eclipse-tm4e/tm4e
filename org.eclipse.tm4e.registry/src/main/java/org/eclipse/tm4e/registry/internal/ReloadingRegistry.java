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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
 * Newly referenced grammars are loaded before the updated grammar is returned.
 * </p>
 * <p>
 * File timestamp checks are throttled to once per configured interval (5 seconds) to avoid excessive filesystem access.
 * A scope changing to a different source is detected on every query and takes effect immediately.
 * </p>
 */
public class ReloadingRegistry extends Registry {

	/** Interval for re-checking grammar changes, in nanoseconds (5 seconds). */
	private static final long RECHECK_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(5);

	/**
	 * Holds caching state for a scope and its currently selected source.
	 *
	 * @param sourceUri the selected source, or null when no source is registered
	 * @param isLoaded whether this source was successfully loaded
	 * @param modifiedAt the last known file.lastModified() (milliseconds since epoch)
	 * @param lastCheckedAt the System.nanoTime() timestamp when we last compared file.modified()
	 */
	private static record GrammarCacheState(@Nullable URI sourceUri, boolean isLoaded, long modifiedAt, long lastCheckedAt) {
	}

	private final Map<String /* scopeName */, GrammarCacheState> grammarCacheStates = new HashMap<>();

	public ReloadingRegistry() {
	}

	public ReloadingRegistry(final IRegistryOptions options) {
		super(options);
	}

	@Override
	public @Nullable IGrammar grammarForScopeName(final String scopeName) {
		final GrammarCacheState oldState = grammarCacheStates.get(scopeName);
		if (oldState == null)
			return null;

		final GrammarCacheState state = loadGrammarSource(scopeName);
		// A removed source must not return the grammar still held in the underlying cache.
		if (!state.isLoaded())
			return null;

		// A changed source may introduce new includes. Ignore lastCheckedAt: a timestamp check alone does not change the grammar.
		// Load dependencies after the cache update, because loadGrammar() checks this scope again.
		if (!Objects.equals(state.sourceUri(), oldState.sourceUri()) || state.modifiedAt() != oldState.modifiedAt()) {
			return loadGrammar(scopeName);
		}
		return super.grammarForScopeName(scopeName);
	}

	@Override
	public @Nullable IGrammar loadGrammar(final String scopeName) {
		try {
			return super.loadGrammar(scopeName);
		} catch (final RuntimeException ex) {
			// A dependency can fail after the root is cached. Clear the root after every failed load, including
			// retries made directly by the registry manager, so the next lookup loads dependencies again.
			grammarCacheStates.remove(scopeName);
			throw ex;
		}
	}

	@Override
	protected boolean _loadSingleGrammar(final String scopeName) {
		return loadGrammarSource(scopeName).isLoaded();
	}

	private GrammarCacheState loadGrammarSource(final String scopeName) {
		final long nowNanos = System.nanoTime();

		try {
			return grammarCacheStates.compute(scopeName, (key, oldState) -> {
				final IGrammarSource source = _grammarSourceForScopeName(key);
				final URI sourceUri = source == null ? null : source.getURI();

				// Removing or replacing an import can select another file for the same scope. Check the URI before
				// the timestamp delay: the replacement can be older, and comparing URIs needs no filesystem access.
				if (oldState == null || !Objects.equals(sourceUri, oldState.sourceUri())) {
					final boolean isLoaded = _doLoadSingleGrammar(key);
					final long lastModified = Math.max(source == null ? 0 : source.getLastModified(), 0);
					return new GrammarCacheState(sourceUri, isLoaded, lastModified, nowNanos);
				}

				// Skip re-check if within interval
				if (nowNanos - oldState.lastCheckedAt() < RECHECK_INTERVAL_NANOS) {
					return oldState;
				}

				if (source == null) {
					// No source available: update lastCheckedAt only
					return new GrammarCacheState(sourceUri, oldState.isLoaded(), oldState.modifiedAt(), nowNanos);
				}

				final long lastModified = source.getLastModified();
				if (lastModified != oldState.modifiedAt()) {
					// Deletion reports zero, and replacements can be older. Read the source to distinguish absence from a valid timestamp.
					final boolean isLoaded = _doLoadSingleGrammar(key);
					return new GrammarCacheState(sourceUri, isLoaded, lastModified, nowNanos);
				}

				// No change: update lastCheckedAt only
				return new GrammarCacheState(sourceUri, oldState.isLoaded(), oldState.modifiedAt(), nowNanos);
			});
		} catch (final RuntimeException ex) {
			// compute keeps the old entry if loading fails. Clear it here for both roots and dependencies,
			// so restoring a file with its old timestamp still forces a fresh read.
			grammarCacheStates.remove(scopeName);
			throw ex;
		}
	}
}
