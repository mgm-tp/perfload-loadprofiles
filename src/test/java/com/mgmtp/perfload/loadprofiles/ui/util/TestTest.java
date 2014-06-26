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
package com.mgmtp.perfload.loadprofiles.ui.util;

import org.testng.annotations.Test;

import com.google.common.collect.TreeMultimap;
import com.mgmtp.perfload.loadprofiles.model.CurveAssignment;
import com.mgmtp.perfload.loadprofiles.model.Operation;
import com.mgmtp.perfload.loadprofiles.ui.model.OneTime;

/**
 * @author rnaegele
 */
public class TestTest {

	@Test
	public void test() {
		TreeMultimap<String, CurveAssignment> treeData = TreeMultimap.create();
		Operation operation = new Operation();
		operation.setName("foo");
		treeData.put("foo", new OneTime(operation, 0));
		treeData.put("foo", new OneTime(operation, 0));
	}
}
