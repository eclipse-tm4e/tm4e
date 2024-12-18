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
package org.eclipse.tm4e.ui.internal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.ui.text.TMPresentationReconciler;
import org.eclipse.ui.IEditorPart;

/**
 * An Eclipse property tester to check if a given editor part is linked to the
 * {@link TMPresentationReconciler}.
 */
public final class TMPropertyTester extends PropertyTester {

	private static final String CAN_SUPPORT_TEXT_MATE = "canSupportTextMate";

	@Override
	public boolean test(final @Nullable Object receiver, final String property, final Object[] args, final @Nullable Object expectedValue) {
		if (CAN_SUPPORT_TEXT_MATE.equals(property) && receiver instanceof final IEditorPart editorPart) {
			final var reconciler = TMPresentationReconciler.getTMPresentationReconciler(editorPart);
			return reconciler != null && reconciler.isEnabled();
		}
		return false;
	}
}
