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
import org.eclipse.tm4e.languageconfiguration.ILanguageConfiguration;
import org.eclipse.tm4e.languageconfiguration.internal.supports.AutoClosingPairConditional;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CharacterPair;
import org.eclipse.tm4e.languageconfiguration.internal.supports.Comments;
import org.eclipse.tm4e.languageconfiguration.internal.supports.EnterAction;
import org.eclipse.tm4e.languageconfiguration.internal.supports.EnterAction.IndentAction;
import org.eclipse.tm4e.languageconfiguration.internal.supports.Folding;
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
	 * @param reader
	 *
	 * @return an instance of {@link LanguageConfiguration} loaded from the VSCode
	 *         language-configuration.json file reader.
	 */
	@Nullable
	public static LanguageConfiguration load(final Reader reader) {
		return new GsonBuilder()
				.registerTypeAdapter(OnEnterRule.class, (JsonDeserializer<@Nullable OnEnterRule>) (json, typeOfT, context) -> {
					String beforeText = null;
					String afterText = null;
					EnterAction action = null;
					if (json.isJsonObject()) {
						final JsonObject object = json.getAsJsonObject();
						beforeText = getAsString(object.get("beforeText")); //$NON-NLS-1$
						afterText = getAsString(object.get("afterText")); //$NON-NLS-1$
						final JsonElement actionElement = object.get("action"); //$NON-NLS-1$
						if (actionElement != null && actionElement.isJsonObject()) {
							final JsonObject actionObject = actionElement.getAsJsonObject();
							final String indentActionString = getAsString(actionObject.get("indentAction")); //$NON-NLS-1$
							if (indentActionString != null) {
								final IndentAction indentAction = IndentAction.valueOf(indentActionString);
								final Integer removeText = getAsInteger(actionObject.get("removeText")); //$NON-NLS-1$
								final String appendText = getAsString(actionObject.get("appendText")); //$NON-NLS-1$
								action = new EnterAction(indentAction);
								action.setAppendText(appendText);
								action.setRemoveText(removeText);
							}
						}
					}
					if (beforeText == null || action == null) {
						return null;
					}
					return new OnEnterRule(beforeText, afterText, action);
				}).registerTypeAdapter(Comments.class, (JsonDeserializer<@Nullable Comments>) (json, typeOfT, context) -> {
					// ex: {"lineComment": "//","blockComment": [ "/*", "*/" ]}
					String lineComment = null;
					CharacterPair blockComment = null;
					if (json.isJsonObject()) {
						final JsonObject object = json.getAsJsonObject();
						lineComment = getAsString(object.get("lineComment")); //$NON-NLS-1$
						final JsonElement blockCommentElement = object.get("blockComment"); //$NON-NLS-1$
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
					}
					if (lineComment == null && blockComment == null) {
						return null;
					}
					return new Comments(lineComment, blockComment);
				}).registerTypeAdapter(CharacterPair.class,
						(JsonDeserializer<@Nullable CharacterPair>) (json, typeOfT, context) -> {
							String open = null;
							String close = null;
							if (json.isJsonArray()) {
								// ex: ["{","}"]
								final JsonArray characterPairs = json.getAsJsonArray();
								if (characterPairs.size() == 2) {
									open = getAsString(characterPairs.get(0));
									close = getAsString(characterPairs.get(1));
								}
							}
							if (open == null || close == null) {
								return null;
							}
							return new CharacterPair(open, close);
						})
				.registerTypeAdapter(AutoClosingPairConditional.class,
						(JsonDeserializer<@Nullable AutoClosingPairConditional>) (json, typeOfT, context) -> {
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
									final JsonArray notInArray = notInElement.getAsJsonArray();
									notInArray.forEach(element -> {
										final String string = getAsString(element);
										if (string != null) {
											notInList.add(string);
										}
									});
								}
							}
							if (open == null || close == null) {
								return null;
							}
							return new AutoClosingPairConditional(open, close, notInList);
						})
				.registerTypeAdapter(Folding.class, (JsonDeserializer<@Nullable Folding>) (json, typeOfT, context) -> {
					// ex: {"offSide": true, "markers": {"start": "^\\s*/", "end": "^\\s*"}}
					boolean offSide = false;
					String startMarker = null;
					String endMarker = null;
					if (json.isJsonObject()) {
						final JsonObject object = json.getAsJsonObject();
						offSide = getAsBoolean(object.get("offSide"), offSide); //$NON-NLS-1$
						final JsonElement markersElement = object.get("markers"); //$NON-NLS-1$
						if (markersElement != null && markersElement.isJsonObject()) {
							final JsonObject markersObject = markersElement.getAsJsonObject();
							startMarker = getAsString(markersObject.get("start")); //$NON-NLS-1$
							endMarker = getAsString(markersObject.get("end")); //$NON-NLS-1$
						}
					}
					if (startMarker == null || endMarker == null) {
						return null;
					}
					return new Folding(offSide, startMarker, endMarker);
				}).create().fromJson(reader, LanguageConfiguration.class);
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
	private Comments comments;

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
	private List<AutoClosingPairConditional> autoClosingPairs;

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
	private Folding folding;

	/**
	 * Regex which defines what is considered to be a word in the programming language.
	 */
	@Nullable
	private String wordPattern;

	@Nullable
	@Override
	public Comments getComments() {
		return comments;
	}

	@Nullable
	@Override
	public List<CharacterPair> getBrackets() {
		return brackets;
	}

	@Nullable
	@Override
	public List<AutoClosingPairConditional> getAutoClosingPairs() {
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
	public Folding getFolding() {
		return folding;
	}

	@Nullable
	@Override
	public String getWordPattern() {
		return wordPattern;
	}
}
