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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mgmtp.perfload.loadprofiles.model.CurveAssignment;
import com.mgmtp.perfload.loadprofiles.ui.model.LoadProfileEntity;
import com.mgmtp.perfload.loadprofiles.ui.model.Stairs;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates points for the load curve graph.
 * 
 * @author rnaegele
 * major rework due to using different algorithms by mvarendo
 */
public class GraphPointsCalculator {
	private static final Logger LOG = LoggerFactory.getLogger(GraphPointsCalculator.class);

	/**
	 * Calculates graph points for the given list of curve assignments (one graph per curve name).
	 * 
	 * @param stairways
	 *            The list of curve assignments
	 * @return A map of lists of points. The curve names are the keys.
	 */
	public Map<String, List<Point>> calculatePoints(final Collection<Stairs> stairways) {
		List<String> operationNames = deriveListOfOperationNames(stairways);

		Map<String, List<Point>> pointsMap = Maps.newLinkedHashMap();

                // for each operation combine all defined stairs to one load curve and calculate points of this load curve
		for (final String operationName : operationNames) {
			List<Curve> curves = Lists.newArrayList();

			for (Stairs stairway : stairways) {
				if (stairway.operation.getName().equals(operationName)) {
					Curve curve = new Curve(calculatePoints(stairway));
					curves.add(curve);
				}
			}
			pointsMap.put(operationName, sumUpCurves(curves));
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
	private List<Point> sumUpCurves(final List<Curve> curves) {

                Curve sumCurve = curves.get(0);
                for (int iCurve=1; iCurve < curves.size(); iCurve++) {
                    Curve curveToAdd = curves.get(iCurve);
                    sumCurve = addCurves(sumCurve, curveToAdd);
                }
                
                return sumCurve.points;
        }
        
	/**
	 * Checks, whether there is a vertical step starting at this point.
         * A vertical step is defined by 2 consecutive points with the same x-value.
	 * 
	 * @param curve the curve for which we check, whther ther is a vertical step starting at the given index
         * @param iPoint the index for the point at which we check, wether there is a vertical step starting
         * 
	 * @return true, if a vertical step is starting at the given point, false otherwise
	 */
        private boolean isAVerticalStepStartingAtThisPoint(Curve curve, int iPoint) {
            // at the last point of the curve can't be the start point of a vertical step
            if (curve.getPoints().size() <= iPoint+1) {
                return false;
            }
            Point thisPoint = curve.getPoints().get(iPoint);
            Point nextPoint = curve.getPoints().get(iPoint+1);
            // if x-values are equal, then there must be a vertical step starting here.
            // both values can't be equal, since we do not add identical points to a curve
            return thisPoint.getX() == nextPoint.getX();
        }
        
        /**
         * Find the curve, which has the next point in x-direction
         * The method assumes, that there is a next point. If both indices are at the last point or beyond an exception is thrown.
         * 
         * @param curve1 the first curve to find the next point 
         * @param currentIndex1 the index to start the search for the first curve
         * @param curve2 the second curve to find the next point
         * @param currentIndex2 the index to start the search for the second curve
         * @return CURVE_1 if first has next point, CURVE_2 if curve 2 has next point and CURVE_BOTH,
         *         if both next points share the same x-value
         */
        private static final int CURVE_1 = -1;
        private static final int CURVE_BOTH = 0;
        private static final int CURVE_2 = 1;
        private int getCurveOfNextPoint(Curve curve1, int currentIndex1, Curve curve2, int currentIndex2) {
            // take care of being at last point or beyond for both curves
            if ( curve1.noMorePoints(currentIndex1) ) {
                if (curve2.noMorePoints(currentIndex2) ) {
                    throw new IllegalArgumentException("<getStairwayOfNextPoint> currentIndices are at last point or beyond: "+
                            "curve1 currentIndex1 "+currentIndex1+" size "+curve1.getPoints().size()+
                            "curve2 currentIndex2 "+currentIndex2+" size "+curve2.getPoints().size());
                } else {
                    // curve1 is at last point, curev2 not
                    return CURVE_2;
                }
            } else {
                if (curve2.noMorePoints(currentIndex2)) {
                    // curve2 is at last point, curve1 not
                    return CURVE_1;
                }
            }
            // normal case, both have more points
            Point nextPointCurve1 = curve1.getPoints().get(currentIndex1+1);
            Point nextPointCurve2 = curve2.getPoints().get(currentIndex2+1);
            if (nextPointCurve1.getX() == nextPointCurve2.getX()) {
                return CURVE_BOTH;
            } else if (nextPointCurve1.getX() < nextPointCurve2.getX()) {
                return CURVE_1;
            } else {
                return CURVE_2;
            }
        }
        
        /**
         * Adds two curves. The basic algorithm is simple, the corner cases nasty.
         * The basic algorithm:
         *    Start with the curve with the lower c-value of the first point.
         *    Take this as the start point of the result curve.
         *    Find the next point in increasing x-direction regardless, which curve.
         *    Derive the y-value from an interpolation of the other curve at the x-value of this point.
         *    If the x-value is outside the other curve, take 0 as interpolated y-value.
         *    Define a new point with this x-value and the sum of the y-value and the interpolated y-value.
         *    Repeat this until the last point
         * Corner cases:
         *    - the next point of both curves has the same x-value
         *    - the next point of a curve is the start of a vertical step
         *    - really nasty: combinations of the corner cases above
         * @param baseCurve  the curve to which the other curve is added
         * @param curveToAdd  the curve, which is added to the base curve
         * @return the resulting curve of the addition
         */
        private Curve addCurves(Curve baseCurve, Curve curveToAdd) {
                
            // algorithm in detail
            // for all points of both curves:
            //     get stairway of next point (next in direction of x-axis starting at minimum of all x-values)
            //     decisions for next point calculation: 
            //      -both curves have common x-value for next point: advance both indices, y of nextPoint is sum
            //      -next point on base curve: advance base curve index, take next point from base curve
            //      -next point on curve to add: advance curve to add index, take next point from curve to add
            //     
            //     decisions for addition of points:
            //      - next point was from both curves with common x-value:
            //          add derived next point
            //          has base curve and curve to add a vertical step at this x-value: 
            //              advance to following points for both curves, add point with y-values summed up
            //          has only base curve vertical step at this x-value:
            //              advance to next point on base curve, add y-value of curve to add, add point
            //          has only curve to add vertical step at this x-value:
            //              advance to next point on curve to add, add y-value of base curve, add point
            //      - next point was from base curve:
            //          derive y-value of curve to add at x-value of next point
            //          add derived y-value to next point, add point
            //          has base curve vertical step at this x-value:
            //              advance to next base curve point, add derived y-value to this point, add point
            //          has curve to add vertical step at this x-value:
            //              advance to next curve to add point, add derived y-value to this point, add point
            //      - next point was from curve to add:
            //           same as above just base curve and curve to add switched
                
            int currentIndexBaseCurve = -1;
            int currentIndexCurveToAdd = -1;
            List<Point> pointsAddedCurve = new LinkedList<>();
            
            while (baseCurve.hasNextPoint(currentIndexBaseCurve) || curveToAdd.hasNextPoint(currentIndexCurveToAdd)) {
                
                int curveNextPoint = getCurveOfNextPoint(baseCurve, currentIndexBaseCurve, curveToAdd, currentIndexCurveToAdd);
                Point nextPoint;
                if (curveNextPoint == CURVE_BOTH) {
                    currentIndexBaseCurve++;
                    currentIndexCurveToAdd++;
                    Point baseCurvePoint = baseCurve.getPoints().get(currentIndexBaseCurve);
                    Point curveToAddPoint = curveToAdd.getPoints().get(currentIndexCurveToAdd);
                    nextPoint = new Point(baseCurvePoint.getX(), baseCurvePoint.getY() + curveToAddPoint.getY());
                } else if (curveNextPoint == CURVE_1) {
                    currentIndexBaseCurve++;
                    nextPoint = baseCurve.getPoints().get(currentIndexBaseCurve);
                } else {
                    currentIndexCurveToAdd++;
                    nextPoint = curveToAdd.getPoints().get(currentIndexCurveToAdd);
                }
                boolean baseCurveHasVerticalStep = isAVerticalStepStartingAtThisPoint(baseCurve, currentIndexBaseCurve);
                boolean curveToAddHasVerticalStep = isAVerticalStepStartingAtThisPoint(baseCurve, currentIndexBaseCurve);
                LOG.debug("currentIndexBaseCurve "+currentIndexBaseCurve+",  currentIndexCurveToAdd "+currentIndexCurveToAdd+
                        ", curveNextPoint "+curveNextPoint+", nextPoint "+nextPoint+
                        ", baseCurveHasVerticalStep "+baseCurveHasVerticalStep+", curveToAddHasVerticalStep "+curveToAddHasVerticalStep);
                if (curveNextPoint == CURVE_BOTH) {
                    // add next point
                    Point newPoint = nextPoint;
                    pointsAddedCurve.add(newPoint);
                    LOG.debug("Added point "+newPoint);
                    if (baseCurveHasVerticalStep && curveToAddHasVerticalStep) {
                        // add following point as sum of both following curve points
                        currentIndexBaseCurve++;
                        currentIndexCurveToAdd++;
                        Point baseCurvePoint = baseCurve.getPoints().get(currentIndexBaseCurve);
                        Point curveToAddPoint = curveToAdd.getPoints().get(currentIndexCurveToAdd);
                        newPoint = new Point(baseCurvePoint.getX(), baseCurvePoint.getY() + curveToAddPoint.getY());
                        pointsAddedCurve.add(newPoint);
                        LOG.debug("Added point "+newPoint);
                    } else if (baseCurveHasVerticalStep) {
                        // add next point and following point of baseCurve as sum of both y-values
                        currentIndexBaseCurve++;
                        Point baseCurvePoint = baseCurve.getPoints().get(currentIndexBaseCurve);
                        Point curveToAddPoint = curveToAdd.getPoints().get(currentIndexCurveToAdd);
                        newPoint = new Point(baseCurvePoint.getX(), baseCurvePoint.getY() + curveToAddPoint.getY());
                        pointsAddedCurve.add(newPoint);
                        LOG.debug("Added point "+newPoint);
                    } else if (curveToAddHasVerticalStep) {
                        // add next point and following point of baseCurve as sum of both y-values
                        currentIndexCurveToAdd++;
                        Point baseCurvePoint = baseCurve.getPoints().get(currentIndexBaseCurve);
                        Point curveToAddPoint = curveToAdd.getPoints().get(currentIndexCurveToAdd);
                        newPoint = new Point(curveToAddPoint.getX(), baseCurvePoint.getY() + curveToAddPoint.getY());
                        pointsAddedCurve.add(newPoint);
                        LOG.debug("Added point "+newPoint);
                    }
                } else if (curveNextPoint == CURVE_1) {
                    double yValueCurveToAdd = curveToAdd.calculateYForX(nextPoint.getX());
                    Point newPoint = new Point(nextPoint.getX(), nextPoint.getY() + yValueCurveToAdd);
                    pointsAddedCurve.add(newPoint);
                    LOG.debug("Added point "+newPoint);
                    if (baseCurveHasVerticalStep) {
                        currentIndexBaseCurve++;
                        nextPoint = baseCurve.getPoints().get(currentIndexBaseCurve);
                        newPoint = new Point(nextPoint.getX(), nextPoint.getY() + yValueCurveToAdd);
                        pointsAddedCurve.add(newPoint);
                        LOG.debug("Added point "+newPoint);
                    }
                } else {
                    double yValueBaseCurve = baseCurve.calculateYForX(nextPoint.getX());
                    Point newPoint = new Point(nextPoint.getX(), nextPoint.getY() + yValueBaseCurve);
                    pointsAddedCurve.add(newPoint);
                    LOG.debug("Added point "+newPoint);
                    if (curveToAddHasVerticalStep) {
                        currentIndexCurveToAdd++;
                        nextPoint = curveToAdd.getPoints().get(currentIndexCurveToAdd);
                        newPoint = new Point(nextPoint.getX(), nextPoint.getY() + yValueBaseCurve);
                        pointsAddedCurve.add(newPoint);
                        LOG.debug("Added point "+newPoint);
                    }
                }
            }
            return new Curve(pointsAddedCurve);
	}

	/**
	 * Calculates graph points for the given stairway.
	 * 
	 * @param stairway
	 *            A stairway as defined in the load profile editor
	 * @return List of points describing the shape of the given stairway
	 */
	public List<Point> calculatePoints(final Stairs stairway) {
		LinkedList<Point> points = new LinkedList();

		int numSteps = stairway.numSteps;
		int steplength = stairway.a + stairway.b;

                // add startpoint
                Point firstPointOfStairway = Point.of(stairway.t0, 0);
		points.add(firstPointOfStairway);

                // add points of each step tread of stairway, if they are not already contained. This happens if parameter values of a stairway are 0.
		for (int i = 0; i < numSteps; ++i) {
                    Point startOfTread = Point.of(stairway.t0 + stairway.a + i * steplength, (i + 1) * stairway.h);
                    if ( ! startOfTread.equals(points.getLast())) {
			points.add(startOfTread);
                    }
                    Point endOfTread = Point.of(stairway.t0 + stairway.a + stairway.b + i * steplength, (i + 1) * stairway.h);
                    if ( ! endOfTread.equals(points.getLast())) {
			points.add(endOfTread);
                    }
		}

                // add last point of stairway
                Point lastPointOfStairway = Point.of(stairway.t0 + numSteps * steplength + stairway.c, 0);
                if ( ! lastPointOfStairway.equals(points.getLast())) {
                    points.add(lastPointOfStairway);
                }

                return points;
	}

	private List<String> deriveListOfOperationNames(final Collection<Stairs> loadProfileEnities) {
		List<String> operationNames = Lists.newArrayList();

		for (LoadProfileEntity lpe : loadProfileEnities) {
			if (lpe instanceof CurveAssignment) {
				CurveAssignment ca = (CurveAssignment) lpe;
				if (!operationNames.contains(ca.operation.getName())) {
					operationNames.add(ca.operation.getName());
				}
			}
		}
		Collections.sort(operationNames);

		return operationNames;
	}

	private static class Curve {
		private final LinkedList<Point> points;

		/**
		 * Creates a curve from the specified points
		 * 
		 * @param points
		 *            the points that make up the curve
		 */
		Curve(final List<Point> points) {
			this.points = new LinkedList(points);
		}
                
                /**
                 * Is there a point beyond the point defined by the given index
                 * 
                 * @param iPoint index of the point at which this check starts
                 * @return true, if there is at least one point beyond the given point
                 */
                public boolean hasNextPoint(int iPoint) {
                    return points.size() > iPoint+1;
                }
                
                /**
                 * Are there no more points beyond the point defined by the given index
                 * @param iPoint index of the point at which this check starts
                 * @return true, if there are no more points beyond the given point
                 */
                public boolean noMorePoints(int iPoint) {
                    return points.size() <= iPoint+1;
                }

		/**
		 * Calculates the y-value on this curve for the given x. y-values outside the curve definition are 0.
		 */
		double calculateYForX(final double x) {
                    if (points.size() < 2) {
                        throw new IllegalArgumentException("Curve must have at least two points, curve has "+points.size()+" points.");
                    }

                    if (x < points.getFirst().getX()) {
                        return 0.;
                    }
                    if (x > points.getLast().getX()) {
                        return 0.;
                    }
                    Point p1 = points.getFirst();
                    for (int i = 1; i < points.size(); i++) {
                        Point p2 = points.get(i);
                        if (p2.getX() == p1.getX()) {
                            // no interpolation possible
                            p1 = p2;
                            continue;
                        }
                        if (x >= p1.getX() && x <= p2.getX()) {
                            return calculateYOnLine(p1, p2, x);
                        }
                        p1 = p2;
                    }
                    throw new java.lang.IllegalStateException("We should never arrive here");
		}

		/**
		 * Calculates the y-value for x on a line through points p1 and p2.
		 */
		double calculateYOnLine(final Point p1, final Point p2, final double x) {
                    if (x == p1.getX()) {
                        return p1.getY();
                    }
                    if (x == p2.getX()) {
                        return p2.getY();
                    }
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

		public List<Point> getPoints() {
                    return points;
		}
	}
}
