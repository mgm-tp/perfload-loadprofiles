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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;

/**
 * @author rnaegele
 */
public class AboutDialog extends JDialog {

	/**
	 * Create the dialog.
	 */
	public AboutDialog(final JFrame parent, final String appVersion) {
		super(parent, "About...", true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);
		getContentPane().setLayout(new MigLayout("", "", "[][]"));
		JPanel contentPanel = new JPanel();
		contentPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		getContentPane().add(contentPanel, "cell 0 0,alignx left,aligny top");
		contentPanel.setLayout(new MigLayout("", "[grow]", "[]10px[][]20px[]10px"));
		{
			JPanel panel = new JPanel();
			//			panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panel.setName("panel");
			contentPanel.add(panel, "cell 0 0,grow");
			panel.setLayout(new BorderLayout(0, 0));
			{
				JLabel lblLogo = new JLabel("");
				panel.add(lblLogo);
				lblLogo.setName("lblLogo");
				lblLogo.setIcon(new ImageIcon(Thread.currentThread().getContextClassLoader()
						.getResource("com/mgmtp/perfload/loadprofiles/ui/perfLoad_Logo_wort.png")));
			}
		}
		{
			JLabel lblPerfloadLoadProfiles = new JLabel("perfLoad - Load Profile Editor");
			lblPerfloadLoadProfiles.setFont(new Font(getFont().getName(), Font.BOLD, 18));
			lblPerfloadLoadProfiles.setName("lblPerfloadLoadProfiles");
			contentPanel.add(lblPerfloadLoadProfiles, "cell 0 1,alignx center");
		}
		{
			JLabel lblVersion = new JLabel("Version " + appVersion);
			lblVersion.setName("lblVersion");
			lblVersion.setFont(new Font(getFont().getName(), Font.PLAIN, 14));
			contentPanel.add(lblVersion, "cell 0 2,alignx center");
		}
		{
			JLabel lblCopyright = new JLabel("© 2013 by mgm technology partners GmbH. All rights reserved.");
			lblCopyright.setName("lblCopyright");
			contentPanel.add(lblCopyright, "cell 0 3,alignx center");
		}
		{
			JButton btnClose = new JButton("Close");
			btnClose.addActionListener(new BtnCloseActionListener());
			btnClose.setName("btnClose");
			btnClose.setActionCommand("Close");
			getRootPane().setDefaultButton(btnClose);
			getContentPane().add(btnClose, "cell 0 1,alignx center");
		}
		pack();
		setLocationRelativeTo(parent);
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

	private class BtnCloseActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			setVisible(false);
		}
	}
}
