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

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.swt.widgets.Display;
import org.eclipse.tm4e.ui.internal.utils.UI;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.DisplayHelper;

public final class TestUtils {

	@FunctionalInterface
	public interface Condition {
		boolean isMet() throws Exception;
	}

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
			ex.printStackTrace();

			fail("TM4E threads still running:\n" + tm4eThreads.keySet().stream()
					.map(t -> " - " + t + " " + t.getClass().getName()).collect(Collectors.joining("\n")));
		}
	}

	public static void closeEditor(final IEditorPart editor) {
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

	public static File createTempFile(final String fileNameSuffix) throws IOException {
		final var file = File.createTempFile("tm4e_testfile", fileNameSuffix);
		file.deleteOnExit();
		return file;
	}

	public static boolean isCI() {
		return Boolean.getBoolean("CI");
	}

	public static boolean isGitHubActions() {
		return Boolean.getBoolean("GITHUB_ACTIONS");
	}

	public static void waitForAndAssertCondition(int timeout_ms, Condition condition) {
		waitForAndAssertCondition("Condition not met within expected time.", timeout_ms, condition);
	}

	public static void waitForAndAssertCondition(int timeout_ms, Display display, Condition condition) {
		waitForAndAssertCondition("Condition not met within expected time.", timeout_ms, display, condition);
	}

	public static void waitForAndAssertCondition(String errorMessage, int timeout_ms, Condition condition) {
		waitForAndAssertCondition(errorMessage, timeout_ms, UI.getDisplay(), condition);
	}

	public static void waitForAndAssertCondition(String errorMessage, int timeout_ms, Display display,
			Condition condition) {
		final var ex = new AtomicReference<Throwable>();
		final var isConditionMet = new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					final var isMet = condition.isMet();
					ex.set(null);
					return isMet;
				} catch (final AssertionError | Exception e) {
					ex.set(e);
					return false;
				}
			}
		}.waitForCondition(display, timeout_ms, 50);
		if (ex.get() != null) {
			// if the condition was not met because of an exception throw it
			if (ex.get() instanceof AssertionError ae) {
				throw ae;
			}
			if (ex.get() instanceof RuntimeException re) {
				throw re;
			}
			throw new AssertionError(errorMessage, ex.get());
		}
		assertTrue(errorMessage, isConditionMet);
	}

	public static boolean waitForCondition(final int timeout_ms, final Condition condition) {
		return waitForCondition(timeout_ms, UI.getDisplay(), condition);
	}

	public static boolean waitForCondition(final int timeout_ms, final Display display, final Condition condition) {
		final var ex = new AtomicReference<Throwable>();
		final var isConditionMet = new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					final var isMet = condition.isMet();
					ex.set(null);
					return isMet;
				} catch (final AssertionError | Exception e) {
					ex.set(e);
					return false;
				}
			}
		}.waitForCondition(display, timeout_ms, 50);
		if (ex.get() != null) {
			// if the condition was not met because of an exception log it
			ex.get().printStackTrace();
		}
		return isConditionMet;
	}

	private TestUtils() {
	}
}
