/**
 * Copyright (c) 2015-2018 Angelo ZERR.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.xml.sax.SAXException;

import com.google.gson.stream.JsonReader;

public final class PListParserJSON<T> implements PListParser<T> {

	private final MapFactory mapFactory;

	public PListParserJSON(final MapFactory mapFactory) {
		this.mapFactory = mapFactory;
	}

	@Override
	public T parse(final InputStream contents) throws IOException, SAXException {
		final var pList = new PListParserContentHandler<T>(mapFactory);
		try (final var reader = new JsonReader(new InputStreamReader(contents, StandardCharsets.UTF_8))) {
			// reader.setLenient(true);
			boolean parsing = true;
			while (parsing) {
				final var nextToken = reader.peek();
				switch (nextToken) {
				case BEGIN_ARRAY:
					pList.startElement(null, "array", null, null);
					reader.beginArray();
					break;
				case END_ARRAY:
					pList.endElement(null, "array", null);
					reader.endArray();
					break;
				case BEGIN_OBJECT:
					pList.startElement(null, "dict", null, null);
					reader.beginObject();
					break;
				case END_OBJECT:
					pList.endElement(null, "dict", null);
					reader.endObject();
					break;
				case NAME:
					final var lastName = reader.nextName();
					pList.startElement(null, "key", null, null);
					pList.characters(lastName.toCharArray(), 0, lastName.length());
					pList.endElement(null, "key", null);
					break;
				case NULL:
					reader.nextNull();
					break;
				case BOOLEAN:
					reader.nextBoolean();
					break;
				case NUMBER:
					reader.nextLong();
					break;
				case STRING:
					final var value = reader.nextString();
					pList.startElement(null, "string", null, null);
					pList.characters(value.toCharArray(), 0, value.length());
					pList.endElement(null, "string", null);
					break;
				case END_DOCUMENT:
					parsing = false;
					break;
				default:
					break;
				}
			}
		}
		return pList.getResult();
	}
}
