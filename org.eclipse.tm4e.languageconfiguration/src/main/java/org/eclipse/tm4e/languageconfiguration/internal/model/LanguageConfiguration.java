/**
 * Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Lucas Bullen (Red Hat Inc.) - initial API and implementation
 * Sebastian Thomschke (Vegard IT) - improve JSON parsing tolerance
 */
package org.eclipse.tm4e.languageconfiguration.internal.model;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.internal.model.EnterAction.IndentAction;
import org.eclipse.tm4e.languageconfiguration.internal.utils.RegExpUtils;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The language configuration interface defines the contract between extensions and various editor features, like
 * automatic bracket insertion, automatic indentation etc.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/languageConfiguration.ts">
 *      github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/languageConfiguration.ts</a>
 */
public class LanguageConfiguration {

	private static String removeTrailingCommas(String jsonString) {
		/* matches:
		 * --------------
		 * },
		 *    }
		 * --------------
		 * as well as:
		 * --------------
		 * },
		 *   // foo
		 *   // bar
		 *    }
		 * --------------
		 */
		return jsonString.replaceAll("(,)(\\s*\\n(\\s*\\/\\/.*\\n)*\\s*})", "$2");
	}

	/**
	 * See JSON format at https://code.visualstudio.com/api/language-extensions/language-configuration-guide
	 *
	 * @return an instance of {@link LanguageConfiguration} loaded from the VSCode language-configuration.json file
	 *         reader.
	 */
	@NonNullByDefault({})
	@Nullable
	public static LanguageConfiguration load(@NonNull final Reader reader) {
		// GSON does not support trailing commas so we have to manually remove them -> maybe better switch to jackson json parser?
		final var jsonString = removeTrailingCommas(new BufferedReader(reader).lines().collect(Collectors.joining("\n")));
		return new GsonBuilder()
				.registerTypeAdapter(String.class, (JsonDeserializer<String>) (json, typeOfT, context) -> {
					if (json.isJsonObject()) {
						/* for example:
						 * "wordPattern": {
						 *   "pattern": "...",
						 *   "flags": "..."
						 * },
						 */
						final var jsonObj = json.getAsJsonObject();
						return jsonObj.has("pattern") && jsonObj.get("pattern").isJsonPrimitive() //
								? jsonObj.get("pattern").getAsString()
								: null;
					}

					/* for example:
					 * "wordPattern": "...",
					 */
					return json.getAsString();
				})

				.registerTypeAdapter(OnEnterRule.class, (JsonDeserializer<OnEnterRule>) (json, typeOfT, context) -> {
					if (!json.isJsonObject()) {
						return null;
					}

					final var jsonObj = json.getAsJsonObject();
					final var beforeText = getAsPattern(jsonObj.get("beforeText")); //$NON-NLS-1$
					if (beforeText == null) {
						return null;
					}

					final var actionElem = jsonObj.get("action"); //$NON-NLS-1$
					if (actionElem != null && actionElem.isJsonObject()) {
						final var actionJsonObj = actionElem.getAsJsonObject();
						final var indentActionString = getAsString(actionJsonObj.get("indent")); //$NON-NLS-1$
						if (indentActionString != null) {
							final var afterText = getAsPattern(jsonObj.get("afterText")); //$NON-NLS-1$
							final var previousLineText = getAsPattern(jsonObj.get("previousLineText")); //$NON-NLS-1$
							final var indentAction = IndentAction.get(indentActionString);
							final var removeText = getAsInteger(actionJsonObj.get("removeText")); //$NON-NLS-1$
							final var appendText = getAsString(actionJsonObj.get("appendText")); //$NON-NLS-1$
							final var action = new EnterAction(indentAction);
							action.appendText = appendText;
							action.removeText = removeText;
							return new OnEnterRule(beforeText, afterText, previousLineText, action);
						}
					}
					return null;
				})

				.registerTypeAdapter(CommentRule.class, (JsonDeserializer<CommentRule>) (json, typeOfT, context) -> {
					if (!json.isJsonObject()) {
						return null;
					}

					// ex: {"lineComment": "//","blockComment": [ "/*", "*/" ]}
					final var jsonObj = json.getAsJsonObject();
					final var lineComment = getAsString(jsonObj.get("lineComment")); //$NON-NLS-1$
					final var blockCommentElem = jsonObj.get("blockComment"); //$NON-NLS-1$
					CharacterPair blockComment = null;
					if (blockCommentElem != null && blockCommentElem.isJsonArray()) {
						final var blockCommentArray = blockCommentElem.getAsJsonArray();
						if (blockCommentArray.size() == 2) {
							final var blockCommentStart = getAsString(blockCommentArray.get(0));
							final var blockCommentEnd = getAsString(blockCommentArray.get(1));
							if (blockCommentStart != null && blockCommentEnd != null) {
								blockComment = new CharacterPair(blockCommentStart, blockCommentEnd);
							}
						}
					}

					return lineComment == null && blockComment == null
							? null
							: new CommentRule(lineComment, blockComment);
				})

				.registerTypeAdapter(CharacterPair.class, (JsonDeserializer<CharacterPair>) (json, typeOfT,
						context) -> {
					if (!json.isJsonArray()) {
						return null;
					}

					// ex: ["{","}"]
					final var charsPair = json.getAsJsonArray();
					if (charsPair.size() != 2) {
						return null;
					}

					final var open = getAsString(charsPair.get(0));
					final var close = getAsString(charsPair.get(1));

					return open == null || close == null
							? null
							: new CharacterPair(open, close);
				})

				.registerTypeAdapter(AutoClosingPair.class, (JsonDeserializer<AutoClosingPair>) (json, typeOfT,
						context) -> {
					String open = null;
					String close = null;
					if (json.isJsonArray()) {
						// ex: ["{","}"]
						final var charsPair = json.getAsJsonArray();
						if (charsPair.size() != 2) {
							return null;
						}
						open = getAsString(charsPair.get(0));
						close = getAsString(charsPair.get(1));
					} else if (json.isJsonObject()) {
						// ex: {"open":"'","close":"'", "notIn": ["string", "comment"]}
						final var autoClosePair = json.getAsJsonObject();
						open = getAsString(autoClosePair.get("open")); //$NON-NLS-1$
						close = getAsString(autoClosePair.get("close")); //$NON-NLS-1$
					}

					return open == null || close == null
							? null
							: new AutoClosingPair(open, close);
				})

				.registerTypeAdapter(AutoClosingPairConditional.class, (JsonDeserializer<AutoClosingPairConditional>) (
						json, typeOfT, context) -> {
					final var notInList = new ArrayList<String>(2);
					String open = null;
					String close = null;
					if (json.isJsonArray()) {
						// ex: ["{","}"]
						final var charsPair = json.getAsJsonArray();
						if (charsPair.size() != 2) {
							return null;
						}
						open = getAsString(charsPair.get(0));
						close = getAsString(charsPair.get(1));
					} else if (json.isJsonObject()) {
						// ex: {"open":"'","close":"'", "notIn": ["string", "comment"]}
						final var autoClosePair = json.getAsJsonObject();
						open = getAsString(autoClosePair.get("open")); //$NON-NLS-1$
						close = getAsString(autoClosePair.get("close")); //$NON-NLS-1$
						final var notInElem = autoClosePair.get("notIn"); //$NON-NLS-1$
						if (notInElem != null && notInElem.isJsonArray()) {
							notInElem.getAsJsonArray().forEach(element -> {
								final var string = getAsString(element);
								if (string != null) {
									notInList.add(string);
								}
							});
						}
					}

					return open == null || close == null
							? null
							: new AutoClosingPairConditional(open, close, notInList);
				})

				.registerTypeAdapter(FoldingRules.class, (JsonDeserializer<FoldingRules>) (json, typeOfT, context) -> {
					if (!json.isJsonObject()) {
						return null;
					}

					// ex: {"offSide": true, "markers": {"start": "^\\s*/", "end": "^\\s*"}}
					final var jsonObj = json.getAsJsonObject();
					final var markersElem = jsonObj.get("markers"); //$NON-NLS-1$
					if (markersElem != null && markersElem.isJsonObject()) {
						final var markersObj = markersElem.getAsJsonObject();
						final var startMarker = getAsPattern(markersObj.get("start")); //$NON-NLS-1$
						final var endMarker = getAsPattern(markersObj.get("end")); //$NON-NLS-1$
						if (startMarker != null && endMarker != null) {
							final var offSide = getAsBoolean(jsonObj.get("offSide"), false); //$NON-NLS-1$
							return new FoldingRules(offSide, startMarker, endMarker);
						}
					}
					return null;
				})
				.create()
				.fromJson(jsonString, LanguageConfiguration.class);
	}

