/*
 * Copyright (c) 2013 mgm technology partners GmbH
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
package com.mgmtp.perfload.loadprofiles.ui.ctrl;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newTreeSet;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.awt.Frame;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.UIManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.PatternFilenameFilter;
import com.mgmtp.perfload.loadprofiles.ui.ConfigDir;
import com.mgmtp.perfload.loadprofiles.ui.SettingsDir;
import com.mgmtp.perfload.loadprofiles.ui.model.Settings;
import com.mgmtp.perfload.loadprofiles.ui.util.LoadProfileException;

/**
 * Manages properties and settings. State is kept internally.
 * 
 * @author rnaegele
 */
@Singleton
public class ConfigController {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private String activeSettingsFile;
	private Set<String> availableSettingsFiles;
	private Settings activeSettings;
	private Properties properties;

	private final File settingsDir;
	private final File configDir;

	@Inject
	public ConfigController(@SettingsDir final File settingsDir, @ConfigDir final File configDir) {
		this.settingsDir = settingsDir;
		this.configDir = configDir;
	}

	/**
	 * @return the activeSettingsFile
	 */
	public String getActiveSettingsFile() {
		if (activeSettingsFile == null) {
			activeSettingsFile = getAppProperties().getProperty("activeSettingsFile");
		}
		return activeSettingsFile;
	}

	/**
	 * @param activeSettingsFile
	 *            the activeSettingsFile to set
	 */
	public void setActiveSettingsFile(final String activeSettingsFile) {
		this.activeSettingsFile = activeSettingsFile;
		if (!getAvailableSettingsFiles().contains(activeSettingsFile)) {
			// not availabe yet, so we create an empty settings instance
			availableSettingsFiles.add(activeSettingsFile);
			activeSettings = new Settings();
			saveActiveSettings();
		} else {
			// set it to null so it will be lazily reloaded on demand
			activeSettings = null;
		}
	}

	/**
	 * @return the activeSettings
	 */
	public Settings getActiveSettings() {
		if (activeSettings == null) {
			Properties props = getAppProperties();
			String settingsFile = props.getProperty("app.settings");
			if (settingsFile == null) {
				activeSettings = new Settings();
			} else {
				setActiveSettingsFile(settingsFile);
				loadActiveSettings();
			}
		}
		return activeSettings;
	}

	/**
	 * @param activeSettings
	 *            the activeSettings to set
	 */
	public void setActiveSettings(final Settings activeSettings) {
		this.activeSettings = activeSettings;
	}

	/**
	 * @return the availableSettingsFiles
	 */
	public Set<String> getAvailableSettingsFiles() {
		if (availableSettingsFiles == null) {
			String[] list = settingsDir.list(new PatternFilenameFilter(".*\\.xml"));
			if (list == null) {
				availableSettingsFiles = newTreeSet();
			} else {
				availableSettingsFiles = newTreeSet(asList(list));
			}
		}
		return ImmutableSet.copyOf(availableSettingsFiles);
	}

	public void saveActiveSettings() {
		checkState(activeSettingsFile != null, "No active settings file set.");

		Writer wr = null;
		File file = new File(settingsDir, activeSettingsFile);
		file.getParentFile().mkdir();

		try {
			wr = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			JAXBContext context = JAXBContext.newInstance(Settings.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(getActiveSettings(), wr);
		} catch (JAXBException ex) {
			String msg = "Error marshalling contents to file: " + file;
			throw new LoadProfileException(msg, ex);
		} catch (IOException ex) {
			throw new LoadProfileException(ex.getMessage(), ex);
		} finally {
			Closeables.closeQuietly(wr);
		}
	}

	public void loadActiveSettings() {
		checkState(activeSettingsFile != null, "No active settings file set.");

		Reader r = null;
		File file = new File(settingsDir, activeSettingsFile);
		try {
			r = new InputStreamReader(new FileInputStream(file), "UTF-8");
			JAXBContext context = JAXBContext.newInstance(Settings.class);
			Unmarshaller m = context.createUnmarshaller();
			activeSettings = (Settings) m.unmarshal(r);
		} catch (JAXBException ex) {
			String msg = "Error unmarshalling contents from file: " + file;
			throw new LoadProfileException(msg, ex);
		} catch (IOException ex) {
			throw new LoadProfileException(ex.getMessage(), ex);
		} finally {
			Closeables.closeQuietly(r);
		}
	}

	public void copyActiveSettingsFile(final String fileName) {
		try {
			Files.copy(new File(settingsDir, activeSettingsFile), new File(settingsDir, fileName));
		} catch (IOException ex) {
			throw new LoadProfileException(ex.getMessage(), ex);
		}
	}

	public void deleteSettingsFile(final String fileName) {
		checkState(new File(settingsDir, fileName).delete(), "Could not delete settings file.");
		availableSettingsFiles.remove(fileName);
	}

	public Properties getAppProperties() {
		if (properties == null) {
			Reader input = null;
			try {
				properties = new Properties();
				File file = new File(configDir, "app.properties");
				if (file.exists()) {
					input = Files.newReaderSupplier(file, Charset.forName("UTF-8")).getInput();
					properties.load(input);
				}
			} catch (IOException ex) {
				throw new LoadProfileException(ex.getMessage(), ex);
			} finally {
				Closeables.closeQuietly(input);
			}
		}
		return properties;
	}

	public void saveAppProperties() {
		Writer output = null;
		try {
			File file = new File(configDir, "app.properties");
			file.getParentFile().mkdir();
			output = Files.newWriterSupplier(file, Charset.forName("UTF-8")).getOutput();
			properties.store(output, "Load Profile Configurator Properties");
		} catch (IOException ex) {
			throw new LoadProfileException(ex.getMessage(), ex);
		} finally {
			Closeables.closeQuietly(output);
		}
	}

	public Rectangle getFrameBounds() {
		try {
			int width = Integer.parseInt(getAppProperties().getProperty("frame.width"));
			int height = Integer.parseInt(getAppProperties().getProperty("frame.height"));
			int x = Integer.parseInt(getAppProperties().getProperty("frame.x"));
			int y = Integer.parseInt(getAppProperties().getProperty("frame.y"));
			return new Rectangle(x, y, width, height);
		} catch (NumberFormatException ex) {
			log.warn("Could not read frame bounds from properties. Using defaults.");
			return null;
		}
	}

	public int getFrameState() {
		try {
			return Integer.parseInt(getAppProperties().getProperty("frame.state"));
		} catch (NumberFormatException ex) {
			log.warn("Could not read frame state from properties. Using default.");
			return Frame.NORMAL;
		}
	}

	public String getLookAndFeelClassName() {
		String lnfClassName = getAppProperties().getProperty("lnf.className");
		if (isBlank(lnfClassName)) {
			lnfClassName = UIManager.getSystemLookAndFeelClassName();
			getAppProperties().setProperty("lnf.className", lnfClassName);
		}
		return lnfClassName;
	}
}
