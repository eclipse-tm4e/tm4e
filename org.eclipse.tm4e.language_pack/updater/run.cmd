@echo off
:: Copyright (c) 2023 Sebastian Thomschke and contributors.
::
:: This program and the accompanying materials are made
:: available under the terms of the Eclipse Public License 2.0
:: which is available at https://www.eclipse.org/legal/epl-2.0/
::
:: SPDX-License-Identifier: EPL-2.0
::
:: based on https://github.com/sebthom/extra-syntax-highlighting-eclipse-plugin/blob/main/plugin/updater

setlocal

cd /D "%~dp0"

set MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 -XX:TieredStopAtLevel=1 -XX:+UseParallelGC"

echo Launching...
mvn --quiet -e compiler:compile -Dexec.mainClass="updater.Updater" exec:java -Dexec.args="%*"
