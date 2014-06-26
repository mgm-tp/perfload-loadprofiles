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
public class Target extends AbstractNamedObject<Target> {

	private double loadPart = 1.0d;

	/**
	 * @return the loadPart
	 */
	public double getLoadPart() {
		return loadPart;
	}

	/**
	 * @param loadPart
	 *            the loadPart to set
	 */
	public void setLoadPart(final double loadPart) {
		this.loadPart = loadPart;
	}

	@Override
	public int compareTo(final Target o) {
		int result = super.compareTo(o);
		if (result != 0) {
			return result;
		}
		return Double.compare(loadPart, o.loadPart);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(loadPart);
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
		Target other = (Target) obj;
		if (Double.doubleToLongBits(loadPart) != Double.doubleToLongBits(other.loadPart)) {
			return false;
		}
		return true;
	}

}
