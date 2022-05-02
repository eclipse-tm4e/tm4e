/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm4e.core.internal.theme.css;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm4e.core.theme.css.SACConstants;
import org.eclipse.tm4e.core.theme.css.SACParserFactory;
import org.w3c.css.sac.Parser;

/**
 * SAC Parser factory implementation. By default, this SAC FActory support
 * Flute, SteadyState and Batik SAC Parser.
 */
public final class SACParserFactoryImpl extends SACParserFactory {

	private static Map<String, String> parsers = new HashMap<>();

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

	public SACParserFactoryImpl() {
		// Flute parser is the default SAC Parser to use.
		super.setPreferredParserName(SACConstants.SACPARSER_BATIK);
	}

	@Override
	public Parser makeParser(final String name) throws ClassNotFoundException, IllegalAccessException, InstantiationException,
	NullPointerException, ClassCastException {
		final String classNameParser = parsers.get(name);
		if (classNameParser != null) {
			final Class<?> classParser = super.getClass().getClassLoader().loadClass(classNameParser);
			try {
				return (Parser) classParser.getDeclaredConstructor().newInstance();
			} catch (InvocationTargetException|NoSuchMethodException ex) {
				throw (InstantiationException)((new InstantiationException()).initCause(ex));
			}
		}
		throw new IllegalAccessException("SAC parser with name=" + name
				+ " was not registered into SAC parser factory.");
	}

	/**
	 * Register SAC parser name.
	 *
	 * @param parser
	 */
	private static void registerSACParser(final String parser) {
		registerSACParser(parser, parser);
	}

	/**
	 * register SAC parser with name <code>name</code> mapped with Class name
	 * <code>classNameParser</code>.
	 *
	 * @param name
	 * @param classNameParser
	 */
	private static void registerSACParser(final String name, final String classNameParser) {
		parsers.put(name, classNameParser);
	}
}
