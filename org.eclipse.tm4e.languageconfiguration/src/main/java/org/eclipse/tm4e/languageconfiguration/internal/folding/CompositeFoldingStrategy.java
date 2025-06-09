/**
 * Copyright (c) 2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Sebastian Thomschke (Vegard IT) - initial implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.folding;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerLifecycle;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;

/**
 * Applies annotations from both the indent‐based and the language‐configuration‐based
 * folding strategies for a single Eclipse editor.
 * <p>
 * Since only one {@code foldingReconcilingStrategy} can be active per editor
 * at a time, this class delegates to {@link IndentationFoldingStrategy} and {@link TMFoldingStrategy}.
 * </p>
 */
public final class CompositeFoldingStrategy
		implements IReconcilingStrategy, IReconcilingStrategyExtension, ITextViewerLifecycle {

	private static final List<AbstractFoldingStrategy> DELEGATES = List.of(
			new IndentationFoldingStrategy(),
			new TMFoldingStrategy());

	public CompositeFoldingStrategy() {
	}

	@Override
	public void initialReconcile() {
		for (final AbstractFoldingStrategy delegate : DELEGATES) {
			delegate.initialReconcile();
		}
	}

	@Override
	public void install(final ITextViewer textViewer) {
		for (final AbstractFoldingStrategy delegate : DELEGATES) {
			delegate.install(textViewer);
		}
	}

	@Override
	public void reconcile(final DirtyRegion dirtyRegion, final @Nullable IRegion subRegion) {
		for (final AbstractFoldingStrategy delegate : DELEGATES) {
			delegate.reconcile(dirtyRegion, subRegion);
		}
	}

	@Override
	public void reconcile(final IRegion partition) {
		for (final AbstractFoldingStrategy delegate : DELEGATES) {
			delegate.reconcile(partition);
		}
	}

	@Override
	public void setDocument(final @Nullable IDocument document) {
		for (final AbstractFoldingStrategy delegate : DELEGATES) {
			delegate.setDocument(document);
		}
	}

	@Override
	public void setProgressMonitor(final @Nullable IProgressMonitor monitor) {
		for (final AbstractFoldingStrategy delegate : DELEGATES) {
			delegate.setProgressMonitor(monitor);
		}
	}

	@Override
	public void uninstall() {
		for (final AbstractFoldingStrategy delegate : DELEGATES) {
			delegate.uninstall();
		}
	}
}
