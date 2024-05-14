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
package org.eclipse.tm4e.ui.internal.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IStorageEditorInput;

/**
 * {@link IContentType} utilities.
 */
public final class ContentTypeHelper {

	/**
	 * Find the content types from the given {@link IDocument} and null otherwise.
	 *
	 * @param document
	 *
	 * @return the content types from the given {@link IDocument} and null otherwise.
	 */
	@Nullable
	public static ContentTypeInfo findContentTypes(final IDocument document) {
		// Find content types from FileBuffers
		final ContentTypeInfo contentTypes = findContentTypesFromFileBuffers(document);
		if (contentTypes != null) {
			return contentTypes;
		}
		// Find content types from the IEditorInput
		return findContentTypesFromEditorInput(document);
	}

	/**
	 * Find the content type with the given contentTypeId
	 *
	 * @param contentTypeId
	 *
	 * @return matching content type or null
	 */
	@Nullable
	public static IContentType getContentTypeById(final String contentTypeId) {
		final IContentTypeManager manager = Platform.getContentTypeManager();
		return manager.getContentType(contentTypeId);
	}

	// ------------------------- Find content types from FileBuffers

	/**
	 * Find the content types from the given {@link IDocument} by using
	 * {@link ITextFileBufferManager} and null otherwise.
	 *
	 * @param document
	 *
	 * @return the content types from the given {@link IDocument} by using
	 *         {@link ITextFileBufferManager} and null otherwise.
	 */
	@Nullable
	private static ContentTypeInfo findContentTypesFromFileBuffers(final IDocument document) {
		final ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		final ITextFileBuffer buffer = bufferManager.getTextFileBuffer(document);
		if (buffer != null) {
			return getContentTypes(buffer);
		}
		return null;
	}

	/**
	 * Returns the content types from the given {@link ITextFileBuffer}.
	 *
	 * @param buffer
	 *
	 * @return the content types from the given {@link ITextFileBuffer}.
	 */
	@Nullable
	private static ContentTypeInfo getContentTypes(final ITextFileBuffer buffer) {
		try {
			final String fileName = buffer.getFileStore().getName();
			final var contentTypes = new LinkedHashSet<IContentType>();
			final IContentType bufferContentType = buffer.getContentType();
			if (bufferContentType != null) {
				contentTypes.add(bufferContentType);
			}
			if (buffer.isDirty()) {
				// Buffer is dirty (content of the filesystem is not synch with
				// the editor content), use IDocument content.
				try (var input = new DocumentInputStream(buffer.getDocument())) {
					final IContentType[] contentTypesForInput = Platform.getContentTypeManager()
							.findContentTypesFor(input, fileName);
					if (contentTypesForInput != null) {
						contentTypes.addAll(Arrays.asList(contentTypesForInput));
						return new ContentTypeInfo(fileName, contentTypes.toArray(IContentType[]::new));
					}
				}
			}

			// Buffer is synchronized with filesystem content
			try (InputStream contents = getContents(buffer)) {
				contentTypes.addAll(List.of(Platform.getContentTypeManager().findContentTypesFor(contents, fileName)));
				return new ContentTypeInfo(fileName, contentTypes.toArray(IContentType[]::new));
			}
		} catch (final Exception ex) {
			TMUIPlugin.logTrace(ex);
		}
		return null;
	}

	/**
	 * Returns the content of the given buffer.
	 *
	 * @param buffer
	 *
	 * @return the content of the given buffer.
	 *
	 * @throws CoreException
	 */
	private static InputStream getContents(final ITextFileBuffer buffer) throws CoreException {
		final IPath path = buffer.getLocation();
		if (path != null) {
			if (path.isAbsolute()) {
				final var file = path.toFile();
				if (file.exists()) {
					try {
						return new BufferedInputStream(new FileInputStream(file));
					} catch (final FileNotFoundException ex) {
						TMUIPlugin.logError(ex);
					}
				}
			} else {
				final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
				final IFile file = workspaceRoot.getFile(path);
				if (file.exists() && buffer.isSynchronized()) {
					return file.getContents();
				}
			}
		}
		return buffer.getFileStore().openInputStream(EFS.NONE, null);
	}

	// ------------------------- Find content types from FileBuffers

	/**
	 * Find the content types from the given {@link IDocument} by using
	 * {@link IEditorInput} and null otherwise.
	 *
	 * @param document
	 *
	 * @return the content types from the given {@link IDocument} by using
	 *         {@link IEditorInput} and null otherwise.
	 */
	@Nullable
	private static ContentTypeInfo findContentTypesFromEditorInput(final IDocument document) {
		final IEditorInput editorInput = getEditorInput(document);
		if (editorInput != null) {
			if (editorInput instanceof final IStorageEditorInput storageInput) {
				try {
					final IStorage storage = storageInput.getStorage();
					final String fileName = storage.getName();
					try (InputStream input = storage.getContents()) {
						final var contentTypes = Platform.getContentTypeManager().findContentTypesFor(input, fileName);
						return contentTypes == null ? null : new ContentTypeInfo(fileName, contentTypes);
					}
				} catch (final Exception ex) {
					TMUIPlugin.logTrace(ex);
					return null;
				}
			} /*else {
				// TODO: manage other type of IEditorInput
				}*/
		}
		return null;
	}

	/**
	 * Returns the {@link IEditorInput} from the given document and null otherwise.
	 *
	 * @param document
	 *
	 * @return the {@link IEditorInput} from the given document and null otherwise.
	 */
	@Nullable
	private static IEditorInput getEditorInput(final IDocument document) {
		try {
			// This utilities class is very ugly, I have not found a clean mean
			// to retrieve the IEditorInput linked to a document.
			// Here the strategy to retrieve the IEditorInput:

			// AbstractDocumentProvider#ElementInfo add a IDocumentListener to
			// the document.
			// ElementInfo contains a fElement which is the IEditorInput (like
			// ISorageEditorInput, see StorageDocumentProvider)

			// get list of IDocumentListener
			final ListenerList<?> listeners = ClassHelper.getFieldValue(document, "fDocumentListeners");
			if (listeners != null) {
				// Get AbstractDocumentProvider#ElementInfo
				final Object[] l = listeners.getListeners();
				for (int i = 0; i < l.length; i++) {
					final Object /* AbstractDocumentProvider#ElementInfo */ info = l[i];
					try {
						/* The element for which the info is stored */
						final Object input = ClassHelper.getFieldValue(info, "fElement");
						if (input instanceof final IEditorInput editorInput) {
							return editorInput;
						}
					} catch (final RuntimeException ex) {
						TMUIPlugin.logTrace(ex);
					}
				}
			}
		} catch (final RuntimeException ex) {
			TMUIPlugin.logTrace(ex);
		}
		return null;
	}

	private ContentTypeHelper() {
	}
}
