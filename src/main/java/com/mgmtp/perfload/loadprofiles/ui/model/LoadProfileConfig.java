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
package com.mgmtp.perfload.loadprofiles.ui.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.mgmtp.perfload.loadprofiles.model.Client;
import com.mgmtp.perfload.loadprofiles.model.Target;
import com.mgmtp.perfload.loadprofiles.model.jaxb.ClientAdapter;
import com.mgmtp.perfload.loadprofiles.model.jaxb.TargetAdapter;

/**
 * @author rnaegele
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LoadProfileConfig {

	private String name;
	private String description;

	@XmlElementWrapper(name = "clients")
	@XmlElement(name = "client")
	@XmlJavaTypeAdapter(value = ClientAdapter.class, type = Client.class)
	private List<Client> clients = newArrayList();

	@XmlElementWrapper(name = "targets")
	@XmlElement(name = "target")
	@XmlJavaTypeAdapter(value = TargetAdapter.class, type = Target.class)
	private List<Target> targets = newArrayList();

	@XmlElementWrapper(name = "loadProfileEntities")
	@XmlElements({
			@XmlElement(name = "oneTime", type = OneTime.class),
			@XmlElement(name = "stairs", type = Stairs.class),
			@XmlElement(name = "markers", type = Marker.class)
	})
	private List<LoadProfileEntity> loadProfileEntities = newArrayList();

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * @return the clients
	 */
	public List<Client> getClients() {
		return clients;
	}

	/**
	 * @param clients
	 *            the clients to set
	 */
	public void setClients(final List<Client> clients) {
		this.clients = clients;
	}

	/**
	 * @return the targets
	 */
	public List<Target> getTargets() {
		return targets;
	}

	/**
	 * @param targets
	 *            the targets to set
	 */
	public void setTargets(final List<Target> targets) {
		this.targets = targets;
	}

	/**
	 * @return the loadProfileEntities
	 */
	public List<LoadProfileEntity> getLoadProfileEntities() {
		return loadProfileEntities;
	}

	/**
	 * @param loadProfileEntities
	 *            the loadProfileEntities to set
	 */
	public void setLoadProfileEntities(final List<LoadProfileEntity> loadProfileEntities) {
		this.loadProfileEntities = loadProfileEntities;
	}
}
