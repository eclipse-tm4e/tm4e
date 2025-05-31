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
package org.eclipse.tm4e.ui.internal.samples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.samples.ISample;
import org.eclipse.tm4e.ui.samples.ISampleManager;

public final class SampleManager implements ISampleManager {

	private static final ISample[] NO_SAMPLES = {};

	private static final String SAMPLE_ELT = "sample";
	private static final String SNIPPET_ELT = "snippet";

	private static final String EXTENSION_SAMPLES = "samples";
	private static final String EXTENSION_SNIPPETS = "snippets";

	/** see https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java */
	private static final class InstanceHolder {
		static final SampleManager INSTANCE = new SampleManager();
		static {
			INSTANCE.load();
		}
	}

	public static ISampleManager getInstance() {
		return InstanceHolder.INSTANCE;
	}

	private final Map<String, List<ISample>> samples = new HashMap<>();

	private SampleManager() {
	}

	private void load() {
		loadGrammarsFromExtensionPoints();
	}

	/**
	 * Load samples from extension point.
	 */
	private void loadGrammarsFromExtensionPoints() {

		final IConfigurationElement[] snippetsCF = Platform.getExtensionRegistry().getConfigurationElementsFor(
				TMUIPlugin.PLUGIN_ID, EXTENSION_SNIPPETS);
		for (final IConfigurationElement ce : snippetsCF) {
			final String extensionName = ce.getName();
			if (SNIPPET_ELT.equals(extensionName)) {
				this.registerSample(new Sample(ce));
			}
		}

		final IConfigurationElement[] samplesCF = Platform.getExtensionRegistry().getConfigurationElementsFor(
				TMUIPlugin.PLUGIN_ID, EXTENSION_SAMPLES);
		for (final IConfigurationElement ce : samplesCF) {
			final String extensionName = ce.getName();
			if (SAMPLE_ELT.equals(extensionName)) {
				this.registerSample(new Sample(ce));
			}
		}
	}

	private void registerSample(final Sample sample) {
		final String scopeName = sample.getScopeName();
		samples.computeIfAbsent(scopeName, unused -> new ArrayList<>()).add(sample);
	}

	@Override
	public ISample[] getSamples(final String scopeName) {
		final List<ISample> samples = this.samples.get(scopeName);
		return samples != null ? samples.toArray(ISample[]::new) : NO_SAMPLES;
	}
}
