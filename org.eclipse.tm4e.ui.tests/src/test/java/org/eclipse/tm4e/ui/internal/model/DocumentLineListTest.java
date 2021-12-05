/**
 * Copyright (c) 2015-2019 Angelo ZERR, and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.ui.internal.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.jface.text.Document;
import org.junit.jupiter.api.Test;

public class DocumentLineListTest {

	@Test
	public void testMultiLineChange() {
		Document document = new Document();
		DocumentLineList lineList = new DocumentLineList(document);

		document.set("a\nb\nc\nd");
		assertEquals(4, lineList.getNumberOfLines());
		assertNotNull(lineList.get(3));

		document.set("a\nb");
		assertEquals(2, lineList.getNumberOfLines());
	}

}
