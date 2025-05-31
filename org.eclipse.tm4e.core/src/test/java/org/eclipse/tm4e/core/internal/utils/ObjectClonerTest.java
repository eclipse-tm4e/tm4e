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

import static org.assertj.core.api.Assertions.assertThat;

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
		assertThat(repoClone).isNotNull();
		assertThat(repoClone).isNotSameAs(repo);
		assertThat(repoClone).isEqualTo(repo);

		assertThat(repo.getRule("rule1")).isNotNull();
		assertThat(repo.getRule("rule1_1")).isNotNull();
		assertThat(repo.getRule("rule2")).isNotNull();
		assertThat(repo.getRule("rule2_2")).isNotNull();

		assertThat(repoClone.getRule("rule1")).isNotSameAs(rule1);
		assertThat(repoClone.getRule("rule1_1")).isNotSameAs(rule1);
		assertThat(repoClone.getRule("rule2")).isNotSameAs(rule2);
		assertThat(repoClone.getRule("rule2_2")).isNotSameAs(rule2);

		assertThat(repoClone.getRule("rule1")).isEqualTo(rule1);
		assertThat(repoClone.getRule("rule1_1")).isEqualTo(rule1);
		assertThat(repoClone.getRule("rule2")).isEqualTo(rule2);
		assertThat(repoClone.getRule("rule2_2")).isEqualTo(rule2);

		assertThat(repoClone.getRule("rule1")).isSameAs(repoClone.getRule("rule1_1"));
		assertThat(repoClone.getRule("rule2")).isSameAs(repoClone.getRule("rule2_2"));
	}

	@Test
	void testDeepCloneEmptyArray() {
		final var arr = new RawRule[0];
		final var clone = ObjectCloner.deepClone(arr);
		assertThat(clone).isNotSameAs(arr);
		assertThat(clone).isEqualTo(arr);
	}

	@Test
	void testDeepCloneArray() {
		final var rule1 = new RawRule();
		rule1.setName("Rule1");
		final var rule2 = new RawRule();
		rule2.setName("Rule2");
		final var arr = new RawRule[] { rule1, rule1, rule2, rule2 };
		final var arrClone = ObjectCloner.deepClone(arr);

		assertThat(arrClone).isNotSameAs(arr);
		assertThat(arrClone).isEqualTo(arr);

		assertThat(arrClone[0]).isNotSameAs(rule1);
		assertThat(arrClone[1]).isNotSameAs(rule1);
		assertThat(arrClone[2]).isNotSameAs(rule2);
		assertThat(arrClone[3]).isNotSameAs(rule2);

		assertThat(arrClone[0]).isEqualTo(rule1);
		assertThat(arrClone[1]).isEqualTo(rule1);
		assertThat(arrClone[2]).isEqualTo(rule2);
		assertThat(arrClone[3]).isEqualTo(rule2);

		assertThat(arrClone[0]).isSameAs(arrClone[1]);
		assertThat(arrClone[2]).isSameAs(arrClone[3]);
	}
}
