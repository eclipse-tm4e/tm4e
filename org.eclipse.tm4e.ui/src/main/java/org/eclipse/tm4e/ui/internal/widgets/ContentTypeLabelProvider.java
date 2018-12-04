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
package org.eclipse.tm4e.ui.internal.widgets;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for {@link IContentType}.
 */
public class ContentTypeLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getText(Object element) {
		return getColumnText(element, 0);
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		String contentTypeId = (String) element;
		switch (columnIndex) {
		case 0:
			IContentType contentType = Platform.getContentTypeManager().getContentType(contentTypeId);
			if (contentType == null) {
				return contentTypeId;
			}
			return contentType.getName() + " (" + contentType.getId() + ")";
		default:
			return ""; //$NON-NLS-1$
		}
	}
}