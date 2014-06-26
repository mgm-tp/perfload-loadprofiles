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
package com.mgmtp.perfload.loadprofiles.model;

/**
 * @author rnaegele
 */
public class Client extends AbstractNamedObject<Client> {
	private int daemonId;
	private int numProcesses = 2;
	private double relativePower = .5d;

	/**
	 * @return the daemonId
	 */
	public int getDaemonId() {
		return daemonId;
	}

	/**
	 * @param daemonId
	 *            the daemonId to set
	 */
	public void setDaemonId(final int daemonId) {
		this.daemonId = daemonId;
	}

	/**
	 * @return the numProcesses
	 */
	public int getNumProcesses() {
		return numProcesses;
	}

	/**
	 * @param numProcesses
	 *            the numProcesses to set
	 */
	public void setNumProcesses(final int numProcesses) {
		this.numProcesses = numProcesses;
	}

	/**
	 * @return the relativePower
	 */
	public double getRelativePower() {
		return relativePower;
	}

	/**
	 * @param relativePower
	 *            the relativePower to set
	 */
	public void setRelativePower(final double relativePower) {
		this.relativePower = relativePower;
	}

	@Override
	public int compareTo(final Client o) {
		int result = super.compareTo(o);
		if (result != 0) {
			return result;
		}
		result = numProcesses - o.numProcesses;
		if (result != 0) {
			return result;
		}
		return Double.compare(relativePower, o.relativePower);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + numProcesses;
		long temp;
		temp = Double.doubleToLongBits(relativePower);
		result = prime * result + (int) (temp ^ temp >>> 32);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Client other = (Client) obj;
		if (numProcesses != other.numProcesses) {
			return false;
		}
		if (Double.doubleToLongBits(relativePower) != Double.doubleToLongBits(other.relativePower)) {
			return false;
		}
		return true;
	}
}
