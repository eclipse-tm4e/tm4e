# TextMate support in Eclipse IDE (tm4e)

[![Build Status](https://secure.travis-ci.org/pascalleclercq/textmate.java.png)](http://travis-ci.org/pascalleclercq/textmate.java)

**tm4e** provides the capability to support TextMate tokenizer with Java. It provides:

 * [org.eclipse.tm4e.core](https://github.com/angelozerr/textmate.java/tree/master/org.eclipse.tm4e.core) provides the Java TextMate tokenizer. This project is a Java port of [vscode-textmate](https://github.com/Microsoft/vscode-textmate) written in TypeScript. This Java API can be used with any Java UI Toolkit (Swing, Eclipse, etc). See [Core](https://github.com/angelozerr/textmate.java/wiki/Core) section for more information.

 * [org.eclipse.tm4e.ui](https://github.com/angelozerr/textmate.java/tree/master/org.eclipse.tm4e.ui) provides the Eclipse **org.eclipse.jface.text.presentation.IPresentationReconciler** [TMPresentationReconciler](https://github.com/angelozerr/textmate.java/blob/master/org.eclipse.tm4e.ui/src/main/java/org/eclipse/tm4e/ui/text/TMPresentationReconciler.java) which is able to tokenize an editor content by using a given JSON, PList TextMate grammar and do syntax coloration. See [UI](https://github.com/angelozerr/textmate.java/wiki/UI) section for more information.

Here a sample with TypeScript:

![TypeScript Editor](https://github.com/angelozerr/textmate.java/wiki/images/TypeScriptEditor.png)

You can install textmate.java with the update site http://oss.opensagres.fr/textmate/1.0.0-SNAPSHOT/
which provides samples of syntax coloration with:

 * GenericEditor for Language Server (C#, CSS, JSON)
 * a TypeScript Editor.
 
Proposal https://projects.eclipse.org/proposals/textmate-support-eclipse-ide-tm4e