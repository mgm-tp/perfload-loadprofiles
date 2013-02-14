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

import java.util.Comparator;

/**
 * For the Load Curve Calculator load events must be sorted according to time. The sorting criteria
 * are arbitrarily chosen.
 * 
 * @author mvarendo
 */
public class LoadEventComparator implements Comparator<BaseLoadProfileEvent> {

	@Override
	public int compare(final BaseLoadProfileEvent ev1, final BaseLoadProfileEvent ev2) {
		int result = Double.compare(ev1.getTime(), ev2.getTime());
		if (result != 0 || ev1 instanceof MarkerEvent || ev2 instanceof MarkerEvent) {
			return result;
		}

		LoadEvent loadEvent1 = (LoadEvent) ev1;
		LoadEvent loadEvent2 = (LoadEvent) ev2;

		result = loadEvent1.getClientId() - loadEvent2.getClientId();
		if (result == 0) {
			result = loadEvent1.getDaemonId() - loadEvent2.getDaemonId();
			if (result == 0) {
				result = loadEvent1.getProcessId() - loadEvent2.getProcessId();
				if (result == 0) {
					result = loadEvent1.getOperation().getName().compareTo(loadEvent2.getOperation().getName());
					if (result == 0) {
						result = loadEvent1.getTarget().getName().compareTo(loadEvent2.getTarget().getName());
					}
				}
			}
		}
		return result;
	}
}
