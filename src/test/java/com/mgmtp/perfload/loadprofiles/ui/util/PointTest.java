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
package com.mgmtp.perfload.loadprofiles.ui.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;

import org.testng.annotations.Test;

/**
 * @author rnaegele
 */
public class PointTest {

	@Test
	public void testCompareTo() {
		Point p1 = Point.of(1, 1);
		Point p2 = Point.of(1, 1);
		Point p3 = Point.of(2, 1);
		Point p4 = Point.of(1, 2);

		assertThat(p1.compareTo(p1), equalTo(0));
		assertThat(p1.compareTo(p2), equalTo(0));
		assertThat(p1.compareTo(p3), lessThan(0));
		assertThat(p3.compareTo(p1), greaterThan(0));
		assertThat(p1.compareTo(p4), lessThan(0));
		assertThat(p4.compareTo(p1), greaterThan(0));
		assertThat(p3.compareTo(p4), greaterThan(0));
		assertThat(p4.compareTo(p3), lessThan(0));

	}

	@Test
	public void testEquals() {
		Point p1 = Point.of(1, 1);
		Point p2 = Point.of(1, 1);
		Point p3 = Point.of(2, 1);
		Point p4 = Point.of(1, 2);

		assertThat(p1, equalTo(p1));
		assertThat(p1, equalTo(p2));
		assertThat(p2, equalTo(p1));
		assertThat(p3, not(equalTo(p4)));
		assertThat(p4, not(equalTo(p3)));
	}
}
