/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * Copyright (c) 2023 Vegard IT GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - assertHasGenericEditor, assertNoTM4EThreadsRunning methods
 * Sebastian Thomschke (Vegard IT GmbH) - add more util methods
 */
package org.eclipse.tm4e.ui.tests.support;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.DisplayHelper;

public class TestUtils {

	public static IEditorDescriptor assertHasGenericEditor() {
		final var genericEditorDescr = PlatformUI.getWorkbench().getEditorRegistry()
				.findEditor("org.eclipse.ui.genericeditor.GenericEditor");
		assertNotNull(genericEditorDescr);
		return genericEditorDescr;
	}

	public static void assertNoTM4EThreadsRunning() throws InterruptedException {
		var tm4eThreads = Thread.getAllStackTraces();
		tm4eThreads.entrySet().removeIf(e -> !e.getKey().getClass().getName().startsWith("org.eclipse.tm4e"));

		if (!tm4eThreads.isEmpty()) {
			Thread.sleep(5_000); // give threads time to finish
		}

		tm4eThreads = Thread.getAllStackTraces();
		tm4eThreads.entrySet().removeIf(e -> !e.getKey().getClass().getName().startsWith("org.eclipse.tm4e"));

		if (!tm4eThreads.isEmpty()) {
			// print the stacktrace of one of the hung threads
			final var tm4eThread = tm4eThreads.entrySet().iterator().next();
			final var ex = new IllegalStateException("Thread " + tm4eThread.getKey() + " is still busy");
			ex.setStackTrace(tm4eThread.getValue());
			ex.printStackTrace(System.out);

			fail("TM4E threads still running:\n" + tm4eThreads.keySet().stream()
					.map(t -> " - " + t + " " + t.getClass().getName()).collect(Collectors.joining("\n")));
		}
	}

	public static void closeEditor(IEditorPart editor) {
		if (editor == null)
			return;
		final IWorkbenchPartSite currentSite = editor.getSite();
		if (currentSite != null) {
			final IWorkbenchPage currentPage = currentSite.getPage();
			if (currentPage != null) {
				currentPage.closeEditor(editor, false);
			}
		}
	}

	public static File createTempFile(String fileNameSuffix) throws IOException {
		final var file = File.createTempFile("tm4e_testfile", fileNameSuffix);
		file.deleteOnExit();
		return file;
	}

	public static boolean isCI() {
		return "true".equals(System.getenv("CI"));
	}

	public static boolean waitForCondition(int timeout_ms, BooleanSupplier condition) {
		return waitForCondition(timeout_ms, PlatformUI.getWorkbench().getDisplay(), condition);
	}

	public static boolean waitForCondition(int timeout_ms, Display display, BooleanSupplier condition) {
		return new DisplayHelper() {
			@Override
			protected boolean condition() {
				return condition.getAsBoolean();
			}
		}.waitForCondition(display, timeout_ms);
	}
}
