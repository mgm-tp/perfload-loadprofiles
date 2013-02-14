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

/**
 * A load event defines at which time an operation is executed from which process of which daemon on
 * which client against which targets.
 * 
 * @author mvarendo
 */
public class LoadEvent implements BaseLoadProfileEvent, Cloneable {

	private int clientId;
	private int daemonId;
	private Operation operation;
	private int processId;
	private double time;
	private Target target;

	public LoadEvent(final double time, final Operation operation) {
		this.time = time;
		this.operation = operation;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final LoadEvent other = (LoadEvent) obj;
		if (this.clientId != other.clientId) {
			return false;
		}
		if (this.operation != other.operation && (this.operation == null || !this.operation.equals(other.operation))) {
			return false;
		}
		if (this.time != other.time) {
			return false;
		}
		if (this.target != other.target && (this.target == null || !this.target.equals(other.target))) {
			return false;
		}
		if (this.daemonId != other.daemonId) {
			return false;
		}
		if (this.processId != other.processId) {
			return false;
		}
		return true;
	}

	public int getClientId() {
		return clientId;
	}

	public int getDaemonId() {
		return daemonId;
	}

	public Operation getOperation() {
		return operation;
	}

	public int getProcessId() {
		return processId;
	}

	@Override
	public double getTime() {
		return time;
	}

	public Target getTarget() {
		return target;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 83 * hash + this.clientId;
		hash = 83 * hash + (this.operation != null ? this.operation.hashCode() : 0);
		hash = 83 * hash + (int) (Double.doubleToLongBits(this.time) ^ Double.doubleToLongBits(this.time) >>> 32);
		hash = 83 * hash + (this.target != null ? this.target.hashCode() : 0);
		hash = 83 * hash + this.daemonId;
		hash = 83 * hash + this.processId;
		return hash;
	}

	public void setClientId(final int clientId) {
		this.clientId = clientId;
	}

	public void setDaemonId(final int daemonId) {
		this.daemonId = daemonId;
	}

	public void setOperationName(final Operation operation) {
		this.operation = operation;
	}

	public void setProcessId(final int processId) {
		this.processId = processId;
	}

	public void setTime(final double time) {
		this.time = time;
	}

	public void setTarget(final Target target) {
		this.target = target;
	}

	@Override
	public LoadEvent clone() {
		try {
			LoadEvent clone = (LoadEvent) super.clone();
			if (target != null) {
				clone.target = target.clone();
			}
			if (operation != null) {
				clone.operation = operation.clone();
			}
			return clone;
		} catch (CloneNotSupportedException ex) {
			// can't happen
			return null;
		}
	}
}
