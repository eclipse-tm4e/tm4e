/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke - initial implementation
 */
package org.eclipse.tm4e.core.internal.parser;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.xml.sax.SAXException;

/**
 * Parses TextMate Grammar file in YAML format.
 */
public final class PListParserYAML<T> implements PListParser<T> {

	private final PropertySettable.Factory<PListPath> objectFactory;

	public PListParserYAML(final PropertySettable.Factory<PListPath> objectFactory) {
		this.objectFactory = objectFactory;
	}

	@SuppressWarnings("unchecked")
	private void addListToPList(final PListContentHandler<T> pList, final List<Object> list) throws SAXException {
		pList.startElement(null, "array", null, null);

		for (final Object item : list) {
			if (item instanceof final List items) {
				addListToPList(pList, items);
			} else if (item instanceof final Map map) {
				addMapToPList(pList, map);
			} else {
				addStringToPList(pList, item.toString());
			}
		}

		pList.endElement(null, "array", null);
	}

	@SuppressWarnings("unchecked")
	private void addMapToPList(final PListContentHandler<T> pList, final Map<String, Object> map) throws SAXException {
		pList.startElement(null, "dict", null, null);

		for (final Entry<String, Object> entry : map.entrySet()) {
			pList.startElement(null, "key", null, null);
			pList.characters(entry.getKey());
			pList.endElement(null, "key", null);
			if (entry.getValue() instanceof final List list) {
				addListToPList(pList, list);
			} else if (entry.getValue() instanceof final Map valueMap) {
				addMapToPList(pList, valueMap);
			} else {
				addStringToPList(pList, castNonNull(entry.getValue()).toString());
			}
		}

		pList.endElement(null, "dict", null);
	}

	private void addStringToPList(final PListContentHandler<T> pList, final String value) throws SAXException {
		pList.startElement(null, "string", null, null);
		pList.characters(value);
		pList.endElement(null, "string", null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T parse(final Reader contents) throws SAXException {
		final var yamlLoader = new Load(LoadSettings.builder().build());
		final var pList = new PListContentHandler<T>(objectFactory);
		pList.startElement(null, "plist", null, null);
		addMapToPList(pList, (Map<String, Object>) yamlLoader.loadFromReader(contents));
		pList.endElement(null, "plist", null);
		return pList.getResult();
	}
}
