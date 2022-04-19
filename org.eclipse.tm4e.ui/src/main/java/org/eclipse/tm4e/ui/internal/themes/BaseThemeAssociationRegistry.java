/**
 *  Copyright (c) 2015-2018 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.ui.internal.themes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.tm4e.ui.themes.IThemeAssociation;

/**
 * Base Theme association registry.
 *
 * TODO unused code
 */
final class BaseThemeAssociationRegistry {

	private IThemeAssociation defaultAssociation;
	private final Map<String /* E4 Theme id */, List<IThemeAssociation>> eclipseThemeIds;
	private final List<IThemeAssociation> allAssociations;

	BaseThemeAssociationRegistry() {
		eclipseThemeIds = new HashMap<>();
		this.allAssociations = new ArrayList<>();
	}

	void register(IThemeAssociation association) {
		//String eclipseThemeId = association.getEclipseThemeId();
		// when association is marked as default or scope name is defined,
		// update the default association or association for a given E4 Theme.
		/*if (association.isDefault()) {
			if (eclipseThemeId == null) {
				defaultAssociation = association;
			} else {
				register(association, eclipseThemeId);
			}
		} else if (eclipseThemeId != null) {
			register(association, eclipseThemeId);
		}*/
		allAssociations.clear();
	}

	void unregister(IThemeAssociation association) {
		//String eclipseThemeId = association.getEclipseThemeId();
		/*if (association.isDefault()) {
			if (eclipseThemeId == null) {
				defaultAssociation = null;
			}
		}*/
//		Collection<IThemeAssociation> associations = eclipseThemeIds.get(eclipseThemeId);
//		if (associations != null) {
//			for (IThemeAssociation a : associations) {
//				if (a.equals(association)) {
//					associations.remove(a);
//					break;
//				}
//			}
//		}
		allAssociations.clear();
	}

	IThemeAssociation getThemeAssociationFor(String eclipseThemeId) {
		List<IThemeAssociation> associations = eclipseThemeIds.get(eclipseThemeId);
		if (associations != null) {
			if (associations.size() == 1) {
				return associations.get(0);
			}
			/*for (IThemeAssociation association : associations) {
				if (association.isDefault()) {
					return association;
				}
			}*/
		}
		return null;
	}

	IThemeAssociation[] getThemeAssociations(boolean isDefault) {
		/*if (isDefault) {
			return getThemeAssociations().stream().filter(theme -> theme.isDefault()).collect(Collectors.toList())
					.toArray(new IThemeAssociation[0]);
		}*/
		return getThemeAssociations().toArray(new IThemeAssociation[allAssociations.size()]);
	}

	IThemeAssociation getDefaultAssociation() {
		return defaultAssociation;
	}

	IThemeAssociation[] getThemeAssociationsForTheme(String themeId) {
		return getThemeAssociations().stream().filter(themeAssociation -> themeId.equals(themeAssociation.getThemeId()))
				.collect(Collectors.toList()).toArray(new IThemeAssociation[0]);
	}

	boolean hasThemeAssociationsForTheme(String themeId, String eclipseThemeId) {
//		Collection<IThemeAssociation> associations = eclipseThemeIds.get(eclipseThemeId);
//		if (associations != null) {
//			for (IThemeAssociation themeAssociation : associations) {
//				if (themeId.equals(themeAssociation.getThemeId())) {
//					return eclipseThemeId.equals(themeAssociation.getEclipseThemeId());
//				}
//			}
//			return false;
//		} else {
//			Set<Entry<String, List<IThemeAssociation>>> s = eclipseThemeIds.entrySet();
//			for (Entry<String, List<IThemeAssociation>> entry : s) {
//				for (IThemeAssociation themeAssociation : entry.getValue()) {
//					if (themeId.equals(themeAssociation.getThemeId())) {
//						return eclipseThemeId.equals(themeAssociation.getEclipseThemeId());
//					}
//				}
//			}
//		}
		return true;
	}

	List<IThemeAssociation> getThemeAssociations() {
		if (allAssociations.isEmpty()) {
			if (defaultAssociation != null) {
				allAssociations.add(defaultAssociation);
			}
			Collection<List<IThemeAssociation>> associations = eclipseThemeIds.values();
			for (Collection<IThemeAssociation> collection : associations) {
				allAssociations.addAll(collection);
			}
		}
		return allAssociations;
	}
}
