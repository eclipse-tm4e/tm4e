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
package org.eclipse.tm4e.core.internal.theme.css.util;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.SelectorList;

public abstract class AbstractDocumentHandler implements DocumentHandler {

	@Override
	public void comment(final String arg0) throws CSSException {
	}

	@Override
	public void endDocument(final InputSource arg0) throws CSSException {
	}

	@Override
	public void endFontFace() throws CSSException {
	}

	@Override
	public void endMedia(final SACMediaList arg0) throws CSSException {
	}

	@Override
	public void endPage(final String arg0, final String arg1) throws CSSException {
	}

	@Override
	public void endSelector(final SelectorList selector) throws CSSException {
	}

	@Override
	public void ignorableAtRule(final String arg0) throws CSSException {
	}

	@Override
	public void importStyle(final String arg0, final SACMediaList arg1, final String arg2) throws CSSException {
	}

	@Override
	public void namespaceDeclaration(final String arg0, final String arg1) throws CSSException {
	}

	@Override
	public void property(final String name, final LexicalUnit value, final boolean arg2) throws CSSException {
	}

	@Override
	public void startDocument(final InputSource arg0) throws CSSException {
	}

	@Override
	public void startFontFace() throws CSSException {
	}

	@Override
	public void startMedia(final SACMediaList arg0) throws CSSException {
	}

	@Override
	public void startPage(final String arg0, final String arg1) throws CSSException {
	}

	@Override
	public void startSelector(final SelectorList selector) throws CSSException {
	}
}
