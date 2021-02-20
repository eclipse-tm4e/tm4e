/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.grammar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.tm4e.core.grammar.internal.MatcherTestImpl;
import org.eclipse.tm4e.core.grammar.internal.RawTestImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * VSCode TextMate grammar tests which uses same vscode-textmate tests located
 * at src\test\resources\test-cases
 * 
 * @see https://github.com/Microsoft/vscode-textmate/blob/master/src/tests/tests.ts
 *
 */
public class GrammarSuiteTest {

	private static final File REPO_ROOT = new File("src/test/resources");

	// TODO: fix thoses tests:
	// It seems that problem comes from with encoding. OnigString should support UTF-16 like https://github.com/atom/node-oniguruma/blob/master/src/onig-string.cc 
	private static final List<String> IGNORE_TESTS = List.of("TEST #24", "TEST #66");

	@TestFactory @DisplayName("Tokenization /first-mate/")
	Collection<DynamicTest> firstMate() throws Exception {
		return createVSCodeTestSuite(new File(REPO_ROOT, "test-cases/first-mate/tests.json"));
	}
	
	@TestFactory @DisplayName("Tokenization /suite1/ tests.json")
	Collection<DynamicTest> testsJSon() throws Exception {
		return createVSCodeTestSuite(new File(REPO_ROOT, "test-cases/suite1/tests.json"));
	}
	
	@TestFactory @DisplayName("Tokenization /suite1/ whileTests.json")
	Collection<DynamicTest> whileTests() throws Exception {
		return createVSCodeTestSuite(new File(REPO_ROOT, "test-cases/suite1/whileTests.json"));
	}


	private List<DynamicTest>  createVSCodeTestSuite(File testLocation) throws Exception {
		Type listType = new TypeToken<ArrayList<RawTestImpl>>() {
		}.getType();
		List<RawTestImpl> tests = new GsonBuilder().create().fromJson(new FileReader(testLocation), listType);
		List<DynamicTest> dynamicTests = new ArrayList<>();
		for (RawTestImpl test : tests) {
			if (!IGNORE_TESTS.contains(test.getDesc())) {
				test.setTestLocation(testLocation);
				dynamicTests.add(DynamicTest.dynamicTest(test.getDesc(), () -> test.executeTest()));
			}
		}
		return dynamicTests;
	}
	
	@TestFactory @DisplayName("Matcher tests")
	Collection<DynamicTest> dynamicTestsWithCollection() throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		Type listType = new TypeToken<ArrayList<MatcherTestImpl>>() {
		}.getType();
		List<MatcherTestImpl> tests = new GsonBuilder().create()
				.fromJson(new FileReader(new File(REPO_ROOT, "matcher-tests.json")), listType);
		List<DynamicTest> dynamicTests = new ArrayList<>();
		int i = 0;
		for (MatcherTestImpl test : tests) {
			dynamicTests.add(DynamicTest.dynamicTest("Test #" + (i++), () -> test.executeTest()));
		}
		return dynamicTests;
	}

}
