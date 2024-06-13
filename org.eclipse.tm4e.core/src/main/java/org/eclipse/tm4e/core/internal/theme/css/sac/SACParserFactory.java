/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm4e.core.internal.theme.css.sac;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.w3c.css.sac.Parser;

/**
 * SAC Parser factory implementation.
 *
 * By default, this SAC FActory supports Flute, SteadyState and Batik SAC Parser.
 *
 * @see <a href=
 *      "https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/bundles/org.eclipse.e4.ui.css.core/src/org/eclipse/e4/ui/css/core/impl/sac/SACParserFactoryImpl.java">github.com/eclipse-platform/eclipse.platform.ui/blob/master/bundles/org.eclipse.e4.ui.css.core/src/org/eclipse/e4/ui/css/core/impl/sac/SACParserFactoryImpl.java</a>
 */
public final class SACParserFactory extends AbstractSACParserFactory {

	private static Map<String, @Nullable String> parsers = new HashMap<>();

	static {
		// Register Flute SAC Parser
		registerSACParser(SACConstants.SACPARSER_FLUTE);
		// Register Flute SAC CSS3Parser
		registerSACParser(SACConstants.SACPARSER_FLUTE_CSS3);
		// Register SteadyState SAC Parser
		registerSACParser(SACConstants.SACPARSER_STEADYSTATE);
		// Register Batik SAC Parser
		registerSACParser(SACConstants.SACPARSER_BATIK);
	}

	public SACParserFactory() {
		// Flute parser is the default SAC Parser to use.
		super.setPreferredParserName(SACConstants.SACPARSER_BATIK);
	}

	@Override
	public Parser makeParser(final String name) throws ClassNotFoundException, IllegalAccessException,
			InstantiationException, ClassCastException {
		final String classNameParser = parsers.get(name);
		if (classNameParser != null) {
			final Class<?> classParser = castNonNull(super.getClass().getClassLoader()).loadClass(classNameParser);
			try {
				return (Parser) classParser.getDeclaredConstructor().newInstance();
			} catch (InvocationTargetException | NoSuchMethodException ex) {
				throw (InstantiationException) new InstantiationException().initCause(ex);
			}
		}
		throw new IllegalAccessException(
				"SAC parser with name=" + name + " was not registered into SAC parser factory.");
	}

	/**
	 * Register SAC parser name.
	 */
	private static void registerSACParser(final String parser) {
		registerSACParser(parser, parser);
	}

	/**
	 * Register SAC parser with name <code>name</code> mapped with Class name <code>classNameParser</code>.
	 */
	private static void registerSACParser(final String name, final String classNameParser) {
		parsers.put(name, classNameParser);
	}
}
