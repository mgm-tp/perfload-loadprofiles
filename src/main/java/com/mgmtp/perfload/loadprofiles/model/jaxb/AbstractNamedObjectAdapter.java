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
package com.mgmtp.perfload.loadprofiles.model.jaxb;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.mgmtp.perfload.loadprofiles.model.AbstractNamedObject;

/**
 * Base class for JAXB adapters.
 * 
 * @author rnaegele
 */
public abstract class AbstractNamedObjectAdapter<T extends AbstractNamedObject<T>> extends XmlAdapter<String, T> {

	private final List<T> list;
	private final Class<?> type;

	/**
	 * @param list
	 *            a list of objects used for unmarshaling
	 */
	public AbstractNamedObjectAdapter(final List<T> list) {
		this.list = list;
		// capture the generic type parameter
		type = (Class<?>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	/**
	 * Looks up and returns an object with the specified name from the list.
	 * 
	 * @param name
	 *            the String that is unmarshaled
	 */
	@Override
	public T unmarshal(final String name) throws Exception {
		for (T ca : list) {
			if (ca.getName().equals(name)) {
				return ca;
			}
		}
		throw new JAXBException(type.getSimpleName() + " '" + name
				+ "' used in load profile is not availabe in configured operations/targets.\nPlease check the current settings.");
	}

	/**
	 * Returns the object's name for writing to the XML.
	 * 
	 * @param object
	 *            the object that is marshaled
	 */
	@Override
	public String marshal(final T object) throws Exception {
		return object.getName();
	}

}
