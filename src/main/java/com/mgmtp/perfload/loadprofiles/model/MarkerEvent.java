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
public class MarkerEvent implements BaseLoadProfileEvent {
	public static enum Type {
		left, right
	}

	private final String name;
	private final double time;
	private final Type type;

	/**
	 * @param name
	 *            the marker name
	 * @param time
	 *            the event time
	 * @param type
	 *            the type of the marker
	 */
	public MarkerEvent(final String name, final double time, final Type type) {
		this.name = name;
		this.time = time;
		this.type = type;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the time
	 */
	@Override
	public double getTime() {
		return time;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}
}
