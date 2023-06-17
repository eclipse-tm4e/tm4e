/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.model;

import static java.lang.System.Logger.Level.*;
import static org.eclipse.tm4e.core.internal.utils.MoreCollections.findLastElement;
import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

import java.lang.System.Logger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.PriorityBlockingQueue;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.internal.utils.StringUtils;

/**
 * TextMate model class.
 *
 * @see <a href="https://github.com/microsoft/vscode/blob/main/src/vs/editor/common/model/tokenizationTextModelPart.ts">
 *      github.com/microsoft/vscode/src/vs/editor/common/model/tokenizationTextModelPart.ts <code>#TokenizationTextModelPart</code></a>
 */
public class TMModel implements ITMModel {

	private static final Logger LOGGER = System.getLogger(TMModel.class.getName());

	/** The TextMate grammar to use to parse for each lines of the document the TextMate tokens. **/
	private @Nullable IGrammar grammar;

	/** Listener when TextMate model tokens changed **/
	private final Set<IModelTokensChangedListener> listeners = new CopyOnWriteArraySet<>();

	/** The background thread performing async tokenization. */
	private @Nullable volatile TokenizerThread tokenizerThread;
	private volatile boolean tokenizerThreadIsWorking;
	private @Nullable TMTokenization tokenizer;

	private final AbstractModelLines modelLines;
	private final PriorityBlockingQueue<Integer> invalidLines = new PriorityBlockingQueue<>();

	public TMModel(final AbstractModelLines lines) {
		modelLines = lines;
		modelLines.setModel(this);
		invalidateLine(0);
	}

	private static final boolean DEBUG_LOGGING = LOGGER.isLoggable(DEBUG);

	private void logDebug(final String msg, final Object... args) {
		if (!DEBUG_LOGGING)
			return;
		final var t = Thread.currentThread();
		final var caller = t.getStackTrace()[2];
		final var threadName = t.getName().endsWith(TokenizerThread.class.getSimpleName()) ? "tknz" : t.getName();
		LOGGER.log(DEBUG, "[" + threadName + "] " + getClass().getSimpleName() + "." + caller.getMethodName() +
				String.format(msg, args));
	}

	/**
	 * The {@link TokenizerThread} continuously runs tokenizing in background on the lines found in
	 * {@link TMModel#modelLines}.
	 *
	 * The {@link TMModel#modelLines} are expected to be accessed through {@link TMModel#getLines()} and manipulated by
	 * the UI part to inform of needs to (re)tokenize area, then the {@link TokenizerThread} processes them and emits
	 * events through the model.
	 *
	 * UI elements are supposed to subscribe and react to the events with
	 * {@link TMModel#addModelTokensChangedListener(IModelTokensChangedListener)}.
	 */
	private final class TokenizerThread extends Thread {

		/** Creates a new background thread. The thread runs with minimal priority. */
		TokenizerThread() {
			super("tm4e." + TokenizerThread.class.getSimpleName());
			setPriority(Thread.MIN_PRIORITY);
			setDaemon(true);
		}

		@Override
		public void run() {
			try {
				while (tokenizerThread == this) {
					tokenizerThreadIsWorking = !invalidLines.isEmpty();

					final int lineIndexToProcess = invalidLines.take();
					tokenizerThreadIsWorking = true;

					// skip if the queued line is not invalid anymore
					final var modelLine = modelLines.getOrNull(lineIndexToProcess);
					if (modelLine == null || !modelLine.isInvalid)
						continue;

					try {
						revalidateTokens(lineIndexToProcess);
					} catch (final Exception ex) {
						LOGGER.log(ERROR, ex.getMessage(), ex);
						invalidateLine(lineIndexToProcess);
					}
				}
			} catch (final InterruptedException e) {
				interrupt();
			}
			tokenizerThreadIsWorking = false;
		}

		/** process follow-up lines until this limit is reached */
		private static final int MAX_LOOP_TIME = 200;
		/** max time a single line can be processed */
		private static final Duration MAX_TIME_PER_LINE = Duration.ofSeconds(1);

