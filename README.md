# Eclipse tm4e - TextMate and language-configuration support for Java and in Eclipse IDE

TM4E brings Java API to tokenize textual documents according to TextMate grammars with an Eclipse IDE client that can do syntax highlighting according to this tokenization; and Eclipse IDE client for  VSCode [Language Configuration](https://code.visualstudio.com/docs/extensionAPI/extension-points#_contributeslanguages) to support matching bracket, auto close, on enter support.

`tm4e` is an [official Eclipse.org project](https://projects.eclipse.org/projects/technology.tm4e) so it conforms to typical Eclipse.org requirements and guarantees.

## 📥 Install

### in Eclipse IDE or RCP applications

You can install `tm4e` with the update site [http://download.eclipse.org/tm4e/snapshots/](http://download.eclipse.org/tm4e/snapshots/). TM4E is usually installed together with its consumers, so end-user should usually not need to directly install it.

### as a Java API with Maven

[more information coming soon]

## ⌨️ Code

<a href="https://mickaelistria.github.io/redirctToEclipseIDECloneCommand/redirect.html"><img src="https://mickaelistria.github.io/redirctToEclipseIDECloneCommand/cloneToEclipseBadge.png" alt="Clone to Eclipse IDE"/></a>

The following class and modules should be used as entry point provides:

 * [org.eclipse.tm4e.core](https://github.com/eclipse/tm4e/tree/master/org.eclipse.tm4e.core) provides the Java TextMate tokenizer. This project is a Java port of [vscode-textmate](https://github.com/Microsoft/vscode-textmate) written in TypeScript. This Java API can be used with any Java UI Toolkit (Swing, Eclipse, etc). See [Core](https://github.com/eclipse/tm4e/wiki/Core) section for more information.

 * [org.eclipse.tm4e.ui](https://github.com/eclipse/tm4e/tree/master/org.eclipse.tm4e.ui) provides the Eclipse **org.eclipse.jface.text.presentation.IPresentationReconciler** [TMPresentationReconciler](https://github.com/eclipse/tm4e/blob/master/org.eclipse.tm4e.ui/src/main/java/org/eclipse/tm4e/ui/text/TMPresentationReconciler.java) which is able to tokenize an editor content by using a given JSON, PList TextMate grammar and do syntax coloration. See [UI](https://github.com/eclipse/tm4e/wiki/UI) section for more information.

 * [org.eclipse.tm4e.languageconfiguration](https://github.com/eclipse/tm4e/tree/master/org.eclipse.tm4e.languageconfiguration) provides the VSCode [Language Configuration](https://code.visualstudio.com/docs/extensionAPI/extension-points#_contributeslanguages) to support matching bracket, auto close, on enter support with a simple **language-configuration.json**.
 
Here a sample with TypeScript:

![TypeScript Editor](https://github.com/eclipse/tm4e/wiki/images/TypeScriptEditor.png)

## 👪 Who is using tm4e?

Here are some projects that use tm4e:

* Eclipse IDE languages and frameworks integrations
	* [Eclipse Corrosion](https://github.com/eclipse/corrosion) Rust development tools in Eclipse IDE.
	* [Eclipse aCute](https://github.com/eclipse/aCute) C# edition in Eclipse IDE.
	* [Eclipse Wild Web Developer](https://github.com/eclipse/wildwebdeveloper) a simple and productive Web Development Tools in the Eclipse IDE.
	* [Eclipse ShellWax](https://github.com/eclipse/shellwax) is a rich Bash script editor in the Eclipse IDE.
	* [LiClipseText](http://www.liclipse.com/text/) enables Eclipse to be used as a general-purpose text editor, providing support for several languages out of the box.
	* [typescript.java](https://github.com/angelozerr/typescript.java) (Deprecated) TypeScript IDE for Eclipse with JSDT & tsserver.
	* [EditorConfig for Eclipse](https://github.com/angelozerr/ec4e) EditorConfig for Eclipse with GenericEditor.
	* [Phaser Editor 2D](https://phasereditor2d.com) An IDE for the creation of HTML5 games.
	* [Solargraph](https://github.com/PyvesB/eclipse-solargraph) Ruby edition in Eclipse IDE.
	* [Dartboard](https://github.com/eclipse/dartboard) Dart language support in the Eclipse IDE.
* [Apache NetBeans](https://github.com/apache/netbeans) is a multi-language IDE written in Java and uses TM4E core parts to support syntax highlighting based on TextMate grammars
 
## 👷 Get support and contribute

* **License and community**: `tm4e` is a community open-source project licensed under the Eclipse Public License 1.0.
* **Support:** You can ask (and answer!) questions, report bugs, and request features using [GitHub issues](http://github.com/eclipse/tm4e/issues).
* **Git**: This `eclipse/tm4e` repository is the reference repository to contribute to `tm4e`
* **Build**: build can be performed with a simple `mvn clean verify`, continuous integration and deployment is performed by CI jobs at https://hudson.eclipse.org/tm4e
* **Continuous testing, integration and deployment**  is performed by CI jobs at https://hudson.eclipse.org/tm4e
* **Developers mailing-list**: Contributors are also expected to subscribe the [tm4e-dev mailing-list](https://dev.eclipse.org/mailman/listinfo/tm4e-dev).
* **Becoming a committer**: as usual with Eclipse.org projects, anyone who's made significant contributions and who's upheld quality standards alongside good judgement and open-mindedness.
