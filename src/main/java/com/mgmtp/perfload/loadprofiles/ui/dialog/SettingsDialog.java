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
package com.mgmtp.perfload.loadprofiles.ui.dialog;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.max;
import static org.apache.commons.io.FilenameUtils.isExtension;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.mgmtp.perfload.loadprofiles.model.AbstractNamedObject;
import com.mgmtp.perfload.loadprofiles.model.Client;
import com.mgmtp.perfload.loadprofiles.model.Operation;
import com.mgmtp.perfload.loadprofiles.model.Target;
import com.mgmtp.perfload.loadprofiles.ui.component.DoubleCellEditor;
import com.mgmtp.perfload.loadprofiles.ui.component.DoubleCellRenderer;
import com.mgmtp.perfload.loadprofiles.ui.component.IntegerCellEditor;
import com.mgmtp.perfload.loadprofiles.ui.component.JButtonExt;
import com.mgmtp.perfload.loadprofiles.ui.component.JTableExt;
import com.mgmtp.perfload.loadprofiles.ui.component.StringCellEditor;
import com.mgmtp.perfload.loadprofiles.ui.ctrl.ConfigController;
import com.mgmtp.perfload.loadprofiles.ui.model.Settings;
import com.mgmtp.perfload.loadprofiles.ui.util.ModelUtils;

/**
 * @author rnaegele
 */
public class SettingsDialog extends JDialog implements ListEventListener<AbstractNamedObject<?>> {

	private static final String MSG_EDITING = "You are currently editing %s.\nPlease finish this first!";

	private final Logger log = LoggerFactory.getLogger(getClass());

	private JTableExt tblOperations;
	private JTableExt tblTargets;
	private JTableExt tblClients;

	private final EventList<Operation> operations;
	private final EventList<Target> targets;
	private final EventList<Client> clients;
	private final EventList<String> configFiles;

	private AbstractNamedObject<?> activeCoreDataObject;

	private JComboBox cboConfigurationFile;

	private final ConfigController controller;

	private ModalResult modalResult;

	private boolean dirty;

	/**
	 * Create the dialog.
	 */
	public SettingsDialog(final JFrame parent, final ConfigController controller) {
		super(parent, "Settings", true);
		this.controller = controller;

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(600, 600);
		setResizable(false);

		Settings settings = controller.getActiveSettings().deepClone();
		operations = GlazedLists.<Operation>eventList(settings.getOperations());
		operations.addListEventListener(this);
		targets = GlazedLists.<Target>eventList(settings.getTargets());
		targets.addListEventListener(this);
		clients = GlazedLists.<Client>eventList(settings.getClients());
		clients.addListEventListener(this);
		configFiles = SortedList.create(GlazedLists.<String>eventList(newArrayList(controller.getAvailableSettingsFiles())));

		initComponents();
		initModels();
	}

