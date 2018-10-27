package org.eclipse.tm4e.ui.internal.themes;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.themes.IThemeBehaviour;

public class ThemeBehaviourManager {

	private static final String EXTENSION_POINT_NAME = TMUIPlugin.PLUGIN_ID + ".themeBehaviour";
	private static ThemeBehaviourManager INSTANCE = new ThemeBehaviourManager();

	private IThemeBehaviour behaviour;

	private ThemeBehaviourManager() {
	}

	public static ThemeBehaviourManager getInstance() {
		return INSTANCE;
	}

	public IThemeBehaviour getThemeBehaviour() {
		return behaviour;
	}

	/**
	 * Initialize manager
	 */
	public void init() {
		IConfigurationElement[] configurationElements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_NAME);

		if (configurationElements.length > 0) {
			behaviour = getBehaviour(configurationElements[0]);
		}

		if (behaviour == null) {
			behaviour = new DefaultThemeBehaviour();
		}
	}

	/**
	 * Get behaviour from extension point.
	 * 
	 * @param element
	 *            configurationElement for extension point
	 * @return behaviour from configuration or default behaviour if exception occured
	 */
	private IThemeBehaviour getBehaviour(IConfigurationElement element) {
		try {
			return (IThemeBehaviour) element.createExecutableExtension("class");
		} catch (CoreException ex) {
			// ignore
		}

		return null;

	}
}
