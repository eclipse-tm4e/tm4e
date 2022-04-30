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
package org.eclipse.tm4e.core.model;

import static java.lang.System.Logger.Level.*;

import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract class for Model lines used by the TextMate model.
 *
 * Implementation class must:
 * <ul>
 * <li>synchronizes lines with the lines of the editor content when it changed.</li>
 * <li>call {@link AbstractLineList#invalidateLine(int)} with the first changed line.</li>
 * </ul>
 */
public abstract class AbstractLineList implements IModelLines {

	private static final Logger LOGGER = System.getLogger(AbstractLineList.class.getName());

	private final List<ModelLine> list = Collections.synchronizedList(new ArrayList<>());

	@Nullable
	private TMModel model;

	void setModel(@Nullable final TMModel model) {
		this.model = model;
	}

	@Override
	public void addLine(final int line) {
		try {
			this.list.add(line, new ModelLine());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void removeLine(final int line) {
		this.list.remove(line);
	}

	@Override
	public void updateLine(final int line) {
		try {
			// TODO this.list.get(line).text = this.lineToTextResolver.apply(line);
		} catch (final Exception ex) {
			LOGGER.log(ERROR, ex.getMessage(), ex);
		}
	}

	@Override
	public ModelLine get(final int index) {
		return this.list.get(index);
	}

	@Override
	public void forEach(final Consumer<ModelLine> consumer) {
		this.list.forEach(consumer);
	}

	protected void invalidateLine(final int lineIndex) {
		if (model != null) {
			model.invalidateLine(lineIndex);
		}
	}

	@Override
	@Deprecated
	public int getSize() {
		return getNumberOfLines();
	}
}