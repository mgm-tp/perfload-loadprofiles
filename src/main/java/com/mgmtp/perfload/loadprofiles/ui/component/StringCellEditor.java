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
package com.mgmtp.perfload.loadprofiles.ui.component;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import com.mgmtp.perfload.loadprofiles.model.AbstractNamedObject;

/**
 * @author rnaegele
 */
public class StringCellEditor extends DefaultCellEditor {

	private final List<? extends AbstractNamedObject<?>> objects;
	private final JTable table;

	public StringCellEditor(final JTable table, final List<? extends AbstractNamedObject<?>> objects) {
		super(new JTextField());
		this.table = table;
		this.objects = objects;
	}

	@Override
	public boolean stopCellEditing() {
		String s = (String) super.getCellEditorValue();
		if (isBlank(s)) {
			editorComponent.setBorder(new LineBorder(Color.red));
			return false;
		}

		int row = table.getSelectedRow();

		for (int i = 0; i < objects.size(); ++i) {
			if (i != row && s.equals(objects.get(i).getName())) {
				editorComponent.setBorder(new LineBorder(Color.red));
				return false;
			}
		}

		return super.stopCellEditing();
	}

	@Override
	public Component getTableCellEditorComponent(final JTable tbl, final Object value, final boolean isSelected, final int row,
			final int column) {
		editorComponent.setBorder(new LineBorder(Color.black));
		return super.getTableCellEditorComponent(table, value, isSelected, row, column);
	}
}
