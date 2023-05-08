/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.ui.internal.utils;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.model.ITMModel;
import org.eclipse.tm4e.core.model.ModelTokensChangedEvent;
import org.eclipse.tm4e.core.model.TMToken;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.model.TMDocumentModel;
import org.eclipse.tm4e.ui.internal.preferences.PreferenceHelper;

public final class MarkerUtils {

	private static final String TEXTMARKER_TYPE = "org.eclipse.tm4e.ui.textmarker";
	private static final String PROBLEMMARKER_TYPE = "org.eclipse.tm4e.ui.problemmarker";
	private static final String TASKMARKER_TYPE = "org.eclipse.tm4e.ui.taskmarker";

	private static final Map<String, MarkerConfig> MARKERCONFIG_BY_TAG = new HashMap<>();
	private static Pattern TAG_SELECTOR_PATTERN = lazyNonNull();

	static {
		reloadMarkerConfigs();
	}

	public static void reloadMarkerConfigs() {
		synchronized (MARKERCONFIG_BY_TAG) {
			MARKERCONFIG_BY_TAG.clear();
			for (final var markerConfig : PreferenceHelper.loadMarkerConfigs()) {
				MARKERCONFIG_BY_TAG.put(markerConfig.tag, markerConfig);
			}
			TAG_SELECTOR_PATTERN = Pattern
					.compile("^\\s*(" + MARKERCONFIG_BY_TAG.keySet().stream().collect(Collectors.joining("|")) + ")\\b");
		}
	}

	/**
	 * Updates all TM4E text markers of the corresponding document starting from
	 * <code>event.ranges.get(0).fromLineNumber</code> until the end of the document.
	 */
	public static void updateTextMarkers(final ModelTokensChangedEvent event) {
		final ITMModel model = event.model;
		if (model instanceof final TMDocumentModel docModel) {
			try {
				updateTextMarkers(docModel, event.ranges.get(0).fromLineNumber);
			} catch (final CoreException ex) {
				TMUIPlugin.logError(ex);
			}
		}
	}

	/**
	 * Updates all TM4E text markers of the {@link TMDocumentModel}'s document starting from the given line number until
	 * the end of the document.
	 *
	 * @param startLineNumber 1-based
	 */
	private static void updateTextMarkers(final TMDocumentModel docModel, final int startLineNumber)
			throws CoreException {

		final var doc = docModel.getDocument();

		final var res = ResourceUtils.findResource(doc);
		if (res == null)
			return;

		final var numberOfLines = docModel.getNumberOfLines();

		// collect affected markers
		final var markers = new HashMap<Integer, List<IMarker>>();
		for (final var marker : res.findMarkers(TEXTMARKER_TYPE, true, 0)) {
			final var lineNumberObj = getLineNumber(marker);
			if (lineNumberObj == null) {
				marker.delete(); // this marker is missing line information, should never happen
				continue;
			}

			final var lineNumber = lineNumberObj.intValue();
			if (lineNumber < startLineNumber) {
				continue; // this marker needs no update
			}
			if (lineNumber > numberOfLines) {
				marker.delete(); // this marker is for a non-existing line
				continue;
			}
			final var markersOfLine = markers.computeIfAbsent(lineNumberObj, l -> new ArrayList<>(1));
			markersOfLine.add(marker);
		}

		// iterate over all lines
		for (int lineNumber = startLineNumber; lineNumber <= numberOfLines; lineNumber++) {
			final var lineNumberObj = Integer.valueOf(lineNumber);
			final var lineIndex = lineNumber - 1;
			final var tokens = castNonNull(docModel.getLineTokens(lineIndex));
			final var tokensCount = tokens.size();
			final var outdatedMarkers = markers.getOrDefault(lineNumberObj, Collections.emptyList());

			// iterate over all tokens of the current line
			for (int tokenIndex = 0; tokenIndex < tokensCount; tokenIndex++) {
				final var token = tokens.get(tokenIndex);

				if (!token.type.contains("comment") || token.type.contains("definition"))
					continue;

				final TMToken nextToken = tokenIndex + 1 < tokensCount ? tokens.get(tokenIndex + 1) : null;
				try {
					final var lineOffset = doc.getLineOffset(lineIndex);
					final var commentText = doc.get(
							lineOffset + token.startIndex,
							(nextToken == null ? doc.getLineLength(lineIndex) : nextToken.startIndex) - token.startIndex);
					if (commentText.length() < 3)
						continue;

					final var matcher = MarkerUtils.TAG_SELECTOR_PATTERN.matcher(commentText);
					if (!matcher.find())
						continue;

					final var markerConfig = MarkerUtils.MARKERCONFIG_BY_TAG.get(matcher.group(1));
					final var markerText = commentText.substring(matcher.start()).trim();

					final var attrs = new HashMap<String, Object>();
					attrs.put(IMarker.LINE_NUMBER, lineNumberObj);
					attrs.put(IMarker.MESSAGE, markerText);
					switch (markerConfig.type) {
						case PROBLEM -> attrs.put(IMarker.SEVERITY, markerConfig.asProblemMarkerConfig().severity.value);
						case TASK -> attrs.put(IMarker.PRIORITY, markerConfig.asTaskMarkerConfig().priority.value);
					}
					attrs.put(IMarker.USER_EDITABLE, Boolean.FALSE);
					attrs.put(IMarker.SOURCE_ID, "TM4E");

					// only create a new marker if no matching marker already exists
					String markerTypeId = switch (markerConfig.type) {
						case PROBLEM -> PROBLEMMARKER_TYPE;
						case TASK -> TASKMARKER_TYPE;
					};
					if (!removeMatchingMarker(outdatedMarkers, markerTypeId, attrs)) {
						final var markerTextStartOffset = lineOffset + token.startIndex + matcher.start();
						attrs.put(IMarker.CHAR_START, markerTextStartOffset);
						attrs.put(IMarker.CHAR_END, markerTextStartOffset + markerText.length());
						res.createMarker(markerTypeId, attrs);
					}
				} catch (final Exception ex) {
					TMUIPlugin.logError(ex);
				}
			}

			// remove any obsolete markers
			if (!outdatedMarkers.isEmpty()) {
				for (final var marker : outdatedMarkers) {
					marker.delete();
				}
			}
		}
	}

	@Nullable
	private static Integer getLineNumber(final IMarker marker) {
		try {
			final var lineNumberAttr = marker.getAttribute(IMarker.LINE_NUMBER);
			if (lineNumberAttr instanceof final Integer lineNumber)
				return lineNumber;
		} catch (final CoreException ex) {
			TMUIPlugin.logError(ex);
		}
		return null;
	}

	/**
	 * Removes a matching marker from the given list.
	 *
	 * @return true if a matching marker was found and removed.
	 */
	private static boolean removeMatchingMarker(final List<IMarker> markers, final String type,
			final Map<String, ?> attributes) throws CoreException {
		if (markers.isEmpty())
			return false;

		for (final var it = markers.iterator(); it.hasNext();) {
			final var marker = it.next();
			if (!marker.getType().equals(type))
				continue;

			boolean hasMatchingAttrs = true;
			for (final var attr : attributes.entrySet()) {
				if (!Objects.equals(marker.getAttribute(attr.getKey()), attr.getValue())) {
					hasMatchingAttrs = false;
					break;
				}
			}

			if (hasMatchingAttrs) {
				it.remove();
				return true;
			}
		}
		return false;
	}

	/**
	 * private constructor to prevent instantiation of utility class
	 */
	private MarkerUtils() {
	}
}
