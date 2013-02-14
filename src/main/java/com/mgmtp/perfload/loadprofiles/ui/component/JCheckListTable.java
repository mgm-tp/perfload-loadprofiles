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
package com.mgmtp.perfload.loadprofiles.ui.component;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;

/**
 * {@link JTable} descendant with special settings suitable for checkbox lists.
 * 
 * @author rnaegele
 */
public class JCheckListTable extends JTableExt {

	private final boolean frozen;

	public JCheckListTable() {
		setTableHeader(null);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setColumnSelectionAllowed(false);
		setRowSelectionAllowed(false);
		setFillsViewportHeight(true);
		setShowGrid(false);
		frozen = true;
	}

	/**
	 * Throws {@link UnsupportedOperationException}
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public void setTableHeader(final JTableHeader tableHeader) {
		if (frozen) {
			throw new UnsupportedOperationException("Property change not allowed.");
		}
		super.setTableHeader(tableHeader);
	}

	/**
	 * Throws {@link UnsupportedOperationException}
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public void setSelectionMode(final int selectionMode) {
		if (frozen) {
			throw new UnsupportedOperationException("Property change not allowed.");
		}
		super.setSelectionMode(selectionMode);
	}

	/**
	 * Throws {@link UnsupportedOperationException}
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public void setColumnSelectionAllowed(final boolean columnSelectionAllowed) {
		if (frozen) {
			throw new UnsupportedOperationException("Property change not allowed.");
		}
		super.setColumnSelectionAllowed(columnSelectionAllowed);
	}

	@Override
	public void setRowSelectionAllowed(final boolean rowSelectionAllowed) {
		if (frozen) {
			throw new UnsupportedOperationException("Property change not allowed.");
		}
		super.setRowSelectionAllowed(rowSelectionAllowed);
	}

	/**
	 * Throws {@link UnsupportedOperationException}
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public void setFillsViewportHeight(final boolean fillsViewportHeight) {
		if (frozen) {
			throw new UnsupportedOperationException("Property change not allowed.");
		}
		super.setFillsViewportHeight(fillsViewportHeight);
	}

	/**
	 * Throws {@link UnsupportedOperationException}
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public void setShowGrid(final boolean showGrid) {
		if (frozen) {
			throw new UnsupportedOperationException("Property change not allowed.");
		}
		super.setShowGrid(showGrid);
	}
}
