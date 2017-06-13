/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.grammar;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.tm4e.core.grammar.internal.MatcherTestImpl;
import org.eclipse.tm4e.core.grammar.internal.RawTestImpl;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import junit.framework.TestSuite;

/**
 * VSCode TextMate grammar tests which uses same vscode-textmate tests located
 * at src\test\resources\test-cases
 * 
 * @see https://github.com/Microsoft/vscode-textmate/blob/master/src/tests/tests.ts
 *
 */
@RunWith(AllTests.class)
public class GrammarSuiteTest {

	private static final File REPO_ROOT = new File("src/test/resources");

	// TODO: fix thoses tests:
	// It seems that problem comes from with encoding. OnigString should support UTF-16 like https://github.com/atom/node-oniguruma/blob/master/src/onig-string.cc 
	private static final List<String> IGNORE_TESTS = Arrays.asList("TEST #24", "TEST #66", "TEST #67", "Issue #8");

	public static TestSuite suite() throws Exception {
		TestSuite rootTestSuite = new TestSuite();

		// Tokenization /first-mate/
		TestSuite firstMateTestSuite = createTestSuite("Tokenization /first-mate/", rootTestSuite);
		createVSCodeTestSuite("test-cases/first-mate/tests.json", firstMateTestSuite);

		// Tokenization /suite1/
		TestSuite suite1TestSuite = createTestSuite("Tokenization /suite1/", rootTestSuite);
		createVSCodeTestSuite("test-cases/suite1/tests.json", suite1TestSuite);
		createVSCodeTestSuite("test-cases/suite1/whileTests.json", suite1TestSuite);

		// Matcher
		TestSuite matcherTestSuite = createTestSuite("Matcher", rootTestSuite);
		createMatcherTestSuite(matcherTestSuite);

		return rootTestSuite;
	}

	private static TestSuite createTestSuite(String name, TestSuite parentTestSuite) {
		TestSuite testSuite = new TestSuite(name);
		parentTestSuite.addTest(testSuite);
		return testSuite;
	}

	private static TestSuite createVSCodeTestSuite(String name, TestSuite parentTestSuite) throws Exception {
		TestSuite vscodeTestSuite = createTestSuite(name, parentTestSuite);
		addVSCodeTestSuite(new File(REPO_ROOT, name), vscodeTestSuite);
		return vscodeTestSuite;
	}

	private static void addVSCodeTestSuite(File testLocation, TestSuite suite) throws Exception {
		Type listType = new TypeToken<ArrayList<RawTestImpl>>() {
		}.getType();
		List<RawTestImpl> tests = new GsonBuilder().create().fromJson(new FileReader(testLocation), listType);
		for (RawTestImpl test : tests) {
			if (!IGNORE_TESTS.contains(test.getDesc())) {
				test.setTestLocation(testLocation);
				suite.addTest(test);
			}
		}
	}

	private static void createMatcherTestSuite(TestSuite suite) throws Exception {
		Type listType = new TypeToken<ArrayList<MatcherTestImpl>>() {
		}.getType();
		List<MatcherTestImpl> tests = new GsonBuilder().create()
				.fromJson(new FileReader(new File(REPO_ROOT, "matcher-tests.json")), listType);
		int i = 0;
		for (MatcherTestImpl test : tests) {
			test.setDesc("Test #" + (i++));
			suite.addTest(test);
		}
	}
}
