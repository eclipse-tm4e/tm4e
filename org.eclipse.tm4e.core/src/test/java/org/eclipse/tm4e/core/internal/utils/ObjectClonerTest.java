/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.core.internal.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.tm4e.core.internal.grammar.raw.RawRepository;
import org.eclipse.tm4e.core.internal.grammar.raw.RawRule;
import org.junit.jupiter.api.Test;

class ObjectClonerTest {

	@Test
	void testDeepCloneRawRepository() {
		final var rule1 = new RawRule();
		rule1.setName("Rule1");
		final var rule2 = new RawRule();
		rule2.setName("Rule2");
		final var repo = new RawRepository();
		repo.put("rule1", rule1);
		repo.put("rule1_1", rule1);
		repo.put("rule2", rule2);
		repo.put("rule2_2", rule2);

		final var repoClone = ObjectCloner.deepClone(repo);
		assertNotNull(repoClone);
		assertNotSame(repo, repoClone);
		assertEquals(repo, repoClone);

		assertNotNull(repo.getRule("rule1"));
		assertNotNull(repo.getRule("rule1_1"));
		assertNotNull(repo.getRule("rule2"));
		assertNotNull(repo.getRule("rule2_2"));

		assertNotSame(rule1, repoClone.getRule("rule1"));
		assertNotSame(rule1, repoClone.getRule("rule1_1"));
		assertNotSame(rule2, repoClone.getRule("rule2"));
		assertNotSame(rule2, repoClone.getRule("rule2_2"));

		assertEquals(rule1, repoClone.getRule("rule1"));
		assertEquals(rule1, repoClone.getRule("rule1_1"));
		assertEquals(rule2, repoClone.getRule("rule2"));
		assertEquals(rule2, repoClone.getRule("rule2_2"));

		assertSame(repoClone.getRule("rule1"), repoClone.getRule("rule1_1"));
		assertSame(repoClone.getRule("rule2"), repoClone.getRule("rule2_2"));
	}

	@Test
	void testDeepCloneEmptyArray() {
		final var arr = new RawRule[0];
		final var clone = ObjectCloner.deepClone(arr);
		assertArrayEquals(arr, clone);
	}

	@Test
	void testDeepCloneArray() {
		final var rule1 = new RawRule();
		rule1.setName("Rule1");
		final var rule2 = new RawRule();
		rule2.setName("Rule2");
		final var arr = new RawRule[] { rule1, rule1, rule2, rule2 };
		final var arrClone = ObjectCloner.deepClone(arr);
		assertNotSame(arr, arrClone);
		assertArrayEquals(arr, arrClone);

		assertNotSame(rule1, arrClone[0]);
		assertNotSame(rule1, arrClone[1]);
		assertNotSame(rule2, arrClone[2]);
		assertNotSame(rule2, arrClone[3]);

		assertEquals(rule1, arrClone[0]);
		assertEquals(rule1, arrClone[1]);
		assertEquals(rule2, arrClone[2]);
		assertEquals(rule2, arrClone[3]);

		assertSame(arrClone[0], arrClone[1]);
		assertSame(arrClone[2], arrClone[3]);
	}
}
