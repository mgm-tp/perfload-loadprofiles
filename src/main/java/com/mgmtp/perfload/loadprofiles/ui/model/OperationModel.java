/*
 * Copyright (c) 2014 mgm technology partners GmbH
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

import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import com.google.common.collect.Lists;
import com.mgmtp.perfload.loadprofiles.model.Operation;

/**
 * @author rnaegele
 */
public class OperationModel extends AbstractListModel implements ComboBoxModel {

	private static final long serialVersionUID = 1L;

	private final List<Operation> operations;
	private Operation selectedItem;

	public OperationModel(final Operation... operations) {
		this.operations = Lists.newArrayList(operations);
	}

	@Override
	public Object getSelectedItem() {
		return selectedItem;
	}

	@Override
	public void setSelectedItem(final Object anItem) {
		if (anItem == null) {
			selectedItem = null;
		} else if (operations.contains(anItem)) {
			if ((selectedItem != null && !selectedItem.equals(anItem)) || selectedItem == null) {
				selectedItem = (Operation) anItem;
			}
		}
		fireContentsChanged(this, -1, -1);
	}

	@Override
	public Object getElementAt(final int index) {
		return operations.get(index);
	}

	@Override
	public int getSize() {
		return operations.size();
	}
}
