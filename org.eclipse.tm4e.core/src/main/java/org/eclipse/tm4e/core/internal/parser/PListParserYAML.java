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
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xml.sax.SAXException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * Parses TextMate Grammar file in YAML format.
 */
public final class PListParserYAML<T> implements PListParser<T> {

	private final MapFactory mapFactory;

	public PListParserYAML(final MapFactory mapFactory) {
		this.mapFactory = mapFactory;
	}

	@SuppressWarnings("unchecked")
	private void addListToPList(final PListParserContentHandler<T> pList, final List<Object> list) throws SAXException {
		pList.startElement(null, "array", null, null);

		for (final Object item : list) {
			if (item instanceof List) {
				addListToPList(pList, (List<Object>) item);
			} else if (item instanceof Map) {
				addMapToPList(pList, (Map<String, Object>) item);
			} else {
				addStringToPList(pList, item.toString());
			}
		}

		pList.endElement(null, "array", null);
	}

	@SuppressWarnings("unchecked")
	private void addMapToPList(final PListParserContentHandler<T> pList, final Map<String, Object> map)
			throws SAXException {
		pList.startElement(null, "dict", null, null);

		for (final Entry< String, Object> entry : map.entrySet()) {
			pList.startElement(null, "key", null, null);
			pList.characters(castNonNull(entry.getKey()).toCharArray(), 0, castNonNull(entry.getKey()).length());
			pList.endElement(null, "key", null);
			if (entry.getValue() instanceof List) {
				addListToPList(pList, (List<Object>) entry.getValue());
			} else if (entry.getValue() instanceof Map) {
				addMapToPList(pList, (Map<String, Object>) entry.getValue());
			} else {
				addStringToPList(pList, castNonNull(entry.getValue()).toString());
			}
		}

		pList.endElement(null, "dict", null);
	}

	private void addStringToPList(final PListParserContentHandler<T> pList, final String value) throws SAXException {
		pList.startElement(null, "string", null, null);
		pList.characters(value.toCharArray(), 0, value.length());
		pList.endElement(null, "string", null);
	}

	@Override
	public T parse(final InputStream contents) throws SAXException, YAMLException {
		final var pList = new PListParserContentHandler<T>(mapFactory);
		addMapToPList(pList, new Yaml().loadAs(contents, Map.class));
		return pList.getResult();
	}
}
