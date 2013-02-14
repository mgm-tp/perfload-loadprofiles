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
package com.mgmtp.perfload.loadprofiles.generation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.loadprofiles.model.LoadCurve;

/**
 * Contains calculations involving load curves including statistics and the utilities necessary to
 * create event lists. For all time calculations it is assumed, that the time and rate values of the
 * load curve are in the units [hour] and [events/hour].
 * 
 * @author mvarendo
 */
public class LoadCurveCalculator {
	private static final Logger log = LoggerFactory.getLogger(LoadCurveCalculator.class);

	/** Defined time units (hour, minute, second, millisecond. */
	public static final String timeUnit_hour = "[h]";
	public static final String timeUnit_minute = "[m]";
	public static final String timeUnit_second = "[s]";
	public static final String timeUnit_millisecond = "[ms]";

	/** Defined rate units (per hour, per minute, per second, per millisecond. */
	public static final String rateUnit_perHour = "[1/h]";
	public static final String rateUnit_perMinute = "[1/m]";
	public static final String rateUnit_perSecond = "[1/s]";
	public static final String rateUnit_perMillisecond = "[1/ms]";

	/** value, which is smaller than the shortest distance between two events (= 1./maximal rate. */
	public static double EPSILON = 1. / 1000000;

	/** value, which is smaller than all non zero rate values */
	public static double EPSILON_RATE = 0.001;

	/** value, which is smaller than all non zero rate changes */
	public static double EPSILON_RATE_CHANGE = 0.001;

	/**
	 * Calculates start times of load test events from a given load curve for a given event index.
	 * The time value is in the same unit as the time units in the load curve. The load curve
	 * consists of line segments. Each line segment defines the linear devolution of the rate within
	 * its time interval. First the relevant line segment, into which the given eventIndex falls is
	 * searched. Then the time and number of events up to this time is derived for the lower and
	 * upper boundary of the time interval. This values are used calculating the start time of the
	 * event using the formula derived from the following algorithm: The integral over the time
	 * interval of the load curve from the last event up to this event is 1. More details can be
	 * found in the accompanying document in SVN at
	 * elsterportal\trunk\dokumente\QA\Konzepte\Lastkurven\Lasttest_AbfahrenVonLastkurven.doc
	 * 
	 * @param loadCurve
	 *            Contains the time and rate values of the load curve definition
	 * @param eventIndex
	 *            Index of event, for which the start time is calculated, could be a fractional
	 *            value
	 * @return The start time for the given event index
	 */
	public static double deriveStartTime(final LoadCurve loadCurve, final double eventIndex) {
		double Tn;
		int nPoints = loadCurve.getTimeValues().length;
		double nEvents = loadCurve.getNEvents();
		double normalizedIndex = eventIndex / nEvents;
		// find appropriate line segment, make sure iSegment is the point at the lower end of the
		// segment
		int iSegment = getLineSegment(loadCurve, normalizedIndex);
		// check values outside allowed range
		if (iSegment < 0) {
			// return a time, which is definitely before the start of the load curve
			Tn = loadCurve.getTimeValues(0) - loadCurve.getTimeValues(1);
			return Tn;
		}
		if (iSegment >= nPoints) {
			// return a time, which is definitely beyond the load curve end
			Tn = loadCurve.getTimeValues(nPoints - 1) * 2.;
			return Tn;
		}
		// derive Tn
		double r0 = loadCurve.getRateValues(iSegment);
		double rN = loadCurve.getRateValues(iSegment + 1);
		double T0 = loadCurve.getTimeValues(iSegment);
		double TN = loadCurve.getTimeValues(iSegment + 1);
		double n = eventIndex - nEvents * loadCurve.getNormedEvents(iSegment);
		double N = nEvents * (loadCurve.getNormedEvents(iSegment + 1) - loadCurve.getNormedEvents(iSegment));
		Tn = Tn(n, N, r0, rN, T0, TN);
		if (Tn < 0) {
			log.warn("Time of event with index " + eventIndex + " < 0 (=" + Tn + "), nEvents = " + nEvents +
					", nPoints = " + nPoints + ", setting time to 0.");
			Tn = 0.;
		}
		log.debug("Tn= " + Tn + ", iSegment=" + iSegment + ", n= " + n + ", N=" + N + ", r0=" + r0 + ", rN=" + rN + ", T0=" + T0
				+ ", TN=" + TN);
		return Tn;
	}

