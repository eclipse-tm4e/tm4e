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
import static org.eclipse.tm4e.core.internal.utils.MoreCollections.*;
import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.lang.System.Logger;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.internal.model.ModelTokensChangedEventBuilder;

/**
 * TextMate model class.
 */
public class TMModel implements ITMModel {

	private static final Logger LOGGER = System.getLogger(TMModel.class.getName());

	/** The TextMate grammar to use to parse for each lines of the document the TextMate tokens. **/
	@Nullable
	private IGrammar grammar;

	/** Listener when TextMate model tokens changed **/
	private final Set<IModelTokensChangedListener> listeners = new CopyOnWriteArraySet<>();

	@Nullable
	private Tokenizer tokenizer;

	/** The background thread. */
	@Nullable
	private volatile TokenizerThread fThread;

	private final IModelLines lines;
	private final PriorityBlockingQueue<Integer> invalidLines = new PriorityBlockingQueue<>();

	public TMModel(final IModelLines lines) {
		this.lines = lines;
		if (lines instanceof AbstractLineList) {
			((AbstractLineList) lines).setModel(this);
		}
		lines.forEach(ModelLine::resetTokenizationState);
		invalidateLine(0);
	}

	/**
	 * The {@link TokenizerThread} takes as input an {@link TMModel} and continuously runs tokenizing in background on
	 * the lines found in {@link TMModel#lines}.
	 *
	 * The {@link TMModel#lines} are expected to be accessed through {@link TMModel#getLines()} and manipulated by the
	 * UI part to inform of needs to (re)tokenize area, then the {@link TokenizerThread} processes them and emits events
	 * through the model.
	 *
	 * UI elements are supposed to subscribe and react to the events with
	 * {@link TMModel#addModelTokensChangedListener(IModelTokensChangedListener)}.
	 */
	private static final class TokenizerThread extends Thread {
		private final TMModel model;

		@Nullable
		private TMState lastState;

		/**
		 * Creates a new background thread. The thread runs with minimal priority.
		 *
		 * @param name the thread's name
		 */
		TokenizerThread(final String name, final TMModel model) {
			super(name);
			this.model = model;
			setPriority(Thread.MIN_PRIORITY);
			setDaemon(true);
		}

		@Override
		public void run() {
			while (!isInterrupted() && model.fThread == this) {
				try {
					final int toProcess = model.invalidLines.take().intValue();
					if (model.lines.get(toProcess).isInvalid) {
						try {
							revalidateTokensNow(toProcess, null);
						} catch (final Exception ex) {
							LOGGER.log(ERROR, ex.getMessage());
							if (toProcess < model.lines.getNumberOfLines()) {
								model.invalidateLine(toProcess);
							}
						}
					}
				} catch (final InterruptedException e) {
					interrupt();
				}
			}
		}

		/**
		 * @param startLine 0-based
		 * @param toLineIndexOrNull 0-based
		 */
		private void revalidateTokensNow(final int startLine, @Nullable final Integer toLineIndexOrNull) {
			model.buildAndEmitEvent(eventBuilder -> {
				final int toLineIndex;
				if (toLineIndexOrNull == null || toLineIndexOrNull >= model.lines.getNumberOfLines()) {
					toLineIndex = model.lines.getNumberOfLines() - 1;
				} else {
					toLineIndex = toLineIndexOrNull;
				}

				long tokenizedChars = 0;
				long currentCharsToTokenize = 0;
				final long MAX_ALLOWED_TIME = 20;
				long currentEstimatedTimeToTokenize = 0;
				long elapsedTime;
				final long startTime = System.currentTimeMillis();
				// Tokenize at most 1000 lines. Estimate the tokenization speed per character and stop when:
				// - MAX_ALLOWED_TIME is reached
				// - tokenizing the next line would go above MAX_ALLOWED_TIME

				int lineIndex = startLine;
				while (lineIndex <= toLineIndex && lineIndex < model.lines.getNumberOfLines()) {
					elapsedTime = System.currentTimeMillis() - startTime;
					if (elapsedTime > MAX_ALLOWED_TIME) {
						// Stop if MAX_ALLOWED_TIME is reached
						model.invalidateLine(lineIndex);
						return;
					}

					// Compute how many characters will be tokenized for this line
					try {
						currentCharsToTokenize = model.lines.getLineLength(lineIndex);
					} catch (final Exception ex) {
						LOGGER.log(ERROR, ex.getMessage());
					}

					if (tokenizedChars > 0) {
						// If we have enough history, estimate how long tokenizing this line would take
						currentEstimatedTimeToTokenize = (long) ((double) elapsedTime / tokenizedChars)
								* currentCharsToTokenize;
						if (elapsedTime + currentEstimatedTimeToTokenize > MAX_ALLOWED_TIME) {
							// Tokenizing this line will go above MAX_ALLOWED_TIME
							model.invalidateLine(lineIndex);
							return;
						}
					}

					lineIndex = updateTokensInRange(eventBuilder, lineIndex, lineIndex) + 1;
					tokenizedChars += currentCharsToTokenize;
				}
			});

		}

