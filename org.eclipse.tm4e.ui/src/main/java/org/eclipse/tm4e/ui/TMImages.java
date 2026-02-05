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
package org.eclipse.tm4e.ui;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

public final class TMImages {

	private static final String ICONS_PATH = "$nl$/icons/full/"; //$NON-NLS-1$
	private static final String OBJECT = ICONS_PATH + "obj16/"; // basic colors - size 16x16 //$NON-NLS-1$

	public static final String IMG_TEMPLATE = "IMG_TEMPALTE"; //$NON-NLS-1$

	private TMImages() {
		// no instantiation desired
	}

	private static @Nullable ImageRegistry imageRegistry;

	public static void initalize(final ImageRegistry registry) {
		imageRegistry = registry;

		registerImage(IMG_TEMPLATE, OBJECT + "template_obj.svg"); //$NON-NLS-1$
	}

	private static void registerImage(final String key, final String path) {
		ImageDescriptor desc = ImageDescriptor.getMissingImageDescriptor();
		final Bundle bundle = Platform.getBundle(TMUIPlugin.PLUGIN_ID);
		final ImageRegistry imageRegistry = getImageRegistry();
		URL url = null;
		if (bundle != null) {
			url = FileLocator.find(bundle, new Path(path), null);
			if (url != null) {
				desc = ImageDescriptor.createFromURL(url);
			}
		}
		if (imageRegistry != null) {
			imageRegistry.put(key, desc);
		}
	}

	/**
	 * Returns the {@link Image} identified by the given key, or <code>null</code> if it does not exist.
	 */
	public static @Nullable Image getImage(final String key) {
		final ImageRegistry imageRegistry = getImageRegistry();
		if (imageRegistry == null) {
			return null;
		}
		return imageRegistry.get(key);
	}

	/**
	 * Returns the {@link ImageDescriptor} identified by the given key, or <code>null</code> if it does not exist.
	 */
	public static @Nullable ImageDescriptor getImageDescriptor(final String key) {
		final ImageRegistry imageRegistry = getImageRegistry();
		if (imageRegistry == null) {
			return null;
		}
		return imageRegistry.getDescriptor(key);
	}

	public static @Nullable ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			final TMUIPlugin plugin = TMUIPlugin.getDefault();
			if (plugin == null) {
				return null;
			}
			imageRegistry = plugin.getImageRegistry();
		}
		return imageRegistry;
	}

}
