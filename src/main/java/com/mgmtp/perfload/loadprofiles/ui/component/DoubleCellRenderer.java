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

import java.text.DecimalFormat;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author rnaegele
 */
public class DoubleCellRenderer extends DefaultTableCellRenderer {

	private static final DecimalFormat FORMAT = new DecimalFormat("0.00");

	public DoubleCellRenderer() {
		setHorizontalAlignment(SwingConstants.RIGHT);
	}

	@Override
	public void setValue(final Object value) {
		setText(FORMAT.format(value));
	}
}
