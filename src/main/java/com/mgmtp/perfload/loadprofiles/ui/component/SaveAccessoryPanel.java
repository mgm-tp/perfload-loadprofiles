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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;

/**
 * Accessory panel for the save dialog. Allows selection of additional files to be created.
 * 
 * @author rnaegele
 */
public class SaveAccessoryPanel extends Box {

	private static final long serialVersionUID = 1L;

	private final JCheckBox chkEventDistri = new JCheckBox("Create Client BaseLoadProfileEvent Distribution File");
	private final JCheckBox chkOperationHistogram = new JCheckBox("Create Operation Histogram");
	private final JCheckBox chkClientLoadHistrogram = new JCheckBox("Create Client Load Histogram");

	public SaveAccessoryPanel() {
		super(BoxLayout.Y_AXIS);
		add(chkEventDistri);
		add(chkOperationHistogram);
		add(chkClientLoadHistrogram);
		add(Box.createVerticalGlue());
	}

	public boolean isEventDistriChecked() {
		return chkEventDistri.isSelected();
	}

	public boolean isOperationHistogramChecked() {
		return chkOperationHistogram.isSelected();
	}

	public boolean isClientLoadHistrogramChecked() {
		return chkClientLoadHistrogram.isSelected();
	}
}