	@Nullable
	private static Pattern getAsPattern(@Nullable final JsonElement element) {
		final var pattern = getPattern(element);
		return pattern == null ? null : RegExpUtils.create(pattern);
	}

	@Nullable
	private static String getPattern(@Nullable final JsonElement element) {
		if (element == null) {
			return null;
		}
		if (element.isJsonObject()) {
			// ex : { "pattern": "^<\\/([_:\\w][_:\\w-.\\d]*)\\s*>", "flags": "i" }
			final var pattern = getAsString(((JsonObject) element).get("pattern"));
			if (pattern == null) {
				return null;
			}
			final var flags = getAsString(((JsonObject) element).get("flags"));
			return flags != null ? pattern + "(?" + flags + ")" : pattern;
		}
		// ex : "^<\\/([_:\\w][_:\\w-.\\d]*)\\s*>"
		return getAsString(element);
	}

	@Nullable
	private static String getAsString(@Nullable final JsonElement element) {
		if (element == null) {
			return null;
		}
		try {
			return element.getAsString();
		} catch (final Exception e) {
			return null;
		}
	}

	private static boolean getAsBoolean(@Nullable final JsonElement element, final boolean defaultValue) {
		if (element == null) {
			return defaultValue;
		}
		try {
			return element.getAsBoolean();
		} catch (final Exception e) {
			return defaultValue;
		}
	}

