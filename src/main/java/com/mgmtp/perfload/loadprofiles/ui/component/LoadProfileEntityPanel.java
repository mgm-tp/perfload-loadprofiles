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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.loadprofiles.ui.model.LoadProfileEntity;

/**
 * @author rnaegele
 */
public abstract class LoadProfileEntityPanel<T extends LoadProfileEntity> extends JPanel implements PropertyChangeListener,
		ListDataListener, TableModelListener, DocumentListener {

	private static final long serialVersionUID = 1L;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private boolean listenersEnabled;
	protected boolean dirty;
	protected T loadProfileEntity;

	protected void addChangeListener(final ComboBoxModel cboModel) {
		cboModel.addListDataListener(this);
	}

	protected void addChangeListener(final TableModel tblModel) {
		tblModel.addTableModelListener(this);
	}

	@Override
	public void tableChanged(final TableModelEvent e) {
		if (listenersEnabled && isEnabled()) {
			setDirty(true);
		}
	}

	@Override
	public void contentsChanged(final ListDataEvent e) {
		if (listenersEnabled && isEnabled()) {
			setDirty(true);
		}
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if (listenersEnabled && isEnabled()) {
			setDirty(true);
		}
	}

	@Override
	public void changedUpdate(final DocumentEvent e) {
		if (listenersEnabled && isEnabled()) {
			setDirty(true);
		}
	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		log.debug("insertUpdate(): {}", listenersEnabled && isEnabled());
		if (listenersEnabled && isEnabled()) {
			setDirty(true);
		}
	}

	@Override
	public void removeUpdate(final DocumentEvent e) {
		log.debug("removeUpdate(): {}", listenersEnabled && isEnabled());
		if (listenersEnabled && isEnabled()) {
			setDirty(true);
		}
	}

	@Override
	public void intervalAdded(final ListDataEvent e) {
		// no-op
	}

	@Override
	public void intervalRemoved(final ListDataEvent e) {
		// no-op
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		setEnabledRecursive(enabled, this);
	}

	private void setEnabledRecursive(final boolean enabled, final JComponent comp) {
		for (Component c : comp.getComponents()) {
			if (c instanceof JComponent) {
				JComponent jComp = (JComponent) c;
				jComp.setEnabled(enabled);
				setEnabledRecursive(enabled, jComp);
			}
		}
	}

	public boolean isDirty() {
		log.debug("isDirty(): {}", dirty);
		return dirty;
	}

	public void setDirty(final boolean dirty) {
		boolean oldDirty = this.dirty;
		log.debug("setDirty({}, {})", dirty, oldDirty);
		if (dirty != oldDirty) {
			log.debug("setDirty2({}, {})", dirty, oldDirty);
			this.dirty = dirty;
			if (listenersEnabled) {
				firePropertyChange("dirty", oldDirty, dirty);
			}
		}
	}

	public abstract T getLoadProfileEntity();

	public abstract void setLoadProfileEntity(T curveAssignment);

	protected JIntegerTextField createTextField() {
		JIntegerTextField txt = new JIntegerTextField();
		txt.getDocument().addDocumentListener(this);
		return txt;
	}

	public void enableListeners() {
		log.debug("enableListeners()");
		listenersEnabled = true;
	}

	public void disableListeners() {
		log.debug("disableListeners()");
		listenersEnabled = false;
	}
}