	/**
	 * Find the line segment containing the normalized event number. The lower index of the points
	 * defining the line segment is returned.
	 * 
	 * @arg LoadCurve containing the load curve (normedEvents must be defined), in which the line
	 *      segment is searched
	 * @arg normalizedEventIndex The index of the event to be searched divided by the total number
	 *      of events
	 * @return the lower index of the line segment containing the given normalized eventIndex
	 */
	public static int getLineSegment(final LoadCurve loadCurve, final double normalizedEventIndex) {
		int nPoints = loadCurve.getTimeValues().length;
		double[] normedEvents = loadCurve.getNormedEvents();

		// first check values outside the range of the normed events
		if (normalizedEventIndex < normedEvents[0] - EPSILON) {
			// the event index is definitely outside
			return -1;
		}
		if (normalizedEventIndex <= normedEvents[0] - EPSILON) {
			// this case should only occur due to numeric deviations (dont throw away possible start
			// or end events)
			return 0;
		}
		if (normalizedEventIndex > normedEvents[nPoints - 1] && normalizedEventIndex < normedEvents[nPoints - 1] + EPSILON) {
			// this case is for events, which should occur at the end, but deviate numerically
			return nPoints - 1;
		}
		if (normalizedEventIndex > normedEvents[nPoints - 1]) {
			// if it is beyond the interval time, return the last index. This occurs for the last
			// event +1,
			// which is the first event outside the boundary of the load curve and thus not
			// executed.
			return nPoints;
		}

		// execute binary search for the index of the line segment
		int iLow = 0;
		int iUp = nPoints - 1;
		while (iUp - iLow > 1) {
			int halfIndex = (iUp + iLow) / 2;
			if (normalizedEventIndex >= normedEvents[halfIndex]) {
				iLow = halfIndex;
			} else {
				iUp = halfIndex;
			}
		}
		return iLow;
	}

	/**
	 * Find the time interval containing the given time. The lower index of the points defining the
	 * time interval is returned. This method is used by the calculation of the event rate for a
	 * given time and load curve. The given time must be in the same time unit as the time values in
	 * the load curve.
	 * 
	 * @arg LoadCurve containing the load curve (normedEvents must be defined), in which the line
	 *      segment is searched
	 * @arg t The time for which the time interval in the rate curve is searched (time unit is the
	 *      same as in the load curve)
	 * @return the lower index of the time interval containing the given time
	 */
	public static int getTimeInterval(final LoadCurve loadCurve, final double t) {
		int nPoints = loadCurve.getTimeValues().length;
		double[] timeValues = loadCurve.getTimeValues();

		// first check values outside the range of the load curve
		if (t < timeValues[0]) {
			// the event index is definitely outside
			return -1;
		}
		if (t > timeValues[nPoints - 1]) {
			// if t is beyond the range of the load curve
			return nPoints;
		}

		// execute binary search for the index of time interval
		int iLow = 0;
		int iUp = nPoints - 1;
		while (iUp - iLow > 1) {
			int halfIndex = (iUp + iLow) / 2;
			if (t >= timeValues[halfIndex]) {
				iLow = halfIndex;
			} else {
				iUp = halfIndex;
			}
		}
		return iLow;
	}

	/**
	 * Derive the time unit from the given column descriptor. The column descriptor is normally
	 * taken from the first line of the time column of a load curve in .csv-format.
	 * 
	 * @param columnDescriptor
	 *            containing the column descriptor of the time column of a load curve
	 * @return a string containing the time unit.
	 */
	public static String getTimeUnit(final String columnDescriptor) {
		if (columnDescriptor == null) {
			throw new IllegalArgumentException("GivenColumnDescriptor is null");
		}
		if (columnDescriptor.indexOf(timeUnit_hour) >= 0) {
			return timeUnit_hour;
		}
		if (columnDescriptor.indexOf(timeUnit_minute) >= 0) {
			return timeUnit_minute;
		}
		if (columnDescriptor.indexOf(timeUnit_second) >= 0) {
			return timeUnit_second;
		}
		if (columnDescriptor.indexOf(timeUnit_millisecond) >= 0) {
			return timeUnit_millisecond;
		}
		throw new IllegalArgumentException("No known time unit found in columnDescriptor " + columnDescriptor);
	}

