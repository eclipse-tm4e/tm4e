/*******************************************************************************
 * Copyright (c) 2026 Advantest Europe GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Dietrich Travkin (SOLUNAR GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.utils;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.model.TMToken;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.ITMScope;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.tm4e.ui.TMUIPlugin;

public class CodeTemplateContextTypeUtils {

	private static final String CONTEXT_TYPE_ID_PREFIX = TMUIPlugin.PLUGIN_ID + ".templates.context."; //$NON-NLS-1$

	private CodeTemplateContextTypeUtils() {
		// no instantiation desired
	}

	public static String toContextTypeId(final ITMScope languageScope) {
		return CONTEXT_TYPE_ID_PREFIX + languageScope.getQualifiedName();
	}

	public static String toContextTypeId(final TMToken textMateToken) {
		return CONTEXT_TYPE_ID_PREFIX + textMateToken.grammarScope;
	}

	public static @Nullable ITMScope findScopeFor(final String contextTypeId) {
		final IGrammarDefinition[] grammarDefinitions = TMEclipseRegistryPlugin.getGrammarRegistryManager().getDefinitions();

		return Arrays.stream(grammarDefinitions)
				.map(IGrammarDefinition::getScope)
				.filter(scope -> contextTypeId.equals(CodeTemplateContextTypeUtils.toContextTypeId(scope)))
				.findFirst().orElse(null);
	}

	public static @Nullable String getContentTypeName(final ITMScope languageScope) {
		@Nullable
		final Collection<IContentType> contentTypes = TMEclipseRegistryPlugin.getGrammarRegistryManager()
				.getContentTypesForScope(languageScope);

		if (contentTypes != null && contentTypes.size() > 0) {
			// we only consider the first content type
			return contentTypes.iterator().next().getName();
		}
		return null;
	}

	public static String toContextTypeName(final ITMScope languageScope) {
		final String contentTypeName = getContentTypeName(languageScope);

		String name = "";
		if (contentTypeName != null) {
			name = contentTypeName;
		}

		name += " (" + languageScope.getQualifiedName() + ")";
		return name;
	}

	public static @Nullable IGrammar toGrammar(final String contextTypeName) {
		final IGrammarDefinition[] grammarDefinitions = TMEclipseRegistryPlugin.getGrammarRegistryManager().getDefinitions();

		return Arrays.stream(grammarDefinitions)
				.map(IGrammarDefinition::getScope)
				.filter(scope -> contextTypeName.equals(toContextTypeName(scope)))
				.map(scope -> TMEclipseRegistryPlugin.getGrammarRegistryManager().getGrammarForScope(scope))
				.findFirst().orElse(null);
	}

}
