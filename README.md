# Eclipse TM4E - TextMate and language-configuration support for Java and in Eclipse IDE

[![GitHub Actions](https://github.com/eclipse-tm4e/tm4e/actions/workflows/build.yml/badge.svg)](https://github.com/eclipse-tm4e/tm4e/actions/workflows/build.yml)
[![Jenkins tests](https://img.shields.io/jenkins/tests?jobUrl=https%3A%2F%2Fci.eclipse.org%2Ftm4e%2Fjob%2FTM4E%2Fjob%2Fmain%2F&logo=jenkins&logoColor=white)](https://ci.eclipse.org/tm4e/job/TM4E/)
[![License](https://img.shields.io/github/license/eclipse-tm4e/tm4e.svg?color=blue)](LICENSE)

TM4E brings Java API to tokenize textual documents according to TextMate grammars with an Eclipse IDE client that can do syntax highlighting according to this tokenization; and Eclipse IDE client for VSCode [Language Configuration](https://code.visualstudio.com/api/references/contribution-points#contributes.languages) to support matching bracket, auto close, on enter support.

TM4E is an [official Eclipse.org project](https://projects.eclipse.org/projects/technology.tm4e) so it conforms to typical Eclipse.org requirements and guarantees.


## 📥 Install

### 1) in Eclipse IDE or RCP applications

You can install TM4E from the update site [https://download.eclipse.org/tm4e/releases/latest/](https://download.eclipse.org/tm4e/releases/latest/).
TM4E is usually installed together with its consumers, so end-user should usually not need to directly install it.

The latest snapshot build can be installed from the update site [https://download.eclipse.org/tm4e/snapshots/](https://download.eclipse.org/tm4e/snapshots/).

#### Eclipse IDE compatibility

TM4E version | Min. Eclipse version
-------------|--------------------
[latest](https://download.eclipse.org/tm4e/releases/latest/) | [2023-09](https://projects.eclipse.org/releases/2023-09) ([4.29](https://projects.eclipse.org/projects/eclipse/releases/4.29.0))
[0.9.0](https://download.eclipse.org/tm4e/releases/0.9/)+    | [2023-09](https://projects.eclipse.org/releases/2023-09) ([4.29](https://projects.eclipse.org/projects/eclipse/releases/4.29.0))
[0.8.1](https://download.eclipse.org/tm4e/releases/0.8.1/)+  | [2023-06](https://projects.eclipse.org/releases/2023-06) ([4.28](https://projects.eclipse.org/projects/eclipse/releases/4.28.0))
[0.8](https://download.eclipse.org/tm4e/releases/0.8/)+      | [2023-03](https://projects.eclipse.org/releases/2023-03) ([4.27](https://projects.eclipse.org/projects/eclipse/releases/4.27.0))
[0.6.3](https://download.eclipse.org/tm4e/releases/0.6.3/)+  | [2022-12](https://projects.eclipse.org/releases/2022-12) ([4.26](https://projects.eclipse.org/projects/eclipse/releases/4.26.0))
[0.6.2](https://download.eclipse.org/tm4e/releases/0.6.2/)+  | [2022-09](https://projects.eclipse.org/releases/2022-09) ([4.25](https://projects.eclipse.org/projects/eclipse/releases/4.25.0))
[0.6.0](https://download.eclipse.org/tm4e/releases/0.6.0/)+  | [2022-03](https://projects.eclipse.org/releases/2022-03) ([4.23](https://projects.eclipse.org/projects/eclipse/releases/4.23.0))


### 2) as a Java API with Maven

**Snapshot** binaries of `org.eclipse.tm4e.core` are available at https://repo.eclipse.org/content/repositories/tm4e-snapshots/org/eclipse/org.eclipse.tm4e.core/.
You need to add the following repository configuration to your Maven `settings.xml`:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<settings>
  <profiles>
    <profile>
      <repositories>
        <repository>
          <id>tm4e-snapshots</id>
          <name>tm4e-snapshots</name>
          <url>https://repo.eclipse.org/content/repositories/tm4e-snapshots/</url>
          <releases><enabled>false</enabled></releases>
          <snapshots><enabled>true</enabled></snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>tm4e-snapshots</activeProfile>
  </activeProfiles>
</settings>
```

Then you can reference the dependency in your pom.xml like so:
```xml
<dependencies>
  <dependency>
    <groupId>org.eclipse</groupId>
    <artifactId>org.eclipse.tm4e.core</artifactId>
    <version>[VERSION]-SNAPSHOT</version>
  </dependency>
</dependencies>
```

[more information coming ...]


## ⌨️ Code

<a href="https://mickaelistria.github.io/redirctToEclipseIDECloneCommand/redirect.html"><img src="https://mickaelistria.github.io/redirctToEclipseIDECloneCommand/cloneToEclipseBadge.png" alt="Clone to Eclipse IDE"/></a>

The following class and modules should be used as entry point provides:
 - [org.eclipse.tm4e.core](https://github.com/eclipse-tm4e/tm4e/tree/main/org.eclipse.tm4e.core) provides the Java TextMate tokenizer. This project is a Java port of [vscode-textmate](https://github.com/Microsoft/vscode-textmate) written in TypeScript. This Java API can be used with any Java UI Toolkit (Swing, Eclipse, etc). See [Core](https://github.com/eclipse-tm4e/tm4e/wiki/Core) section for more information.
 - [org.eclipse.tm4e.ui](https://github.com/eclipse-tm4e/tm4e/tree/main/org.eclipse.tm4e.ui) provides the Eclipse **org.eclipse.jface.text.presentation.IPresentationReconciler** [TMPresentationReconciler](https://github.com/eclipse-tm4e/tm4e/blob/main/org.eclipse.tm4e.ui/src/main/java/org/eclipse/tm4e/ui/text/TMPresentationReconciler.java) which is able to tokenize an editor content by using a given JSON, PList TextMate grammar and do syntax coloration. See [UI](https://github.com/eclipse-tm4e/tm4e/wiki/UI) section for more information.
 - [org.eclipse.tm4e.languageconfiguration](https://github.com/eclipse-tm4e/tm4e/tree/main/org.eclipse.tm4e.languageconfiguration) provides the VSCode [Language Configuration](https://code.visualstudio.com/api/references/contribution-points#contributes.languages) to support matching bracket, auto close, on enter support with a simple **language-configuration.json**.

An example with TypeScript:

![TypeScript Editor](https://raw.githubusercontent.com/eclipse/wildwebdeveloper/master/documentation-files/typescript38.png)


## 👪 Who is using tm4e?

Here are some projects that use tm4e:

- [Apache NetBeans](https://github.com/apache/netbeans) - A multi-language IDE written in Java that uses TM4E core parts to support syntax highlighting based on TextMate grammars.
- Eclipse IDE languages and frameworks integrations
  - [Eclipse Corrosion](https://github.com/eclipse/corrosion) - Rust development tools in Eclipse IDE.
  - [Eclipse aCute](https://github.com/eclipse/aCute) - C# edition in Eclipse IDE.
  - [Eclipse Wild Web Developer](https://github.com/eclipse/wildwebdeveloper) - Simple and productive Web Development Tools in the Eclipse IDE.
  - [Eclipse ShellWax](https://github.com/eclipse/shellwax) - A shell script development plugin for the Eclipse IDE, providing a rich edition experience through integration with the [Bash Language Server](https://github.com/bash-lsp/bash-language-server).
  - [LiClipseText](https://www.liclipse.com/text/) - An editor which enables Eclipse to be used as a general-purpose text editor, providing support for multiple languages out of the box.
  - [EditorConfig for Eclipse](https://github.com/angelozerr/ec4e) - EditorConfig for Eclipse with GenericEditor.
  - [Solargraph](https://github.com/PyvesB/eclipse-solargraph) - Ruby development tools for Eclipse.
  - [dart4e](https://github.com/dart4e/dart4e) - Dart/Flutter development support for the Eclipse IDE.
  - [haxe4e](https://github.com/haxe4e/haxe4e) - [Haxe](https://haxe.org/) programming language support for the Eclipse IDE.
  - [BEF: Bazel Eclipse Feature](https://github.com/salesforce/bazel-eclipse/) - [Bazel](https://bazel.build/) support for the Eclipse IDE.
  - Deprecated projects:
    - [Dartboard](https://github.com/eclipse/dartboard) - Dart language support  for the Eclipse IDE.
    - [typescript.java](https://github.com/angelozerr/typescript.java) - TypeScript IDE for Eclipse with JSDT & tsserver.
    - [Phaser Editor 2D](https://github.com/PhaserEditor2D/PhaserEditor) - an IDE for the creation of HTML5 games.

## Documentation

See [Further documentation](https://github.com/eclipse-tm4e/tm4e/blob/main/documentation/README.md) for additional information.

## 👷 Get support and contribute

- **License**: TM4E is a community open-source project licensed under the [Eclipse Public License 2.0](LICENSE).
- **Support**: You can ask (and answer!) questions in the [GitHub discussions](https://github.com/eclipse-tm4e/tm4e/discussions). Report bugs, and request features using [GitHub issues](https://github.com/eclipse-tm4e/tm4e/issues).
- **Source Code**: This `github.com/eclipse-tm4e/tm4e` repository is the reference repository to contribute to TM4E
- **Build**: Builds can be performed with a simple `./mvnw clean verify`
- **Continuous testing, integration and deployment** is performed by CI jobs at https://ci.eclipse.org/tm4e/ and https://github.com/eclipse-tm4e/tm4e/actions
- **Developers mailing-list**: Contributors are also expected to subscribe the [tm4e-dev mailing-list](https://dev.eclipse.org/mailman/listinfo/tm4e-dev).
- **Becoming a committer**: as usual with Eclipse.org projects, anyone who's made significant contributions and who's upheld quality standards alongside good judgement and open-mindedness.
