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
package org.eclipse.tm4e.languageconfiguration.internal;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.internal.supports.StandardAutoClosingPairConditional;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CharacterPair;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CommentRule;
import org.eclipse.tm4e.languageconfiguration.internal.supports.EnterAction;
import org.eclipse.tm4e.languageconfiguration.internal.supports.EnterAction.IndentAction;
import org.eclipse.tm4e.languageconfiguration.internal.supports.FoldingRule;
import org.eclipse.tm4e.languageconfiguration.internal.supports.OnEnterRule;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * VSCode language-configuration.json
 *
 * @see <a href="https://code.visualstudio.com/docs/extensionAPI/extension-points#_contributeslanguages">
 *      code.visualstudio.com/docs/extensionAPI/extension-points#_contributeslanguages</a>
 */
public final class LanguageConfiguration implements ILanguageConfiguration {

	/**
	 * Returns an instance of {@link LanguageConfiguration} loaded from the VSCode
	 * language-configuration.json file reader.
	 *
	 * @return an instance of {@link LanguageConfiguration} loaded from the VSCode
	 *         language-configuration.json file reader.
	 */
	@Nullable
	public static LanguageConfiguration load(final Reader reader) {
		return new GsonBuilder()

				.registerTypeAdapter(OnEnterRule.class,
						(JsonDeserializer<@Nullable OnEnterRule>) (json, typeOfT, context) -> {
							if (!json.isJsonObject()) {
								return null;
							}

							final JsonObject object = json.getAsJsonObject();
							final var beforeText = getAsString(object.get("beforeText")); //$NON-NLS-1$
							if (beforeText == null) {
								return null;
							}
							final var afterText = getAsString(object.get("afterText")); //$NON-NLS-1$

							EnterAction action = null;
							final JsonElement actionElement = object.get("action"); //$NON-NLS-1$
							if (actionElement != null && actionElement.isJsonObject()) {
								final JsonObject actionObject = actionElement.getAsJsonObject();
								final String indentActionString = getAsString(actionObject.get("indentAction")); //$NON-NLS-1$
								if (indentActionString != null) {
									final IndentAction indentAction = IndentAction.valueOf(indentActionString);
									final Integer removeText = getAsInteger(actionObject.get("removeText")); //$NON-NLS-1$
									final String appendText = getAsString(actionObject.get("appendText")); //$NON-NLS-1$
									action = new EnterAction(indentAction);
									action.appendText = appendText;
									action.removeText = removeText;
								}
							}

							return action == null
									? null
									: new OnEnterRule(beforeText, afterText, action);
						})

				.registerTypeAdapter(CommentRule.class,
						(JsonDeserializer<@Nullable CommentRule>) (json, typeOfT, context) -> {
							if (!json.isJsonObject()) {
								return null;
							}

							// ex: {"lineComment": "//","blockComment": [ "/*", "*/" ]}
							final JsonObject object = json.getAsJsonObject();
							final String lineComment = getAsString(object.get("lineComment")); //$NON-NLS-1$
							final JsonElement blockCommentElement = object.get("blockComment"); //$NON-NLS-1$
							CharacterPair blockComment = null;
							if (blockCommentElement != null && blockCommentElement.isJsonArray()) {
								final JsonArray blockCommentArray = blockCommentElement.getAsJsonArray();
								if (blockCommentArray.size() == 2) {
									final String blockCommentStart = getAsString(blockCommentArray.get(0));
									final String blockCommentEnd = getAsString(blockCommentArray.get(1));
									if (blockCommentStart != null && blockCommentEnd != null) {
										blockComment = new CharacterPair(blockCommentStart, blockCommentEnd);
									}
								}
							}

							return lineComment == null && blockComment == null
									? null
									: new CommentRule(lineComment, blockComment);
						})

				.registerTypeAdapter(CharacterPair.class,
						(JsonDeserializer<@Nullable CharacterPair>) (json, typeOfT, context) -> {
							if (!json.isJsonArray()) {
								return null;
							}

							// ex: ["{","}"]
							final JsonArray characterPairs = json.getAsJsonArray();
							if (characterPairs.size() != 2) {
								return null;
							}

							final String open = getAsString(characterPairs.get(0));
							final String close = getAsString(characterPairs.get(1));

							return open == null || close == null
									? null
									: new CharacterPair(open, close);
						})

				.registerTypeAdapter(StandardAutoClosingPairConditional.class,
						(JsonDeserializer<@Nullable StandardAutoClosingPairConditional>) (json, typeOfT, context) -> {
							final var notInList = new ArrayList<String>();
							String open = null;
							String close = null;
							if (json.isJsonArray()) {
								// ex: ["{","}"]
								final JsonArray characterPairs = json.getAsJsonArray();
								if (characterPairs.size() == 2) {
									open = getAsString(characterPairs.get(0));
									close = getAsString(characterPairs.get(1));
								}
							} else if (json.isJsonObject()) {
								// ex: {"open":"'","close":"'", "notIn": ["string", "comment"]}
								final JsonObject object = json.getAsJsonObject();
								open = getAsString(object.get("open")); //$NON-NLS-1$
								close = getAsString(object.get("close")); //$NON-NLS-1$
								final JsonElement notInElement = object.get("notIn"); //$NON-NLS-1$
								if (notInElement != null && notInElement.isJsonArray()) {
									notInElement.getAsJsonArray().forEach(element -> {
										final String string = getAsString(element);
										if (string != null) {
											notInList.add(string);
										}
									});
								}
							}

							return open == null || close == null
									? null
									: new StandardAutoClosingPairConditional(open, close, notInList);
						})

				.registerTypeAdapter(FoldingRule.class, (JsonDeserializer<@Nullable FoldingRule>) (json, typeOfT, context) -> {
					if (!json.isJsonObject()) {
						return null;
					}

					// ex: {"offSide": true, "markers": {"start": "^\\s*/", "end": "^\\s*"}}
					final JsonObject object = json.getAsJsonObject();
					final JsonElement markersElement = object.get("markers"); //$NON-NLS-1$
					if (markersElement != null && markersElement.isJsonObject()) {
						final boolean offSide = getAsBoolean(object.get("offSide"), false); //$NON-NLS-1$
						final JsonObject markersObject = markersElement.getAsJsonObject();
						final String startMarker = getAsString(markersObject.get("start")); //$NON-NLS-1$
						final String endMarker = getAsString(markersObject.get("end")); //$NON-NLS-1$
						if (startMarker != null && endMarker != null) {
							return new FoldingRule(offSide, startMarker, endMarker);
						}
					}
					return null;
				})
				.create()
				.fromJson(reader, LanguageConfiguration.class);
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

	/**
	 * Defines the comment symbols
	 */
	@Nullable
	private CommentRule comments;

	/**
	 * The language's brackets. This configuration implicitly affects pressing Enter around these brackets.
	 */
	@Nullable
	private List<CharacterPair> brackets;

	/**
	 * The language's rules to be evaluated when pressing Enter.
	 */
	@Nullable
	private List<OnEnterRule> onEnterRules;

	/**
	 * The language's auto closing pairs. The 'close' character is automatically
	 * inserted with the 'open' character is typed. If not set, the configured
	 * brackets will be used.
	 */
	@Nullable
	private List<StandardAutoClosingPairConditional> autoClosingPairs;

	/**
	 * The language's surrounding pairs. When the 'open' character is typed on a
	 * selection, the selected string is surrounded by the open and close
	 * characters. If not set, the autoclosing pairs settings will be used.
	 */
	@Nullable
	private List<CharacterPair> surroundingPairs;

	/**
	 * Defines when and how code should be folded in the editor
	 */
	@Nullable
	private FoldingRule folding;

	/**
	 * Regex which defines what is considered to be a word in the programming language.
	 */
	@Nullable
	private String wordPattern;

	@Nullable
	@Override
	public CommentRule getComments() {
		return comments;
	}

	@Nullable
	@Override
	public List<CharacterPair> getBrackets() {
		return brackets;
	}

	@Nullable
	@Override
	public List<StandardAutoClosingPairConditional> getAutoClosingPairs() {
		return autoClosingPairs;
	}

	@Nullable
	@Override
	public List<OnEnterRule> getOnEnterRules() {
		return onEnterRules;
	}

	@Nullable
	@Override
	public List<CharacterPair> getSurroundingPairs() {
		return surroundingPairs;
	}

	@Nullable
	@Override
	public FoldingRule getFolding() {
		return folding;
	}

	@Nullable
	@Override
	public String getWordPattern() {
		return wordPattern;
	}
}
