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

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.text.JTextComponent;

/**
 * 
 * @author rnaegele
 */
public class JTableExt extends JTable {

	public JTableExt() {
		setColumnSelectionAllowed(false);
		getTableHeader().setReorderingAllowed(false);
	}

	public void setColumnWidths(final int... widths) {
		for (int i = 0; i < widths.length; ++i) {
			getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
		}
	}

	@Override
	public void changeSelection(final int row, final int column, final boolean toggle, final boolean extend) {
		super.changeSelection(row, column, toggle, extend);

		if (editCellAt(row, column)) {
			Component editor = getEditorComponent();
			editor.requestFocusInWindow();
			if (editor instanceof JTextComponent) {
				((JTextComponent) editor).selectAll();
			}
		}
	}
}
