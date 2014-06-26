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

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import com.mgmtp.perfload.loadprofiles.model.Operation;

/**
 * Renderer delegate for setting the operation's name.
 * 
 * @author rnaegele
 */
public class OperationComboRendererDecorator extends BasicComboBoxRenderer {

	private final BasicComboBoxRenderer delegate;

	public OperationComboRendererDecorator(final BasicComboBoxRenderer delegate) {
		this.delegate = delegate;
	}

	@Override
	public Dimension getPreferredSize() {
		return delegate.getPreferredSize();
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
			final boolean isSelected, final boolean cellHasFocus) {
		Component comp = delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value != null) {
			String name = ((Operation) value).getName();
			((JLabel) comp).setText(name);
		}
		return comp;
	}
}
