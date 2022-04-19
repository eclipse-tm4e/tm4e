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
package org.eclipse.tm4e.core.internal.parser.yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.tm4e.core.internal.parser.PList;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.Yaml;

/**
 * Parses TextMate Grammar file in YAML format.
 */
public final class YamlPListParser<T> {

	private final boolean theme;

	public YamlPListParser(boolean theme) {
		this.theme = theme;
	}

	@SuppressWarnings("unchecked")
	private void addListToPList(PList<T> pList, List<Object> list) throws SAXException {
		pList.startElement(null, "array", null, null);

		for (Object item : list) {
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
	private void addMapToPList(PList<T> pList, Map<String, Object> map) throws SAXException {
		pList.startElement(null, "dict", null, null);

		for (Entry<String, Object> entry : map.entrySet()) {
			pList.startElement(null, "key", null, null);
			pList.characters(entry.getKey().toCharArray(), 0, entry.getKey().length());
			pList.endElement(null, "key", null);
			if (entry.getValue() instanceof List) {
				addListToPList(pList, (List<Object>) entry.getValue());
			} else if (entry.getValue() instanceof Map) {
				addMapToPList(pList, (Map<String, Object>) entry.getValue());
			} else {
				addStringToPList(pList, entry.getValue().toString());
			}
		}

		pList.endElement(null, "dict", null);
	}

	private void addStringToPList(PList<T> pList, String value) throws SAXException {
		pList.startElement(null, "string", null, null);
		pList.characters(value.toCharArray(), 0, value.length());
		pList.endElement(null, "string", null);
	}

	public T parse(InputStream contents) throws Exception {
		PList<T> pList = new PList<>(theme);
		addMapToPList(pList, new Yaml().loadAs(contents, Map.class));
		return pList.getResult();
	}
}
