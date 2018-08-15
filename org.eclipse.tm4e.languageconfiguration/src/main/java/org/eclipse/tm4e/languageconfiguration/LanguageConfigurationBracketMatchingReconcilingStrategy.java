/**
 *  Copyright (c) 2018 Red Hat Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationPlugin;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.languageconfiguration.internal.preferences.PreferenceConstants;
import org.eclipse.tm4e.languageconfiguration.internal.preferences.PreferenceHelper;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CharacterPair;
import org.eclipse.tm4e.languageconfiguration.internal.utils.TextUtils;
import org.eclipse.tm4e.ui.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.utils.ContentTypeInfo;

public class LanguageConfigurationBracketMatchingReconcilingStrategy
		implements IReconcilingStrategy, IPreferenceChangeListener, IReconcilingStrategyExtension {
	public static final String BRACKET_MATCHING_TYPE = "org.eclipse.tm4e.languageconfiguration.bracketmatching"; //$NON-NLS-1$
	private boolean enabled;
	private ISourceViewer sourceViewer;
	private IDocument document;
	private IContentType[] contentTypes;
	private final Annotation bracketMatchingAnnotation = new Annotation(BRACKET_MATCHING_TYPE, false, null);

	private void updateAnnotations(ISelection selection) {
		if (!(selection instanceof ITextSelection)) {
			removeAnnotation();
			return;
		}
		ITextSelection textSelection = (ITextSelection) selection;
		IContentType[] contentTypes = findContentTypes(document);
		if (contentTypes == null) {
			removeAnnotation();
			return;
		}
		IAnnotationModel annotationModel = sourceViewer.getAnnotationModel();
		if (annotationModel == null) {
			removeAnnotation();
			return;
		}
		int offset = textSelection.getOffset();
		LanguageConfigurationRegistryManager registry = LanguageConfigurationRegistryManager.getInstance();
		for (IContentType contentType : contentTypes) {
			if (!registry.shouldSurroundingPairs(document, offset, contentType)) {
				continue;
			}
			List<CharacterPair> surroundingPairs = registry.getSurroundingPairs(contentType);
			for (CharacterPair surroundingPair : surroundingPairs) {
				Position positionOfMatchingPair = getPositionOfMatchingPair(document.get(), offset, surroundingPair);
				if (positionOfMatchingPair != null) {
					Position oldPosition = annotationModel.getPosition(bracketMatchingAnnotation);
					if (oldPosition == null || !oldPosition.equals(positionOfMatchingPair)) {
						removeAnnotation();
						annotationModel.addAnnotation(bracketMatchingAnnotation, positionOfMatchingPair);
					}
					return;
				}
			}
		}
		removeAnnotation();
	}

	private Position getPositionOfMatchingPair(String text, int offset, CharacterPair surroundingPair) {
		String openning = surroundingPair.getKey();
		String closing = surroundingPair.getValue();
		int startOfMatchingPair = -1;
		int lengthOfMatchingPair = -1;
		int indexOfClose = TextUtils.startIndexOfOffsetTouchingString(text, offset, closing);
		if (indexOfClose != -1) {
			startOfMatchingPair = findOpenning(text, indexOfClose, surroundingPair);
			lengthOfMatchingPair = closing.length();
		} else {
			int indexOfOpen = TextUtils.startIndexOfOffsetTouchingString(text, offset, openning);
			if (indexOfOpen != -1) {
				startOfMatchingPair = findClosing(text, indexOfOpen, surroundingPair);
				lengthOfMatchingPair = openning.length();
			}
		}
		if (startOfMatchingPair == -1) {
			return null;
		}
		return new Position(startOfMatchingPair, lengthOfMatchingPair);
	}

	/**
	 * Finds the beginning index of the closing that matches the given openning
	 * index
	 *
	 * @param text            of the document
	 * @param offset          before the openning
	 * @param surroundingPair the openning, closing pair
	 * @return the ending index of the closing
	 */
	private int findClosing(String text, int offset, CharacterPair surroundingPair) {
		String openning = surroundingPair.getKey();
		String closing = surroundingPair.getValue();
		if (openning.equals(closing)) {
			return findMatchingforDuplicatePairs(text, offset, openning);
		}
		int closeOffset = offset + surroundingPair.getKey().length();
		int counter = 1;
		while (counter > 0 && closeOffset <= text.length() + closing.length()) {
			int nextOpenIndex = text.indexOf(openning, closeOffset);
			int nextCloseIndex = text.indexOf(closing, closeOffset);
			if (nextCloseIndex == -1) {
				break;
			}
			if (nextOpenIndex == -1 || nextCloseIndex <= nextOpenIndex) {
				counter--;
				closeOffset = nextCloseIndex + closing.length();
			} else {
				counter++;
				closeOffset = nextOpenIndex + openning.length();
			}
		}
		if (counter == 0) {
			return closeOffset - closing.length();
		}
		return -1;
	}

	/**
	 * Finds the beginning index of the openning that matches the given openning
	 * index
	 *
	 * @param text            of the document
	 * @param offset          before the closing
	 * @param surroundingPair the openning, closing pair
	 * @return the beginning index of the openning
	 */
	private int findOpenning(String text, int offset, CharacterPair surroundingPair) {
		String openning = surroundingPair.getKey();
		String closing = surroundingPair.getValue();
		if (openning.equals(closing)) {
			return findMatchingforDuplicatePairs(text, offset, openning);
		}
		int openOffset = offset;
		int counter = 1;
		while (counter > 0 && openOffset >= 0) {
			int previousOpenIndex = text.lastIndexOf(openning, openOffset - 1);
			int previousCloseIndex = text.lastIndexOf(closing, openOffset - 1);
			if (previousOpenIndex == -1 && previousCloseIndex == -1) {
				break;
			}
			if (previousOpenIndex > previousCloseIndex) {
				counter--;
				openOffset = previousOpenIndex;
			} else {
				counter++;
				openOffset = previousCloseIndex;
			}
		}
		if (counter == 0) {
			return openOffset;
		}
		return -1;
	}

	/**
	 * Finds the beginning index of the matching pair when both of the pairs are the
	 * same.
	 *
	 * @param text     of the document
	 * @param index    before the current character
	 * @param instance of the surrounding pair
	 * @return
	 */
	private int findMatchingforDuplicatePairs(String text, int index, String instance) {
		String prefix = text.substring(0, index);
		int matchesBeforeIndex = prefix.split(Pattern.quote(instance), -1).length - 1;
		if (matchesBeforeIndex % 2 == 0) {
			String suffix = text.substring(index + instance.length(), text.length());
			int matchIndex = suffix.indexOf(instance);
			return matchIndex == -1 ? -1 : (matchIndex + index + instance.length());
		}
		return prefix.lastIndexOf(instance);
	}

	private IContentType[] findContentTypes(IDocument document) {
		if (contentTypes != null && this.document != null && this.document.equals(document)) {
			return contentTypes;
		}
		try {
			ContentTypeInfo info = ContentTypeHelper.findContentTypes(document);
			this.contentTypes = info.getContentTypes();
			this.document = document;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return contentTypes;
	}

	class EditorSelectionChangedListener implements ISelectionChangedListener {
		public void install(ISelectionProvider selectionProvider) {
			if (selectionProvider == null) {
				return;
			}
			if (selectionProvider instanceof IPostSelectionProvider) {
				IPostSelectionProvider provider = (IPostSelectionProvider) selectionProvider;
				provider.addPostSelectionChangedListener(this);
			} else {
				selectionProvider.addSelectionChangedListener(this);
			}
		}

		public void uninstall(ISelectionProvider selectionProvider) {
			if (selectionProvider == null) {
				return;
			}
			if (selectionProvider instanceof IPostSelectionProvider) {
				IPostSelectionProvider provider = (IPostSelectionProvider) selectionProvider;
				provider.removePostSelectionChangedListener(this);
			} else {
				selectionProvider.removeSelectionChangedListener(this);
			}
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			updateAnnotations(event.getSelection());
		}
	}

	private EditorSelectionChangedListener editorSelectionChangedListener;

	public void install(ITextViewer viewer) {
		if (!(viewer instanceof ISourceViewer)) {
			return;
		}
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(LanguageConfigurationPlugin.PLUGIN_ID);
		preferences.addPreferenceChangeListener(this);
		this.enabled = preferences.getBoolean(PreferenceConstants.LANGUAGE_CONFIGURATIONS, true);
		this.sourceViewer = (ISourceViewer) viewer;
		editorSelectionChangedListener = new EditorSelectionChangedListener();
		editorSelectionChangedListener.install(sourceViewer.getSelectionProvider());
	}

	public void uninstall() {
		if (sourceViewer != null) {
			editorSelectionChangedListener.uninstall(sourceViewer.getSelectionProvider());
		}
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(LanguageConfigurationPlugin.PLUGIN_ID);
		preferences.removePreferenceChangeListener(this);
	}

	@Override
	public void setDocument(IDocument document) {
		this.document = document;
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getKey().equals(PreferenceConstants.LANGUAGE_CONFIGURATIONS)) {
			ILanguageConfigurationDefinition[] definitions = PreferenceHelper
					.loadLanguageConfigurationDefinitions(event.getNewValue().toString());
			List<IContentType> contentTypes = Arrays.asList(findContentTypes(document));
			this.enabled = false;
			if (contentTypes != null) {
				for (ILanguageConfigurationDefinition definition : definitions) {
					if (contentTypes.contains(definition.getContentType())
							&& definition.isBracketAutoClosingEnabled()) {
						this.enabled = true;
						break;
					}
				}
			}
			if (enabled) {
				initialReconcile();
			} else {
				removeAnnotation();
			}
		}
	}

	void removeAnnotation() {
		IAnnotationModel annotationModel = sourceViewer.getAnnotationModel();
		if (annotationModel == null) {
			return;
		}
		annotationModel.removeAnnotation(bracketMatchingAnnotation);
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		// Do nothing
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		// Do nothing
	}

	@Override
	public void reconcile(IRegion partition) {
		// Do nothing
	}

	@Override
	public void initialReconcile() {
		if (sourceViewer != null) {
			ISelectionProvider selectionProvider = sourceViewer.getSelectionProvider();
			final StyledText textWidget = sourceViewer.getTextWidget();
			if (textWidget != null && selectionProvider != null) {
				textWidget.getDisplay().asyncExec(() -> {
					if (!textWidget.isDisposed()) {
						updateAnnotations(selectionProvider.getSelection());
					}
				});
			}
		}
	}
}