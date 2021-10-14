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
package org.eclipse.tm4e.ui.internal.text;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.tm4e.ui.text.ITMPresentationReconcilerListener;

public class StyleRangesCollector implements ITMPresentationReconcilerListener {

	private IDocument document;
	private Command command;
	private Integer waitForToLineNumber;
	private boolean isFinished;
	
	private StringBuilder currentRanges;

	private Object lock = new Object();

	public StyleRangesCollector() {

	}

	@Override
	public void install(ITextViewer viewer, IDocument document) {
		this.document = document;
	}

	@Override
	public void uninstall() {
		this.document = null;
	}

	@Override
	public void colorize(TextPresentation presentation, Throwable error) {
		add(presentation);
		if (waitForToLineNumber != null) {
			int offset = presentation.getExtent().getOffset() + presentation.getExtent().getLength();
			try {
				if (waitForToLineNumber != document.getLineOfOffset(offset)) {
					return;
				} else {
					waitForToLineNumber = null;
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		((Command) command).setStyleRanges("[" + currentRanges.toString() + "]");
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	private String add(TextPresentation presentation) {
		Iterator<StyleRange> ranges = presentation.getAllStyleRangeIterator();
		while(ranges.hasNext()) {
			if (currentRanges.length() > 0) {
				currentRanges.append(", ");
			}
			currentRanges.append(ranges.next());
		}
		return null;
	}

	public static String toString(StyleRange[] ranges) {
		return Arrays.asList(ranges).toString();
	}

	public void executeCommand(Command command) {
		setCommand(command);
		if (command.getStyleRanges() == null) {
			synchronized (lock) {
				try {
					wait(command);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void wait(Command command) throws InterruptedException {
		lock.wait();
		if (command.getStyleRanges() == null) {
			wait(command);
		}
	}

	public void setCommand(Command command) {
		this.currentRanges = new StringBuilder();
		this.command = command;
		this.waitForToLineNumber = command.getLineTo();
		command.execute();
	}

	public boolean isFinished() {
		return isFinished;
	}

}