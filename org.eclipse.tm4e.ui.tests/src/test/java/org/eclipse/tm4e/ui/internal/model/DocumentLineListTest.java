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
package org.eclipse.tm4e.ui.internal.model;

import org.eclipse.jface.text.Document;
import org.junit.Test;

public class DocumentLineListTest {

	@Test
	public void test() {
		Document document = new Document();
		DocumentLineList lineList = new DocumentLineList(document);
		
		System.err.println(lineList.getSize());
		document.set("var");
		//document.set("var\nv");
		System.err.println(lineList.getSize());
	}
}
