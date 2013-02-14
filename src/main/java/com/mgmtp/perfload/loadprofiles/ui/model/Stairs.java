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
package com.mgmtp.perfload.loadprofiles.ui.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.mgmtp.perfload.loadprofiles.model.CurveAssignment;
import com.mgmtp.perfload.loadprofiles.model.Operation;

/**
 * @author rnaegele
 */
public class Stairs extends CurveAssignment {
	public int a, b, c, h, numSteps;

	public Stairs() {
		//
	}

	public Stairs(final Operation operation, final int t0) {
		super(operation, t0);
	}

	public Stairs(final Operation operation, final int t0, final int a, final int b, final int c, final int h, final int numSteps) {
		this(operation, t0);
		this.a = a;
		this.b = b;
		this.c = c;
		this.h = h;
		this.numSteps = numSteps;
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
		tsb.append("t0", t0);
		tsb.append("a", a);
		tsb.append("b", b);
		tsb.append("c", c);
		tsb.append("h", h);
		tsb.append("steps", numSteps);
		return tsb.toString();
	}
}
