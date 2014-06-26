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
 * LoadCurve.java
 *
 * Created on 12. Oktober 2007, 09:47
 *
 * Contains the definition of a loadcurve for the named test case.
 * The load for each time during the test is described by
 * pairs of rate and time values. The
 * rate is linearly interpolated between rate/time points.
 * The start time of the test (excluding any preparation)
 * is defined by 0. The unit of the time values is minutes.
 * Normed events, nevents and minimum and maximum rate are
 * values derived from the time and rate values and stored here to save
 * the time of recomputing them.
 */

package com.mgmtp.perfload.loadprofiles.model;

import org.slf4j.Logger;

/**
 * @author mvarendo
 */
public class LoadCurve implements Cloneable {

	/**
	 * Holds value of property name /
	 */
	private String name;

	/**
	 * Number of events. The number of events is calculated and not guaranteed to be integral and is
	 * therefore stored in a double
	 */
	private double nEvents;

	/**
	 * Holds value of property normedEvents.
	 */
	private double[] normedEvents;

	/**
	 * Holds value of property rateMax.
	 */
	private double rateMax;

	/**
	 * Holds value of property rateMin.
	 */
	private double rateMin;

	protected String rateUnit;

	/**
	 * Holds value of property rateValues.
	 */
	private double[] rateValues;

	protected String timeUnit;

	/**
	 * Holds value of property timeValues.
	 */
	private double[] timeValues;

	@Override
	public LoadCurve clone() {
		LoadCurve clonedLoadCurve = new LoadCurve();
		int nPoint = timeValues.length;
		double[] clonedTimeValues = new double[nPoint];
		double[] clonedRateValues = new double[nPoint];
		for (int iPoint = 0; iPoint < nPoint; iPoint++) {
			clonedTimeValues[iPoint] = this.timeValues[iPoint];
			clonedRateValues[iPoint] = this.rateValues[iPoint];
		}
		clonedLoadCurve.setTimeValues(clonedTimeValues);
		clonedLoadCurve.setRateValues(clonedRateValues);
		clonedLoadCurve.setName(new String(this.name));
		clonedLoadCurve.setTimeUnit(new String(this.timeUnit));
		clonedLoadCurve.setRateUnit(new String(this.rateUnit));
		return clonedLoadCurve;
	}

	public void dump(final Logger log) {
		log.info("LoadCurve " + name);
		log.info("Time, rate");
		for (int i = 0; i < this.timeValues.length; i++) {
			log.info(timeValues[i] + ", " + rateValues[i]);
		}
	}

	public String getName() {
		return name;
	}

	/**
	 * Get number of events.
	 * 
	 * @return Number of events.
	 */
	public double getNEvents() {
		return this.nEvents;
	}

	/**
	 * Getter for property normedEvents.
	 * 
	 * @return Value of property normedEvents.
	 */
	public double[] getNormedEvents() {
		return this.normedEvents;
	}

	/**
	 * Indexed getter for property normedEvents.
	 * 
	 * @param index
	 *            Index of the property.
	 * @return Value of the property at <CODE>index</CODE>.
	 */
	public double getNormedEvents(final int index) {
		return this.normedEvents[index];
	}

	/**
	 * Getter for property maxLoad.
	 * 
	 * @return Value of property maxLoad.
	 */
	public double getRateMax() {
		return this.rateMax;
	}

	/**
	 * Getter for property minLoad.
	 * 
	 * @return Value of property minLoad.
	 */
	public double getRateMin() {
		return this.rateMin;
	}

	/**
	 * Get the value of rateUnit
	 * 
	 * @return the value of rateUnit
	 */
	public String getRateUnit() {
		return rateUnit;
	}

	/**
	 * Getter for property rateValues.
	 * 
	 * @return Value of property rateValues.
	 */
	public double[] getRateValues() {
		return this.rateValues;
	}

	/**
	 * Indexed getter for property rateValues.
	 * 
	 * @param index
	 *            Index of the property.
	 * @return Value of the property at <CODE>index</CODE>.
	 */
	public double getRateValues(final int index) {
		return this.rateValues[index];
	}

	/**
	 * Get the value of timeUnit
	 * 
	 * @return the value of timeUnit
	 */
	public String getTimeUnit() {
		return timeUnit;
	}

	/**
	 * Getter for property timeValues.
	 * 
	 * @return Value of property timeValues.
	 */
	public double[] getTimeValues() {
		return this.timeValues;
	}

	/**
	 * Indexed getter for property timeValues.
	 * 
	 * @param index
	 *            Index of the property.
	 * @return Value of the property at <CODE>index</CODE>.
	 */
	public double getTimeValues(final int index) {
		return this.timeValues[index];
	}

	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Set number of events.
	 * 
	 * @param nEvents
	 *            New value of number of events.
	 */
	public void setNEvents(final double nEvents) {
		this.nEvents = nEvents;
	}

	/**
	 * Setter for property normedEvents.
	 * 
	 * @param normedEvents
	 *            New value of property normedEvents.
	 */
	public void setNormedEvents(final double[] normedEvents) {
		this.normedEvents = normedEvents;
	}

	/**
	 * Indexed setter for property normedEvents.
	 * 
	 * @param index
	 *            Index of the property.
	 * @param normedEvents
	 *            New value of the property at <CODE>index</CODE>.
	 */
	public void setNormedEvents(final int index, final double normedEvents) {
		this.normedEvents[index] = normedEvents;
	}

	/**
	 * Setter for property maxLoad.
	 * 
	 * @param rateMax
	 *            New value of property maxLoad.
	 */
	public void setRateMax(final double rateMax) {
		this.rateMax = rateMax;
	}

	/**
	 * Setter for property rateMin.
	 * 
	 * @param rateMin
	 *            New value of property minLoad.
	 */
	public void setRateMin(final double rateMin) {
		this.rateMin = rateMin;
	}

	/**
	 * Set the value of rateUnit
	 * 
	 * @param rateUnit
	 *            new value of rateUnit
	 */
	public void setRateUnit(final String rateUnit) {
		this.rateUnit = rateUnit;
	}

	/**
	 * Setter for property rateValues.
	 * 
	 * @param rateValues
	 *            New value of property rateValues.
	 */
	public void setRateValues(final double[] rateValues) {
		this.rateValues = rateValues;
	}

	/**
	 * Indexed setter for property rateValues.
	 * 
	 * @param index
	 *            Index of the property.
	 * @param rateValues
	 *            New value of the property at <CODE>index</CODE>.
	 */
	public void setRateValues(final int index, final double rateValues) {
		this.rateValues[index] = rateValues;
	}

	/**
	 * Set the value of timeUnit
	 * 
	 * @param timeUnit
	 *            new value of timeUnit
	 */
	public void setTimeUnit(final String timeUnit) {
		this.timeUnit = timeUnit;
	}

	/**
	 * Setter for property timeValues.
	 * 
	 * @param timeValues
	 *            New value of property timeValues.
	 */
	public void setTimeValues(final double[] timeValues) {
		this.timeValues = timeValues;
	}

	/**
	 * Indexed setter for property timeValues.
	 * 
	 * @param index
	 *            Index of the property.
	 * @param timeValues
	 *            New value of the property at <CODE>index</CODE>.
	 */
	public void setTimeValues(final int index, final double timeValues) {
		this.timeValues[index] = timeValues;
	}
}
