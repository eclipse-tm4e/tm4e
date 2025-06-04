/**
 * Copyright (c) 2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Sebastian Thomschke (Vegard IT) - initial implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.expressions;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.Bundle;

/**
 * Property tester that checks whether a given bundle is active.
 * <p>
 * Used as a workaround for the fact that
 *
 * <pre>{@code
 * <with variable="platform">
 *   <test property="org.eclipse.core.runtime.isBundleInstalled" args="org.eclipse.lsp4e"/>
 * </with>}</pre>
 *
 * does not work under
 * {@code <foldingReconcilingStrategy><enabledWhen>...</enabledWhen></foldingReconcilingStrategy>}
 * where it throws {@code CoreException: The variable platform is not defined}.
 */
public final class IsBundleActivePropertyTester extends PropertyTester {
	@Override
	public boolean test(final @Nullable Object receiver, final String property, final Object[] args, final @Nullable Object expectedValue) {
		if (args.length == 0)
			return false;
		final var bundle = Platform.getBundle(String.valueOf(args[0]));
		return bundle != null && bundle.getState() == Bundle.ACTIVE;
	}
}
