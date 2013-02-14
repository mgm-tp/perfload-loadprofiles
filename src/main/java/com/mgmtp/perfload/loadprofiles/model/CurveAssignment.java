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

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.mgmtp.perfload.loadprofiles.model.jaxb.OperationAdapter;
import com.mgmtp.perfload.loadprofiles.ui.model.LoadProfileEntity;

/**
 * Base class for curve assignment types.
 * 
 * @author rnaegele
 */
public abstract class CurveAssignment implements LoadProfileEntity {

	@XmlJavaTypeAdapter(OperationAdapter.class)
	public Operation operation;

	public int t0;

	public CurveAssignment() {
		//
	}

	public CurveAssignment(final Operation operation, final int t0) {
		this.operation = operation;
		this.t0 = t0;
	}

	public Operation getOperation() {
		return operation;
	}

	@Override
	public int compareTo(final LoadProfileEntity other) {
		if (other instanceof CurveAssignment) {
			CurveAssignment ca = (CurveAssignment) other;
			int result = this.operation.getName().compareTo(ca.operation.getName());
			if (result == 0) {
				result = t0 - ca.t0;
			}
			return result;
		}
		return 1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (operation == null ? 0 : operation.hashCode());
		result = prime * result + t0;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CurveAssignment other = (CurveAssignment) obj;
		if (operation == null) {
			if (other.operation != null) {
				return false;
			}
		} else if (!operation.equals(other.operation)) {
			return false;
		}
		if (t0 != other.t0) {
			return false;
		}
		return true;
	}
}
