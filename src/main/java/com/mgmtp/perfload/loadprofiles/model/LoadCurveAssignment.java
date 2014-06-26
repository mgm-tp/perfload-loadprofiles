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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mgmtp.perfload.loadprofiles.model;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;

import java.util.List;

/**
 * A load curve assignment defines, which operation is executed against which targets (server) on
 * according to which scaled load curve. Operations and load curves are not persisted in this
 * object, only the name of the operation and the name of the load curve.
 * 
 * @author mvarendo
 */
public class LoadCurveAssignment implements Cloneable {

	/**
	 * Direct reference to the load curve defining the temporal evolution of the load of the given
	 * operation.
	 */
	protected LoadCurve loadCurve;

	/**
	 * The name of the load curve, defining the temporal evolution of the load of the given
	 * operation.
	 */
	protected String loadCurveName;

	/** Scaling factor, by which the given load curve is scaled. */
	protected double loadCurveScaling = 1.0d;

	/** A direct reference to the operation to be executed against the targets. */
	protected Operation operation;

	/** Name of the operation to be executed against the targets. */
	protected String operationName;

	/** List of targets agains which the load is executed. */
	protected List<Target> targets = newArrayList();

	/**
	 * Get the value of loadCurve
	 * 
	 * @return the value of loadCurve
	 */
	public LoadCurve getLoadCurve() {
		return loadCurve;
	}

	/**
	 * Get the value of loadCurveName
	 * 
	 * @return the value of loadCurveName
	 */
	public String getLoadCurveName() {
		return loadCurveName;
	}

	/**
	 * Get the value of loadCurveScaling
	 * 
	 * @return the value of loadCurveScaling
	 */
	public double getLoadCurveScaling() {
		return loadCurveScaling;
	}

	/**
	 * Get the value of operation
	 * 
	 * @return the value of operation
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * Get the value of operationName
	 * 
	 * @return the value of operationName
	 */
	public String getOperationName() {
		return operationName;
	}

	/**
	 * Get the value of targets
	 * 
	 * @return the value of targets
	 */
	public List<Target> getTargets() {
		return targets;
	}

	/**
	 * Set the value of loadCurve
	 * 
	 * @param loadCurve
	 *            new value of loadCurve
	 */
	public void setLoadCurve(final LoadCurve loadCurve) {
		this.loadCurve = loadCurve;
	}

	/**
	 * Set the value of loadCurveName
	 * 
	 * @param loadCurveName
	 *            new value of loadCurveName
	 */
	public void setLoadCurveName(final String loadCurveName) {
		this.loadCurveName = loadCurveName;
	}

	/**
	 * Set the value of loadCurveScaling
	 * 
	 * @param loadCurveScaling
	 *            new value of loadCurveScaling
	 */
	public void setLoadCurveScaling(final double loadCurveScaling) {
		this.loadCurveScaling = loadCurveScaling;
	}

	/**
	 * Set the value of operation
	 * 
	 * @param operation
	 *            new value of operation
	 */
	public void setOperation(final Operation operation) {
		this.operation = operation;
	}

	/**
	 * Set the value of operationName
	 * 
	 * @param operationName
	 *            new value of operationName
	 */
	public void setOperationName(final String operationName) {
		this.operationName = operationName;
	}

	@Override
	public LoadCurveAssignment clone() {
		try {
			LoadCurveAssignment clone = (LoadCurveAssignment) super.clone();
			if (loadCurve != null) {
				clone.loadCurve = loadCurve.clone();
			}
			if (operation != null) {
				clone.operation = operation.clone();
			}
			if (targets != null) {
				int size = targets.size();
				clone.targets = newArrayListWithCapacity(size);
				for (Target target : targets) {
					clone.targets.add(target.clone());
				}
			}
			return clone;
		} catch (CloneNotSupportedException ex) {
			// can't happen
			return null;
		}
	}
}