	/**
	 * Derive the rate unit from the given column descriptor. The column descriptor is normally
	 * taken from the first line of the rate column of a load curve in .csv-format.
	 * 
	 * @param columnDescriptor
	 *            containing the column descriptor of the rate column of a load curve
	 * @return a string containing the rate unit.
	 */
	public static String getRateUnit(final String columnDescriptor) {
		if (columnDescriptor == null) {
			throw new IllegalArgumentException("GivenColumnDescriptor is null");
		}
		if (columnDescriptor.indexOf(rateUnit_perHour) >= 0) {
			return rateUnit_perHour;
		}
		if (columnDescriptor.indexOf(rateUnit_perMinute) >= 0) {
			return rateUnit_perMinute;
		}
		if (columnDescriptor.indexOf(rateUnit_perSecond) >= 0) {
			return rateUnit_perSecond;
		}
		if (columnDescriptor.indexOf(rateUnit_perMillisecond) >= 0) {
			return rateUnit_perMillisecond;
		}
		throw new IllegalArgumentException("No known rate unit found in columnDescriptor " + columnDescriptor);
	}

	/**
	 * derive the scaling factor to scale time values from the origin time unit to the destination
	 * time unit. The original time values have to be multiplied by this scaling factor to convert
	 * them to the destination time unit.
	 * 
	 * @param originTimeUnit
	 *            time unit of the original time values
	 * @param targetTimeUnit
	 *            time unit to which the time values have to be converted.
	 */
	public static double getTimeScalingFactor(final String originTimeUnit, final String targetTimeUnit) {
		double originScaling = 0.;
		if (timeUnit_hour.equals(originTimeUnit)) {
			originScaling = 1.;
		} else if (timeUnit_minute.equals(originTimeUnit)) {
			originScaling = 60.;
		} else if (timeUnit_second.equals(originTimeUnit)) {
			originScaling = 3600.;
		} else if (timeUnit_millisecond.equals(originTimeUnit)) {
			originScaling = 3600000.;
		} else {
			throw new IllegalArgumentException("originTimeUnit is unkown: " + originTimeUnit);
		}

		double targetScaling = 0.;
		if (timeUnit_hour.equals(targetTimeUnit)) {
			targetScaling = 1.;
		} else if (timeUnit_minute.equals(targetTimeUnit)) {
			targetScaling = 60.;
		} else if (timeUnit_second.equals(targetTimeUnit)) {
			targetScaling = 3600.;
		} else if (timeUnit_millisecond.equals(targetTimeUnit)) {
			targetScaling = 3600000.;
		} else {
			throw new IllegalArgumentException("targetTimeUnit is unkown: " + targetTimeUnit);
		}

		double timeScalingFactor = targetScaling / originScaling;

		return timeScalingFactor;
	}

