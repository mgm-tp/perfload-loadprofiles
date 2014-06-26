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

import static com.google.common.base.Preconditions.checkArgument;

import com.mgmtp.perfload.loadprofiles.model.AbstractNamedObject;

/**
 * @author rnaegele
 */
public class SelectionDecorator {
	private AbstractNamedObject<?> baseObject;
	private boolean selected;

	public SelectionDecorator(final AbstractNamedObject<?> baseObject, final boolean selected) {
		checkArgument(baseObject != null, "'baseObject' must not be null");
		this.baseObject = baseObject;
		this.selected = selected;
	}

	/**
	 * @return the baseObject
	 */
	public AbstractNamedObject<?> getBaseObject() {
		return baseObject;
	}

	/**
	 * @param baseObject
	 *            the baseObject to set
	 */
	public void setBaseObject(final AbstractNamedObject<?> baseObject) {
		this.baseObject = baseObject;
	}

	/**
	 * @return the selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @param selected
	 *            the selected to set
	 */
	public void setSelected(final boolean selected) {
		this.selected = selected;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (baseObject == null ? 0 : baseObject.hashCode());
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
		SelectionDecorator other = (SelectionDecorator) obj;
		if (baseObject == null) {
			if (other.baseObject != null) {
				return false;
			}
		} else if (!baseObject.equals(other.baseObject)) {
			return false;
		}
		return true;
	}

	//	@Override
	//	public int compareTo(final SelectionDecorator o) {
	//		AbstractNamedObject<?> baseObject2 = o.baseObject;
	//		return baseObject.compareTo(baseObject2);
	//	}
}
