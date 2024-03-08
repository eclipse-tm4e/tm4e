/*******************************************************************************
 * Copyright (c) 2021-2024 Vegard IT GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT) - initial implementation
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.utils;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
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
		if (editorPart instanceof final ITextEditor textEditor) {
			return textEditor;
		} else if (editorPart instanceof final MultiPageEditorPart multiPageEditorPart) {
			final Object page = multiPageEditorPart.getSelectedPage();
			if (page instanceof final ITextEditor textEditor) {
				return textEditor;
			}
		}
		return null;
	}

	public static @Nullable ITextSelection getActiveTextSelection() {
		final var editor = getActiveTextEditor();
		if (editor == null)
			return null;
		if (editor.getSelectionProvider().getSelection() instanceof final ITextSelection sel)
			return sel;
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

	public static ModifyListener debounceModifyListener(final int delay, final ModifyListener listener) {
		return new ModifyListener() {
			private Runnable later = () -> {
			};

			@Override
			public void modifyText(final @Nullable ModifyEvent e) {
				final var display = UI.getDisplay();
				// Cancel previous scheduled call
				display.timerExec(-1, later);

				later = () -> listener.modifyText(e);

				// Schedule a new call to execute after the delay
				display.timerExec(delay, later);
			}
		};
	}

	private static @Nullable FontMetrics fontMetrics;

	public static int convertHeightInCharsToPixels(int chars) {
		if (fontMetrics == null) {
			final GC gc = new GC(getActiveShell());
			try {
				gc.setFont(JFaceResources.getDialogFont());
				fontMetrics = gc.getFontMetrics();
			} finally {
				gc.dispose();
			}
		}
		return Dialog.convertHeightInCharsToPixels(fontMetrics, chars);
	}

	public static int getTextWidth(final String string) {
		final GC gc = new GC(getActiveShell());
		try {
			gc.setFont(JFaceResources.getDialogFont());
			return gc.stringExtent(string).x;
		} finally {
			gc.dispose();
		}
	}

	/**
	 * @return 0-255
	 */
	private static int getBrightness(final int red, final int green, final int blue) {
		// https://www.w3.org/TR/AERT/#color-contrast
		return (int) (0.299 * red + 0.587 * green + 0.114 * blue);
	}

	public static boolean isDarkColor(final RGB color) {
		return getBrightness(color.red, color.green, color.blue) < 128;
	}

	public static boolean isDarkColor(final Color color) {
		return getBrightness(color.getRed(), color.getGreen(), color.getBlue()) < 128;
	}

	public static boolean isDarkEclipseTheme() {
		final var shell = getActiveShell();
		if (shell == null)
			throw new IllegalStateException("No active shell found!");
		return isDarkColor(shell.getBackground());
	}

	public static boolean isUIThread() {
		return Display.getCurrent() != null;
	}

	/**
	 * Runs the given runnable synchronously on the UI thread
	 *
	 * @throws SWTException if the {@link Display} has been disposed
	 */
	public static <T> T runSync(final Supplier<T> runnable) {
		if (isUIThread())
			return runnable.get();

		final var resultRef = new AtomicReference<T>();
		final var exRef = new AtomicReference<@Nullable RuntimeException>();
		getDisplay().syncExec(() -> {
			try {
				resultRef.set(runnable.get());
			} catch (final RuntimeException ex) {
				exRef.set(ex);
			}
		});

		final @Nullable RuntimeException ex = exRef.get();
		if (ex != null) {
			throw ex;
		}
		return resultRef.get();
	}

	private UI() {
	}
}
