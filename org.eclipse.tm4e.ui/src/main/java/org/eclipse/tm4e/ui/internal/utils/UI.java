/*******************************************************************************
 * Copyright (c) 2021, 2022 Vegard IT GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Sebastian Thomschke (Vegard IT GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.utils;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Common UI utilities
 */
public final class UI {

	@Nullable
	public static IWorkbenchPage getActivePage() {
		final var window = getActiveWindow();
		return window == null ? null : window.getActivePage();
	}

	@Nullable
	public static IWorkbenchPart getActivePart() {
		final var page = getActivePage();
		return page == null ? null : page.getActivePart();
	}

	@Nullable
	public static Shell getActiveShell() {
		final var window = getActiveWindow();
		return window == null ? null : window.getShell();
	}

	@Nullable
	public static ITextEditor getActiveTextEditor() {
		final var activePage = getActivePage();
		if (activePage == null) {
			return null;
		}
		final var editorPart = activePage.getActiveEditor();
		if (editorPart instanceof ITextEditor textEditor) {
			return textEditor;
		} else if (editorPart instanceof MultiPageEditorPart multiPageEditorPart) {
			final Object page = multiPageEditorPart.getSelectedPage();
			if (page instanceof ITextEditor textEditor) {
				return textEditor;
			}
		}
		return null;
	}

	@Nullable
	public static ITextViewer getActiveTextViewer() {
		final var editor = getActiveTextEditor();
		if (editor != null) {
			return editor.getAdapter(ITextViewer.class);
		}
		return null;
	}

	@Nullable
	public static IWorkbenchWindow getActiveWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	/**
	 * @return the current display
	 */
	public static Display getDisplay() {
		if (PlatformUI.isWorkbenchRunning())
			return PlatformUI.getWorkbench().getDisplay();

		final var display = Display.getCurrent();
		if (display != null)
			return display;

		return Display.getDefault();
	}

	public static boolean selectFirstElement(final TableViewer viewer) {
		final var firstElement = viewer.getElementAt(0);
		if (firstElement == null)
			return false;
		viewer.setSelection(new StructuredSelection(firstElement), true);
		return true;
	}

	private UI() {
	}
}
