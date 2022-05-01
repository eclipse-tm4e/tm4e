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

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

final class PListParserContentHandler<T> extends DefaultHandler {

	private final MapFactory mapFactory;

	private final List<String> errors = new ArrayList<>();

	@Nullable
	private PListObject currObject;

	@Nullable
	private T result;

	private final StringBuilder text = new StringBuilder();

	PListParserContentHandler(final MapFactory mapFactory) {
		this.mapFactory = mapFactory;
	}

	@Override
	public void startElement(@Nullable final String uri, @Nullable final String localName, @Nullable final String qName,
			@Nullable final Attributes attributes) throws SAXException {
		assert localName != null;
		switch (localName) {
		case "dict":
			currObject = new PListDict(currObject, mapFactory);
			break;
		case "array":
			currObject = new PListArray(currObject);
			break;
		case "key":
			if (currObject instanceof PListDict) {
				((PListDict) currObject).setLastKey(null);
			}
			break;
		}

		text.setLength(0);
	}

	@Override
	public void endElement(@Nullable final String uri, @Nullable final String localName, @Nullable final String qName)
			throws SAXException {
		assert localName != null;

		Object value = null;

		var currObject = this.currObject;
		switch (localName) {
		case "key":
			if (!(currObject instanceof PListDict)) {
				errors.add("<key> tag can only be used inside an open <dict> element");
				return;
			}
			((PListDict) currObject).setLastKey(text.toString());
			return;
		case "dict":
		case "array":
			if (currObject == null) {
				errors.add("Closing </" + localName + "> tag found, without opening tag");
				return;
			}
			value = currObject.getValue();
			currObject = this.currObject = currObject.parent;
			break;
		case "string":
		case "data":
			value = text.toString();
			break;
		case "date":
			// e.g. <date>2007-10-25T12:36:35Z</date>
			try {
				value = ZonedDateTime.parse(text.toString());
			} catch (final DateTimeParseException ex) {
				errors.add("Failed to parse date '" + text + "'. " + ex);
				return;
			}
			break;
		case "integer":
			try {
				value = Integer.parseInt(text.toString());
			} catch (final NumberFormatException ex) {
				errors.add("Failed to parse integer '" + text + "'. " + ex);
				return;
			}
			break;
		case "real":
			try {
				value = Float.parseFloat(text.toString());
			} catch (final NumberFormatException ex) {
				errors.add("Failed to parse real as float '" + text + "'. " + ex);
				return;
			}
			break;
		case "true":
			value = Boolean.TRUE;
			break;
		case "false":
			value = Boolean.FALSE;
			break;
		case "plist":
			return;
		default:
			errors.add("Invalid tag name: " + localName);
			return;
		}

		if (currObject == null) {
			@SuppressWarnings("unchecked")
			final var t = (T) value;
			result = t;
		} else if (currObject instanceof PListDict) {
			if (((PListDict) currObject).getLastKey() != null) {
				currObject.addValue(value);
			} else {
				errors.add("Dictionary key missing for value " + value);
			}
		} else { // PListArray
			currObject.addValue(value);
		}
	}

	@Override
	public void characters(final char @Nullable [] ch, final int start, final int length) throws SAXException {
		text.append(ch, start, length);
	}

	void characters(final String chars)  {
		text.append(chars);
	}

	public T getResult() {
		assert result != null;
		return result;
	}
}
