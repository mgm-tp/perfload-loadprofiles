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

import static com.google.common.collect.Lists.newArrayListWithExpectedSize;

import java.awt.BorderLayout;
import java.beans.PropertyVetoException;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.TableModel;

import net.miginfocom.swing.MigLayout;
import ca.odell.glazedlists.swing.EventTableModel;

import com.mgmtp.perfload.loadprofiles.model.Operation;
import com.mgmtp.perfload.loadprofiles.model.Target;
import com.mgmtp.perfload.loadprofiles.ui.model.OneTime;
import com.mgmtp.perfload.loadprofiles.ui.model.SelectionDecorator;

/**
 * @author rnaegele
 */
public class OneTimePanel extends LoadProfileEntityPanel<OneTime> {

	private ComboBoxModel cboOperationModel;
	private final JComboBox cboOperation;
	private EventTableModel<SelectionDecorator> tblTargetModel;
	private final JTableExt tblTarget;
	private final JIntegerTextField txtT0;

	/**
	 * Create the panel.
	 */
	public OneTimePanel() {
		setLayout(new MigLayout("insets 0", "[120!,left][200:200:]16[:300:]", "[][75px][][]"));

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
				.getResource("com/mgmtp/perfload/loadprofiles/ui/onetime.png")));
		add(lblImage, "cell 2 0 1 4, alignx right, grow");

		JLabel lblTarget = new JLabel("Targets");
		lblTarget.setName("lblTarget");
		add(lblTarget, "cell 0 1");

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setName("scrollPane");
		add(scrollPane, "cell 1 1,growx");

		{
			tblTarget = new JCheckListTable();
			scrollPane.setViewportView(tblTarget);
		}

		lblTarget.setLabelFor(tblTarget);

		JLabel lblT0 = new JLabel("<html>T<sub>0</sub> (min)");
		add(lblT0, "cell 0 2");

		txtT0 = createTextField();
		lblT0.setLabelFor(txtT0);
		add(txtT0, "cell 1 2, width 50!");
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

	/**
	 * @return the tblTargetModel
	 */
	public TableModel getTblTargetModel() {
		return tblTargetModel;
	}

	/**
	 * @param tblTargetModel
	 *            the lstTargetModel to set
	 */
	public void setTblTargetModel(final EventTableModel<SelectionDecorator> tblTargetModel) {
		this.tblTargetModel = tblTargetModel;
		tblTarget.setModel(tblTargetModel);
		tblTarget.getColumnModel().getColumn(0).setMaxWidth(24);
		addChangeListener(tblTargetModel);
	}

	@Override
	public OneTime getLoadProfileEntity() {
		loadProfileEntity.operation = (Operation) cboOperationModel.getSelectedItem();
		List<Target> targets = newArrayListWithExpectedSize(2);
		int size = tblTarget.getRowCount();
		for (int i = 0; i < size; ++i) {
			SelectionDecorator std = tblTargetModel.getElementAt(i);
			if (std.isSelected()) {
				targets.add((Target) std.getBaseObject());
			}
		}
		loadProfileEntity.targets = targets;
		loadProfileEntity.t0 = txtT0.getValue();
		return loadProfileEntity;
	}

	@Override
	public void setLoadProfileEntity(final OneTime oneTime) {
		if (oneTime != this.loadProfileEntity) {
			try {
				disableListeners();
				dirty = false;
				this.loadProfileEntity = oneTime;
				cboOperationModel.setSelectedItem(oneTime.operation);

				int size = tblTargetModel.getRowCount();
				for (int i = 0; i < size; ++i) {
					SelectionDecorator std = tblTargetModel.getElementAt(i);
					std.setSelected(false); // always reset first
					for (Target target : oneTime.targets) {
						if (std.getBaseObject().equals(target)) {
							std.setSelected(true);
							break;
						}
					}
				}
				txtT0.setValue(oneTime.t0);
				//			lstTargetModel.clearSelection();
			} catch (PropertyVetoException ex) {
				// Can't really happen here.
				throw new IllegalStateException(ex);
			} finally {
				enableListeners();
			}
		}
	}
}
