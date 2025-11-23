# Contributing to Eclipse TM4E

Welcome to the Eclipse TM4E contributor land, and thanks in advance for your help in making Eclipse TM4E better and better!

🏠 The official Eclipse TM4E Git repository is https://github.com/eclipse-tm4e/tm4e.


## ⚖️ Legal and Eclipse Foundation terms

The project license is available at [LICENSE](LICENSE).

This Eclipse Foundation open project is governed by the Eclipse Foundation Development Process and operates under the terms of
the Eclipse IP Policy.

Before your contribution can be accepted by the project team, contributors must have an Eclipse Foundation account and must
electronically sign the Eclipse Contributor Agreement (ECA):
- http://www.eclipse.org/legal/ECA.php

For more information, please see the Eclipse Committer Handbook: https://www.eclipse.org/projects/handbook/#resources-commit.


## 💬 Get in touch with the community

Eclipse TM4E uses the following channels for strategical and technical discussions:

- 🐞 View and report issues through GitHub Issues at https://github.com/eclipse-tm4e/tm4e/issues.
- 💬 Ask questions, propose ideas, and discuss features in GitHub Discussions at https://github.com/eclipse-tm4e/tm4e/discussions.
- 📧 Join the tm4e-dev@eclipse.org mailing list to get in touch with other contributors about project organization and planning,
  and browse the archive at 📜 https://accounts.eclipse.org/mailing-list/tm4e-dev


## 🆕 Trying latest builds

Latest builds, for testing, can be found at https://download.eclipse.org/tm4e/snapshots/.


## 🧑‍💻 Developer resources

For regular contributors and maintainers, the main technical reference is the [TM4E Contributor Guide](docs/contributor-guide.md). It covers:

- Repository and module structure.
- Development environment setup and target platform configuration.
- Build and test workflows (command line and within Eclipse).
- Diagnostics and troubleshooting (traces, test generation, token hover).
- Versioning and the TM4E release process.

### 🏗️ Build & Test

If you just want to run the full build locally, the short version is:

- On Windows: `mvnw clean verify`
- On Linux/macOS: `./mvnw clean verify`

For everything beyond that, including IDE setup and CI-style runs with `act`, please refer to `docs/contributor-guide.md`.

### ➕ Submit changes

TM4E only accepts contributions via GitHub Pull Requests against https://github.com/eclipse-tm4e/tm4e repository.

Before sending us a pull request, please ensure that:

1. You are working against the latest source on the **main** branch.
1. You check existing open and recently merged pull requests to make sure someone else hasn't already addressed the issue.

To send us a pull request, please:

1. Fork the repository.
1. Modify the source while focusing on the specific change you are contributing.
1. Commit to your fork using clear, descriptive [semantic commit messages](https://www.conventionalcommits.org/en/).
1. Send us a pull request, answering any default questions in the pull request interface.

GitHub provides additional documentation on [forking a repository](https://help.github.com/articles/fork-a-repo/) and
[creating a pull request](https://help.github.com/articles/creating-a-pull-request/)

For release engineering details (version bumping, CI promotion, SimRel updates), see the "Extension and API Evolution Guidelines" and "Release Process" sections in `docs/contributor-guide.md`.
