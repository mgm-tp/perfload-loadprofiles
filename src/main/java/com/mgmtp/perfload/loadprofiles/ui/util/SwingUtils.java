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
package com.mgmtp.perfload.loadprofiles.ui.util;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.UIDefaults.ProxyLazyValue;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;

import org.apache.commons.io.FilenameUtils;

/**
 * Swing utility methods.
 * 
 * @author rnaegele
 */
public class SwingUtils {

	/**
	 * Registers the enter key a {@link JButton}.
	 * 
	 * @param button
	 *            The button
	 */
	public static void enterPressesWhenFocused(final JButton button) {
		button.registerKeyboardAction(button.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false)),
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_FOCUSED);

		button.registerKeyboardAction(button.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true)),
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), JComponent.WHEN_FOCUSED);
	}

	/**
	 * Sets the default UI font style.
	 * 
	 * @param fontStyle
	 *            The font style
	 * @see Font See Font Javadoc for possible styles
	 */
	public static void setUIFontStyle(final int fontStyle) {
		for (Enumeration<?> en = UIManager.getDefaults().keys(); en.hasMoreElements();) {
			Object key = en.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource) {
				FontUIResource fontRes = (FontUIResource) value;
				UIManager.put(key, new ProxyLazyValue("javax.swing.plaf.FontUIResource", null, new Object[] { fontRes.getName(),
						fontStyle, fontRes.getSize() }));
			}
		}
	}

	public static JFileChooser createFileChooser(final File dir, final String description, final String extension) {
		JFileChooser fc = new JFileChooser(dir);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(false);
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return description;
			}

			@Override
			public boolean accept(final File f) {
				return f.isDirectory() || FilenameUtils.isExtension(f.getName(), extension);
			}
		});
		return fc;
	}
}
