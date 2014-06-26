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
package com.mgmtp.perfload.loadprofiles.ui.model;

import static com.google.common.collect.Lists.transform;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.base.Function;
import com.mgmtp.perfload.loadprofiles.model.CurveAssignment;
import com.mgmtp.perfload.loadprofiles.model.Operation;
import com.mgmtp.perfload.loadprofiles.model.Target;
import com.mgmtp.perfload.loadprofiles.model.jaxb.TargetAdapter;

/**
 * @author rnaegele
 */
public class OneTime extends CurveAssignment {

	@XmlElementWrapper(name = "targets")
	@XmlElement(name = "target")
	@XmlJavaTypeAdapter(value = TargetAdapter.class, type = Target.class)
	public List<Target> targets;

	public OneTime() {
		this(null, 0);
	}

	public OneTime(final Operation operation, final int t0) {
		this(operation, t0, new ArrayList<Target>());
	}

	public OneTime(final Operation operation, final int t0, final List<Target> target) {
		super(operation, t0);
		this.targets = target;
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
		tsb.append("t0", t0);
		tsb.append("targets", transform(targets, new Function<Target, String>() {
			@Override
			public String apply(final Target input) {
				return input.getName();
			}
		}));
		return tsb.toString();
	}
}