	private void initComponents() {
		getContentPane().setLayout(new MigLayout("", "[grow]", "[][grow][grow][grow][]"));
		{
			JPanel pnlConfigurations = new JPanel();
			pnlConfigurations.setBorder(new TitledBorder(null, "Saved Configurations", TitledBorder.LEADING, TitledBorder.TOP,
					null, null));
			pnlConfigurations.setName("pnlConfigurations");
			getContentPane().add(pnlConfigurations, "cell 0 0, growx");
			pnlConfigurations.setLayout(new MigLayout("", "[grow]", "[]"));
			{
				JLabel lblFileName = new JLabel("File Name");
				lblFileName.setName("lblFileName");
				pnlConfigurations.add(lblFileName, "flowx,cell 0 0");
			}
			{
				cboConfigurationFile = new JComboBox() {
					@Override
					public void setSelectedItem(final Object anObject) {
						if (checkDirty()) {
							super.setSelectedItem(anObject);
						}
					}
				};
				cboConfigurationFile.addItemListener(new CboConfigurationFileItemListener());
				pnlConfigurations.add(cboConfigurationFile, "cell 0 0,growx");
				cboConfigurationFile.setName("cboConfigurationFile");
			}
			{
				JButton btnNewConfigurationFile = new JButtonExt("New...");
				btnNewConfigurationFile.addActionListener(new BtnNewConfigurationFileActionListener());
				pnlConfigurations.add(btnNewConfigurationFile, "cell 0 0, sg btns");
				btnNewConfigurationFile.setName("btnNewButton");
			}
			{
				JButton btnCopy = new JButtonExt("Copy...");
				btnCopy.addActionListener(new BtnCopyActionListener());
				btnCopy.setName("btnCopy");
				pnlConfigurations.add(btnCopy, "cell 0 0, sg btns");
			}
			{
				JButton btnDelete = new JButtonExt("Delete...");
				btnDelete.addActionListener(new BtnDeleteActionListener());
				btnDelete.setName("btnDelete");
				pnlConfigurations.add(btnDelete, "cell 0 0, sg btns");
			}
		}
		{
			JPanel pnlOperations = new JPanel();
			getContentPane().add(pnlOperations, "cell 0 1, grow");
			pnlOperations.setBorder(new TitledBorder(null, "Operations", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			pnlOperations.setName("pnlOperations");
			pnlOperations.setLayout(new MigLayout("insets 4", "[grow][110!]", "[][]"));
			{
				JScrollPane spOperations = new JScrollPane();
				spOperations.setName("spOperations");
				pnlOperations.add(spOperations, "cell 0 0 1 2, height 180::, grow");
				{
					tblOperations = new JTableExt();
					tblOperations.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					tblOperations.setFillsViewportHeight(true);
					tblOperations.setName("tblOperations");
					spOperations.setViewportView(tblOperations);
				}
			}
			{
				JButton btnAddOperation = new JButtonExt("Add Operation");
				btnAddOperation.addActionListener(new BtnAddOperationActionListener());
				btnAddOperation.setMargin(new Insets(2, 2, 2, 2));
				btnAddOperation.setName("btnAddOperation");
				pnlOperations.add(btnAddOperation, "cell 1 0, grow");
			}
			{
				JButton btnRemoveOperation = new JButtonExt("Delete Operation");
				btnRemoveOperation.addActionListener(new BtnRemoveOperationActionListener());
				btnRemoveOperation.setMargin(new Insets(2, 2, 2, 2));
				btnRemoveOperation.setName("btnRemoveOperation");
				pnlOperations.add(btnRemoveOperation, "cell 1 1, growx, top");
			}
		}

		{
			JPanel pnlTargets = new JPanel();
			getContentPane().add(pnlTargets, "cell 0 2, grow");
			pnlTargets.setBorder(new TitledBorder(null, "Targets", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			pnlTargets.setName("pnlTargets");
			pnlTargets.setLayout(new MigLayout("insets 4", "[grow][110!]", "[][]"));
			{
				JScrollPane spTargets = new JScrollPane();
				spTargets.setName("spTargets");
				pnlTargets.add(spTargets, "cell 0 0 1 2, height 50::, grow");
				{
					tblTargets = new JTableExt();
					tblTargets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					tblTargets.setFillsViewportHeight(true);
					tblTargets.setName("tblTargets");
					spTargets.setViewportView(tblTargets);
				}
			}
			{
				JButton btnAddTarget = new JButtonExt("Add Target");
				btnAddTarget.addActionListener(new BtnAddTargetActionListener());
				btnAddTarget.setMargin(new Insets(2, 2, 2, 2));
				btnAddTarget.setName("btnAddClientConfig");
				pnlTargets.add(btnAddTarget, "cell 1 0, growx");
			}
			{
				JButton btnRemoveTarget = new JButtonExt("Delete Target");
				btnRemoveTarget.addActionListener(new BtnRemoveTargetActionListener());
				btnRemoveTarget.setMargin(new Insets(2, 2, 2, 2));
				btnRemoveTarget.setName("btnRemoveClientConfig");
				pnlTargets.add(btnRemoveTarget, "cell 1 1, growx, top");
			}
		}

		{
			JPanel pnlClients = new JPanel();
			getContentPane().add(pnlClients, "cell 0 3, grow");
			pnlClients.setBorder(new TitledBorder(null, "Clients", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			pnlClients.setName("pnlClientConfigs");
			pnlClients.setLayout(new MigLayout("insets 4", "[grow][110!]", "[][]"));
			{
				JScrollPane spClients = new JScrollPane();
				spClients.setName("spClients");
				pnlClients.add(spClients, "cell 0 0 1 2, height 50::, grow");
				{
					tblClients = new JTableExt();
					tblClients.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					tblClients.setFillsViewportHeight(true);
					tblClients.setName("tblClients");
					spClients.setViewportView(tblClients);
				}
			}
			{
				JButton btnAddClient = new JButtonExt("Add Client");
				btnAddClient.addActionListener(new BtnAddClientActionListener());
				btnAddClient.setMargin(new Insets(2, 2, 2, 2));
				btnAddClient.setName("btnAddClient");
				pnlClients.add(btnAddClient, "cell 1 0, growx");
			}
			{
				JButton btnRemoveClient = new JButtonExt("Delete Client");
				btnRemoveClient.addActionListener(new BtnRemoveClientActionListener());
				btnRemoveClient.setMargin(new Insets(2, 2, 2, 2));
				btnRemoveClient.setName("btnRemoveClient");
				pnlClients.add(btnRemoveClient, "cell 1 1, growx, top");
			}
		}

		{
			JButton btnOk = new JButtonExt("OK");
			getRootPane().setDefaultButton(btnOk);
			btnOk.addActionListener(new BtnOkActionListener());
			btnOk.setName("btnOk");
			getContentPane().add(btnOk, "cell 0 4,alignx right");
		}
		{
			JButton btnCancel = new JButtonExt("Cancel");
			btnCancel.addActionListener(new BtnCancelActionListener());
			btnCancel.setName("btnCancel");
			getContentPane().add(btnCancel, "cell 0 4,alignx right");
		}
	}

	private void initModels() {
		TableFormat<Operation> operationsTableFormat = GlazedLists.tableFormat(Operation.class, new String[] { "name",
				"relativeClientLoad" },
				new String[] { "Name", "Rel. Client Load" }, new boolean[] { true, true });
		EventTableModel<Operation> operationsModel = new EventTableModel<Operation>(operations, operationsTableFormat);
		tblOperations.setModel(operationsModel);
		tblOperations.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		tblOperations.setColumnWidths(300, 100);
		tblOperations.getColumnModel().getColumn(0).setCellEditor(new StringCellEditor(tblOperations, operations));
		TableColumn column = tblOperations.getColumnModel().getColumn(1);
		column.setCellEditor(new DoubleCellEditor());
		column.setCellRenderer(new DoubleCellRenderer());

		TableFormat<Target> targetsTableFormat = GlazedLists.tableFormat(Target.class, new String[] { "name", "loadPart" },
				new String[] { "Name", "Load Part" }, new boolean[] { true, true });
		EventTableModel<Target> targetsModel = new EventTableModel<Target>(targets, targetsTableFormat);
		tblTargets.setModel(targetsModel);
		tblTargets.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		tblTargets.setColumnWidths(300, 100);
		tblTargets.getColumnModel().getColumn(0).setCellEditor(new StringCellEditor(tblTargets, targets));
		column = tblTargets.getColumnModel().getColumn(1);
		column.setCellEditor(new DoubleCellEditor());
		column.setCellRenderer(new DoubleCellRenderer());

		TableFormat<Client> clientsTableFormat = GlazedLists
				.tableFormat(Client.class,
						new String[] { "daemonId", "name", "numProcesses", "relativePower" },
						new String[] { "Daemon ID", "Name", "# of Processes", "Rel. Power" }, new boolean[] { false, true,
								true, true });
		EventTableModel<Client> clientsModel = new EventTableModel<Client>(clients, clientsTableFormat);
		tblClients.setModel(clientsModel);
		tblClients.setColumnWidths(80, 200, 80, 60);
		tblClients.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		//		tblClients.getColumnModel().getColumn(0).setCellEditor(new IntegerCellEditor());
		tblClients.getColumnModel().getColumn(1).setCellEditor(new StringCellEditor(tblClients, clients));
		tblClients.getColumnModel().getColumn(2).setCellEditor(new IntegerCellEditor());
		column = tblClients.getColumnModel().getColumn(3);
		column.setCellEditor(new DoubleCellEditor());
		column.setCellRenderer(new DoubleCellRenderer());

		cboConfigurationFile.setModel(new EventComboBoxModel<String>(configFiles));
		cboConfigurationFile.setSelectedItem(controller.getActiveSettingsFile());
	}

	@Override
	protected JRootPane createRootPane() {
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		JRootPane rp = super.createRootPane();
		rp.registerKeyboardAction(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				setVisible(false);
			}
		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		return rp;
	}

	private void reloadSettings() {
		controller.loadActiveSettings();
		Settings settings = controller.getActiveSettings().deepClone();

		ModelUtils.updateOperations(settings.getOperations(), operations);
		ModelUtils.updateTargets(settings.getTargets(), targets);
		ModelUtils.updateClients(settings.getClients(), clients);

		dirty = false;

		tblOperations.repaint();
		tblTargets.repaint();
		tblClients.repaint();
	}

	/**
	 * @return the modalResult
	 */
	public ModalResult getModalResult() {
		return modalResult;
	}

	private <T extends AbstractNamedObject<T>> void addCoreDataObject(final EventList<T> objectsList, final T object,
			final JTable table) {
		checkAndResetActiveCoreDataObject();

		log.debug("Adding a new item...");
		objectsList.add(object);
		activeCoreDataObject = object;
		int index = objectsList.size() - 1;

		if (index == -1) {
			index = table.getSelectedRow();
		}
		table.changeSelection(index, 0, false, false);
	}

	private void deleteCoreDataObject(final EventList<?> objectsList, final JTable table) {
		if (!objectsList.isEmpty()) {
			int selectedClientIndex = table.getSelectedRow();
			checkState(selectedClientIndex >= 0, "Cannot delete item. No row selected in table.");

			log.debug("Deleting item: {}", objectsList.get(selectedClientIndex));
			objectsList.remove(selectedClientIndex);
			activeCoreDataObject = null;

			if (selectedClientIndex >= 0) {
				int newIndex = max(0, selectedClientIndex - 1);
				table.getSelectionModel().setSelectionInterval(newIndex, newIndex);
			}
		}
	}

	private void checkAndResetActiveCoreDataObject() {
		log.debug("Check if already editing...");

		if (activeCoreDataObject != null) {
			String name = activeCoreDataObject.getName();

			if (activeCoreDataObject instanceof Operation) {
				if (isBlank(name)) {
					throw new IllegalStateException(String.format(MSG_EDITING, "an operation"));
				}
			}
			if (activeCoreDataObject instanceof Target) {
				if (isBlank(name)) {
					throw new IllegalStateException(String.format(MSG_EDITING, "a target"));
				}
			}
			if (activeCoreDataObject instanceof Client) {
				if (isBlank(name)) {
					throw new IllegalStateException(String.format(MSG_EDITING, "a client"));
				}
			}
		}
		activeCoreDataObject = null;
	}

	@Override
	public void listChanged(final ListEvent<AbstractNamedObject<?>> listChanges) {
		dirty = true;
	}

	private boolean checkDirty() {
		if (dirty) {
			switch (JOptionPane.showConfirmDialog(null, "Saves changes?")) {
				case JOptionPane.YES_OPTION:
					controller.setActiveSettings(Settings.of(operations, targets, clients));
					controller.saveActiveSettings();
					dirty = false;
					return true;
				case JOptionPane.NO_OPTION:
					dirty = false;
					return true;
				default:
					return false;
			}
		}
		return true;
	}

	private void newConfigFile() {
		String fileName = JOptionPane.showInputDialog(null, "Please enter a name for the new configuration:",
				"Configuration Name",
				JOptionPane.QUESTION_MESSAGE);
		if (fileName == null) {
			return;
		}

		if (!isExtension(fileName, "xml")) {
			fileName += ".xml";
		}
		checkState(isNotBlank(fileName), "File name must not be empty.");
		checkState(!configFiles.contains(fileName), "The file '" + fileName + "' already exists.");

		configFiles.add(fileName);

		controller.setActiveSettingsFile(fileName);
		cboConfigurationFile.setSelectedItem(fileName);
	}

	private void deleteConfigFile() {
		if (JOptionPane.showConfirmDialog(null, "Delete settings?", "Confirmation", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			int index = cboConfigurationFile.getSelectedIndex();
			String fileName = (String) cboConfigurationFile.getSelectedItem();
			configFiles.remove(fileName);

			index--;
			if (index < 0 && !configFiles.isEmpty()) {
				index = 0;
			}
			cboConfigurationFile.setSelectedIndex(index);

			if (configFiles.isEmpty()) {
				operations.clear();
				targets.clear();
				clients.clear();
				dirty = false;
			}
			controller.deleteSettingsFile(fileName);
		}
	}

	private void copyConfigFile() {
		if (checkDirty()) {
			String fileName = JOptionPane.showInputDialog(null, "Please enter a name for the copied configuration:",
					"Configuration Name",
					JOptionPane.QUESTION_MESSAGE);
			if (fileName == null) {
				return;
			}

			if (!isExtension(fileName, "xml")) {
				fileName += ".xml";
			}
			checkState(isNotBlank(fileName), "File name must not be empty.");
			checkState(!configFiles.contains(fileName), "The file '" + fileName + "' already exists.");

			configFiles.add(fileName);
			controller.copyActiveSettingsFile(fileName);
			cboConfigurationFile.setSelectedItem(fileName);
		}
	}

	private class BtnOkActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			checkAndResetActiveCoreDataObject();
			controller.setActiveSettings(Settings.of(operations, targets, clients));
			controller.saveActiveSettings();
			modalResult = ModalResult.OK;
			setVisible(false);
		}
	}

	private class BtnCancelActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			modalResult = ModalResult.CANCEL;
			setVisible(false);
		}
	}

	private class BtnAddOperationActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			addCoreDataObject(operations, new Operation(), tblOperations);
		}
	}

	private class BtnRemoveOperationActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			deleteCoreDataObject(operations, tblOperations);
		}
	}

	private class BtnAddTargetActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			addCoreDataObject(targets, new Target(), tblTargets);
		}
	}

	private class BtnRemoveTargetActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			deleteCoreDataObject(targets, tblTargets);
		}
	}

	private class BtnAddClientActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			Client client = new Client();
			client.setDaemonId(clients.size() + 1);
			addCoreDataObject(clients, client, tblClients);
		}
	}

	private class BtnRemoveClientActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			deleteCoreDataObject(clients, tblClients);
			for (int i = 0, len = clients.size(); i < len; ++i) {
				clients.get(i).setDaemonId(i + 1);
			}
		}
	}

	private class BtnNewConfigurationFileActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			newConfigFile();
		}
	}

	private class BtnCopyActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			copyConfigFile();
		}
	}

	private class BtnDeleteActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			deleteConfigFile();
		}
	}

	private class CboConfigurationFileItemListener implements ItemListener {
		@Override
		public void itemStateChanged(final ItemEvent e) {
			switch (e.getStateChange()) {
				case ItemEvent.SELECTED:
					controller.setActiveSettingsFile((String) e.getItem());
					reloadSettings();
					break;
				case ItemEvent.DESELECTED:
				default:
					//
			}
		}
	}
}
