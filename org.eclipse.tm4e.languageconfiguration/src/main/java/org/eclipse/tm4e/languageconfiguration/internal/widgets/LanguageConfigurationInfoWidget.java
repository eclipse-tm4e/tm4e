/**
 * Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.widgets;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.lazyNonNull;
import static org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationMessages.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm4e.languageconfiguration.internal.model.CharacterPair;
import org.eclipse.tm4e.languageconfiguration.internal.model.CommentRule;
import org.eclipse.tm4e.languageconfiguration.internal.model.FoldingRules;
import org.eclipse.tm4e.languageconfiguration.internal.model.LanguageConfiguration;

public class LanguageConfigurationInfoWidget extends Composite {

	private final String[] tabs = {
		LanguageConfigurationInfoWidget_comments_title,
		LanguageConfigurationInfoWidget_brackets_title,
		LanguageConfigurationInfoWidget_autoClosingPairs_title,
		LanguageConfigurationInfoWidget_autoCloseBefore_title,
		LanguageConfigurationInfoWidget_surroundingPairs_title,
		LanguageConfigurationInfoWidget_folding_title,
		LanguageConfigurationInfoWidget_wordPattern_title,
		LanguageConfigurationInfoWidget_onEnterRules_title,
		LanguageConfigurationInfoWidget_indentationRules_title,
		LanguageConfigurationInfoWidget_colorizedBracketPairs_title,
	};

	private Text lineCommentText = lazyNonNull();
	private Text blockCommentStartText = lazyNonNull();
	private Text blockCommentEndText = lazyNonNull();

	private CharacterPairsTableWidget bracketsTable = lazyNonNull();

	private AutoClosingPairConditionalTableWidget autoClosingPairsTable = lazyNonNull();

	private Text autoCloseBeforeText = lazyNonNull();

	private CharacterPairsTableWidget surroundingPairsTable = lazyNonNull();

	private Text foldingOffsideText = lazyNonNull();
	private Text foldingMarkersStartText = lazyNonNull();
	private Text foldingMarkersEndText = lazyNonNull();

	private Text wordPatternText = lazyNonNull();

	private OnEnterRuleTableWidget onEnterRuleTable = lazyNonNull();

	private Text indentationDecreaseIndentPattern = lazyNonNull();
	private Text indentationIncreaseIndentPattern = lazyNonNull();
	private Text indentationIndentNextLinePattern = lazyNonNull();
	private Text indentationUnIndentedLinePattern = lazyNonNull();

	private CharacterPairsTableWidget colorizedBracketPairsTable = lazyNonNull();

	public LanguageConfigurationInfoWidget(final Composite parent, final int style) {
		super(parent, style);
		super.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).spacing(-1, 0).equalWidth(false).create());
		super.setLayoutData(new GridData(GridData.FILL_BOTH));
		createUI(this);
	}

	private void createUI(final Composite parent) {
		final var tableViewer = new TableViewer(parent, SWT.BORDER | SWT.NO_SCROLL);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setInput(tabs);

		final var table = tableViewer.getTable();

		final GC gc = new GC(table.getShell());
		gc.setFont(JFaceResources.getDialogFont());
		int maxLabelWidth = 0;
		for (final String tab : tabs) {
			final int width = gc.stringExtent(tab).x;
			if (width > maxLabelWidth)
				maxLabelWidth = width;
		}
		maxLabelWidth += 10;
		gc.dispose();
		final var gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.widthHint = maxLabelWidth;
		table.setLayoutData(gd);
		table.setHeaderVisible(false);
		table.setLinesVisible(false);

		final var column = new TableColumn(table, SWT.NONE);
		column.setWidth(maxLabelWidth);
		column.setResizable(false);

		final var stackLayout = new StackLayout();
		final var stack = new Composite(parent, SWT.FILL);
		stack.setLayout(stackLayout);
		stack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final List<Consumer<Composite>> stackLayerDecorators = List.of(
				this::createCommentsInfo,
				this::createBracketsInfo,
				this::createAutoClosingPairsInfo,
				this::createAutoCloseBeforeInfo,
				this::createSurroundingPairsInfo,
				this::createFoldingInfo,
				this::createWordPatternInfo,
				this::createOnEnterRulesInfo,
				this::createIndentationRulesInfo,
				this::createColorizedBracketPairsInfo);

		final var stackLayers = stackLayerDecorators.stream().map(layerDecorator -> {
			final var layer = new Composite(stack, SWT.BORDER);
			layer.setLayout(GridLayoutFactory.swtDefaults().create());
			layer.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
			layer.setBackgroundMode(SWT.INHERIT_DEFAULT);
			layerDecorator.accept(layer);
			return layer;
		}).toArray(Composite[]::new);

		tableViewer.addSelectionChangedListener(event -> {
			final var selection = (StructuredSelection) event.getSelection();
			if (!selection.isEmpty()) {
				stackLayout.topControl = stackLayers[table.getSelectionIndex()];
				stack.layout();
			}
		});
		tableViewer.setSelection(new StructuredSelection(tabs[0]));
	}

	public void refresh(final @Nullable LanguageConfiguration configuration) {
		lineCommentText.setText("");
		blockCommentStartText.setText("");
		blockCommentEndText.setText("");
		bracketsTable.setInput(null);
		autoClosingPairsTable.setInput(null);
		autoCloseBeforeText.setText("");
		surroundingPairsTable.setInput(null);
		foldingOffsideText.setText("");
		foldingMarkersStartText.setText("");
		foldingMarkersEndText.setText("");
		wordPatternText.setText("");
		onEnterRuleTable.setInput(null);
		indentationDecreaseIndentPattern.setText("");
		indentationIncreaseIndentPattern.setText("");
		indentationIndentNextLinePattern.setText("");
		indentationUnIndentedLinePattern.setText("");
		colorizedBracketPairsTable.setInput(null);

		if (configuration == null)
			return;

		final CommentRule comments = configuration.getComments();
		if (comments != null) {
			lineCommentText.setText(Objects.toString(comments.lineComment, ""));
			final CharacterPair blockComment = comments.blockComment;
			if (blockComment != null) {
				blockCommentStartText.setText(blockComment.open);
				blockCommentEndText.setText(blockComment.close);
			}
		}

		bracketsTable.setInput(configuration.getBrackets());

		autoClosingPairsTable.setInput(configuration.getAutoClosingPairs());

		final String autoCloseBefore = configuration.getAutoCloseBefore();
		if (autoCloseBefore != null) {
			autoCloseBeforeText.setText(autoCloseBefore);
		}

		surroundingPairsTable.setInput(configuration.getSurroundingPairs());

		final FoldingRules folding = configuration.getFolding();
		if (folding != null) {
			foldingOffsideText.setText(Boolean.toString(folding.offSide));
			foldingMarkersStartText.setText(folding.markers.start.pattern());
			foldingMarkersEndText.setText(folding.markers.end.pattern());
		}

		final String wordPattern = configuration.getWordPattern();
		if (wordPattern != null) {
			wordPatternText.setText(wordPattern);
		}

		onEnterRuleTable.setInput(configuration.getOnEnterRules());

		final var indentationRules = configuration.getIndentationRules();
		if (indentationRules != null) {
			indentationDecreaseIndentPattern.setText(indentationRules.decreaseIndentPattern.toString());
			indentationIncreaseIndentPattern.setText(indentationRules.increaseIndentPattern.toString());
			indentationIndentNextLinePattern.setText(Objects.toString(indentationRules.indentNextLinePattern, ""));
			indentationUnIndentedLinePattern.setText(Objects.toString(indentationRules.unIndentedLinePattern, ""));
		}

		colorizedBracketPairsTable.setInput(configuration.getColorizedBracketPairs());
	}

	private void createCommentsInfo(final Composite parent) {
		lineCommentText = createText(parent, LanguageConfigurationInfoWidget_lineComments);
		blockCommentStartText = createText(parent, LanguageConfigurationInfoWidget_blockCommentsStart);
		blockCommentEndText = createText(parent, LanguageConfigurationInfoWidget_blockCommentsEnd);
	}

	private void createBracketsInfo(final Composite parent) {
		bracketsTable = new CharacterPairsTableWidget(parent);
	}

	protected void createAutoClosingPairsInfo(final Composite parent) {
		autoClosingPairsTable = new AutoClosingPairConditionalTableWidget(parent);
	}

	private void createAutoCloseBeforeInfo(final Composite parent) {
		autoCloseBeforeText = createText(parent, LanguageConfigurationInfoWidget_autoCloseBefore_message);
	}

	protected void createSurroundingPairsInfo(final Composite parent) {
		surroundingPairsTable = new CharacterPairsTableWidget(parent);
	}

	private void createFoldingInfo(final Composite parent) {
		foldingOffsideText = createText(parent, LanguageConfigurationInfoWidget_offSide);
		foldingOffsideText.setToolTipText(LanguageConfigurationInfoWidget_offSide_tooltip);
		new Label(parent, SWT.NONE).setText(LanguageConfigurationInfoWidget_markers);
		foldingMarkersStartText = createText(parent, LanguageConfigurationInfoWidget_start);
		foldingMarkersEndText = createText(parent, LanguageConfigurationInfoWidget_end);
	}

	private void createWordPatternInfo(final Composite parent) {
		wordPatternText = createText(parent, LanguageConfigurationInfoWidget_wordPattern_message);
	}

	protected void createOnEnterRulesInfo(final Composite parent) {
		onEnterRuleTable = new OnEnterRuleTableWidget(parent);
	}

	protected void createIndentationRulesInfo(final Composite parent) {
		indentationDecreaseIndentPattern = createText(parent, LanguageConfigurationInfoWidget_indentationRules_decreaseIndentPattern);
		indentationIncreaseIndentPattern = createText(parent, LanguageConfigurationInfoWidget_indentationRules_increaseIndentPattern);
		indentationIndentNextLinePattern = createText(parent, LanguageConfigurationInfoWidget_indentationRules_indentNextLinePattern);
		indentationUnIndentedLinePattern = createText(parent, LanguageConfigurationInfoWidget_indentationRules_unIndentedLinePattern);
	}

	private void createColorizedBracketPairsInfo(final Composite parent) {
		colorizedBracketPairsTable = new CharacterPairsTableWidget(parent);
	}

	private Text createText(final Composite parent, final String s) {
		final var label = new Label(parent, SWT.NONE);
		label.setText(s);

		final var text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.setEditable(false);
		return text;
	}
}
