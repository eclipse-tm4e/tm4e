@echo off
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Copyright (c) 2022 Sebatian Thomschke and others.
:: All rights reserved. This program and the accompanying materials
:: are made available under the terms of the Eclipse Public License v1.0
:: which accompanies this distribution, and is available at
:: http://www.eclipse.org/legal/epl-v10.html
::
:: Contributors:
::     Sebatian Thomschke - Initial API and implementation
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

setlocal

cd /D "%~dp0..\..\.."

mvn clean verify ^
  -pl target-platform,org.eclipse.tm4e.core ^
  -Djgit.dirtyWorkingTree=warning ^
  -DskipTests ^
  -DbenchmarkClass=org.eclipse.tm4e.core.benchmark.GrammarBenchmark
