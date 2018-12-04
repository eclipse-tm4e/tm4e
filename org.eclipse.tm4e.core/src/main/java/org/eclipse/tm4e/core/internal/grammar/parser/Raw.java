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
package org.eclipse.tm4e.core.internal.grammar.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.tm4e.core.internal.types.IRawCaptures;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;
import org.eclipse.tm4e.core.internal.types.IRawRepository;
import org.eclipse.tm4e.core.internal.types.IRawRule;
import org.eclipse.tm4e.core.internal.utils.CloneUtils;

/**
 * Raw
 *
 */
public class Raw extends HashMap<String, Object> implements IRawRepository, IRawRule, IRawGrammar, IRawCaptures {

	@Override
	public IRawRule getProp(String name) {
		return (IRawRule) super.get(name);
	}

	@Override
	public IRawRule getBase() {
		return (IRawRule) super.get("$base");
	}

	@Override
	public void setBase(IRawRule base) {
		super.put("$base", base);
	}

	@Override
	public IRawRule getSelf() {
		return (IRawRule) super.get("$self");
	}

	@Override
	public void setSelf(IRawRule self) {
		super.put("$self", self);
	}

	@Override
	public Integer getId() {
		return (Integer) super.get("id");
	}

	@Override
	public void setId(Integer id) {
		super.put("id", id);
	}

	@Override
	public String getName() {
		return (String) super.get("name");
	}

	@Override
	public void setName(String name) {
		super.put("name", name);
	}

	@Override
	public String getContentName() {
		return (String) super.get("contentName");
	}

	@Override
	public void setContentName(String name) {
		super.put("contentName", name);
	}

	@Override
	public String getMatch() {
		return (String) super.get("match");
	}

	@Override
	public void setMatch(String match) {
		super.put("match", match);
	}

	@Override
	public IRawCaptures getCaptures() {
		updateCaptures("captures");
		return (IRawCaptures) super.get("captures");
	}

	private void updateCaptures(String name) {
		Object captures = super.get(name);
		if (captures instanceof List) {
			Raw rawCaptures = new Raw();
			int i = 0;
			for (Object capture : (List) captures) {
				i++;
				rawCaptures.put(i + "", capture);
			}
			super.put(name, rawCaptures);
		}
	}

	@Override
	public void setCaptures(IRawCaptures captures) {
		super.put("captures", captures);
	}

	@Override
	public String getBegin() {
		return (String) super.get("begin");
	}

	@Override
	public void setBegin(String begin) {
		super.put("begin", begin);
	}

	@Override
	public String getWhile() {
		return (String) super.get("while");
	}

	@Override
	public String getInclude() {
		return (String) super.get("include");
	}

	@Override
	public void setInclude(String include) {
		super.put("include", include);
	}

	@Override
	public IRawCaptures getBeginCaptures() {
		updateCaptures("beginCaptures");
		return (IRawCaptures) super.get("beginCaptures");
	}

	@Override
	public void setBeginCaptures(IRawCaptures beginCaptures) {
		super.put("beginCaptures", beginCaptures);
	}

	@Override
	public String getEnd() {
		return (String) super.get("end");
	}

	@Override
	public void setEnd(String end) {
		super.put("end", end);
	}

	@Override
	public IRawCaptures getEndCaptures() {
		updateCaptures("endCaptures");
		return (IRawCaptures) super.get("endCaptures");
	}

	@Override
	public void setEndCaptures(IRawCaptures endCaptures) {
		super.put("endCaptures", endCaptures);
	}

	@Override
	public IRawCaptures getWhileCaptures() {
		updateCaptures("whileCaptures");
		return (IRawCaptures) super.get("whileCaptures");
	}

	@Override
	public Collection<IRawRule> getPatterns() {
		return (Collection<IRawRule>) super.get("patterns");
	}

	@Override
	public void setPatterns(Collection<IRawRule> patterns) {
		super.put("patterns", patterns);
	}

	@Override
	public Map<String, IRawRule> getInjections() {
		return (Map<String, IRawRule>) super.get("injections");
	}

	@Override
	public String getInjectionSelector() {
		return (String) super.get("injectionSelector");
	}

	@Override
	public IRawRepository getRepository() {
		return (IRawRepository) super.get("repository");
	}

	@Override
	public void setRepository(IRawRepository repository) {
		super.put("repository", repository);
	}

	@Override
	public boolean isApplyEndPatternLast() {
		Object applyEndPatternLast = super.get("applyEndPatternLast");
		if (applyEndPatternLast == null) {
			return false;
		}
		if (applyEndPatternLast instanceof Boolean) {
			return (Boolean) applyEndPatternLast;
		}
		if (applyEndPatternLast instanceof Integer) {
			return ((Integer) applyEndPatternLast).equals(1);
		}
		return false;
	}

	@Override
	public void setApplyEndPatternLast(boolean applyEndPatternLast) {
		super.put("applyEndPatternLast", applyEndPatternLast);
	}

	@Override
	public String getScopeName() {
		return (String) super.get("scopeName");
	}

	@Override
	public Collection<String> getFileTypes() {
		return (Collection<String>) super.get("fileTypes");
	}

	@Override
	public String getFirstLineMatch() {
		return (String) super.get("firstLineMatch");
	}

	@Override
	public IRawRule getCapture(String captureId) {
		return getProp(captureId);
	}

	@Override
	public Iterator<String> iterator() {
		return super.keySet().iterator();
	}

	@Override
	public Object clone() {
		return CloneUtils.clone(this);
	}

}