/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.model;

import java.util.function.Consumer;

/**
 * Mode lines API which must be initalize with a document and changed of
 * document.
 *
 */
public interface IModelLines {

	/**
	 * Add a new line at specified index line.
	 * 
	 * @param line
	 */
	void addLine(int line);

	/**
	 * Remove the line at specified index line.
	 * 
	 * @param line
	 */
	void removeLine(int line);

	/**
	 * Mark as line is updated.
	 * 
	 * @param line
	 */
	void updateLine(int line);

	/**
	 * Return lines size.
	 * 
	 * @return lines size.
	 */
	int getSize();

	ModelLine get(int index);
	
	void forEach(Consumer<ModelLine> consumer);
	
	int getNumberOfLines();

	String getLineText(int line) throws Exception;

	int getLineLength(int line) throws Exception;

	void dispose();
	
	
}
