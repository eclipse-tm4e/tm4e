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
package org.eclipse.tm4e.ui.internal.menus;

import java.util.ArrayList;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.text.TMPresentationReconciler;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.eclipse.tm4e.ui.themes.ThemeAssociation;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Contribute "Switch to theme" menu item with list of available themes.
 *
 */
public final class ThemeContribution extends CompoundContributionItem implements IWorkbenchContribution {

	@Nullable
	private IHandlerService handlerService;

	@Override
	public void initialize(@Nullable final IServiceLocator serviceLocator) {
		assert serviceLocator != null;
		handlerService = serviceLocator.getService(IHandlerService.class);
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		final var items = new ArrayList<IContributionItem>();
		if (handlerService != null) {
			final IEditorPart editorPart = getActivePart(handlerService.getCurrentState());
			if (editorPart != null) {
				final TMPresentationReconciler presentationReconciler = TMPresentationReconciler
						.getTMPresentationReconciler(editorPart);
				if (presentationReconciler != null) {
					final var grammar = presentationReconciler.getGrammar();
					if (grammar != null) {
						final IThemeManager manager = TMUIPlugin.getThemeManager();
						final boolean dark = manager.isDarkEclipseTheme();
						final ITheme[] themes = manager.getThemes();
						final String scopeName = grammar.getScopeName();
						final ITheme selectedTheme = manager.getThemeForScope(scopeName, dark);
						for (final ITheme theme : themes) {
							final IAction action = createAction(scopeName, theme, dark);
							if (theme.equals(selectedTheme)) {
								action.setChecked(true);
							}
							final var item = new ActionContributionItem(action);
							items.add(item);
						}
					}
				}

			}
		}
		return items.toArray(IContributionItem[]::new);
	}

	private Action createAction(final String scopeName, final ITheme theme, final boolean whenDark) {
		return new Action(theme.getName()) {
			@Override
			public void run() {
				final IThemeManager manager = TMUIPlugin.getThemeManager();
				final var association = new ThemeAssociation(theme.getId(), scopeName, whenDark);
				manager.registerThemeAssociation(association);
				try {
					manager.save();
				} catch (final BackingStoreException e) {
					e.printStackTrace();
				}
			}
		};
	}

	@Nullable
	private static IEditorPart getActivePart(@Nullable final IEvaluationContext context) {
		if (context == null)
			return null;

		final Object activePart = context.getVariable(ISources.ACTIVE_PART_NAME);
		if (activePart instanceof final IEditorPart editorPart) {
			return editorPart;
		}

		return null;
	}

}
