# Language Pack Update

To update/recreate the content of the [`org.eclipse.tm4e.language_pack`](../README.md) plugin,
run the following Maven command inside the `_update/` folder:

```bash
$ mvn clean process-resources

[INFO] ------------------------< language_pack:update >------------------------
[INFO] Building update 1.0.0-SNAPSHOT
[INFO] --------------------------------[ pom ]---------------------------------
[INFO] --- antrun:3.0.0:run (update-language-pack) @ update ---
[INFO] Executing tasks
[INFO]       [get] Getting: https://github.com/microsoft/vscode/tree/main/extensions
...
[INFO]      [copy] Copying 142 files to tm4e/org.eclipse.tm4e.language_pack
[INFO] Executed tasks
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

Do not edit the files of the project directly. If required, adapt the update/recreate scripts in this folder instead.

Ensure that the manual changes in the following files are not overwritten:
1. [../plugin.xml](../plugin.xml): `markdown-math` stays disabled in  until https://github.com/microsoft/vscode/issues/181662 is solved
1. [../javascript/Regular Expressions (JavaScript).tmLanguage](../javascript/Regular%20Expressions%20%28JavaScript%29.tmLanguage): `scopeName` is changed to `lngpck.source.js.regexp`
1. [../xml/xml.tmLanguage.json](xml/xml.tmLanguage.json): syntax fixes, see https://github.com/microsoft/vscode/issues/184852 and https://github.com/atom/language-xml/issues/96
