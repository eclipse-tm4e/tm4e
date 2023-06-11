# Language Pack Update

Run [`update/pom.xml`](update/pom.xml) via Maven to update/recreate the content of the
[`org.eclipse.tm4e.language_pack`](../README.md) plugin.

Do not edit the files of the project directly. If required, adapt the update/recreate scripts in this folder instead.

Ensure that the manual changes in the following files are not overwritten:
1. [../plugin.xml](../plugin.xml): `markdown-math` stays disabled in  until https://github.com/microsoft/vscode/issues/181662 is solved
1. [../javascript/Regular Expressions (JavaScript).tmLanguage](../javascript/Regular%20Expressions%20%28JavaScript%29.tmLanguage): `scopeName` is changed to `lngpck.source.js.regexp`
