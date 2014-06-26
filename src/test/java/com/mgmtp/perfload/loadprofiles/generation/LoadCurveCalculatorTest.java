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
/*
 * LoadCurveCalculatorTest.java
 *
 * Created on 16. Oktober 2007, 10:53
 *
 */

package com.mgmtp.perfload.loadprofiles.generation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.Format;
import java.text.NumberFormat;

import org.testng.annotations.Test;

import com.mgmtp.perfload.loadprofiles.model.LoadCurve;

/**
 * Drives the tests of calculations involving the load curves including the event generation from
 * load curves.
 * 
 * @author mvarendo
 */
public class LoadCurveCalculatorTest {

	/**
	 * Generates a load curve, which is used in the tests. Don't change the values of the load curve
	 * without changing the expected values in the different test methods.
	 */
	private LoadCurve generateLoadCurve() {
		String name = "Registration";
		double[] timeValues = { 0., 0.5, 1.5, 2., 3., 3.5 };
		double[] rateValues = { 0., 100., 100., 200., 200., 0. };
		LoadCurve loadCurve = new LoadCurve();
		loadCurve.setTimeValues(timeValues);
		loadCurve.setRateValues(rateValues);
		loadCurve.setTimeUnit(LoadCurveCalculator.timeUnit_hour);
		loadCurve.setRateUnit(LoadCurveCalculator.rateUnit_perHour);
		loadCurve.setName(name);
		return loadCurve;
	}

	/** Define the number of events in each line segments of the load curve. */
	private double[] getSegmentsNEvents() {
		double[] segmentsNEvents = new double[5];
		segmentsNEvents[0] = 0.5 * 50.;
		segmentsNEvents[1] = 1. * 100.;
		segmentsNEvents[2] = 0.5 * 150.;
		segmentsNEvents[3] = 1. * 200.;
		segmentsNEvents[4] = 0.5 * 100.;
		return segmentsNEvents;
	}

	/** Return the manually derived normalized number of events for the load curve defined above. */
	private double[] getManualNormedEvents(final double[] segmentsNEvents, final double manualNEvents) {
		double[] integratedEvents = new double[6];
		double[] manualNormedEvents = new double[6];
		manualNormedEvents[0] = 0.;
		integratedEvents[0] = 0.;
		for (int i = 1; i < 6; i++) {
			integratedEvents[i] = integratedEvents[i - 1] + segmentsNEvents[i - 1];
			manualNormedEvents[i] = integratedEvents[i] / manualNEvents;
		}
		return manualNormedEvents;
	}

	/**
	 * Tests the fill of the statistic and the derivation of norm values of the load curve by a
	 * comparison of values from the LoadCurveCalculator with manually calculated values.
	 */
	@Test
	public void testFillStatisticsAndNormValuesOfLoadCurve() {
		LoadCurve testLoadCurve = generateLoadCurve();
		// manually calculate statistics and normalized values
		double manualRatemax = 200.;
		double manualRatemin = 0.;
		double manualNEvents = 0.;
		double[] segmentsNEvents = getSegmentsNEvents();
		for (int i = 0; i < 5; i++) {
			manualNEvents += segmentsNEvents[i];
		}
		double[] manualNormedEvents = getManualNormedEvents(segmentsNEvents, manualNEvents);

		// use LoadCurveCalculator for statistics and normalized values
		LoadCurveCalculator.fillStatisticsAndNormValuesOfLoadCurve(testLoadCurve);

		// compare calculations
		assertEquals(manualRatemax, testLoadCurve.getRateMax(), "Ratemax is not correct");
		assertEquals(manualRatemin, testLoadCurve.getRateMin(), "Ratemin is not correct");
		assertEquals(manualNEvents, testLoadCurve.getNEvents(), "NEvents is not correct");
		for (int i = 1; i < testLoadCurve.getTimeValues().length; i++) {
			assertEquals(manualNormedEvents[i], testLoadCurve.getNormedEvents(i), "normedEvent for index " + i
					+ " is not correct");
		}
	}