	/**
	 * Transform the load curve to the time unit [h] and rate unit [1/h]. The calculation of event
	 * start times is based on time units of [h] and rate units of [1/h]. After the transformation
	 * the statistics of the load curve is recalculated.
	 * 
	 * @arg LoadCurve containing the load curve to be transformed
	 * @return the transformed load curve object, the same object as given in the argument.
	 */
	public static LoadCurve transformToHours(final LoadCurve loadCurve) {

		if (loadCurve == null) {
			throw new IllegalArgumentException("Given loadCurve is null.");
		}

		log.info("Transforming load curve " + loadCurve.getName() + " to hours.");

		int nPoints = loadCurve.getTimeValues().length;
		double[] timeValues = loadCurve.getTimeValues();
		double[] rateValues = loadCurve.getRateValues();
		if (rateValues.length != timeValues.length) {
			throw new IllegalArgumentException(
					"Length of arrayy timeValues <> length of array rateValues in load curve with name " +
							loadCurve.getName());
		}

		double timeScaling = getTimeScalingFactor(loadCurve.getTimeUnit(), timeUnit_hour);

		double rateScaling = 0.;
		if (rateUnit_perHour.equals(loadCurve.getRateUnit())) {
			rateScaling = 1.;
		} else if (rateUnit_perMinute.equals(loadCurve.getRateUnit())) {
			rateScaling = 1. / 60.;
		} else if (rateUnit_perSecond.equals(loadCurve.getRateUnit())) {
			rateScaling = 1. / 3600.;
		} else if (rateUnit_perMillisecond.equals(loadCurve.getRateUnit())) {
			rateScaling = 1. / 3600000.;
		} else {
			throw new IllegalArgumentException("Rate Unit of load curve is unkown: " + loadCurve.getTimeUnit());
		}

		log.info("Scaling factors for loadCurve " + loadCurve.getName() + " from time unit " +
				loadCurve.getTimeUnit() + " is " + timeScaling +
				", from rate unit " + loadCurve.getRateUnit() + " is " + rateScaling);
		for (int iPoint = 0; iPoint < nPoints; iPoint++) {
			timeValues[iPoint] *= timeScaling;
			rateValues[iPoint] *= rateScaling;
		}

		loadCurve.setTimeUnit(timeUnit_hour);
		loadCurve.setRateUnit(rateUnit_perHour);

		// update minima and maxima after scaling
		// ToDo the following call should be replaced by a call only updating the minima and maxima
		LoadCurveCalculator.fillStatisticsAndNormValuesOfLoadCurve(loadCurve);

		return loadCurve;
	}

	/**
	 * Find the time interval containing the given time. The lower index of the points defining the
	 * time interval is returned. This method is used by the calculation of the event rate for a
	 * given time and load curve.
	 * 
	 * @arg LoadCurve containing the load curve to be scaled
	 * @arg factor The factor used for scaling the load curve
	 * @return the scaled load curve in the same object, given as input
	 */
	public static LoadCurve scaleLoadCurve(final LoadCurve loadCurve, final double factor) {
		int nPoints = loadCurve.getTimeValues().length;
		double[] rateValues = loadCurve.getRateValues();

		for (int iPoint = 0; iPoint < nPoints; iPoint++) {
			rateValues[iPoint] *= factor;
		}

		LoadCurveCalculator.fillStatisticsAndNormValuesOfLoadCurve(loadCurve);

		return loadCurve;
	}

	/**
	 * Derive minimum and maximum value of rates in the loadcurve and the number of events
	 * integrated over the entire duration of the load curve. Finally derive normalized event curve
	 * = integral value up to each defined timepoint in the load curve divided by the total number
	 * of events integrated over the entire duration of the load curve. Fill in all derived values
	 * in the properties of the given load curve bean. The statistics are only valid if time and
	 * rate values are based on the same time unit.
	 * 
	 * @arg loadCurve LoadCurve to be filled with statistics and normalized values
	 * @return the loadCurveBean from the argument (not cloned), filled with statistics and
	 *         normalized values.
	 */
	public static LoadCurve fillStatisticsAndNormValuesOfLoadCurve(final LoadCurve loadCurve) {
		int nPoints = loadCurve.getTimeValues().length;
		double integralNEvents = 0.;
		double[] rateValues = loadCurve.getRateValues();
		double[] timeValues = loadCurve.getTimeValues();
		double rateMax = rateValues[0];
		double rateMin = rateValues[0];
		double[] integratedEvents = new double[nPoints];
		// derive maximum and minimum rate and integral value
		for (int iPoint = 0; iPoint < nPoints; iPoint++) {
			if (rateValues[iPoint] > rateMax) {
				rateMax = rateValues[iPoint];
			}
			if (rateValues[iPoint] < rateMin) {
				rateMin = rateValues[iPoint];
			}

			if (iPoint > 0) {
				integralNEvents += (rateValues[iPoint] + rateValues[iPoint - 1]) / 2. *
						(timeValues[iPoint] - timeValues[iPoint - 1]);
			} else {
				integralNEvents = 0.;
			}
			integratedEvents[iPoint] = integralNEvents;
		}
		loadCurve.setNEvents(integralNEvents);
		loadCurve.setRateMax(rateMax);
		loadCurve.setRateMin(rateMin);

		// normalize Events
		double[] normedEvents = new double[nPoints];
		for (int iPoint = 0; iPoint < nPoints; iPoint++) {
			normedEvents[iPoint] = integratedEvents[iPoint] / integralNEvents;
		}
		loadCurve.setNormedEvents(normedEvents);

		return loadCurve;
	}

