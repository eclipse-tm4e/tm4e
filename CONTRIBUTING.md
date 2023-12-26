# Contributing to Eclipse TM4E

Welcome to the Eclipse TM4E contributor land, and thanks in advance for your help in making Eclipse TM4E better and better!

🏠 Official Eclipse TM4E Git repo is [https://github.com/eclipse/tm4e](https://github.com/eclipse/tm4e) .


## ⚖️ Legal and Eclipse Foundation terms

The project license is available at [LICENSE](LICENSE).

This Eclipse Foundation open project is governed by the Eclipse Foundation
Development Process and operates under the terms of the Eclipse IP Policy.

Before your contribution can be accepted by the project team,
contributors must have an Eclipse Foundation account and
must electronically sign the Eclipse Contributor Agreement (ECA).

* [http://www.eclipse.org/legal/ECA.php](http://www.eclipse.org/legal/ECA.php)

For more information, please see the Eclipse Committer Handbook:
[https://www.eclipse.org/projects/handbook/#resources-commit](https://www.eclipse.org/projects/handbook/#resources-commit).


## 💬 Get in touch with the community

Eclipse TM4E use mainly 2 channels for strategical and technical discussions

* 🐞 View and report issues through uses GitHub Issues at https://github.com/eclipse/m2e-tm4e/issues.
* 📧 Join the tm4e-dev@eclipse.org mailing-list to get in touch with other contributors about project organization and planning, and browse archive at 📜 [https://accounts.eclipse.org/mailing-list/tm4e-dev](https://accounts.eclipse.org/mailing-list/tm4e-dev)


## 🆕 Trying latest builds

Latest builds, for testing, can usually be found at `https://download.eclipse.org/tm4e/snapshots/` .


## 🧑‍💻 Developer resources

### ⌨️ Setting up the Development Environment manually

* Download and install the **Eclipse IDE for Eclipse Committers** from https://www.eclipse.org/downloads/packages/ or another Eclipse installation with the [Plug-in Development Environment (PDE)](https://www.eclipse.org/pde/) installed.
* Clone this repository <a href="https://mickaelistria.github.io/redirctToEclipseIDECloneCommand/redirect.html"><img src="https://mickaelistria.github.io/redirctToEclipseIDECloneCommand/cloneToEclipseBadge.png" alt="Clone to Eclipse IDE"/></a>.
* _File > Open Projects from Filesystem..._ , select the path to the tm4e Git repo and the relevant children projects you want to import

### 🏗️ Build

Prerequisite: Latest Maven release or Eclipse [m2e](https://www.eclipse.org/m2e/).

1. From the command line: run `mvn clean verify`
1. From within Eclipse : right-click on the tm4e root folder > Run As > Maven build

#### Running the CI job locally:

The GitHub actions wokflow is compatible with [nektos/act](https://github.com/nektos/act) a command-line tool that allows you to run GitHub action workflows locally.

1. Install docker
1. Install [nektos/act](https://github.com/nektos/act)
1. From the commandline navigate into the tm4e rpoject root
1. Run the command `act`
1. On subsequent re-runs you can use `act -r` to reuse previous container which avoids reinstallation system packages and reduces build time.

In case of build failures the docker container will still be running and you can ssh into it for analysis using `docker exec -u root -it <CONTAINER_ID> /bin/bash`, e.g.:
```bash
container_id=$(docker container ps --filter status=running --filter name=act-Build-build --format {{.ID}})
docker exec -u root -it $container_id /bin/bash
```

### ⬆️ Version bump

The TM4E project adopts [Semantic Versioning](https://semver.org/) on release level, ensuring the proper exposure of API contracts and addressing potential breakages.

To alleviate confusion among end-users regarding the effectively installed TM4E release and to ease the development process and troubleshooting, starting with version **0.9.0**, individual TM4E features/plugins are no longer versioned independently (OSGi semantic versioning).
Instead, they are aligned with the overall TM4E release version, following a practice that is common in other Eclipse Platform projects, such as EGit or Mylyn, as well as popular projects outside the Eclipse Platform universe, like the Spring Application Framework.

In this versioning approach, when any plugin introduces new features necessitating a minor version increment, the versions of **all** TM4E plugins/features are updated collectively, and the next release version will be adjusted accordingly.

To simplify version increments, utilize the `bump-versions.py` Python script located in the root project directory.
This script facilitates the recursive update of the project version and plugin dependencies in all relevant files, including `pom.xml`, `feature.xml`, and `META-INF/MANIFEST.MF`.

The usage is as follows:
```bash
$ python bump-version.py (major|minor|micro)
```

Where
* `micro` (`+0.0.1`) is for a backward compatible bugfix, or an internal change that doesn't surface to APIs
* `minor` (`+0.1.0`) is for a backward compatible API or feature addition
* `major` (`+1.0.0`) is for an API breakage (needs to be discussed on the mailing-list first)

### ➕ Submit changes

TM4E only accepts contributions via GitHub Pull Requests against [https://github.com/eclipse/tm4e](https://github.com/eclipse/tm4e) repository.

Before sending us a pull request, please ensure that:

1. You are working against the latest source on the **master** branch.
1. You check existing open and recently merged pull requests to make sure someone else hasn't already addressed the issue.

To send us a pull request, please:

1. Fork the repository.
1. Modify the source while focusing on the specific change you are contributing.
1. Commit to your fork using clear, descriptive [semantic commit messages](https://www.conventionalcommits.org/en/).
1. Send us a pull request, answering any default questions in the pull request interface.

GitHub provides additional documentation on [forking a repository](https://help.github.com/articles/fork-a-repo/) and [creating a pull request](https://help.github.com/articles/creating-a-pull-request/)

### Release

1. Decide of a version name, we'll call it `x.y.z` here. The version is not so important as it may not be the version of the built artifacts (itself being the important one as it usually follow semantic versioning)
1. Contribute necessary content to [RELEASE_NOTES.md](RELEASE_NOTES.md)
1. Get the main code locally: `git fetch eclipse main && git checkout FETCH_HEAD`
1. tag it `git tag x.y.x`
1. push tag `git push eclipse x.y.z`
1. Re-run a build from https://ci.eclipse.org/tm4e/job/TM4E/job/main/ and ensure it passes
1. Upon completion of the build, run https://ci.eclipse.org/tm4e/job/promote-snapshot-to-release/ with version `x.y.z`
1. Create a new `x.y.z` release entry on GitHub, from the `x.y.z` tag
1. Create a new `x.y.z` release entry on https://projects.eclipse.org/projects/technology.tm4e
1. (Optionally) Announce on mailing-lists. social media...
