/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm4e.core.internal.theme.css.sac;

import org.eclipse.jdt.annotation.Nullable;
import org.w3c.css.sac.Parser;

/**
 * SAC parser factory interface to get instance of SAC {@link Parser}.
 */
public interface ISACParserFactory {

	/**
	 * Return preferred SAC parser name if it is filled and null otherwise.
	 */
	@Nullable
	String getPreferredParserName();

	/**
	 * Set the preferred SAC parser name to use when makeParser is called.
	 *
	 * @param preferredParserName
	 */
	void setPreferredParserName(@Nullable String preferredParserName);

	/**
	 * Return default instance of SAC Parser.
	 *
	 * If preferredParserName is filled, it return the instance of SAC Parser registered with this name,
	 * otherwise this method search the SAC Parser class name to instantiate into System property with key
	 * <code>org.w3c.css.sac.parser</code>.
	 */
	Parser makeParser()
			throws ClassNotFoundException, IllegalAccessException, InstantiationException, ClassCastException;

	/**
	 * Return instance of SAC Parser registered into the factory with name <code>name</code>.
	 *
	 * @param name
	 *
	 * @see SACConstants
	 */
	Parser makeParser(String name)
			throws ClassNotFoundException, IllegalAccessException, InstantiationException, ClassCastException;
}