	/**
	 * Test the binary search for line segments using a given normalized event index. The test
	 * checks values below and above the normalized event indices in the load curve and then in the
	 * middle of line segments and around the boundaries.
	 */
	public void testGetLineSegment() {
		LoadCurve testLoadCurve = generateLoadCurve();
		LoadCurveCalculator.fillStatisticsAndNormValuesOfLoadCurve(testLoadCurve);

		double[] normedEvents = testLoadCurve.getNormedEvents();

		// test value far below lowest value of nomrlized values
		double valueFarBelowLowest = -2. * LoadCurveCalculator.EPSILON;
		int indexOfValueFarBelowLowest = LoadCurveCalculator.getLineSegment(testLoadCurve, valueFarBelowLowest);
		assertEquals(indexOfValueFarBelowLowest, -1, "Index of value far below lowest is not correct");

		// test value below lowest value of nomrlized values
		double valueBelowLowest = -LoadCurveCalculator.EPSILON / 2.;
		int indexOfValueBelowLowest = LoadCurveCalculator.getLineSegment(testLoadCurve, valueBelowLowest);
		assertEquals(indexOfValueBelowLowest, 0, "Index of value below lowest is not correct");

		// test value at start of load curve
		double valueAtStart = normedEvents[0];
		int indexOfValueAtStart = LoadCurveCalculator.getLineSegment(testLoadCurve, valueAtStart);
		assertEquals(0, indexOfValueAtStart, "Index of value at start is not correct");

		// test values in the middle and at upper boundary
		for (int i = 1; i < normedEvents.length; i++) {
			double valueAtMiddle = (normedEvents[i] + normedEvents[i - 1]) / 2.;
			int indexOfValueAtMiddle = LoadCurveCalculator.getLineSegment(testLoadCurve, valueAtMiddle);
			assertEquals(i - 1, indexOfValueAtMiddle, "Index of value in the middle between " + (i - 1) + " and " + i
					+ " is not correct");

			double valueBelowUpperBound = normedEvents[i] - LoadCurveCalculator.EPSILON / 2.;
			int indexOfValueBelowUpperBound = LoadCurveCalculator.getLineSegment(testLoadCurve, valueBelowUpperBound);
			assertEquals(i - 1, indexOfValueBelowUpperBound, "Index of value below upper bound of " + (i - 1) + " and " + i
					+ " is not correct");

			double valueAboveUpperBound = normedEvents[i] + LoadCurveCalculator.EPSILON / 2.;
			int indexOfValueAboveUpperBound = LoadCurveCalculator.getLineSegment(testLoadCurve, valueAboveUpperBound);
			assertEquals(i, indexOfValueAboveUpperBound, "Index of value above upper bound of " + (i - 1) + " and " + i
					+ " is not correct");
		}

		// test value at the end of load curve is the last test of the loop above

		// test value beyond the end of load curve (numerical deviation is only considered at the
		// start of the load curve)
		double valueBeyondEnd = normedEvents[normedEvents.length - 1] + 2. * LoadCurveCalculator.EPSILON;
		int indexOfValueBeyondEnd = LoadCurveCalculator.getLineSegment(testLoadCurve, valueBeyondEnd);
		assertEquals(indexOfValueBeyondEnd, normedEvents.length, "Index of value beyond the end is not correct");
	}

	/**
	 * Test the calculation of start times by deriving the start times for all events of a load
	 * curve, binning the result according to the load curve time intervals and then comparing the
	 * result with the number of events, which is supposed to be in the interval. For the evaluation
	 * of the correct distribution of the events, the event times are written together with the
	 * current rate to a csv-file, which can be plotted.
	 */
	public void testDeriveStartTime() throws Exception {
		Format format = NumberFormat.getNumberInstance();
		LoadCurve testLoadCurve = generateLoadCurve();
		LoadCurveCalculator.fillStatisticsAndNormValuesOfLoadCurve(testLoadCurve);
		double[] segmentsNEvents = getSegmentsNEvents();
		for (int i = 0; i < segmentsNEvents.length; i++) {
			segmentsNEvents[i] = segmentsNEvents[i];
		}
		double nEvents = testLoadCurve.getNEvents();
		double[] timeValues = testLoadCurve.getTimeValues();
		double[] histogram = new double[segmentsNEvents.length];
		java.io.PrintWriter pw = new java.io.PrintWriter("data/plots/LoadCurveCalculatorTest_StartTimes");
		pw.println("BaseLoadProfileEvent start time; Rate");
		pw.println("0,0; 0,0");
		for (int iEvent = 1; iEvent <= nEvents; iEvent++) {
			double eventIndex = iEvent;
			// double Tn = LoadCurveCalculator.deriveStartTimeForClient(testLoadCurve, eventIndex,
			// nClients, iClient);
			double Tn = LoadCurveCalculator.deriveStartTime(testLoadCurve, eventIndex);
			double r = LoadCurveCalculator.r(testLoadCurve, Tn);
			pw.println(format.format(Tn) + "; " + format.format(r));
			for (int ind = 1; ind < timeValues.length; ind++) {
				if (Tn >= timeValues[ind - 1] && Tn < timeValues[ind]) {
					histogram[ind - 1]++;
				}
			}
		}
		pw.close();
		for (int ind = 1; ind < histogram.length; ind++) {
			boolean isCorrect = Math.abs(segmentsNEvents[ind] - histogram[ind]) < 1.1;
			assertTrue(isCorrect, "Number of events found in time interval " + ind + " deviates, expected " +
					segmentsNEvents[ind] + ", found " + histogram[ind] + ", a deviation < 1.1 is accepted,");
		}
	}
}
