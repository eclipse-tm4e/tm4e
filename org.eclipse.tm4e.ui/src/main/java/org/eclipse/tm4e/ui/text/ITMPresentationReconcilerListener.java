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
package org.eclipse.tm4e.ui.text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;

/**
 * TextMate presentation reconciler listener.
 *
 */
public interface ITMPresentationReconcilerListener {

	/**
	 * Install the given viewer and document.
	 * 
	 * @param viewer
	 * @param document
	 */
	void install(ITextViewer viewer, IDocument document);

	/**
	 * Uninstall.
	 */
	void uninstall();

	/**
	 * Colorize the StyledText with the given text presentation.
	 * 
	 * @param presentation
	 *            the text presentation.
	 * @param error
	 *            when there are error.
	 */
	void colorize(TextPresentation presentation, Throwable error);
}
