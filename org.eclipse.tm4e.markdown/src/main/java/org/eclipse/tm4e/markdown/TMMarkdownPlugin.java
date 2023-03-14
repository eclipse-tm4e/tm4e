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
package org.eclipse.tm4e.markdown;

import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * OSGi Activator for TextMate Markdown bundle.
 */
public class TMMarkdownPlugin implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.tm4e.markdown";

	@Nullable
	private static BundleContext context;

	@Nullable
	static BundleContext getContext() {
		return context;
	}

	@Override
	public void start(@Nullable final BundleContext bundleContext) throws Exception {
		TMMarkdownPlugin.context = bundleContext;
	}

	@Override
	public void stop(@Nullable final BundleContext bundleContext) throws Exception {
		TMMarkdownPlugin.context = null;
	}
}