		/**
		 * @param startIndex 0-based
		 * @param endLineIndex 0-based
		 *
		 * @return the first line index (0-based) that was NOT processed by this operation
		 */
		private int updateTokensInRange(final ModelTokensChangedEventBuilder eventBuilder, final int startIndex,
				final int endLineIndex) {
			final int stopLineTokenizationAfter = 1_000_000_000; // 1 billion, if a line is so long, you have other
																 // trouble :)

			// Validate all states up to and including endLineIndex
			int nextInvalidLineIndex = startIndex;
			int lineIndex = startIndex;
			while (lineIndex <= endLineIndex && lineIndex < model.lines.getNumberOfLines()) {
				final int endStateIndex = lineIndex + 1;
				LineTokens r = null;
				String text = null;
				final ModelLine modeLine = model.lines.get(lineIndex);
				try {
					text = model.lines.getLineText(lineIndex);
					// Tokenize only the first X characters
					r = castNonNull(model.tokenizer).tokenize(text, modeLine.state, 0, stopLineTokenizationAfter);
				} catch (final Exception ex) {
					LOGGER.log(ERROR, ex.toString());
					return nextInvalidLineIndex;
				}

				if (!r.tokens.isEmpty()) {
					// Cannot have a stop offset before the last token
					r.actualStopOffset = Math.max(r.actualStopOffset, getLastElement(r.tokens).startIndex + 1);
				}

				if (r.actualStopOffset < text.length()) {
					// Treat the rest of the line (if above limit) as one default token
					r.tokens.add(new TMToken(r.actualStopOffset, ""));
					// Use as end state the starting state
					r.endState = modeLine.getState();
				}

				modeLine.setTokens(r.tokens);
				eventBuilder.registerChangedTokens(lineIndex + 1);
				modeLine.isInvalid = false;

				if (endStateIndex < model.lines.getNumberOfLines()) {
					final ModelLine endStateLine = castNonNull(model.lines.get(endStateIndex));
					if (endStateLine.getState() != null && Objects.equals(endStateLine.getState(), r.endState)) {
						// The end state of this line remains the same
						nextInvalidLineIndex = lineIndex + 1;
						while (nextInvalidLineIndex < model.lines.getNumberOfLines()) {
							if (model.lines.get(nextInvalidLineIndex).isInvalid) {
								break;
							}
							final var isLastLine = nextInvalidLineIndex + 1 >= model.lines.getNumberOfLines();
							if (isLastLine
									? lastState == null
									: model.lines.get(nextInvalidLineIndex + 1).getState() == null) {
								break;
							}
							nextInvalidLineIndex++;
						}
						lineIndex = nextInvalidLineIndex;
					} else {
						endStateLine.setState(r.endState);
						lineIndex++;
					}
				} else {
					lastState = r.endState;
					lineIndex++;
				}
			}
			return nextInvalidLineIndex;
		}
	}

	@Nullable
	@Override
	public IGrammar getGrammar() {
		return grammar;
	}

	@Override
	public void setGrammar(final IGrammar grammar) {
		if (!Objects.equals(grammar, this.grammar)) {
			this.grammar = grammar;
			final var tokenizer = this.tokenizer = new Tokenizer(grammar);
			lines.get(0).setState(tokenizer.getInitialState());
		}
	}

	@Override
	public synchronized void addModelTokensChangedListener(final IModelTokensChangedListener listener) {
		listeners.add(listener);

		var fThread = this.fThread;
		if (fThread == null || fThread.isInterrupted()) {
			fThread = this.fThread = new TokenizerThread(getClass().getName(), this);
		}
		if (!fThread.isAlive()) {
			fThread.start();
		}
	}

	@Override
	public synchronized void removeModelTokensChangedListener(final IModelTokensChangedListener listener) {
		listeners.remove(listener);

		if (listeners.isEmpty()) {
			// no need to keep tokenizing if no-one cares
			stop();
		}
	}

	@Override
	public void dispose() {
		stop();
		lines.dispose();
	}

	/**
	 * Interrupt the thread.
	 */
	private synchronized void stop() {
		final var fThread = this.fThread;
		if (fThread == null) {
			return;
		}
		fThread.interrupt();
		this.fThread = null;
	}

	private void buildAndEmitEvent(final Consumer<ModelTokensChangedEventBuilder> callback) {
		final ModelTokensChangedEventBuilder eventBuilder = new ModelTokensChangedEventBuilder(this);

		callback.accept(eventBuilder);

		final ModelTokensChangedEvent event = eventBuilder.build();
		if (event != null) {
			emit(event);
		}
	}

	private void emit(final ModelTokensChangedEvent e) {
		for (final IModelTokensChangedListener listener : listeners) {
			listener.modelTokensChanged(e);
		}
	}

	@Override
	public void forceTokenization(final int lineNumber) {
		final var tokenizerThread = this.fThread;
		if (tokenizerThread == null) {
			return;
		}
		buildAndEmitEvent(eventBuilder -> tokenizerThread.updateTokensInRange(eventBuilder, lineNumber, lineNumber));
	}

	@Override
	@Nullable
	public List<TMToken> getLineTokens(final int lineNumber) {
		return lines.get(lineNumber).tokens;
	}

	/**
	 * @throws IndexOutOfBoundsException
	 */
	public boolean isLineInvalid(final int lineNumber) {
		return lines.get(lineNumber).isInvalid;
	}

	/**
	 * @throws IndexOutOfBoundsException
	 */
	void invalidateLine(final int lineIndex) {
		lines.get(lineIndex).isInvalid = true;
		invalidLines.add(lineIndex);
	}
}
