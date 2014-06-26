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
package com.mgmtp.perfload.loadprofiles.ui.model;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.mgmtp.perfload.loadprofiles.model.Client;
import com.mgmtp.perfload.loadprofiles.model.Operation;
import com.mgmtp.perfload.loadprofiles.model.Target;

/**
 * @author rnaegele
 */
@XmlRootElement
public class Settings implements Cloneable {

	@XmlElement(name = "operation")
	@XmlElementWrapper(name = "operations")
	private final List<Operation> operations = newArrayList();

	@XmlElement(name = "target")
	@XmlElementWrapper(name = "targets")
	private final List<Target> targets = newArrayList();

	@XmlElement(name = "client")
	@XmlElementWrapper(name = "clients")
	private final List<Client> clients = newArrayList();

	public static Settings of(final List<Operation> operations, final List<Target> targets, final List<Client> clients) {
		Settings settings = new Settings();
		settings.operations.addAll(operations);
		settings.targets.addAll(targets);
		settings.clients.addAll(clients);
		return settings;
	}

	/**
	 * @return the operations
	 */
	public List<Operation> getOperations() {
		return ImmutableList.copyOf(operations);
	}

	/**
	 * @return the targets
	 */
	public List<Target> getTargets() {
		return ImmutableList.copyOf(targets);
	}

	/**
	 * @return the clients
	 */
	public List<Client> getClients() {
		return ImmutableList.copyOf(clients);
	}

	public Settings deepClone() {
		List<Operation> clonedOperations = ImmutableList.copyOf(transform(getOperations(), new Function<Operation, Operation>() {
			@Override
			public Operation apply(final Operation input) {
				return input.clone();
			}
		}));

		List<Target> clonedTargets = ImmutableList.copyOf(transform(getTargets(), new Function<Target, Target>() {
			@Override
			public Target apply(final Target input) {
				return input.clone();
			}
		}));

		List<Client> clonedClients = ImmutableList.copyOf(transform(getClients(), new Function<Client, Client>() {
			@Override
			public Client apply(final Client input) {
				return input.clone();
			}
		}));

		return Settings.of(clonedOperations, clonedTargets, clonedClients);
	}
}
