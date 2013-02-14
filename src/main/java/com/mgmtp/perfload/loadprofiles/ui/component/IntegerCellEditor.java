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

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.awt.Color;

import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

/**
 * @author rnaegele
 */
public class IntegerCellEditor extends DefaultCellEditor {

	private Integer value;

	public IntegerCellEditor() {
		super(new JFormattedTextField());
		((JTextField) getComponent()).setHorizontalAlignment(SwingConstants.RIGHT);
	}

	@Override
	public boolean stopCellEditing() {
		String s = (String) super.getCellEditorValue();
		JTextField textField = (JTextField) getComponent();

		if (isBlank(s)) {
			textField.setBorder(new LineBorder(Color.red));
			return false;
		}

		try {
			value = Integer.valueOf(s);
		} catch (NumberFormatException ex2) {
			textField.setBorder(new LineBorder(Color.red));
			textField.selectAll();
			return false;
		}

		return super.stopCellEditing();
	}

	@Override
	public Object getCellEditorValue() {
		return value;
	}
}
