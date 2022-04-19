/**
 *  Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.widgets;

import java.util.List;
import java.util.stream.Collectors;

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
import org.eclipse.tm4e.languageconfiguration.ILanguageConfiguration;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationMessages;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CharacterPair;
import org.eclipse.tm4e.languageconfiguration.internal.supports.Comments;
import org.eclipse.tm4e.languageconfiguration.internal.supports.Folding;

public class LanguageConfigurationInfoWidget extends Composite {

	public LanguageConfigurationInfoWidget(Composite parent, int style) {
		super(parent, style);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		super.setLayout(layout);
		super.setLayoutData(new GridData(GridData.FILL_BOTH));
		createUI(this);
	}

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

	private void createUI(Composite ancestor) {
		TabFolder folder = new TabFolder(ancestor, SWT.NONE);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		folder.setLayoutData(gd);

		createCommentsTab(folder);
		createBracketsTab(folder);
		createAutoClosingPairsTab(folder);
		createSurroundingPairsTab(folder);
		createFoldingTab(folder);
		createWordPatternTab(folder);
		createOnEnterRulesTab(folder);
	}

	public void refresh(ILanguageConfiguration configuration) {
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

		Comments comments = configuration.getComments();
		if (comments != null) {
			lineCommentText.setText(comments.getLineComment() == null ? "" : comments.getLineComment());
			CharacterPair blockComment = comments.getBlockComment();
			if (blockComment != null) {
				blockCommentStartText.setText(comments.getBlockComment().getKey());
				blockCommentEndText.setText(comments.getBlockComment().getValue());
			}
		}

		bracketsTable.setInput(removeNullElements(configuration.getBrackets()));
		autoClosingPairsTable.setInput(removeNullElements(configuration.getAutoClosingPairs()));
		surroundingPairsTable.setInput(removeNullElements(configuration.getSurroundingPairs()));

		Folding folding = configuration.getFolding();
		if (folding != null) {
			offsideText.setText(folding.getOffSide().toString());
			markersStartText.setText(folding.getMarkersStart());
			markersEndText.setText(folding.getMarkersEnd());
		}

		String wordPattern = configuration.getWordPattern();
		if (wordPattern != null) {
			wordPatternText.setText(wordPattern);
		}

		onEnterRuleTable.setInput(removeNullElements(configuration.getOnEnterRules()));
	}

	private List<?> removeNullElements(List<?> list) {
		if (list == null) {
			return null;
		}
		return list.stream().filter(el -> el != null).collect(Collectors.toList());
	}

	private void createCommentsTab(TabFolder folder) {
		commentsTab = createTab(folder, LanguageConfigurationMessages.LanguageConfigurationInfoWidget_comments);
		Composite parent = (Composite) commentsTab.getControl();

		lineCommentText = createText(parent,
				LanguageConfigurationMessages.LanguageConfigurationInfoWidget_lineComments);
		blockCommentStartText = createText(parent,
				LanguageConfigurationMessages.LanguageConfigurationInfoWidget_blockCommentsStart);
		blockCommentEndText = createText(parent,
				LanguageConfigurationMessages.LanguageConfigurationInfoWidget_blockCommentsEnd);
	}

	private void createBracketsTab(TabFolder folder) {
		bracketsTab = createTab(folder, LanguageConfigurationMessages.LanguageConfigurationInfoWidget_brackets);
		bracketsTable = new CharacterPairsTableWidget(createTable((Composite) bracketsTab.getControl()));
	}

	protected void createAutoClosingPairsTab(TabFolder folder) {
		autoClosingPairsTab = createTab(folder,
				LanguageConfigurationMessages.LanguageConfigurationInfoWidget_autoClosingPairs);
		autoClosingPairsTable = new AutoClosingPairConditionalTableWidget(
				createTable((Composite) autoClosingPairsTab.getControl()));
	}

	protected void createSurroundingPairsTab(TabFolder folder) {
		surroundingPairsTab = createTab(folder,
				LanguageConfigurationMessages.LanguageConfigurationInfoWidget_surroundingPairs);
		surroundingPairsTable = new CharacterPairsTableWidget(
				createTable((Composite) surroundingPairsTab.getControl()));
	}

	private void createFoldingTab(TabFolder folder) {
		foldingTab = createTab(folder, LanguageConfigurationMessages.LanguageConfigurationInfoWidget_folding_title);
		Composite parent = (Composite) foldingTab.getControl();

		offsideText = createText(parent, LanguageConfigurationMessages.LanguageConfigurationInfoWidget_offSide);
		offsideText.setToolTipText(LanguageConfigurationMessages.LanguageConfigurationInfoWidget_offSide_tooltip);
		new Label(parent, SWT.NONE).setText(LanguageConfigurationMessages.LanguageConfigurationInfoWidget_markers);
		markersStartText = createText(parent, LanguageConfigurationMessages.LanguageConfigurationInfoWidget_start);
		markersEndText = createText(parent, LanguageConfigurationMessages.LanguageConfigurationInfoWidget_end);
	}

	private void createWordPatternTab(TabFolder folder) {
		wordPatternTab = createTab(folder,
				LanguageConfigurationMessages.LanguageConfigurationInfoWidget_wordPattern_title);
		Composite parent = (Composite) wordPatternTab.getControl();

		wordPatternText = createText(parent,
				LanguageConfigurationMessages.LanguageConfigurationInfoWidget_wordPattern_message);
	}

	protected void createOnEnterRulesTab(TabFolder folder) {
		onEnterRulesTab = createTab(folder, LanguageConfigurationMessages.LanguageConfigurationInfoWidget_onEnterRules);
		onEnterRuleTable = new OnEnterRuleTableWidget(createTable((Composite) onEnterRulesTab.getControl()));
	}

	private Table createTable(Composite parent) {
		Composite tableComposite = new Composite(parent, SWT.NONE);
		tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableComposite.setLayout(new TableColumnLayout());
		Table table = new Table(tableComposite,
				SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		return table;
	}

	private TabItem createTab(TabFolder folder, String title) {
		TabItem tab = new TabItem(folder, SWT.NONE);
		tab.setText(title);

		Composite parent = new Composite(folder, SWT.NONE);
		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		tab.setControl(parent);

		return tab;
	}

	private Text createText(Composite parent, String s) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(s);

		Text text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.setEditable(false);
		return text;
	}
}
