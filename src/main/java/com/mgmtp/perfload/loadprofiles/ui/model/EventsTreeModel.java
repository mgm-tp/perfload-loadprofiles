/*
 * Copyright (c) 2013 mgm technology partners GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mgmtp.perfload.loadprofiles.ui.model;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.indexOf;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.google.common.collect.Maps.newTreeMap;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.mgmtp.perfload.loadprofiles.model.CurveAssignment;

/**
 * A {@link TreeModel} implementation in GlazedLists style that turns an {@link EventList} into a
 * tree structure. Load profile events are grouped by operation and markers are kept in an extra
 * group. Changes in the underlying list are reflected automatically.
 * 
 * @author rnaegele
 */
public class EventsTreeModel implements TreeModel, ListEventListener<LoadProfileEntity> {

	private static final String ROOT = "Elements";

	private final EventList<LoadProfileEntity> loadProfileEnities;
	private final SortedMap<String, List<LoadProfileEntity>> treeData = newTreeMap();
	private final List<TreeModelListener> listenerList = newArrayListWithExpectedSize(1);
	private final List<TreePath> paths = newArrayList();

	public EventsTreeModel(final EventList<LoadProfileEntity> loadProfileEnities) {
		this.loadProfileEnities = loadProfileEnities;
		loadProfileEnities.addListEventListener(this);
	}

	@Override
	public void listChanged(final ListEvent<LoadProfileEntity> listChanges) {
		treeData.clear();
		paths.clear();

		for (LoadProfileEntity lpe : loadProfileEnities) {
			String key;
			if (lpe instanceof CurveAssignment) {
				CurveAssignment ca = (CurveAssignment) lpe;
				key = ca.getOperation().getName();
			} else {
				key = "***Markers***";
			}
			addLoadProfileEnity(lpe, key);
			paths.add(new TreePath(new Object[] { ROOT, key, lpe }));
		}

		for (TreeModelListener listener : listenerList) {
			listener.treeStructureChanged(new TreeModelEvent(this, new Object[] { ROOT }));
		}
	}

	private void addLoadProfileEnity(final LoadProfileEntity ca, final String key) {
		List<LoadProfileEntity> list = treeData.get(key);
		if (list == null) {
			list = newArrayListWithCapacity(3);
			treeData.put(key, list);
		}
		list.add(ca);
		Collections.sort(list);
	}

	@Override
	public Object getRoot() {
		return ROOT;
	}

	public List<TreePath> getPaths() {
		return ImmutableList.copyOf(paths);
	}

	@Override
	public Object getChild(final Object parent, final int index) {
		Object result;
		if (ROOT.equals(parent)) {
			Set<String> keySet = treeData.keySet();
			result = get(keySet, index);
		} else {
			String key = (String) parent;
			List<LoadProfileEntity> caList = treeData.get(key);
			result = caList.get(index);
		}
		return result;
	}

	@Override
	public int getChildCount(final Object parent) {
		int result;
		if (ROOT.equals(parent)) {
			result = treeData.keySet().size();
		} else {
			String key = (String) parent;
			result = treeData.get(key).size();
		}
		return result;
	}

	@Override
	public boolean isLeaf(final Object node) {
		return node instanceof LoadProfileEntity;
	}

	@Override
	public void valueForPathChanged(final TreePath path, final Object newValue) {
		// no-op
	}

	@Override
	public int getIndexOfChild(final Object parent, final Object child) {
		Predicate<Object> predicate = new Predicate<Object>() {
			@Override
			public boolean apply(final Object input) {
				return child.equals(input);
			}
		};

		int result;
		if (ROOT.equals(parent)) {
			result = indexOf(treeData.keySet(), predicate);
		} else {
			String key = (String) parent;
			result = indexOf(treeData.get(key), predicate);
		}
		return result;
	}

	@Override
	public void addTreeModelListener(final TreeModelListener l) {
		listenerList.add(l);
	}

	@Override
	public void removeTreeModelListener(final TreeModelListener l) {
		listenerList.remove(l);
	}
}
