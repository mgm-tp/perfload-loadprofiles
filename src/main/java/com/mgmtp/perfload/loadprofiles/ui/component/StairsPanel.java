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

import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import net.miginfocom.swing.MigLayout;

import com.mgmtp.perfload.loadprofiles.model.Operation;
import com.mgmtp.perfload.loadprofiles.ui.model.Stairs;

/**
 * @author rnaegele
 */
public class StairsPanel extends LoadProfileEntityPanel<Stairs> {

	private final JIntegerTextField txtT0;
	private final JIntegerTextField txtA;
	private final JIntegerTextField txtB;
	private final JIntegerTextField txtC;
	private final JIntegerTextField txtH;
	private final JIntegerTextField txtNumSteps;
	private ComboBoxModel cboOperationModel;
	private final JComboBox cboOperation;

	/**
	 * Create the panel.
	 */
	public StairsPanel() {
		setLayout(new MigLayout("insets 0", "[120!,left][200:200:]16[:300:]", "[][][][][][][][]"));

		JLabel lblOperation = new JLabel("Operation");
		lblOperation.setName("lblOperation");
		add(lblOperation, "cell 0 0");

		cboOperation = new JComboBox();
		cboOperation.setRenderer(new OperationComboRendererDecorator((BasicComboBoxRenderer) cboOperation.getRenderer()));

		lblOperation.setLabelFor(cboOperation);
		add(cboOperation, "cell 1 0,growx");

		JLabel lblImage = new JLabel();
		lblImage.setLayout(new BorderLayout(0, 0));
		lblImage.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		lblImage.setIcon(new ImageIcon(Thread.currentThread().getContextClassLoader()
				.getResource("com/mgmtp/perfload/loadprofiles/ui/stairs.png")));
		add(lblImage, "cell 2 0 1 8, alignx right, grow");

		JLabel lblT0 = new JLabel("<html>T<sub>0</sub> (min)");
		add(lblT0, "cell 0 1");

		txtT0 = createTextField();
		lblT0.setLabelFor(txtT0);
		add(txtT0, "cell 1 1, width 50!");

		JLabel lblA = new JLabel("a (min)");
		add(lblA, "cell 0 2");

		txtA = createTextField();
		lblA.setLabelFor(txtA);
		add(txtA, "cell 1 2, width 50!");

		JLabel lblB = new JLabel("b (min)");
		add(lblB, "cell 0 3");
		txtB = createTextField();
		lblB.setLabelFor(txtB);
		add(txtB, "cell 1 3, width 50!");

		JLabel lblC = new JLabel("c (min)");
		add(lblC, "cell 0 4");

		txtC = createTextField();
		lblC.setLabelFor(txtC);
		add(txtC, "cell 1 4, width 50!");

		JLabel lblH = new JLabel("h (executions/hour)");
		add(lblH, "cell 0 5");

		txtH = createTextField();
		lblH.setLabelFor(txtH);
		add(txtH, "cell 1 5, width 50!");

		JLabel lblSteps = new JLabel("# of steps");
		add(lblSteps, "cell 0 6");

		txtNumSteps = createTextField();
		lblSteps.setLabelFor(txtNumSteps);
		add(txtNumSteps, "cell 1 6, width 50!");
	}

	/**
	 * @return the cboOperationModel
	 */
	public ComboBoxModel getCboOperationModel() {
		return cboOperationModel;
	}

	/**
	 * @param cboOperationModel
	 *            the cboOperationModel to set
	 */
	public void setCboOperationModel(final ComboBoxModel cboOperationModel) {
		this.cboOperationModel = cboOperationModel;
		cboOperation.setModel(cboOperationModel);
		addChangeListener(cboOperationModel);
	}

	@Override
	public Stairs getLoadProfileEntity() {
		loadProfileEntity.operation = (Operation) cboOperationModel.getSelectedItem();
		loadProfileEntity.t0 = txtT0.getValue();
		loadProfileEntity.a = txtA.getValue();
		loadProfileEntity.b = txtB.getValue();
		loadProfileEntity.c = txtC.getValue();
		loadProfileEntity.h = txtH.getValue();
		loadProfileEntity.numSteps = txtNumSteps.getValue();
		return loadProfileEntity;
	}

	@Override
	public void setLoadProfileEntity(final Stairs stairs) {
		if (stairs != this.loadProfileEntity) {
			try {
				disableListeners();
				dirty = false;
				txtNumSteps.setValue(stairs.numSteps);
				this.loadProfileEntity = stairs;
				cboOperationModel.setSelectedItem(stairs.getOperation());
				txtT0.setValue(stairs.t0);
				txtA.setValue(stairs.a);
				txtB.setValue(stairs.b);
				txtC.setValue(stairs.c);
				txtH.setValue(stairs.h);
			} catch (PropertyVetoException ex) {
				// Can't really happen here.
				throw new IllegalStateException(ex);
			} finally {
				enableListeners();
			}
		}
	}
}
