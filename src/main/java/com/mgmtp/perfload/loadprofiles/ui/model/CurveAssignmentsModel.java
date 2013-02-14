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

import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;

import com.google.common.collect.Lists;
import com.mgmtp.perfload.loadprofiles.model.CurveAssignment;

/**
 * {@link ListModel} implementation backing the list of curve assignments.
 * 
 * @author rnaegele
 */
public class CurveAssignmentsModel extends AbstractListModel {

	private static final long serialVersionUID = 1L;

	private final List<CurveAssignment> curveAssignments = Lists.newArrayList();

	public int addElement(final CurveAssignment ca) {
		int i = curveAssignments.indexOf(ca);
		if (i >= 0) {
			return i;
		}
		curveAssignments.add(ca);
		Collections.sort(curveAssignments);
		i = curveAssignments.indexOf(ca);
		fireIntervalAdded(this, i, i);
		return i;
	}

	public int insertElementAt(final CurveAssignment ca, final int index) {
		int i = curveAssignments.indexOf(ca);
		if (i >= 0) {
			return i;
		}
		curveAssignments.add(index, ca);
		Collections.sort(curveAssignments);
		// index may have change because of the sorting
		i = curveAssignments.indexOf(ca);
		fireIntervalAdded(this, i, i);
		return i;
	}

	public int setElementAt(final CurveAssignment ca, final int index) {
		int i = curveAssignments.indexOf(ca);
		if (i >= 0) {
			return i;
		}
		curveAssignments.set(index, ca);
		Collections.sort(curveAssignments);
		// index may have change because of the sorting
		i = curveAssignments.indexOf(ca);
		fireContentsChanged(this, i, i);
		return i;
	}

	public void removeElement(final CurveAssignment ca) {
		int index = curveAssignments.indexOf(ca);
		if (index != -1) {
			removeElementAt(index);
		}
	}

	public void removeElementAt(final int index) {
		curveAssignments.remove(index);
		fireIntervalRemoved(this, index, index);
	}

	public void clear() {
		curveAssignments.clear();
		fireContentsChanged(this, -1, -1);
	}

	public List<CurveAssignment> getItems() {
		return Collections.unmodifiableList(curveAssignments);
	}

	@Override
	public CurveAssignment getElementAt(final int index) {
		return curveAssignments.get(index);
	}

	@Override
	public int getSize() {
		return curveAssignments.size();
	}

	public void modelUpdated() {
		fireContentsChanged(this, -1, -1);
	}
}
