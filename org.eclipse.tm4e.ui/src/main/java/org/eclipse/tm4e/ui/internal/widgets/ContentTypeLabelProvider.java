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
package org.eclipse.tm4e.ui.internal.widgets;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for {@link IContentType}.
 */
public final class ContentTypeLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Nullable
	@Override
	public Image getColumnImage(@Nullable final Object element, final int columnIndex) {
		return null;
	}

	@Nullable
	@Override
	public String getText(@Nullable final Object element) {
		return getColumnText(element, 0);
	}

	@Nullable
	@Override
	public String getColumnText(@Nullable final Object element, final int columnIndex) {
		return switch (columnIndex) {
			case 0 -> {
				IContentType contentType = null;
				if (element instanceof final IContentType contentTypeElement) {
					contentType = contentTypeElement;
				} else if (element instanceof final String contentTypeId) {
					contentType = Platform.getContentTypeManager().getContentType(contentTypeId);
					if (contentType == null) {
						yield contentTypeId;
					}
				} else {
					yield ""; //$NON-NLS-1$
				}
				yield contentType.getName() + " (" + contentType.getId() + ")";
			}
			default -> ""; //$NON-NLS-1$
		};
	}
}
