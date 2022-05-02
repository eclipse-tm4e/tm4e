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
package org.eclipse.tm4e.core.internal.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public final class PListParserXML<T> implements PListParser<T> {

	private final MapFactory mapFactory;

	public PListParserXML(final MapFactory mapFactory) {
		this.mapFactory = mapFactory;
	}

	@Override
	public T parse(final InputStream contents) throws IOException, ParserConfigurationException, SAXException {
		final var spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);

		// make parser invulnerable to XXE attacks, see https://rules.sonarsource.com/java/RSPEC-2755
		spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
		spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

		final var saxParser = spf.newSAXParser();

		// make parser invulnerable to XXE attacks, see https://rules.sonarsource.com/java/RSPEC-2755
		saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

		final XMLReader xmlReader = saxParser.getXMLReader();
		xmlReader.setEntityResolver((publicId, systemId) -> new InputSource(
				new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes())));
		final var result = new PListParserContentHandler<T>(mapFactory);
		xmlReader.setContentHandler(result);
		xmlReader.parse(new InputSource(contents));
		return result.getResult();
	}
}
