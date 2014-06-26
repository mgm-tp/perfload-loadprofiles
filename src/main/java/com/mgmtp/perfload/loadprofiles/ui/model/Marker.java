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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author rnaegele
 */
public class Marker implements LoadProfileEntity {

	public String name;
	public int left;
	public int right;

	public Marker() {
		//
	}

	public Marker(final String name, final int left, final int right) {
		this.name = name;
		this.left = left;
		this.right = right;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

	@Override
	public int compareTo(final LoadProfileEntity other) {
		if (other instanceof Marker) {
			Marker marker = (Marker) other;
			int result = name.compareTo(((Marker) other).name);
			if (result == 0) {
				result = left - marker.left;
				if (result == 0) {
					result = right - marker.right;
				}
			}
			return result;
		}
		return -1;
	}
}
