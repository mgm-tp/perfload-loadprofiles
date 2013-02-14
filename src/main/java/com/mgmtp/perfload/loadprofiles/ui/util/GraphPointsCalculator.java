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

import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.google.common.collect.Sets.newTreeSet;
import static java.lang.Math.ulp;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mgmtp.perfload.loadprofiles.model.CurveAssignment;
import com.mgmtp.perfload.loadprofiles.ui.model.LoadProfileEntity;
import com.mgmtp.perfload.loadprofiles.ui.model.Stairs;

/**
 * Calculates points for the load curve graph.
 * 
 * @author rnaegele
 */
public class GraphPointsCalculator {

	/**
	 * Calculates graph points for the given list of curve assignments (one graph per curve name).
	 * 
	 * @param stairs
	 *            The list of curve assignments
	 * @return A map of lists of points. The curve names are the keys.
	 */
	public Map<String, Set<Point>> calculatePoints(final Collection<Stairs> stairs) {
		List<String> curveNames = computeCurveNames(stairs);

		Map<String, Set<Point>> pointsMap = Maps.newLinkedHashMap();

		for (final String curveName : curveNames) {
			List<CurveItem> curves = Lists.newArrayList();

			for (Stairs item : stairs) {
				if (item.operation.getName().equals(curveName)) {
					CurveItem curve = new CurveItem(calculatePoints(item));
					curves.add(curve);
				}
			}

			pointsMap.put(curveName, sumUpCurves(curves));
		}

		return pointsMap;
	}

	/**
	 * Sums up the specified curves returning a list of points representing one curve.
	 * 
	 * @param curves
	 *            A list of separate curves
	 * @return The summed list of points
	 */
	private Set<Point> sumUpCurves(final List<CurveItem> curves) {
		Map<Double, Point[]> curvePoints = Maps.newHashMap();
		Set<Double> xValues = newLinkedHashSet();

		// Determine all x-values that have points on the separate curves
		// and collect all points by their x-values
		int curveCount = curves.size();
		for (int i = 0; i < curveCount; ++i) {
			CurveItem curve = curves.get(i);
			for (Point p : curve.getPoints()) {
				Double x = p.getX();
				Point[] pointsForX = curvePoints.get(x);
				if (pointsForX == null) {
					pointsForX = new Point[curveCount];
					curvePoints.put(x, pointsForX);
				}
				// Store point in the array for the x-value. Array elements may remain emtpy (null),
				// if there is no point for an x-value for the curve with the current index.
				pointsForX[i] = p;
				xValues.add(x);
			}
		}

		Set<Point> points = newLinkedHashSet();

		// iterate over x-values
		for (Double x : xValues) {
			//			if (i == 0) {
			//				// y is zero for the first point
			//				points.add(new Point(x, 0.));
			//			} else {
			// Get points for a certain x-value
			Point[] pointsForX = curvePoints.get(x);
			double sumY = 0.;
			for (int j = 0, len = pointsForX.length; j < len; ++j) {
				Point p = pointsForX[j];
				if (p == null) {
					CurveItem curve = curves.get(j);
					if (curve.isInRange(x)) {
						// If the curve is in range but does not have a direct y-value for this x,
						// we need to calculate the value on the line between the previous and the next point of the curve.
						double y = curve.calculateYForX(x);

						// Add calculated y-value
						sumY += y;
					}
				} else {
					// If there is a y-value for this x, we can directly add it.
					sumY += p.getY();
				}
				//				}

			}
			// Create a new point with current x-value and the sum of y-values
			points.add(new Point(x, sumY));
		}

		return points;
	}

	/**
	 * Calculates graph points for the given curve assignment.
	 * 
	 * @param stairs
	 *            The load profile entity
	 * @return A list of points
	 */
	public Set<Point> calculatePoints(final Stairs stairs) {
		Set<Point> points = newLinkedHashSet();

		int numSteps = stairs.numSteps;
		int offset = stairs.a + stairs.b;

		points.add(Point.of(stairs.t0, 0));

		for (int i = 0; i < numSteps; ++i) {
			points.add(Point.of(stairs.t0 + stairs.a + i * offset, (i + 1) * stairs.h));
			points.add(Point.of(stairs.t0 + stairs.a + stairs.b + i * offset, (i + 1) * stairs.h));
		}

		points.add(Point.of(stairs.t0 + numSteps * offset + stairs.c, 0));

		// A single curve must have distinct x-values, which is not the case, if a or c are set to 0. Otherwise the logic
		// for summing up single curves would not work. Thus, we simply move the first x-value of two distinct
		// ones by an ulp to the left.
		Point previous = null;
		Set<Point> changedPoints = newLinkedHashSet();
		for (Point p : points) {
			if (previous != null) {
				if (previous.getX() == p.getX()) {
					changedPoints.add(Point.of(previous.getX() - ulp(previous.getX()), previous.getY()));
				} else {
					changedPoints.add(previous);
				}
			}
			previous = p;
		}
		changedPoints.add(previous);
		return changedPoints;
	}

	private List<String> computeCurveNames(final Collection<Stairs> loadProfileEnities) {
		List<String> curveNames = Lists.newArrayList();

		for (LoadProfileEntity lpe : loadProfileEnities) {
			if (lpe instanceof CurveAssignment) {
				CurveAssignment ca = (CurveAssignment) lpe;
				if (!curveNames.contains(ca.operation.getName())) {
					curveNames.add(ca.operation.getName());
				}
			}
		}
		Collections.sort(curveNames);

		return curveNames;
	}

	private static class CurveItem {
		private final NavigableSet<Point> points;
		private final double minX;
		private final double maxX;

		/**
		 * Creates a curve from the specified points
		 * 
		 * @param points
		 *            the points that make up the curve
		 */
		CurveItem(final Set<Point> points) {
			this.points = newTreeSet(points);
			this.minX = this.points.iterator().next().getX();
			this.maxX = this.points.descendingIterator().next().getX();
		}

		boolean isInRange(final double x) {
			return x >= minX && x <= maxX;
		}

		/**
		 * Calculates the y-value on this curve for the given x.
		 */
		double calculateYForX(final double x) {
			Point p1 = null;
			for (Point p : points) {
				if (p.getX() == x) {
					return p.getY();
				}
				if (p1 != null) {
					Point p2 = p;
					if (p1.getX() < x && p2.getX() > x) {
						return calculateYOnLine(p1, p2, x);
					}
				}
				p1 = p;
			}
			throw new IllegalArgumentException(x + " not in range of curve");
		}

		/**
		 * Calculates the y-value for x on a line through points p1 and p2.
		 */
		double calculateYOnLine(final Point p1, final Point p2, final double x) {
			// slope of a line:
			// m = (y2 - y1) / (x2 - x1)
			double m = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());

			// y-intercept of the line
			// c = y - mx
			double c = p1.getY() - m * p1.getX();

			// value for y on the line between p1 and p2
			double y = m * x + c;

			return y;
		}

		public NavigableSet<Point> getPoints() {
			return newTreeSet(points);
		}
	}
}
