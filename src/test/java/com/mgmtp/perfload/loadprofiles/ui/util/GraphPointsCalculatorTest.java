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

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.mgmtp.perfload.loadprofiles.model.Operation;
import com.mgmtp.perfload.loadprofiles.ui.model.Stairs;

/**
 * @author rnaegele
 */
public class GraphPointsCalculatorTest {
	private static final Logger LOG = LoggerFactory.getLogger(GraphPointsCalculatorTest.class);

	@Test
	public void testSingleCurve() {
		logInfoStartingTest("testSingleCurve");

		Operation operation = new Operation();
		operation.setName("test");

		Stairs trapeze = new Stairs(operation, 0, 3, 3, 3, 3, 1);

		GraphPointsCalculator calc = new GraphPointsCalculator();
		List<Point> points = calc.calculatePoints(trapeze);

		List<Point> expectedPoints = Lists.newArrayList(Point.of(0, 0), Point.of(3, 3), Point.of(6, 3), Point.of(9, 0));
		assertEqualsEveryPoint(points, expectedPoints);
	}

	@Test
	public void testMultipleCurves() {
		logInfoStartingTest("testMultipleCurves");

		Operation operation = new Operation();
		operation.setName("test");

		Stairs trapeze = new Stairs(operation, 0, 3, 3, 3, 3, 1);

		GraphPointsCalculator calc = new GraphPointsCalculator();
		List<Stairs> trapezes = newArrayList();

		for (int i = 1; i <= 10; ++i) {
			trapezes.add(trapeze);
			List<Point> points = calc.calculatePoints(trapezes).get("test");
			List<Point> expectedPoints = Lists.newArrayList(Point.of(0, 0), Point.of(3, i * 3), Point.of(6, i * 3),
					Point.of(9, 0));
			assertEqualsEveryPoint(points, expectedPoints);
		}
	}

	@Test
	public void test2CurvesWith2VerticalSteps() {
		logInfoStartingTest("test2CurvesWith2VerticalSteps");

		Operation operation = new Operation();
		operation.setName("test");

		Stairs stairs1 = new Stairs(operation, 0, 2, 1, 0, 2, 1);
		Stairs stairs2 = new Stairs(operation, 3, 0, 1, 1, 1, 1);

		GraphPointsCalculator calc = new GraphPointsCalculator();
		List<Stairs> curves = newArrayList(stairs1, stairs2);

		List<Point> points = calc.calculatePoints(curves).get("test");

		List<Point> expectedPoints = Lists.newArrayList(Point.of(0, 0), Point.of(2, 2), Point.of(3, 2), Point.of(3, 1),
				Point.of(4, 1), Point.of(5, 0));
		assertEqualsEveryPoint(points, expectedPoints);
	}

	@Test
	public void test2CurvesWith1VerticalStep() {
		logInfoStartingTest("test2CurvesWith1VerticalStep");

		Operation operation = new Operation();
		operation.setName("test");

		Stairs stairs1 = new Stairs(operation, 0, 2, 1, 1, 1, 1);
		Stairs stairs2 = new Stairs(operation, 2, 0, 1, 1, 2, 1);

		GraphPointsCalculator calc = new GraphPointsCalculator();
		List<Stairs> curves = newArrayList(stairs1, stairs2);

		List<Point> points = calc.calculatePoints(curves).get("test");

		List<Point> expectedPoints = Lists.newArrayList(Point.of(0, 0), Point.of(2, 1), Point.of(2, 3), Point.of(3, 3),
				Point.of(4, 0));
		assertEqualsEveryPoint(points, expectedPoints);
	}

	@Test
	public void testTrapezeWithStairs() {
		logInfoStartingTest("testTrapezeWithStairs");

		Operation operation = new Operation();
		operation.setName("test");

		Stairs stairs1 = new Stairs(operation, 0, 2, 2, 4, 2, 2);
		Stairs stairs2 = new Stairs(operation, 1, 6, 1, 6, 6, 1);

		GraphPointsCalculator calc = new GraphPointsCalculator();
		List<Stairs> curves = newArrayList(stairs1, stairs2);

		List<Point> points = calc.calculatePoints(curves).get("test");

		List<Point> expectedPoints = Lists.newArrayList(Point.of(0, 0), Point.of(1, 1), Point.of(2, 3), Point.of(4, 5),
				Point.of(6, 9), Point.of(7, 10), Point.of(8, 10), Point.of(12, 2), Point.of(14, 0));
		assertEqualsEveryPoint(points, expectedPoints);
	}

	private void assertEqualsEveryPoint(List<Point> points, List<Point> expectedPoints) {
		for (int i = 0; i < expectedPoints.size(); i++) {
			assertThat(points.get(i), equalTo(expectedPoints.get(i)));
		}
	}

	private void logInfoStartingTest(String testName) {
		LOG.info("Starting test {}", testName);
	}
}