		/**
		 * @param startLineIndex 0-based
		 */
		private void revalidateTokens(final int startLineIndex) {
			if (DEBUG_LOGGING)
				logDebug("(%d)", startLineIndex);

			final var changedRanges = new ArrayList<Range>();
			int lineIndex = startLineIndex;
			final long startTime = System.currentTimeMillis();

			revalidateTokens_Loop: {
				while (true) {

					final var modelLine = modelLines.getOrNull(lineIndex);
					if (modelLine == null) {
						if (DEBUG_LOGGING)
							logDebug("(%d) >> DONE - line %d does not exist anymore", startLineIndex, lineIndex);
						break revalidateTokens_Loop; // EXIT: line does not exist anymore
					}

					// (re-)tokenize the line
					if (DEBUG_LOGGING)
						logDebug("(%d) >> tokenizing line %d...", startLineIndex, lineIndex);
					final TokenizationResult r;
					try {
						final String lineText = modelLines.getLineText(lineIndex);
						r = castNonNull(tokenizer).tokenize(lineText, modelLine.startState, 0, MAX_TIME_PER_LINE);
					} catch (final Exception ex) {
						LOGGER.log(ERROR, ex.toString());
						// mark the current line as invalid and add it to the end of the queue
						invalidateLine(lineIndex);
						break revalidateTokens_Loop; // EXIT: error occurred
					}

					// check if complete line was tokenized
					if (r.stoppedEarly) {
						// treat the rest of the line as one default token
						r.tokens.add(new TMToken(r.actualStopOffset, ""));
						// Use the line's starting state as end state in case of incomplete tokenization
						r.endState = modelLine.startState;
					}
					modelLine.tokens = r.tokens;

					// add the line number to the changed ranges
					final int lineNumber = lineIndex + 1;
					final Range previousRange = findLastElement(changedRanges);
					if (previousRange != null && previousRange.toLineNumber == lineNumber - 1) {
						previousRange.toLineNumber = lineNumber; // extend previous range
					} else {
						changedRanges.add(new Range(lineNumber)); // insert new range
					}
					modelLine.isInvalid = false;

					// check if the next line requires re-tokenization too
					lineIndex++;
					final var nextModelLine = modelLines.getOrNull(lineIndex);
					if (nextModelLine == null) {
						if (DEBUG_LOGGING)
							logDebug("(%d) >> DONE - next line %d does not exist", startLineIndex, lineIndex);
						break revalidateTokens_Loop;
					}
					if (!nextModelLine.isInvalid && nextModelLine.startState.equals(r.endState)) {
						// has matching start state == is up to date
						if (DEBUG_LOGGING)
							logDebug("(%d) >> DONE - tokens of next line %d are up-to-date", startLineIndex, lineIndex);
						break revalidateTokens_Loop;
					}

					// next line is out of date
					nextModelLine.startState = r.endState;
					if (System.currentTimeMillis() - startTime >= MAX_LOOP_TIME) {
						if (DEBUG_LOGGING)
							logDebug("(%d) >> DONE - no more time left", startLineIndex);
						invalidateLine(lineIndex); // mark the next line as invalid and add it to the end of the queue
						break revalidateTokens_Loop;
					}
				}
			}

			if (!changedRanges.isEmpty()) {
				emit(new ModelTokensChangedEvent(changedRanges, TMModel.this));
			}
		}
	}

	@Override
	public BackgroundTokenizationState getBackgroundTokenizationState() {
		return tokenizerThreadIsWorking ? BackgroundTokenizationState.IN_PROGRESS : BackgroundTokenizationState.COMPLETED;
	}

	@Override
	public @Nullable IGrammar getGrammar() {
		return grammar;
	}

	@Override
	public synchronized void setGrammar(final IGrammar grammar) {
		if (!Objects.equals(grammar, this.grammar)) {
			this.grammar = grammar;
			final var tokenizer = this.tokenizer = new TMTokenization(grammar);
			modelLines.get(0).startState = tokenizer.getInitialState();
			invalidateLine(0);
			startTokenizerThread();
		}
	}

	@Override
	public synchronized void addModelTokensChangedListener(final IModelTokensChangedListener listener) {
		listeners.add(listener);
		startTokenizerThread();
	}

	@Override
	public synchronized void removeModelTokensChangedListener(final IModelTokensChangedListener listener) {
		listeners.remove(listener);

		if (listeners.isEmpty()) {
			// no need to keep tokenizing if no-one cares
			stopTokenizerThread();
		}
	}

	@Override
	public void dispose() {
		stopTokenizerThread();
		modelLines.dispose();
	}

	private void startTokenizerThread() {
		if (tokenizer != null && !listeners.isEmpty()) {
			var thread = this.tokenizerThread;
			if (thread == null || thread.isInterrupted()) {
				thread = this.tokenizerThread = new TokenizerThread();
			}
			if (!thread.isAlive()) {
				thread.start();
			}
		}
	}

	/** Interrupt the thread if running. */
	private synchronized void stopTokenizerThread() {
		final var thread = this.tokenizerThread;
		if (thread == null) {
			return;
		}
		thread.interrupt();
		this.tokenizerThread = null;
	}

	private void emit(final ModelTokensChangedEvent e) {
		if (DEBUG_LOGGING)
			logDebug("(%s)", e);
		for (final IModelTokensChangedListener listener : listeners) {
			listener.modelTokensChanged(e);
		}
	}

	@Override
	public @Nullable List<TMToken> getLineTokens(final int lineIndex) {
		final var modelLine = modelLines.getOrNull(lineIndex);
		return modelLine == null ? null : modelLine.tokens;
	}

	public int getNumberOfLines() {
		return modelLines.getNumberOfLines();
	}

	/** Marks the given line as out-of-date resulting in async re-parsing */
	void invalidateLine(final int lineIndex) {
		if (DEBUG_LOGGING)
			logDebug("(%d)", lineIndex);
		final var modelLine = modelLines.getOrNull(lineIndex);
		if (modelLine != null) {
			modelLine.isInvalid = true;
			invalidLines.add(lineIndex);
		}
	}

	@Override
	public String toString() {
		return StringUtils.toString(this, sb -> sb
				.append("grammar=").append(grammar));
	}
}
