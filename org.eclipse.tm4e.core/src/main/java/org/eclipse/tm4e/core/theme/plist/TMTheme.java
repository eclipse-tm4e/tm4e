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
package org.eclipse.tm4e.core.theme.plist;

public class TMTheme {

	
//	public contributeStyles(themeId: string, themeDocument: IThemeDocument, cssRules: string[]): void {
//		let theme = new Theme(themeId, themeDocument);
//		theme.getSettings().forEach((s: IThemeSetting, index, arr) => {
//			// @martin TS(2.0.2) - s.scope is already a string[] so no need for all this checking.
//			// However will add a cast at split to keep semantic in case s.scope is wrongly typed.
//			let scope: string | string[] = s.scope;
//			let settings = s.settings;
//			if (scope && settings) {
//				let rules = Array.isArray(scope) ? <string[]>scope : (scope as string).split(',');
//				let statements = this._settingsToStatements(settings);
//				rules.forEach(rule => {
//					rule = rule.trim().replace(/ /g, '.'); // until we have scope hierarchy in the editor dom: replace spaces with .
//
//					cssRules.push(`.monaco-editor.${theme.getSelector()} .token.${rule} { ${statements} }`);
//				});
//			}
//		});
//	}
//
//	private _settingsToStatements(settings: IThemeSettingStyle): string {
//		let statements: string[] = [];
//
//		for (let settingName in settings) {
//			const value = settings[settingName];
//			switch (settingName) {
//				case 'foreground':
//					let foreground = new Color(value);
//					statements.push(`color: ${foreground};`);
//					break;
//				case 'background':
//					// do not support background color for now, see bug 18924
//					//let background = new Color(value);
//					//statements.push(`background-color: ${background};`);
//					break;
//				case 'fontStyle':
//					let segments = value.split(' ');
//					segments.forEach(s => {
//						switch (s) {
//							case 'italic':
//								statements.push(`font-style: italic;`);
//								break;
//							case 'bold':
//								statements.push(`font-weight: bold;`);
//								break;
//							case 'underline':
//								statements.push(`text-decoration: underline;`);
//								break;
//						}
//					});
//			}
//		}
//		return statements.join(' ');
//	}
}
