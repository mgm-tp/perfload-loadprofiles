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
public class Operation extends AbstractNamedObject<Operation> {
	private double relativeClientLoad = 1.0d;

	/**
	 * @return the relativeClientLoad
	 */
	public double getRelativeClientLoad() {
		return relativeClientLoad;
	}

	/**
	 * @param relativeClientLoad
	 *            the relativeClientLoad to set
	 */
	public void setRelativeClientLoad(final double relativeClientLoad) {
		this.relativeClientLoad = relativeClientLoad;
	}

	@Override
	public int compareTo(final Operation o) {
		int result = super.compareTo(o);
		if (result != 0) {
			return result;
		}
		return Double.compare(relativeClientLoad, o.relativeClientLoad);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(relativeClientLoad);
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
		Operation other = (Operation) obj;
		if (Double.doubleToLongBits(relativeClientLoad) != Double.doubleToLongBits(other.relativeClientLoad)) {
			return false;
		}
		return true;
	}
}
