/**
 * Copyright (c) 2026 Angelo ZERR and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.core.internal.grammar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ScopeStackTest {

	// --- getSegments ---

	@Nested
	@DisplayName("getSegments()")
	class GetSegments {

		@Test
		@DisplayName("single root scope")
		void singleScope() {
			final var stack = ScopeStack.from("source.java");
			assertThat(stack.getSegments()).containsExactly("source.java");
		}

		@Test
		@DisplayName("nested scopes are returned in root-to-leaf order")
		void nestedScopes() {
			final var stack = castNonNull(ScopeStack.from("source.java", "meta.class", "variable.other"));
			assertThat(stack.getSegments()).containsExactly("source.java", "meta.class", "variable.other");
		}

		@Test
		@DisplayName("deeply nested stack (4 levels)")
		void deeplyNested() {
			final var scopes = new String[] {
				"source.html", "meta.tag", "string.quoted.double", "punctuation.definition.string.begin"
			};
			final var stack = castNonNull(ScopeStack.from(scopes));
			assertThat(stack.getSegments()).containsExactly(scopes);
		}

		@Test
		@DisplayName("returns same list instance on repeated calls (cached)")
		void caching() {
			final var stack = castNonNull(ScopeStack.from("source.js", "string.quoted"));
			assertThat(stack.getSegments()).isSameAs(stack.getSegments());
		}

		@Test
		@DisplayName("parent and child have independent caches")
		void parentChildCaching() {
			final var parent = ScopeStack.from("source.ts");
			final var child = parent.push("meta.block");

			assertThat(child.getSegments()).containsExactly("source.ts", "meta.block");
			assertThat(parent.getSegments()).containsExactly("source.ts");
		}

		@Test
		@DisplayName("siblings pushed from same parent have independent caches")
		void siblingCaching() {
			final var parent = ScopeStack.from("source.css");
			final var child1 = parent.push("meta.selector");
			final var child2 = parent.push("comment.block");

			assertThat(child1.getSegments()).containsExactly("source.css", "meta.selector");
			assertThat(child2.getSegments()).containsExactly("source.css", "comment.block");
			assertThat(parent.getSegments()).containsExactly("source.css");
		}

		@Test
		@DisplayName("root segments list is unmodifiable")
		void rootUnmodifiable() {
			final var stack = ScopeStack.from("source.py");
			final var segments = stack.getSegments();
			assertThrows(UnsupportedOperationException.class,
					() -> segments.add("nope"));
		}

		@Test
		@DisplayName("non-root segments list is unmodifiable")
		void nonRootUnmodifiable() {
			final var stack = ScopeStack.from("source.py").push("meta.class");
			final var segments = stack.getSegments();
			assertThrows(UnsupportedOperationException.class,
					() -> segments.add("nope"));
		}
	}

	// --- push ---

	@Nested
	@DisplayName("push()")
	class Push {

		@Test
		@DisplayName("push(String) extends parent segments")
		void pushString() {
			final var root = ScopeStack.from("source.css");
			final var child = root.push("meta.selector");
			final var grandchild = child.push("entity.name.tag");

			assertThat(root.getSegments()).containsExactly("source.css");
			assertThat(child.getSegments()).containsExactly("source.css", "meta.selector");
			assertThat(grandchild.getSegments()).containsExactly("source.css", "meta.selector", "entity.name.tag");
		}

		@Test
		@DisplayName("push(path, List) builds correct segments from null")
		void pushListFromNull() {
			final var stack = castNonNull(ScopeStack.push(null, List.of("source.md", "meta.paragraph")));
			assertThat(stack.getSegments()).containsExactly("source.md", "meta.paragraph");
		}

		@Test
		@DisplayName("push(path, List) appends to existing stack")
		void pushListOntoExisting() {
			final var root = ScopeStack.from("source.md");
			final var pushed = castNonNull(ScopeStack.push(root, List.of("meta.paragraph", "markup.bold")));
			assertThat(pushed.getSegments()).containsExactly("source.md", "meta.paragraph", "markup.bold");
		}

		@Test
		@DisplayName("push(path, emptyList) returns the same stack")
		void pushEmptyList() {
			final var root = ScopeStack.from("source.go");
			final var pushed = ScopeStack.push(root, Collections.emptyList());
			assertThat(pushed).isSameAs(root);
		}
	}

	// --- from ---

	@Nested
	@DisplayName("from()")
	class From {

		@Test
		@DisplayName("from(String) and from(String...) with single element are consistent")
		void fromVariantsConsistent() {
			final var fromSingle = ScopeStack.from("source.go");
			final var fromVarargs = castNonNull(ScopeStack.from(new String[] { "source.go" }));
			assertThat(fromSingle.getSegments()).isEqualTo(fromVarargs.getSegments());
		}

		@Test
		@DisplayName("from(String...) with empty array returns null")
		void fromEmptyArray() {
			assertThat(ScopeStack.from(new String[0])).isNull();
		}
	}

	// --- isExtending ---

	@Nested
	@DisplayName("isExtending()")
	class IsExtending {

		@Test
		@DisplayName("a stack extends itself")
		void extendsSelf() {
			final var stack = ScopeStack.from("source.java");
			assertThat(stack.isExtending(stack)).isTrue();
		}

		@Test
		@DisplayName("child extends parent")
		void childExtendsParent() {
			final var parent = ScopeStack.from("source.java");
			final var child = parent.push("meta.class");
			assertThat(child.isExtending(parent)).isTrue();
		}

		@Test
		@DisplayName("grandchild extends grandparent")
		void grandchildExtendsGrandparent() {
			final var root = ScopeStack.from("source.java");
			final var child = root.push("meta.class");
			final var grandchild = child.push("variable.other");
			assertThat(grandchild.isExtending(root)).isTrue();
		}

		@Test
		@DisplayName("parent does not extend child")
		void parentDoesNotExtendChild() {
			final var parent = ScopeStack.from("source.java");
			final var child = parent.push("meta.class");
			assertThat(parent.isExtending(child)).isFalse();
		}

		@Test
		@DisplayName("unrelated stacks do not extend each other")
		void unrelatedStacks() {
			final var a = ScopeStack.from("source.java");
			final var b = ScopeStack.from("source.python");
			assertThat(a.isExtending(b)).isFalse();
		}

		@Test
		@DisplayName("siblings do not extend each other")
		void siblingsDoNotExtend() {
			final var parent = ScopeStack.from("source.html");
			final var sibling1 = parent.push("meta.tag");
			final var sibling2 = parent.push("comment.block");
			assertThat(sibling1.isExtending(sibling2)).isFalse();
			assertThat(sibling2.isExtending(sibling1)).isFalse();
		}
	}

	// --- getExtensionIfDefined ---

	@Nested
	@DisplayName("getExtensionIfDefined()")
	class GetExtensionIfDefined {

		@Test
		@DisplayName("returns extension scopes from base to this")
		void extensionFromBase() {
			final var root = ScopeStack.from("source.ts");
			final var child = root.push("meta.block");
			final var grandchild = child.push("variable.other");
			assertThat(grandchild.getExtensionIfDefined(root))
					.containsExactly("meta.block", "variable.other");
		}

		@Test
		@DisplayName("returns single scope when direct child of base")
		void directChildOfBase() {
			final var root = ScopeStack.from("source.ts");
			final var child = root.push("meta.block");
			assertThat(child.getExtensionIfDefined(root)).containsExactly("meta.block");
		}

		@Test
		@DisplayName("returns empty list when this == base")
		void sameAsBase() {
			final var stack = ScopeStack.from("source.ts");
			assertThat(stack.getExtensionIfDefined(stack)).isEmpty();
		}

		@Test
		@DisplayName("returns empty list when base is not an ancestor")
		void baseNotAncestor() {
			final var a = ScopeStack.from("source.java");
			final var b = ScopeStack.from("source.python");
			final var child = b.push("meta.class");
			assertThat(child.getExtensionIfDefined(a)).isEmpty();
		}

		@Test
		@DisplayName("returns all scopes when base is null")
		void nullBase() {
			final var stack = castNonNull(ScopeStack.from("source.go", "meta.function"));
			assertThat(stack.getExtensionIfDefined(null))
					.containsExactly("source.go", "meta.function");
		}
	}

	// --- toString ---

	@Nested
	@DisplayName("toString()")
	class ToString {

		@Test
		@DisplayName("returns space-separated scopes")
		void spaceSeparated() {
			final var stack = castNonNull(ScopeStack.from("source.java", "meta.class"));
			assertThat(stack.toString()).isEqualTo("source.java meta.class");
		}

		@Test
		@DisplayName("single scope has no spaces")
		void singleScope() {
			assertThat(ScopeStack.from("source.java").toString()).isEqualTo("source.java");
		}
	}
}
