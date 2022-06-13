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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm4e.languageconfiguration.internal.ILanguageConfiguration;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CharacterPair;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CommentRule;
import org.eclipse.tm4e.languageconfiguration.internal.supports.FoldingRule;

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

	protected TabItem surroundingPairsTab;
	private CharacterPairsTableWidget surroundingPairsTable;

	private TabItem foldingTab;
	private Text offsideText;
	private Text markersStartText;
	private Text markersEndText;

	private TabItem wordPatternTab;

	private Text wordPatternText;

	protected TabItem onEnterRulesTab;
	private OnEnterRuleTableWidget onEnterRuleTable;

	public LanguageConfigurationInfoWidget(final Composite parent, final int style) {
		super(parent, style);
		final var layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		super.setLayout(layout);
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
		createSurroundingPairsTab(folder);
		createFoldingTab(folder);
		createWordPatternTab(folder);
		createOnEnterRulesTab(folder);
	}

	public void refresh(@Nullable final ILanguageConfiguration configuration) {
		lineCommentText.setText(""); //$NON-NLS-1$
		blockCommentStartText.setText(""); //$NON-NLS-1$
		blockCommentEndText.setText(""); //$NON-NLS-1$
		bracketsTable.setInput(null);
		autoClosingPairsTable.setInput(null);
		surroundingPairsTable.setInput(null);
		offsideText.setText(""); //$NON-NLS-1$
		markersStartText.setText(""); //$NON-NLS-1$
		markersEndText.setText(""); //$NON-NLS-1$
		wordPatternText.setText(""); //$NON-NLS-1$
		onEnterRuleTable.setInput(null);

		if (configuration == null) {
			return;
		}

		final CommentRule comments = configuration.getComments();
		if (comments != null) {
			lineCommentText.setText(comments.lineComment == null ? "" : comments.lineComment);
			final CharacterPair blockComment = comments.blockComment;
			if (blockComment != null) {
				blockCommentStartText.setText(blockComment.open);
				blockCommentEndText.setText(blockComment.close);
			}
		}

		bracketsTable.setInput(removeNullElements(configuration.getBrackets()));
		autoClosingPairsTable.setInput(removeNullElements(configuration.getAutoClosingPairs()));
		surroundingPairsTable.setInput(removeNullElements(configuration.getSurroundingPairs()));

		final FoldingRule folding = configuration.getFolding();
		if (folding != null) {
			offsideText.setText(Boolean.toString(folding.offSide));
			markersStartText.setText(folding.markersStart);
			markersEndText.setText(folding.markersEnd);
		}

		final String wordPattern = configuration.getWordPattern();
		if (wordPattern != null) {
			wordPatternText.setText(wordPattern);
		}

		onEnterRuleTable.setInput(removeNullElements(configuration.getOnEnterRules()));
	}

	@Nullable
	private List<?> removeNullElements(@Nullable final List<?> list) {
		if (list == null) {
			return null;
		}
		return list.stream().filter(Objects::nonNull).collect(Collectors.toList());
	}

	private void createCommentsTab(final TabFolder folder) {
		commentsTab = createTab(folder, LanguageConfigurationInfoWidget_comments);
		final Composite parent = (Composite) commentsTab.getControl();

		lineCommentText = createText(parent, LanguageConfigurationInfoWidget_lineComments);
		blockCommentStartText = createText(parent, LanguageConfigurationInfoWidget_blockCommentsStart);
		blockCommentEndText = createText(parent, LanguageConfigurationInfoWidget_blockCommentsEnd);
	}

	private void createBracketsTab(final TabFolder folder) {
		bracketsTab = createTab(folder, LanguageConfigurationInfoWidget_brackets);
		bracketsTable = new CharacterPairsTableWidget(createTable((Composite) bracketsTab.getControl()));
	}

	protected void createAutoClosingPairsTab(final TabFolder folder) {
		autoClosingPairsTab = createTab(folder,
				LanguageConfigurationInfoWidget_autoClosingPairs);
		autoClosingPairsTable = new AutoClosingPairConditionalTableWidget(
				createTable((Composite) autoClosingPairsTab.getControl()));
	}

	protected void createSurroundingPairsTab(final TabFolder folder) {
		surroundingPairsTab = createTab(folder, LanguageConfigurationInfoWidget_surroundingPairs);
		surroundingPairsTable = new CharacterPairsTableWidget(
				createTable((Composite) surroundingPairsTab.getControl()));
	}

	private void createFoldingTab(final TabFolder folder) {
		foldingTab = createTab(folder, LanguageConfigurationInfoWidget_folding_title);
		final Composite parent = (Composite) foldingTab.getControl();

		offsideText = createText(parent, LanguageConfigurationInfoWidget_offSide);
		offsideText.setToolTipText(LanguageConfigurationInfoWidget_offSide_tooltip);
		new Label(parent, SWT.NONE).setText(LanguageConfigurationInfoWidget_markers);
		markersStartText = createText(parent, LanguageConfigurationInfoWidget_start);
		markersEndText = createText(parent, LanguageConfigurationInfoWidget_end);
	}

	private void createWordPatternTab(final TabFolder folder) {
		wordPatternTab = createTab(folder, LanguageConfigurationInfoWidget_wordPattern_title);
		final Composite parent = (Composite) wordPatternTab.getControl();

		wordPatternText = createText(parent, LanguageConfigurationInfoWidget_wordPattern_message);
	}

	protected void createOnEnterRulesTab(final TabFolder folder) {
		onEnterRulesTab = createTab(folder, LanguageConfigurationInfoWidget_onEnterRules);
		onEnterRuleTable = new OnEnterRuleTableWidget(createTable((Composite) onEnterRulesTab.getControl()));
	}

	private Table createTable(final Composite parent) {
		final var tableComposite = new Composite(parent, SWT.NONE);
		tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableComposite.setLayout(new TableColumnLayout());
		final var table = new Table(tableComposite,
				SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		return table;
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
