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

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyVetoException;
import java.util.regex.Pattern;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * {@link JTextField} subclass that allows only input of integer values.
 * 
 * @author rnaegele
 */
public class JIntegerTextField extends JTextField {

	private static final long serialVersionUID = 1L;
	private static final Pattern INT_PATTERN = Pattern.compile("\\d+");

	public JIntegerTextField() {
		this(5);
	}

	public JIntegerTextField(final int columns) {
		super("0", columns);
		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(final FocusEvent event) {
				if (!event.isTemporary()) {
					selectAll();
				}
			}
		});
	}

	public int getValue() {
		try {
			int result = Integer.parseInt(getText());
			return result >= 0 ? result : 0;
		} catch (NumberFormatException exception) {
			return 0;
		}
	}

	public void setValue(final int value) throws PropertyVetoException {
		if (getValue() != value) {
			Integer oldValue = Integer.valueOf(getValue());
			Integer newValue = Integer.valueOf(value);
			fireVetoableChange("value", getValue(), value);

			setText(String.valueOf(value));
			// value ok (no veto), so we fire the property change
			firePropertyChange("value", oldValue, newValue);
		}
	}

	@Override
	protected Document createDefaultModel() {
		return new IntTextDocument();
	}

	private class IntTextDocument extends PlainDocument {
		private static final long serialVersionUID = 1L;

		@Override
		public void insertString(final int offs, final String str, final AttributeSet a)
				throws BadLocationException {
			if (str == null) {
				return;
			}
			String oldString = getText(0, getLength());
			String newString = oldString.substring(0, offs) + str + oldString.substring(offs);
			try {
				if (INT_PATTERN.matcher(newString).matches()) {
					super.insertString(offs, str, a);
				}
			} catch (NumberFormatException e) {
				// ignore, just don't set value
			}
		}
	}
}
