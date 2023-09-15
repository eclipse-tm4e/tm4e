# Eclipse TM4E: Release notes

> **IMPORTANT:** Releases and changelogs are now tracked via the [GitHub releases](https://github.com/eclipse/tm4e/releases) page. Older changelogs are listed below.

## 0.8.0

* 📅 Release Date: June 23th 2023
* All changes: https://github.com/eclipse/tm4e/compare/0.7.1...0.8.0


## 0.7.1

* 📅 Release Date: May 25th 2023
* All changes: https://github.com/eclipse/tm4e/compare/0.7.0...0.7.1


## 0.7.0

* 📅 Release Date: May 11th 2023
* All changes: https://github.com/eclipse/tm4e/compare/0.6.3...0.7.0

### Fixed
- On enter rule don't use the proper insert spaces / tab width when those settings are changed
- ToggleLineCommentHandler.removeLineComments does not process comment lines in order which may corrupt the open document

### Added
- Task Tag UI preferences

### Changed
- dependency updates
- disable broken markdown.math grammars


## 0.6.3

* 📅 Release Date: March 16th 2023
* All changes: https://github.com/eclipse/tm4e/compare/0.6.2...0.6.3

Many bugfixes and dependency updates.


## 0.6.2

* 📅 Release Date: November 26th 2022
* All changes: https://github.com/eclipse/tm4e/compare/0.6.1...0.6.2

Many bugfixes and dependency updates.

## 0.6.1

* 📅 Release Date: September 14th 2022
* All changes: https://github.com/eclipse/tm4e/compare/0.6.0...0.6.1

#### Support for onEnterRules

The `onEnterRule` directive of language-configuration files is now honored, so new lines can be pre-edited according to previous one (comments, indentations... are propagated).

## 0.6.0

* 📅 Release Date: June 23th 2022
* All changes: https://github.com/eclipse/tm4e/compare/0.4.5...0.6.0

#### Move to Java 17

TM4E now requires Java 17 to run.

#### Added a language pack feature

A "language pack" feature can now be installed. It adds some support for about all possible code files. More details at [org.eclipse.tm4e.language_pack/README.md](org.eclipse.tm4e.language_pack/README.md).

#### Task markers in comments

Marker tasks are now created for each occurrence of BUG,NOTE,FIXME,HACK,TODO,XXX in comments.

#### Improve existing features

Toggling comment, auto-closing pairs... got improved so they now feel more natural.

#### Many bugfixes and code improvement


## 0.4.5

* 📅 Release Date: May 11th 2022
* All changes: https://github.com/eclipse/tm4e/compare/0.4.3...0.4.5
* 0.4.4 release was skipped to align public and internal version numbers


* Many many many... code improvements
* Dependency updates
* Bugfixes in file detection and compare editor

## 0.4.3

* 📅 Release Date: 19th November 2021
* All changes: https://github.com/eclipse/tm4e/compare/0.4.2...0.4.3

#### Support locations outside a local file system

This allows to have syntax highlighting for buffers that are not backed by a filesystem locations.

#### Compatible with viewers that are not IProjectionViewer

Avoid an exception making TM4E incompatible with most simple ITextViewer in some case.

#### Fix content-type detection

Parent content-type are taken into account and allow to enable syntax highlighting for the children of content-types which are bound to a grammar/scope as well, without extra configuration.

#### Choose default theme according to background color of the widget

By default, TM4E now decides of the best theme to use according to the background color of the text widget. This allows to get a better text theme when mixing dark global them with light background or editor, or light global theme with dark background in editor.


## 0.4.2

* 📅 Release Date (tentative): 6th September 2021
* All changes: https://github.com/eclipse/tm4e/compare/0.4.1...0.4.2

No new feature, numerous bugfixes and code improvements.

## 0.4.1

* 📅 Release Date: 27th August 2020
* All changes: https://github.com/eclipse/tm4e/compare/0.4.0...0.4.1

#### Users can add TextMate theme

It's not possible for users to add extra TextMate theme to use in their IDE via the Textmate > Theme preference page.

## 0.4.0

* 📅 Release Date: 21st November 2019
* All changes: https://github.com/eclipse/tm4e/compare/0.3.4...0.4.0

#### Improve logging mechanism

Most TM4E API entry-points can now be configured with a specific logger. That allows embedders (like the Eclipse UI plugin) to pass a specific logger so TM4E can log at the same location as other parts of the application that includes it, or enable some totally different logging is it fits better.

In Eclipse IDE, the logger takes the "trace" settings into account and logs in the usual Eclipse logs.

## Previous releases

No release notes were maintained before that.
