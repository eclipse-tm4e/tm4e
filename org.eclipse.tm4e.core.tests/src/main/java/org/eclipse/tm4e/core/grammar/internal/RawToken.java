/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.grammar.internal;

import java.util.List;
import java.util.Objects;

public class RawToken {

	private String value;
	private List<String> scopes;

	public RawToken() {
	}

	public RawToken(final String value, final List<String> scopes) {
		this.value = value;
		this.scopes = scopes;
	}

	public String getValue() {
		return value;
	}

	public List<String> getScopes() {
		return scopes;
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof final RawToken other //
				&& Objects.equals(this.value, other.value) //
				&& Objects.equals(this.scopes, other.scopes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(scopes, value);
	}

	@Override
	public String toString() {
		return "RawToken{\n  value:" + value + "\n  scopes:" + scopes + "\n}";
	}
}
