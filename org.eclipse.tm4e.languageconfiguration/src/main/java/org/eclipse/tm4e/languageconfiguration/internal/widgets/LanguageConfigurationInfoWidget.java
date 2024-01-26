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

import static org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationMessages.*;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm4e.languageconfiguration.internal.model.CharacterPair;
import org.eclipse.tm4e.languageconfiguration.internal.model.CommentRule;
import org.eclipse.tm4e.languageconfiguration.internal.model.FoldingRules;
import org.eclipse.tm4e.languageconfiguration.internal.model.LanguageConfiguration;

@NonNullByDefault({})
public class LanguageConfigurationInfoWidget extends Composite {

	private TabItem commentsTab;
	private Text lineCommentText;
	private Text blockCommentStartText;
	private Text blockCommentEndText;

	private TabItem bracketsTab;
	private CharacterPairsTableWidget bracketsTable;

	protected TabItem autoClosingPairsTab;
	private AutoClosingPairConditionalTableWidget autoClosingPairsTable;

	private TabItem autoCloseBeforeTab;
	private Text autoCloseBeforeText;

	protected TabItem surroundingPairsTab;
	private CharacterPairsTableWidget surroundingPairsTable;

	private TabItem foldingTab;
	private Text foldingOffsideText;
	private Text foldingMarkersStartText;
	private Text foldingMarkersEndText;

	private TabItem wordPatternTab;
	private Text wordPatternText;

	protected TabItem onEnterRulesTab;
	private OnEnterRuleTableWidget onEnterRuleTable;

	public LanguageConfigurationInfoWidget(final Composite parent, final int style) {
		super(parent, style);
		super.setLayout(GridLayoutFactory.fillDefaults().create());
		super.setLayoutData(new GridData(GridData.FILL_BOTH));
		createUI(this);
	}

	private void createUI(final Composite ancestor) {
		final var folder = new TabFolder(ancestor, SWT.NONE);

		final var gd = new GridData(GridData.FILL_HORIZONTAL);
		folder.setLayoutData(gd);

		createCommentsTab(folder);
		createBracketsTab(folder);
		createAutoClosingPairsTab(folder);
		createAutoCloseBeforeTab(folder);
		createSurroundingPairsTab(folder);
		createFoldingTab(folder);
		createWordPatternTab(folder);
		createOnEnterRulesTab(folder);
	}

	public void refresh(@Nullable final LanguageConfiguration configuration) {
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
			foldingMarkersStartText.setText(folding.markersStart.pattern());
			foldingMarkersEndText.setText(folding.markersEnd.pattern());
		}

		final String wordPattern = configuration.getWordPattern();
		if (wordPattern != null) {
			wordPatternText.setText(wordPattern);
		}

		onEnterRuleTable.setInput(configuration.getOnEnterRules());
	}

	private void createCommentsTab(final TabFolder folder) {
		commentsTab = createTab(folder, LanguageConfigurationInfoWidget_comments_title);
		final Composite parent = (Composite) commentsTab.getControl();

		lineCommentText = createText(parent, LanguageConfigurationInfoWidget_lineComments);
		blockCommentStartText = createText(parent, LanguageConfigurationInfoWidget_blockCommentsStart);
		blockCommentEndText = createText(parent, LanguageConfigurationInfoWidget_blockCommentsEnd);
	}

	private void createBracketsTab(final TabFolder folder) {
		bracketsTab = createTab(folder, LanguageConfigurationInfoWidget_brackets_title);
		bracketsTable = new CharacterPairsTableWidget((Composite) bracketsTab.getControl());
	}

	protected void createAutoClosingPairsTab(final TabFolder folder) {
		autoClosingPairsTab = createTab(folder, LanguageConfigurationInfoWidget_autoClosingPairs_title);
		autoClosingPairsTable = new AutoClosingPairConditionalTableWidget((Composite) autoClosingPairsTab.getControl());
	}

	private void createAutoCloseBeforeTab(final TabFolder folder) {
		autoCloseBeforeTab = createTab(folder, LanguageConfigurationInfoWidget_autoCloseBefore_title);
		final Composite parent = (Composite) autoCloseBeforeTab.getControl();

		autoCloseBeforeText = createText(parent, LanguageConfigurationInfoWidget_autoCloseBefore_message);
	}

	protected void createSurroundingPairsTab(final TabFolder folder) {
		surroundingPairsTab = createTab(folder, LanguageConfigurationInfoWidget_surroundingPairs_title);
		surroundingPairsTable = new CharacterPairsTableWidget((Composite) surroundingPairsTab.getControl());
	}

	private void createFoldingTab(final TabFolder folder) {
		foldingTab = createTab(folder, LanguageConfigurationInfoWidget_folding_title);
		final Composite parent = (Composite) foldingTab.getControl();

		foldingOffsideText = createText(parent, LanguageConfigurationInfoWidget_offSide);
		foldingOffsideText.setToolTipText(LanguageConfigurationInfoWidget_offSide_tooltip);
		new Label(parent, SWT.NONE).setText(LanguageConfigurationInfoWidget_markers);
		foldingMarkersStartText = createText(parent, LanguageConfigurationInfoWidget_start);
		foldingMarkersEndText = createText(parent, LanguageConfigurationInfoWidget_end);
	}

	private void createWordPatternTab(final TabFolder folder) {
		wordPatternTab = createTab(folder, LanguageConfigurationInfoWidget_wordPattern_title);
		final Composite parent = (Composite) wordPatternTab.getControl();

		wordPatternText = createText(parent, LanguageConfigurationInfoWidget_wordPattern_message);
	}

	protected void createOnEnterRulesTab(final TabFolder folder) {
		onEnterRulesTab = createTab(folder, LanguageConfigurationInfoWidget_onEnterRules_title);
		onEnterRuleTable = new OnEnterRuleTableWidget((Composite) onEnterRulesTab.getControl());
	}

	private TabItem createTab(final TabFolder folder, final String title) {
		final var tab = new TabItem(folder, SWT.NONE);
		tab.setText(title);

		final var parent = new Composite(folder, SWT.NONE);
		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		tab.setControl(parent);
		return tab;
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
