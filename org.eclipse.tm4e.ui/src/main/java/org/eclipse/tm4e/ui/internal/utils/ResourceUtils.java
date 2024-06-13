/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.ui.internal.utils;

import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.IDocument;

public final class ResourceUtils {

	public static @Nullable IResource findResource(final IDocument doc) {
		// for local unit tests to prevent ExceptionInInitiaizerError
		if (!Platform.isRunning())
			return null;

		final var buffer = ITextFileBufferManager.DEFAULT.getTextFileBuffer(doc);
		if (buffer == null)
			return null;
		final var loc = buffer.getLocation();
		if (loc == null)
			return null;
		return ResourcesPlugin.getWorkspace().getRoot().findMember(loc);
	}

	/**
	 * private constructor to prevent instantiation of utility class
	 */
	private ResourceUtils() {
	}
}