	/**
	 * Derive the rate at the given time using the given load curve. If the given time is outside
	 * the time range of the load curve, then a rate of 0. is returned.
	 * 
	 * @arg loadCurve Contains the definition of the load curve used for the rate calculation
	 * @arg T time at which the rate is derived
	 * @return The rate at the given time using the given load curve
	 */
	public static final double r(final LoadCurve loadCurve, final double T) {
		int nPoints = loadCurve.getTimeValues().length;
		int index = getTimeInterval(loadCurve, T);
		// the rate outside the load curve is 0.
		if (index < 0 || index >= nPoints) {
			return 0.;
		}
		double r0 = loadCurve.getRateValues(index);
		double rN = loadCurve.getRateValues(index + 1);
		double T0 = loadCurve.getTimeValues(index);
		double TN = loadCurve.getTimeValues(index + 1);
		double rDash = (rN - r0) / (TN - T0);
		double r = rDash * (T - T0) + r0;
		return r;
	}

	/**
	 * Calculate the event time from the given input values. The algorithm is defined in an
	 * accompanying document. This calculation derives the time of the event in the given rate curve
	 * interval. For the interval the lower and upper time values T0 and TN and the rate at the
	 * lower and upper end r0 and rN are given. The value of the rate change rDash can be 0. in the
	 * case of constant rates. For this case (all rDash values from -EPSILON_RATE_CHANGE to
	 * +EPSILON_RATE_CHANGE) a different formula has to be used. The value of r0 can also be 0. This
	 * is usually the case in the ramp up phase of the load curve, when the rate starts at 0. For
	 * these cases (all r0 values from -EPSILON_RATE up to +EPSILON_RATE) a different formula for r0
	 * -> 0 is used.
	 * 
	 * @arg n The number of events the integral over the rate curve from T0 should reach at the
	 *      returned event time
	 * @arg N The number of events delivered by the integral over the rate curve from T0 up to TN
	 * @arg r0 The rate at the time T0
	 * @arg rN The rate at the time TN
	 * @arg T0 The time at the lower end of the time interval, which is used for this calculation
	 * @arg TN The time at the upper end of the time interval, which is used for this calculation
	 * @return The time, when the integral over the rate reaches n
	 * @throws IllegalArgumentException
	 *             in case the given values lead to an argument<0 for the sqrt-calculation.
	 */
	private static final double Tn(final double n, final double N, final double r0, final double rN, final double T0,
			final double TN) {
		double Tn;
		double rDash = (rN - r0) / (TN - T0);
		if (Math.abs(rDash) > EPSILON_RATE_CHANGE) {
			if (r0 < EPSILON_RATE) {
				double argumentOfRoot = r0 / rDash * (r0 / rDash) + 2. * n / rDash;
				// log.debug("rDash="+rDash+", argumentOfRoot="+argumentOfRoot);
				if (argumentOfRoot < 0.) {
					throw new java.lang.IllegalArgumentException(
							"case r0<EPSILON_RATE: The given values lead to a value <0 for sqrt-calc. , n= " + n +
									", N=" + N + ", r0=" + r0 + ", rN=" + rN + ", T0=" + T0 + ", TN=" + TN);
				}
				Tn = T0 + Math.sqrt(argumentOfRoot) - r0 / rDash;
			} else {
				double argumentOfRoot = 1. + 2. * n * rDash / (r0 * r0);
				// log.debug("rDash="+rDash+", argumentOfRoot="+argumentOfRoot);
				if (argumentOfRoot < 0.) {
					throw new java.lang.IllegalArgumentException(
							"case r0>EPSILON_RATE: The given values lead to a value <0 for sqrt-calc. , n= " + n +
									", N=" + N + ", r0=" + r0 + ", rN=" + rN + ", T0=" + T0 + ", TN=" + TN);
				}
				Tn = T0 + r0 / rDash * (Math.sqrt(argumentOfRoot) - 1.);
			}
		} else {
			Tn = T0 + n / N * (TN - T0);
		}
		return Tn;
	}
}
