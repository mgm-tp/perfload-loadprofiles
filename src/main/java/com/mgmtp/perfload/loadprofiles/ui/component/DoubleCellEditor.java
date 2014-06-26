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
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

/**
 * @author rnaegele
 */
public class DoubleCellEditor extends DefaultCellEditor {

	private static final DecimalFormat FORMAT = new DecimalFormat("0.00");

	private Double value;

	public DoubleCellEditor() {
		super(new JTextField());
		final JTextField textField = (JTextField) getComponent();
		textField.setHorizontalAlignment(SwingConstants.RIGHT);
		delegate = new EditorDelegate() {
			@Override
			public void setValue(final Object value) {
				textField.setText(value != null ? FORMAT.format(value) : null);
			}

			@Override
			public Object getCellEditorValue() {
				return textField.getText();
			}
		};
		textField.addActionListener(delegate);
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
			value = Double.valueOf(s);
		} catch (NumberFormatException ex2) {
			try {
				Number n = FORMAT.parse(s);
				value = n.doubleValue();
			} catch (ParseException ex1) {
				textField.setBorder(new LineBorder(Color.red));
				textField.selectAll();
				return false;
			}
		}

		return super.stopCellEditing();
	}

	@Override
	public Object getCellEditorValue() {
		return value;
	}
}
