/*******************************************************************************
 * Copyright (c) 2025 Vegard IT GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT) - initial implementation
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.model.ITMModel.BackgroundTokenizationState;
import org.eclipse.tm4e.core.model.ModelTokensChangedEvent;
import org.eclipse.tm4e.core.model.Range;
import org.eclipse.tm4e.core.model.TMToken;
import org.eclipse.tm4e.registry.internal.TMScope;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.model.TMDocumentModel;
import org.eclipse.tm4e.ui.internal.model.TMModelManager;
import org.eclipse.tm4e.ui.internal.utils.GrammarUtils;
import org.eclipse.tm4e.ui.text.ITMPartitionRegion;
import org.eclipse.tm4e.ui.text.ITMPartitioner;
import org.eclipse.tm4e.ui.text.TMPartitions;

public final class TMPartitioner implements ITMPartitioner {

	record TMPartitionRegion(int offset, int length, String type, String grammarScope)
			implements ITMPartitionRegion {

		private static String getGrammarScope(final String type, final @Nullable IGrammar grammar) {
			if (grammar != null) {
				final var grammarScope = normalizeBaseScope(grammar.getScopeName());
				if (grammarScope != null)
					return grammarScope;
			}
			return scopeFromPartitionType(type);
		}

		public TMPartitionRegion(final int offset, final int length, final TMPartitionRegion region) {
			this(offset, length, region.type, region.grammarScope);
		}

		public TMPartitionRegion(final int offset, final int length, final String type, final @Nullable IGrammar grammar) {
			this(offset, length, type, getGrammarScope(type, grammar));
		}

		@Override
		public String getType() {
			return type;
		}

		@Override
		public int getLength() {
			return length;
		}

		@Override
		public int getOffset() {
			return offset;
		}

		@Override
		public String getGrammarScope() {
			return grammarScope;
		}
	}

	private static final String TEXT_UNKNOWN = "text.unknown";

	private static String ensureGrammarScope(final String type, final @Nullable String candidate, final @Nullable String baseScope) {
		if (candidate != null) {
			final String normalized = normalizeVariantScope(candidate);
			if (normalized != null)
				return normalized;
		}
		if (TMPartitions.BASE_PARTITION_TYPE.equals(type)) {
			final String normalizedBase = normalizeVariantScope(baseScope);
			return normalizedBase != null ? normalizedBase : TEXT_UNKNOWN;
		}
		return scopeFromPartitionType(type);
	}

	private static @Nullable String normalizeBaseScope(@Nullable String scope) {
		if (scope == null)
			return null;
		// Strip any contributor suffix (e.g., "@org.eclipse.tm4e.language_pack") while preserving variant details
		scope = TMScope.toUnqualified(scope);
		if (scope.startsWith("source.")) {
			final int next = scope.indexOf('.', "source.".length());
			return next > 0 ? scope.substring(0, next) : scope;
		}
		if (scope.startsWith("text.")) {
			final int next = scope.indexOf('.', "text.".length());
			return next > 0 ? scope.substring(0, next) : scope;
		}
		return scope;
	}

	private static @Nullable String normalizeVariantScope(final @Nullable String scope) {
		if (scope == null)
			return null;
		// Strip any contributor suffix (e.g., "@org.eclipse.tm4e.language_pack") while preserving variant details
		return TMScope.toUnqualified(scope);
	}

	private static String scopeFromPartitionType(final @Nullable String partitionType) {
		if (partitionType != null && partitionType.startsWith(TMPartitions.PARTITION_TYPE_PREFIX)
				&& !TMPartitions.BASE_PARTITION_TYPE.equals(partitionType))
			return partitionType.substring(TMPartitions.PARTITION_TYPE_PREFIX.length());
		return TEXT_UNKNOWN;
	}

	/**
	 * Builds a partition type name for a TextMate language scope (e.g. source.js).
	 * If the scope is qualified (e.g. "source.ts@com.example.bundle"), the contributor suffix is stripped.
	 */
	public static String scopeToPartitionType(final String scope) {
		return TMPartitions.PARTITION_TYPE_PREFIX + normalizeBaseScope(scope);
	}

	/**
	 * Partition type used for the document's base language when no embedded scope applies.
	 * Initialized to {@code tm4e:base} and switched to {@code tm4e:<root-scope>} once a grammar is known.
	 */
	private volatile String basePartitionType = TMPartitions.BASE_PARTITION_TYPE;

	/**
	 * Partition index keyed by region start offset. Rules:
	 * <li>Keys grow from left to right; regions do not overlap.
	 * <li>Offsets/lengths use document offsets.
	 * <li>Between indexed regions there can be gaps; these are treated as base type.
	 */
	private final TreeMap<Integer, TMPartitionRegion> partitions = new TreeMap<>();

	/**
	 * Discovered partition types for this document. Semantics:
	 * <li>Always contains {@code basePartitionType} after connect/init.
	 * <li>More types are added when we see embedded languages; cleared on disconnect/init.
	 */
	private final Set<String> legalTypes = new HashSet<>();

	/**
	 * Read/write lock that protects the mutable partition state:
	 * <li>{@link #partitions}: read with the read lock, write with the write lock
	 * <li>{@link #legalTypes}: read with the read lock, write with the write lock
	 * <li>{@link #basePartitionType}: writes happen under the write lock together with the above structures to keep them consistent;
	 * reads may occur without a lock because the field is {@code volatile}
	 */
	private final ReadWriteLock partitionsLock = new ReentrantReadWriteLock();

	private final ModelTokensChangedEvent.Listener modelListener = this::onTokensChanged;

	private volatile boolean activated;
	private final Object activationLock = new Object();

	private volatile @Nullable IDocument document;
	private volatile @Nullable IGrammar grammar;
	private volatile @Nullable TMDocumentModel tmModel;

	/**
	 * Merge segments that are next to each other and have the same type into one region.
	 * <p>
	 * Assumptions:
	 * <li>The input list is sorted by {@code offset} and segments do not overlap.
	 * <li>Segments use document offsets.
	 */
	private List<TMPartitionRegion> coalesce(final List<TMPartitionRegion> segs) {
		if (segs.isEmpty())
			return Collections.emptyList();

		final var result = new ArrayList<TMPartitionRegion>(segs.size());
		TMPartitionRegion prev = segs.get(0);
		for (int i = 1; i < segs.size(); i++) {
			final TMPartitionRegion cur = segs.get(i);
			if (prev.getType().equals(cur.getType()) //
					&& prev.getOffset() + prev.getLength() == cur.getOffset()) {
				prev = new TMPartitionRegion(prev.getOffset(), prev.getLength() + cur.getLength(), prev.getType(), prev.getGrammarScope());
			} else {
				result.add(prev);
				prev = cur;
			}

		}
		result.add(prev);
		return result;
	}

	/**
	 * Utility: true when [start,end) is a non-empty span.
	 */
	private static boolean spans(final int start, final int end) {
		return end > start;
	}

	/**
	 * Utility: add a segment if it spans content and ensure its grammar scope is consistent.
	 */
	private static void addSeg(final List<TMPartitionRegion> out, final int start, final int end, final String type,
			final @Nullable String scope, final @Nullable String baseScope) {
		if (spans(start, end)) {
			out.add(new TMPartitionRegion(start, end - start, type, ensureGrammarScope(type, scope, baseScope)));
		}
	}

	/**
	 * Compute the partitioning for the range {@code [offset, offset+length)}.
	 * <p>
	 * Strategy:
	 * <ol>
	 * <li>Clamp the requested range to the document boundaries.
	 * <li>If no partitions are known, return one base segment covering the range.
	 * <li>Iterate the stored partitions that overlap the range and:
	 * <ul>
	 * <li>Emit a base segment for any gap before the first/next partition.</li>
	 * <li>Emit the overlapping slice of the partition.</li>
	 * </ul>
	 * <li>Emit a trailing base segment for any remaining gap until {@code end}.</li>
	 * </ol>
	 * Result: returned regions always cover the requested range contiguously, using base type for gaps.
	 */
	@Override
	public ITMPartitionRegion[] computePartitioning(final int offset, final int length) {
		ensureActivated();

		partitionsLock.readLock().lock();
		try {
			final var doc = document;
			if (doc == null)
				return new ITMPartitionRegion[] { new TMPartitionRegion(0, 0, basePartitionType, (IGrammar) null) };
			final var grammar = this.grammar;

			// Fast path for empty ranges to keep intent obvious
			if (length <= 0) {
				final int start = Math.clamp(offset, 0, doc.getLength());
				return new ITMPartitionRegion[] { new TMPartitionRegion(start, 0, basePartitionType, grammar) };
			}

			final int docLen = doc.getLength();
			final int start = Math.clamp(offset, 0, docLen);
			final int end = Math.clamp(offset + length, start, docLen);

			if (partitions.isEmpty())
				// no known partitions -> everything is base
				return new ITMPartitionRegion[] { new TMPartitionRegion(start, Math.max(0, end - start), basePartitionType, grammar) };

			final var list = new ArrayList<TMPartitionRegion>();

			int cursor = start;
			// Handle partition that starts before 'start' but overlaps it
			final Map.Entry<Integer, TMPartitionRegion> floor = partitions.floorEntry(start);
			if (floor != null) {
				final TMPartitionRegion r = floor.getValue();
				final int rStart = r.getOffset();
				final int rEnd = rStart + r.getLength();
				if (rStart < start && rEnd > start) {
					final int to = Math.min(end, rEnd);
					if (to > cursor) {
						list.add(new TMPartitionRegion(cursor, to - cursor, r));
						cursor = to;
					}
				}
			}

			// Walk only entries that can overlap [start, end)
			for (final var e : partitions.subMap(start, true, Math.max(start, end - 1), true).entrySet()) {
				if (cursor >= end)
					break;
				final int rStart = e.getKey();
				final TMPartitionRegion r = e.getValue();
				final int rEnd = rStart + r.getLength();

				if (rStart > cursor) {
					final int gapEnd = Math.min(end, rStart);
					if (gapEnd > cursor) {
						list.add(new TMPartitionRegion(cursor, gapEnd - cursor, basePartitionType, grammar));
						cursor = gapEnd;
					}
				}

				if (rEnd > cursor) {
					final int to = Math.min(end, rEnd);
					list.add(new TMPartitionRegion(cursor, to - cursor, r));
					cursor = to;
				}
			}

			// fill trailing base gap
			if (cursor < end) {
				list.add(new TMPartitionRegion(cursor, end - cursor, basePartitionType, grammar));
			}

			if (list.isEmpty())
				return new ITMPartitionRegion[] { new TMPartitionRegion(start, Math.max(0, end - start), basePartitionType, grammar) };
			return list.toArray(ITMPartitionRegion[]::new);
		} finally {
			partitionsLock.readLock().unlock();
		}
	}

	@Override
	public void connect(final IDocument doc) {
		document = doc;
		activated = false;

		// start with no indexed partitions; callers get base type until activation
		partitionsLock.writeLock().lock();
		try {
			partitions.clear();
			legalTypes.clear();
			legalTypes.add(basePartitionType);
		} finally {
			partitionsLock.writeLock().unlock();
		}
	}

	@Override
	public void disconnect() {
		final var model = tmModel;
		if (model != null) {
			model.removeModelTokensChangedListener(modelListener);
		}
		tmModel = null;
		document = null;
		activated = false;
		grammar = null;

		partitionsLock.writeLock().lock();
		try {
			partitions.clear();
			legalTypes.clear();
			basePartitionType = TMPartitions.BASE_PARTITION_TYPE;
		} finally {
			partitionsLock.writeLock().unlock();
		}
	}

	@Override
	public void documentAboutToBeChanged(final DocumentEvent event) {
		// no-op; we react to TM model events to rebuild partitions
	}

	@Override
	public boolean documentChanged(final DocumentEvent event) {
		// if not activated yet, do nothing (base will be provided on demand)
		if (!activated || document == null)
			return false;

		// as a simple strategy, trim partitions from the change offset and mark as base until tokenization catches up
		final int changeStart = Math.max(0, event.getOffset());
		final int replacedLen = Math.max(0, event.getLength()); // length in old doc
		final int addedLen = Math.max(0, event.getText().length());
		final int oldEnd = changeStart + replacedLen; // coordinates in old partitions map
		final int newEnd = changeStart + addedLen;   // coordinates in new document

		// if no effective change, signal no partition updates
		if (replacedLen == 0 && addedLen == 0)
			return false;

		return pruneAndFillBase(changeStart, oldEnd, newEnd);
	}

	/**
	 * Ensure the partitioner is initialized and listening to the TM model.
	 * Does nothing if already activated or no document is connected.
	 */
	private void ensureActivated() {
		if (activated || document == null)
			return;

		synchronized (activationLock) {
			if (activated || document == null)
				return;
			initializeModelAndBase();
			rebuildAll();
			activated = true;
		}
	}

	@Override
	public String getContentType(final int offset) {
		return getPartition(offset).getType();
	}

	/**
	 * Testing-only
	 */
	public @Nullable IGrammar getGrammar() {
		return grammar;
	}

	@Override
	public String[] getLegalContentTypes() {
		ensureActivated();

		partitionsLock.readLock().lock();
		try {
			return legalTypes.toArray(String[]::new);
		} finally {
			partitionsLock.readLock().unlock();
		}
	}

	@Override
	public ITMPartitionRegion getPartition(final int offset) {
		ensureActivated();

		partitionsLock.readLock().lock();
		try {
			final var doc = document;
			if (doc == null)
				return new TMPartitionRegion(0, 0, basePartitionType, grammar);
			final int docLen = doc.getLength();
			if (docLen == 0)
				return new TMPartitionRegion(0, 0, basePartitionType, grammar);

			if (partitions.isEmpty())
				return new TMPartitionRegion(0, docLen, basePartitionType, grammar);

			// clamp offset to [0, docLen-1] to handle EOF and negatives
			final int clamped = Math.clamp(offset, 0, docLen - 1);

			final Map.Entry<Integer, TMPartitionRegion> floor = partitions.floorEntry(clamped);
			if (floor != null) {
				final TMPartitionRegion region = floor.getValue();
				final int regionEnd = region.getOffset() + region.getLength();
				if (clamped >= region.getOffset() && clamped < regionEnd)
					return region;
			}

			// no covering partition found: we are in a base gap.
			// build a base region spanning from the end of the previous region (or 0) to the next region start (or doc end)
			final int baseStart = floor != null ? Math.max(0, floor.getValue().getOffset() + floor.getValue().getLength()) : 0;
			final Map.Entry<Integer, TMPartitionRegion> next = partitions.ceilingEntry(clamped);
			final int baseEnd = next != null ? next.getKey() : docLen;
			return new TMPartitionRegion(baseStart, Math.max(0, baseEnd - baseStart), basePartitionType, grammar);
		} finally {
			partitionsLock.readLock().unlock();
		}
	}

	private void initializeModelAndBase() {
		final var doc = Objects.requireNonNull(document);
		final TMDocumentModel model = TMModelManager.INSTANCE.connect(doc);
		tmModel = model;

		// Prefer an existing model grammar to stay consistent with other clients (e.g., presenter)
		final IGrammar modelGrammar = model.getGrammar();
		if (modelGrammar != null) {
			grammar = modelGrammar;
		} else if (grammar == null) {
			// resolve grammar if not forced
			grammar = GrammarUtils.findGrammar(doc);
		}

		final var grammar = this.grammar;
		if (grammar != null) {
			model.setGrammar(grammar);
			partitionsLock.writeLock().lock();
			try {
				basePartitionType = scopeToPartitionType(grammar.getScopeName());
				legalTypes.clear();
				legalTypes.add(basePartitionType);
			} finally {
				partitionsLock.writeLock().unlock();
			}
		} else {
			partitionsLock.writeLock().lock();
			try {
				basePartitionType = TMPartitions.BASE_PARTITION_TYPE;
				legalTypes.clear();
				legalTypes.add(basePartitionType);
			} finally {
				partitionsLock.writeLock().unlock();
			}
		}

		model.addModelTokensChangedListener(modelListener);
	}

	/**
	 * Add {@code newSegs} into the current partition map.
	 * <p>
	 * Strategy:
	 * <ol>
	 * <li>Adjust the partition that crosses the left boundary.
	 * <li>Remove all partitions that start inside [startOffset, endOffset).
	 * <li>Adjust the partition that crosses the right boundary.
	 * <li>Insert {@code newSegs}; if a new segment touches a neighbour with the same type, merge them.
	 * </ol>
	 * Assumptions:
	 * <ol>
	 * <li>{@code newSegs} are sorted by offset and do not overlap.
	 * <li>Offsets use document positions and lie within [startOffset, endOffset].
	 * </ol>
	 */
	private void integratePartitions(final int startOffset, final int endOffset, final List<TMPartitionRegion> newSegs) {
		// capture right-crossing region BEFORE mutating the map so we can rebuild its right remainder later
		TMPartitionRegion rightCrossing = null;
		int rightCrossingEnd = -1;
		final Map.Entry<Integer, TMPartitionRegion> rightCand = partitions.floorEntry(endOffset);
		if (rightCand != null) {
			final TMPartitionRegion region = rightCand.getValue();
			final int regionEnd = region.getOffset() + region.getLength();
			if (region.getOffset() < endOffset && regionEnd > endOffset) {
				rightCrossing = region;
				rightCrossingEnd = regionEnd;
			}
		}

		// prepare left remainder if any
		final Map.Entry<Integer, TMPartitionRegion> left = partitions.floorEntry(startOffset);
		if (left != null) {
			final TMPartitionRegion region = left.getValue();
			final int regionEnd = region.getOffset() + region.getLength();
			if (regionEnd > startOffset) {
				// overlap -> replace with left remainder
				partitions.remove(left.getKey());
				if (region.getOffset() < startOffset) {
					partitions.put(region.getOffset(), new TMPartitionRegion(region.getOffset(), startOffset - region.getOffset(), region));
				}
			}
		}

		// drop all entries starting in [startOffset, endOffset) using a bounded view
		final var bounded = partitions.subMap(startOffset, true, endOffset, false);
		if (!bounded.isEmpty()) {
			bounded.clear();
		}

		// recreate right remainder if a region originally crossed endOffset
		if (rightCrossing != null) {
			partitions.put(endOffset, new TMPartitionRegion(endOffset, rightCrossingEnd - endOffset, rightCrossing));
		}

		// insert new segments; merge with neighbours of the same type when they touch (both sides)
		for (final TMPartitionRegion seg : newSegs) {
			int newStart = seg.getOffset();
			int newLen = seg.getLength();
			final String type = seg.getType();
			final String grammarScope = seg.getGrammarScope();

			// merge with previous neighbour if it touches and has same type
			final Map.Entry<Integer, TMPartitionRegion> prev = partitions.floorEntry(newStart);
			if (prev != null) {
				final TMPartitionRegion pr = prev.getValue();
				final int pEnd = pr.getOffset() + pr.getLength();
				if (pEnd == newStart && pr.getType().equals(type)) {
					newStart = pr.getOffset();
					newLen += pr.getLength();
					partitions.remove(prev.getKey());
				}
			}

			// merge repeatedly with following neighbours while contiguous and of same type
			while (true) {
				final int expectedNextStart = newStart + newLen;
				final TMPartitionRegion nr = partitions.get(expectedNextStart);
				if (nr != null && nr.getType().equals(type)) {
					newLen += nr.getLength();
					partitions.remove(expectedNextStart);
					continue;
				}
				break;
			}

			partitions.put(newStart, new TMPartitionRegion(newStart, newLen, type, grammarScope));
		}
	}

	/**
	 * Reacts to token changes from the TM model.
	 * <p>
	 * Strategy:
	 * <li>Merges overlapping or adjacent line-based ranges from {@code event.ranges} to minimize recomputation.</li>
	 * <li>For each merged line range, converts it to document offsets and calls {@link #recomputeRange(int, int)}.</li>
	 * Errors resolving line offsets are logged.
	 */
	private void onTokensChanged(final ModelTokensChangedEvent event) {
		final var doc = document;
		if (doc == null)
			return;

		try {
			// Merge overlapping or adjacent line ranges to reduce recomputation and lock churn
			if (event.ranges.isEmpty())
				return;

			final var ranges = new ArrayList<int[]>(event.ranges.size());
			for (final Range r : event.ranges) {
				final int s = Math.max(0, r.fromLineNumber - 1);
				final int e = Math.max(s, r.toLineNumber - 1);
				ranges.add(new int[] { s, e });
			}

			ranges.sort((a, b) -> Integer.compare(a[0], b[0]));

			int curS = ranges.get(0)[0];
			int curE = ranges.get(0)[1];
			for (int i = 1; i < ranges.size(); i++) {
				final int s = ranges.get(i)[0];
				final int e = ranges.get(i)[1];
				if (s <= curE + 1) { // overlap or adjacent
					if (e > curE) {
						curE = e;
					}
					continue;
				}
				// flush current merged range
				final int startOffset = doc.getLineOffset(curS);
				final int endOffset = doc.getLineOffset(curE) + doc.getLineLength(curE);
				recomputeRange(startOffset, endOffset);

				// start new merged range
				curS = s;
				curE = e;
			}
			// flush last merged range
			final int startOffset = doc.getLineOffset(curS);
			final int endOffset = doc.getLineOffset(curE) + doc.getLineLength(curE);
			recomputeRange(startOffset, endOffset);
		} catch (final BadLocationException ex) {
			TMUIPlugin.logError(ex);
		}
	}

	/**
	 * Precisely reset only the changed span to base and maintain correctness for partitions to the right.
	 * <p>
	 * Why: when a user edits text, only the modified span becomes potentially invalid. We want to:
	 * <li>discard stale embedded partitions within the modified range,</li>
	 * <li>keep partitions to the left untouched,</li>
	 * <li>preserve partitions to the right by shifting them by the edit delta so their offsets remain correct,</li>
	 * <li>and bridge the changed span with a single base region until the tokenizer recomputes tokens.</li>
	 * <p>
	 * Steps:
	 * <ol>
	 * <li>Normalize inputs and compute {@code delta = newEndOffset - oldEndOffset}.</li>
	 * <li>Capture any partition that crosses {@code oldEndOffset} so its right remainder can be restored at {@code newEndOffset}.</li>
	 * <li>Collect all partitions starting at or after {@code oldEndOffset} to shift them by {@code delta} later.</li>
	 * <li>Remove all partitions whose start lies within {@code [startOffset, oldEndOffset)}.</li>
	 * <li>If a partition crosses {@code startOffset}, shrink it to end at {@code startOffset}.</li>
	 * <li>Shift all collected right-side partitions by {@code delta} so they retain their logical position after the edit.</li>
	 * <li>Recreate the captured right remainder at {@code newEndOffset} (keeping its original type and length).</li>
	 * <li>Insert a single base partition covering {@code [startOffset, newEndOffset)}.</li>
	 * </ol>
	 * Assumptions:
	 * <ol>
	 * <li>{@code startOffset}, {@code oldEndOffset}, {@code newEndOffset} refer to document offsets; {@code oldEndOffset}
	 * is measured on the pre-edit content, {@code newEndOffset} on the post-edit content.</li>
	 * <li>Offsets are clamped internally to ensure {@code start <= oldEnd} and {@code start <= newEnd}.</li>
	 * <li>Calling code provides consistent offsets derived from a single {@link DocumentEvent}.</li>
	 * </ol>
	 * Result: partitions to the left remain intact; the changed span is a base bridge; partitions to the right keep their
	 * original types and relative order with updated offsets. Subsequent tokenization will refine the base span.
	 *
	 * @param startOffset start of changed span (old and new doc)
	 * @param oldEndOffset end of replaced span in the old doc coordinates
	 * @param newEndOffset end of inserted span in the new doc coordinates
	 *
	 * @return true if the partition map was modified; false otherwise
	 */
	private boolean pruneAndFillBase(final int startOffset, final int oldEndOffset, final int newEndOffset) {
		partitionsLock.writeLock().lock();
		try {
			final var grammar = this.grammar;
			boolean changed = false;
			final int boundedStart = Math.clamp(startOffset, 0, oldEndOffset);
			final int boundedOldEnd = Math.max(boundedStart, oldEndOffset);
			final int boundedNewEnd = Math.max(boundedStart, newEndOffset);
			final int delta = boundedNewEnd - boundedOldEnd;

			if (partitions.isEmpty()) {
				// Only add a base region when there is an actual span to bridge
				if (boundedNewEnd > boundedStart) {
					partitions.put(boundedStart,
							new TMPartitionRegion(boundedStart, boundedNewEnd - boundedStart, basePartitionType, grammar));
					return true;
				}
				return false;
			}

			// capture right-crossing region BEFORE mutating the map so we can rebuild its right remainder later
			TMPartitionRegion rightCrossing = null;
			int rightCrossingEnd = -1;
			final Map.Entry<Integer, TMPartitionRegion> rightCand = partitions.floorEntry(boundedOldEnd);
			if (rightCand != null) {
				final TMPartitionRegion region = rightCand.getValue();
				final int regionEnd = region.getOffset() + region.getLength();
				if (region.getOffset() < boundedOldEnd && regionEnd > boundedOldEnd) {
					rightCrossing = region;
					rightCrossingEnd = regionEnd;
				}
			}

			// remove entries starting within [boundedStart, boundedOldEnd) using a bounded view
			final var toDrop = partitions.subMap(boundedStart, true, boundedOldEnd, false);
			if (!toDrop.isEmpty()) {
				toDrop.clear();
				changed = true;
			}

			// adjust possible left partition that overlaps startOffset
			final Map.Entry<Integer, TMPartitionRegion> left = partitions.floorEntry(boundedStart);
			if (left != null) {
				final TMPartitionRegion region = left.getValue();
				final int regionEnd = region.getOffset() + region.getLength();
				if (regionEnd > boundedStart) {
					partitions.put(region.getOffset(),
							new TMPartitionRegion(region.getOffset(), boundedStart - region.getOffset(), region));
					changed = true;
				}
			}

			// shift tail entries (>= oldEnd) by delta so they keep their position
			if (delta != 0) {
				final var tailView = partitions.tailMap(boundedOldEnd, true);
				if (!tailView.isEmpty()) {
					final var shifted = new TreeMap<Integer, TMPartitionRegion>();
					for (final var e : tailView.entrySet()) {
						final int oldKey = e.getKey();
						final TMPartitionRegion region = e.getValue();
						final int newStart = oldKey + delta;
						shifted.put(newStart, new TMPartitionRegion(newStart, region.getLength(), region));
					}
					// remove all tail entries in one shot and insert shifted ones
					tailView.clear();
					partitions.putAll(shifted);
					changed = true;
				}
			}

			// recreate right remainder if a region originally crossed oldEnd
			if (rightCrossing != null) {
				partitions.put(boundedNewEnd, new TMPartitionRegion(boundedNewEnd, rightCrossingEnd - boundedOldEnd, rightCrossing));
				changed = true;
			}

			// add a base region spanning [boundedStart, boundedNewEnd)
			if (boundedNewEnd > boundedStart) {
				partitions.put(boundedStart, new TMPartitionRegion(boundedStart, boundedNewEnd - boundedStart, basePartitionType, grammar));
				changed = true;
			}
			return changed;
		} finally {
			partitionsLock.writeLock().unlock();
		}
	}

	private void rebuildAll() {
		final var model = tmModel;
		final var doc = document;
		// if tokenization is still in progress, keep base-only until events arrive
		if (model == null || doc == null || model.getBackgroundTokenizationState() != BackgroundTokenizationState.COMPLETED)
			return;

		try {
			recomputeRange(0, doc.getLength());
		} catch (final BadLocationException ex) {
			TMUIPlugin.logTrace(ex);
		}
	}

	/**
	 * Rebuild partitions for the text range [startOffset, endOffset) (end not included).
	 * <p>
	 * Strategy and invariants:
	 * <li>Iterate the model line by line and normalize each token's grammar scope at most once per token.
	 * <li>Prefer embedded scopes (e.g., {@code source.*}) over base scopes (e.g., {@code text.*}).
	 * <li>Precompute whether a line contains any embedded token to avoid nested lookahead scans in the hot path
	 * (indentation case: base token at column 0 followed by embedded content later on the same line).
	 * <li>Build a minimal list of contiguous segments, then coalesce and integrate into the tree under a write lock.
	 */
	private void recomputeRange(final int startOffset, final int endOffset) throws BadLocationException {
		final var model = tmModel;
		final var doc = document;
		// Nothing to recompute for an empty range
		if (model == null || doc == null || endOffset <= startOffset)
			return;

		final var grammar = this.grammar;
		final String baseScope = grammar != null ? normalizeVariantScope(grammar.getScopeName()) : null;
		final String baseRoot = normalizeBaseScope(baseScope);
		final String baseRootNN = baseRoot != null ? baseRoot : TEXT_UNKNOWN;

		// Build new segments for the changed range
		final var newSegs = new ArrayList<TMPartitionRegion>();

		final int startLine = doc.getLineOfOffset(startOffset);
		final int endLine = doc.getLineOfOffset(endOffset - 1);

		String currentType = null;
		String currentGrammarScopeStr = null;
		int currentStart = startOffset;

		for (int line = startLine; line <= endLine; line++) {
			final int lineOffset = doc.getLineOffset(line);
			final int lineEnd = lineOffset + doc.getLineLength(line);
			final List<TMToken> tokens = model.getLineTokens(line);

			if (tokens == null || tokens.isEmpty()) {
				// No tokens for this line: extend current segment using base when not initialized yet
				if (currentType == null) {
					currentType = basePartitionType;
					currentGrammarScopeStr = baseRootNN;
					// extend segment to end of line
					// currentStart remains unchanged
				}
			} else {
				// Precompute embedded token boundaries on this line to reduce nested lookahead work.
				// We keep both the first and the last embedded token start indices so we can
				// treat transient base tokens between embedded tokens as part of the embedded run.
				int firstEmbeddedStart = -1;
				int lastEmbeddedStart = -1;
				for (final TMToken t : tokens) {
					final String prefNorm = t.grammarScope == null ? null : normalizeVariantScope(t.grammarScope);
					if (prefNorm != null && !Objects.equals(normalizeBaseScope(prefNorm), baseRootNN)) {
						if (firstEmbeddedStart < 0)
							firstEmbeddedStart = t.startIndex;
						lastEmbeddedStart = t.startIndex;
					}
				}

				for (final TMToken tok : tokens) {
					final String prefNorm = tok.grammarScope == null ? null : normalizeVariantScope(tok.grammarScope);
					final boolean isBase = prefNorm == null || Objects.equals(normalizeBaseScope(prefNorm), baseRootNN);
					final String root = isBase ? basePartitionType : scopeToPartitionType(prefNorm != null ? prefNorm : baseRootNN);

					if (currentType == null) {
						currentType = root;
						final boolean isEmbedded = !isBase;
						final int firstTokenStart = lineOffset + tok.startIndex;
						currentStart = Math.max(currentStart, isEmbedded ? lineOffset : firstTokenStart);
						currentGrammarScopeStr = isBase ? baseRootNN : prefNorm;
						continue;
					}

					if (!currentType.equals(root)) {
						// While inside an embedded run, ignore base tokens that appear
						// either at the start of the line (indentation) when another embedded token exists later,
						// or anywhere before the last embedded token on this line (transient gaps between embedded tokens).
						if (!currentType.equals(basePartitionType) && basePartitionType.equals(root)) {
							final boolean baseAtLineStartWithEmbedLater = tok.startIndex == 0 && firstEmbeddedStart > 0;
							final boolean baseBeforeLastEmbedded = lastEmbeddedStart > 0 && tok.startIndex <= lastEmbeddedStart;
							if (baseAtLineStartWithEmbedLater || baseBeforeLastEmbedded) {
								// keep currentType and currentStart as-is
								continue;
							}
						}
						// Token type changed: finalize previous segment and start a new one
						final int segEnd = lineOffset + tok.startIndex;
						addSeg(newSegs, currentStart, segEnd, currentType, currentGrammarScopeStr, baseScope);
						currentType = root;
						currentStart = segEnd;
						currentGrammarScopeStr = isBase ? baseRootNN : prefNorm;
					}
				}
			}

			// at end of line: make sure currentType is set so we can close the last segment
			if (currentType == null) {
				currentType = basePartitionType;
				currentGrammarScopeStr = baseRootNN;
			}

			// if the next line has tokens we continue the current run; we flush at endLine
			if (line == endLine) {
				// close the last segment at endOffset limit
				final int finalEnd = Math.min(lineEnd, endOffset);
				addSeg(newSegs, currentStart, finalEnd, currentType, currentGrammarScopeStr, baseScope);
			}
		}

		if (newSegs.isEmpty() && endOffset > startOffset) {
			// fallback: at least one base segment covering the requested range
			addSeg(newSegs, startOffset, endOffset, basePartitionType, baseScope, baseScope);
		}

		// merge new segments that are next to each other and have the same type
		final List<TMPartitionRegion> merged = coalesce(newSegs);
		// add them to the map: remove overlaps and keep the other parts
		final Set<String> newTypes = new HashSet<>();
		for (final TMPartitionRegion r : merged) {
			newTypes.add(r.getType());
		}
		partitionsLock.writeLock().lock();
		try {
			legalTypes.addAll(newTypes);
			integratePartitions(startOffset, endOffset, merged);
		} finally {
			partitionsLock.writeLock().unlock();
		}
	}

	/**
	 * Testing-only: forces the grammar for the current connection.
	 * Production code should rely on automatic grammar resolution.
	 * Cleared on disconnect; does not persist across connections.
	 *
	 * @noreference
	 */
	public void setGrammar(final @Nullable IGrammar newGrammar) {
		grammar = newGrammar;
		if (document != null) {
			initializeModelAndBase();
			rebuildAll();
		}
	}
}