	@Nullable
	private static Integer getAsInteger(@Nullable final JsonElement element) {
		if (element == null) {
			return null;
		}
		try {
			return element.getAsInt();
		} catch (final Exception e) {
			return null;
		}
	}

	@Nullable
	private CommentRule comments;

	/**
	 * Returns the language's comments. The comments are used by {@link AutoClosingPairConditional} when
	 * <code>notIn</code> contains <code>comment</code>
	 *
	 * @return the language's comments.
	 */
	@Nullable
	public CommentRule getComments() {
		return comments;
	}

	@Nullable
	private List<CharacterPair> brackets;

	/**
	 * Returns the language's brackets. This configuration implicitly affects pressing Enter around these brackets.
	 *
	 * @return the language's brackets
	 */
	@Nullable
	public List<CharacterPair> getBrackets() {
		return brackets;
	}

	@Nullable
	private String wordPattern;

	/**
	 * Returns the language's definition of a word. This is the regex used when referring to a word.
	 *
	 * @return the language's word pattern.
	 */
	@Nullable
	public String getWordPattern() {
		return wordPattern;
	}

	// TODO @Nullable IndentionRule getIndentionRules();

	@Nullable
	private List<OnEnterRule> onEnterRules;

	/**
	 * Returns the language's rules to be evaluated when pressing Enter.
	 *
	 * @return the language's rules to be evaluated when pressing Enter.
	 */
	@Nullable
	public List<OnEnterRule> getOnEnterRules() {
		return onEnterRules;
	}

	@Nullable
	private List<AutoClosingPairConditional> autoClosingPairs;

	/**
	 * Returns the language's auto closing pairs. The 'close' character is automatically inserted with the 'open'
	 * character is typed. If not set, the configured brackets will be used.
	 *
	 * @return the language's auto closing pairs.
	 */
	@Nullable
	public List<AutoClosingPairConditional> getAutoClosingPairs() {
		return autoClosingPairs;
	}

	@Nullable
	private List<AutoClosingPair> surroundingPairs;

	/**
	 * Returns the language's surrounding pairs. When the 'open' character is typed on a selection, the selected string
	 * is surrounded by the open and close characters. If not set, the autoclosing pairs settings will be used.
	 *
	 * @return the language's surrounding pairs.
	 */
	@Nullable
	public List<AutoClosingPair> getSurroundingPairs() {
		return surroundingPairs;
	}

	// TODO @Nullable List<CharacterPair> getColorizedBracketPairs();

	@Nullable
	private String autoCloseBefore;

	@Nullable
	public String getAutoCloseBefore() {
		return autoCloseBefore;
	}

	@Nullable
	private FoldingRules folding;

	/**
	 * Returns the language's folding rules.
	 *
	 * @return the language's folding rules.
	 */
	@Nullable
	public FoldingRules getFolding() {
		return folding;
	}
}
