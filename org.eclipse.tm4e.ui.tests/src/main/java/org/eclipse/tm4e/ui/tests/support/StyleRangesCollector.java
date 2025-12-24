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
package org.eclipse.tm4e.ui.tests.support;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.tm4e.ui.tests.support.command.Command;
import org.eclipse.tm4e.ui.text.ITMPresentationReconcilerListener;

final class StyleRangesCollector implements ITMPresentationReconcilerListener {

	private IDocument document;
	private Command command;
	private Integer waitForToLineNumber;
	private StringBuilder currentRanges;

	private final Object lock = new Object();

	@Override
	public void onInstalled(final ITextViewer viewer, final IDocument document) {
		this.document = document;
	}

	@Override
	public void onUninstalled() {
		this.document = null;
	}

	@Override
	public void onColorized(final TextPresentation presentation, final Throwable error) {
		add(presentation);
		if (waitForToLineNumber != null) {
			final int endOffsetExclusive = presentation.getExtent().getOffset() + presentation.getExtent().getLength();
			// The extent end offset is exclusive. If it points to the start of the next line,
			// document.getLineOfOffset(endOffsetExclusive) would already return that next line
			// even though the presentation does not actually cover it.
			// We therefore check the line number of the last included character.
			final int offset = presentation.getExtent().getLength() > 0 ? endOffsetExclusive - 1 : endOffsetExclusive;
			try {
				if (waitForToLineNumber != document.getLineOfOffset(offset)) {
					return;
				}
				waitForToLineNumber = null;
			} catch (final BadLocationException e) {
				e.printStackTrace();
			}
		}
		command.setStyleRanges("[" + currentRanges.toString() + "]");
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	private String add(final TextPresentation presentation) {
		final Iterator<StyleRange> ranges = presentation.getAllStyleRangeIterator();
		while (ranges.hasNext()) {
			if (currentRanges.length() > 0) {
				currentRanges.append(", ");
			}
			currentRanges.append(ranges.next());
		}
		return null;
	}

	static String toString(final StyleRange[] ranges) {
		return Arrays.asList(ranges).toString();
	}

	void executeCommandSync(final Command command) {
		executeCommand(command);
		if (command.getStyleRanges() == null) {
			synchronized (lock) {
				try {
					wait(command);
				} catch (final InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	void wait(final Command command) throws InterruptedException {
		lock.wait();
		if (command.getStyleRanges() == null) {
			wait(command);
		}
	}

	void executeCommand(final Command command) {
		this.currentRanges = new StringBuilder();
		this.command = command;
		this.waitForToLineNumber = command.getLineTo();
		command.execute();
	}
}
