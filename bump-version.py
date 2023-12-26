#!/usr/bin/env python3
# Copyright (c) 2023 Sebastian Thomschke and others.
# SPDX-License-Identifier: EPL-2.0

import re, os, sys

MIN_PYTHON_VERSION = (3, 10)
if sys.version_info < MIN_PYTHON_VERSION:
    print(f'ERROR: This script requires at least Python {".".join(map(str, MIN_PYTHON_VERSION))} but found Python {".".join(map(str, sys.version_info[0:2]))}.')
    sys.exit(1)

if not len(sys.argv) == 2:
    print("Recursively bumps the project version in all pom.xml, feature.xml and META-INF/MANIFEST.MF files")
    print()
    print("USAGE: python bump-version.py (major|minor|micro)")
    sys.exit(1)


def increment_version(version:str, level:str):
    match = re.match(r'^(\d+)\.(\d+)\.(\d+)$', version)
    assert match, f'Unparseable version: {version}'

    major = int(match.group(1))
    minor = int(match.group(2))
    patch = int(match.group(3))
    match level.casefold():
        case 'major':
            major += 1
            minor = 0
            patch = 0
        case 'minor':
            minor += 1
            patch = 0
        case 'micro' | 'patch':
            patch += 1
        case _:
            raise RuntimeError(f'Unknown version upgrade level: {level}. Available are: major, minor, patch')

    return f'{major}.{minor}.{patch}'


THIS_FILE_DIR = os.path.dirname(os.path.abspath(__file__))

print(f"Determine current version...")
with open(os.path.join(THIS_FILE_DIR, "pom.xml"), "rt", encoding = 'utf-8') as fh:
    content = fh.read()
    old_version = re.search(r"<version>(\d+\.\d+\.\d+)-SNAPSHOT</version", content).group(1)
    assert old_version, "Cannot determine version in root pom.xml"
print(f"Current version: {old_version}")
new_version = increment_version(old_version, sys.argv[1])
print(f"New version: {new_version}")

for root, dirs, files in os.walk(THIS_FILE_DIR):
    dirs[:] = [d for d in dirs if not d.startswith('.') and not d == "target" ]  # exclude hidden dirs and "target" dirs

    for file in files:
        match file:
            case 'pom.xml':
                filepath = os.path.join(root, file)
                print(f"Updating [{filepath}]...")
                with open(filepath, "rt", encoding = 'utf-8') as fh:
                    content = fh.read()
                    content = content.replace(f"<version>{old_version}-SNAPSHOT</version>", f"<version>{new_version}-SNAPSHOT</version>")
                with open(filepath, "wt", encoding = 'utf-8') as fh:
                    fh.write(content)

            case 'MANIFEST.MF':
                if root.endswith("META-INF"):
                    filepath = os.path.join(root, file)
                    print(f"Updating [{filepath}]...")
                    with open(filepath, "rt", encoding = 'utf-8') as fh:
                        content = fh.read()
                        content = content.replace(f"Bundle-Version: {old_version}.qualifier", f"Bundle-Version: {new_version}.qualifier", 1)
                        content = re.sub(r'(org[.]eclipse[.]tm4e[.].+;bundle-version=")([^"]+)(")', rf'\g<1>{new_version}\g<3>', content)
                    with open(filepath, "wt", encoding = 'utf-8') as fh:
                        fh.write(content)

            case 'feature.xml':
                    filepath = os.path.join(root, file)
                    print(f"Updating [{filepath}]...")
                    with open(filepath, "rt", encoding = 'utf-8') as fh:
                        content = fh.read()
                        content = re.sub(r'(version=")([0-9.]+)(.qualifier")', rf'\g<1>{new_version}\g<3>', content)
                    with open(filepath, "wt", encoding = 'utf-8') as fh:
                        fh.write(content)
