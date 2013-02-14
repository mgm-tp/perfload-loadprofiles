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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mgmtp.perfload.loadprofiles.model;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;

import java.util.List;

/**
 * The definition of a load test configuration to be used by the LoadTestEventGenerator to generate
 * Lists of load events, which are used by perfLoad to drive load tests.
 * 
 * @author mvarendo
 */

public class LoadTestConfiguration implements Cloneable {

	/** The clients to be used in this load test. */
	protected List<Client> clients = newArrayList();

	/** A textual description of the load test configuration. */
	protected String description;

	/**
	 * LOad curve assignements, which define, which operation is executed against which targets
	 * following which load curve.
	 */
	protected List<LoadCurveAssignment> loadCurveAssignments = newArrayList();

	/** Name of the load test configuration. */
	protected String name;

	/** The operations, which are used in the load curve assignements. */
	protected List<Operation> operations = newArrayList();

	@Override
	public LoadTestConfiguration clone() {
		try {
			LoadTestConfiguration clone = (LoadTestConfiguration) super.clone();
			if (clients != null) {
				int size = clients.size();
				clone.clients = newArrayListWithCapacity(size);
				for (Client client : clients) {
					clone.clients.add(client.clone());
				}
			}
			if (operations != null) {
				int size = operations.size();
				clone.operations = newArrayListWithCapacity(size);
				for (Operation operation : operations) {
					clone.operations.add(operation.clone());
				}
			}
			if (loadCurveAssignments != null) {
				int size = loadCurveAssignments.size();
				clone.loadCurveAssignments = newArrayListWithCapacity(size);
				for (LoadCurveAssignment loadCurveAssignment : loadCurveAssignments) {
					clone.loadCurveAssignments.add(loadCurveAssignment.clone());
				}
			}
			return clone;
		} catch (CloneNotSupportedException ex) {
			// can't happen
			return null;
		}
	}

	/**
	 * Get the value of clients
	 * 
	 * @return the value of clients
	 */
	public List<Client> getClients() {
		return clients;
	}

	/**
	 * Get the value of description
	 * 
	 * @return the value of description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Get the value of loadCurveAssignments
	 * 
	 * @return the value of loadCurveAssignments
	 */
	public List<LoadCurveAssignment> getLoadCurveAssignments() {
		return loadCurveAssignments;
	}

	/**
	 * Get the value of name
	 * 
	 * @return the value of name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the value of operations
	 * 
	 * @return the value of operations
	 */
	public List<Operation> getOperations() {
		return operations;
	}

	/**
	 * Set the value of description
	 * 
	 * @param description
	 *            new value of description
	 */
	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * Set the value of name
	 * 
	 * @param name
	 *            new value of name
	 */
	public void setName(final String name) {
		this.name = name;
	}
}
