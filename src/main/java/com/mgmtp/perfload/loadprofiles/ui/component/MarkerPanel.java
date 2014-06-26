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

import java.awt.BorderLayout;
import java.beans.PropertyVetoException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;

import com.mgmtp.perfload.loadprofiles.ui.model.Marker;

/**
 * @author rnaegele
 */
public class MarkerPanel extends LoadProfileEntityPanel<Marker> {

	private final JTextField txtName;
	private final JIntegerTextField txtLeft;
	private final JIntegerTextField txtRight;

	/**
	 * Create the panel.
	 */
	public MarkerPanel() {
		setLayout(new MigLayout("insets 0", "[120!,left][200:200:]16[:300:]", "[][][][]"));

		JLabel lblName = new JLabel("Name");
		add(lblName, "cell 0 0");

		txtName = new JTextField();
		txtName.getDocument().addDocumentListener(this);
		lblName.setLabelFor(txtName);
		add(txtName, "cell 1 0, growx");

		JLabel lblLeft = new JLabel("<html>left");
		add(lblLeft, "cell 0 1");

		txtLeft = createTextField();
		lblLeft.setLabelFor(txtLeft);
		add(txtLeft, "cell 1 1, width 50!");

		JLabel lblRight = new JLabel("right");
		add(lblRight, "cell 0 2");

		txtRight = createTextField();
		lblRight.setLabelFor(txtRight);
		add(txtRight, "cell 1 2, width 50!");

		JLabel lblImage = new JLabel();
		lblImage.setLayout(new BorderLayout(0, 0));
		lblImage.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		lblImage.setIcon(new ImageIcon(Thread.currentThread().getContextClassLoader()
				.getResource("com/mgmtp/perfload/loadprofiles/ui/marker.png")));
		add(lblImage, "cell 2 0 1 4, alignx right, grow");
	}

	@Override
	public Marker getLoadProfileEntity() {
		loadProfileEntity.name = txtName.getText();
		loadProfileEntity.left = txtLeft.getValue();
		loadProfileEntity.right = txtRight.getValue();
		return loadProfileEntity;
	}

	@Override
	public void setLoadProfileEntity(final Marker marker) {
		if (marker != this.loadProfileEntity) {
			try {
				disableListeners();
				dirty = false;
				this.loadProfileEntity = marker;
				txtName.setText(marker.name);
				txtLeft.setValue(marker.left);
				txtRight.setValue(marker.right);
			} catch (PropertyVetoException ex) {
				// Can't really happen here.
				throw new IllegalStateException(ex);
			} finally {
				enableListeners();
			}
		}
	}
}
