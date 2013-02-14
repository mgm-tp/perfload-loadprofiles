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
package com.mgmtp.perfload.loadprofiles.model;

/**
 * @author rnaegele
 */
public class Client extends AbstractNamedObject<Client> {
	private int numDaemons = 1;
	private int numProcesses = 2;
	private double relativePower = .5d;

	/**
	 * @return the numDaemons
	 */
	public int getNumDaemons() {
		return numDaemons;
	}

	/**
	 * @param numDaemons
	 *            the numDaemons to set
	 */
	public void setNumDaemons(final int numDaemons) {
		this.numDaemons = numDaemons;
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
		result = numDaemons - o.numDaemons;
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
		result = prime * result + numDaemons;
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
		if (numDaemons != other.numDaemons) {
			return false;
		}
		if (numProcesses != other.numProcesses) {
			return false;
		}
		if (Double.doubleToLongBits(relativePower) != Double.doubleToLongBits(other.relativePower)) {
			return false;
		}
		return true;
	}
}
