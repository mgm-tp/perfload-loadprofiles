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

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import com.mgmtp.perfload.loadprofiles.model.Operation;
import com.mgmtp.perfload.loadprofiles.ui.model.Stairs;

/**
 * @author rnaegele
 */
public class GraphPointsCalculatorTest {

	@Test
	public void testSingleCurve() {
		Operation operation = new Operation();
		operation.setName("test");

		Stairs trapeze = new Stairs(operation, 0, 3, 3, 3, 3, 1);

		GraphPointsCalculator calc = new GraphPointsCalculator();
		Set<Point> points = calc.calculatePoints(trapeze);

		assertThat(points, hasItems(Point.of(0, 0), Point.of(3, 3), Point.of(6, 3), Point.of(9, 0)));
	}

	@Test
	public void testMultipleCurves() {
		Operation operation = new Operation();
		operation.setName("test");

		Stairs trapeze = new Stairs(operation, 0, 3, 3, 3, 3, 1);

		GraphPointsCalculator calc = new GraphPointsCalculator();
		List<Stairs> trapezes = newArrayList();

		for (int i = 1; i <= 10; ++i) {
			trapezes.add(trapeze);
			Set<Point> points = calc.calculatePoints(trapezes).get("test");
			assertThat(points, hasItems(Point.of(0, 0), Point.of(3, i * 3), Point.of(6, i * 3), Point.of(9, 0)));
		}
	}

	@Test
	public void testTrapezeWithStairs() {
		Operation operation = new Operation();
		operation.setName("test");

		Stairs stairs1 = new Stairs(operation, 0, 2, 2, 4, 2, 2);
		Stairs stairs2 = new Stairs(operation, 1, 6, 1, 6, 6, 1);

		GraphPointsCalculator calc = new GraphPointsCalculator();
		List<Stairs> curves = newArrayList(stairs1, stairs2);

		Set<Point> points = calc.calculatePoints(curves).get("test");

		assertThat(points, hasItems(
				Point.of(0, 0),
				Point.of(1, 1),
				Point.of(2, 3),
				Point.of(4, 5),
				Point.of(6, 9),
				Point.of(7, 10),
				Point.of(8, 10),
				Point.of(12, 2),
				Point.of(14, 0)
				));
	}
}
