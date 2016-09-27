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

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.mgmtp.perfload.loadprofiles.model.Client;
import com.mgmtp.perfload.loadprofiles.model.Operation;
import com.mgmtp.perfload.loadprofiles.model.Target;
import com.mgmtp.perfload.loadprofiles.model.jaxb.ClientAdapter;
import com.mgmtp.perfload.loadprofiles.model.jaxb.OperationAdapter;
import com.mgmtp.perfload.loadprofiles.model.jaxb.TargetAdapter;

/**
 * @author rnaegele
 */
public class JAXBTest {

	private static final Logger LOG = LoggerFactory.getLogger(JAXBTest.class);

	@Test
	public void test() throws Exception {
		LoadProfileConfig config = new LoadProfileConfig();
		config.setName("foo");
		config.setDescription("fooDesc");

		Client cl1 = new Client();
		cl1.setName("client1");
		cl1.setDaemonId(1);
		cl1.setNumProcesses(2);
		cl1.setRelativePower(.5d);
		config.getClients().add(cl1);

		Client cl2 = new Client();
		cl2.setName("client2");
		cl2.setDaemonId(2);
		cl2.setNumProcesses(3);
		cl2.setRelativePower(.7d);
		config.getClients().add(cl2);

		Operation op1 = new Operation();
		op1.setName("op1");
		op1.setRelativeClientLoad(.5d);

		Operation op2 = new Operation();
		op2.setName("op2");
		op2.setRelativeClientLoad(.6d);

		Stairs stairs = new Stairs();
		stairs.operation = op1;
		stairs.t0 = 1;
		stairs.a = 2;
		stairs.b = 3;
		stairs.c = 4;
		stairs.h = 5;
		stairs.numSteps = 6;
		String stairsToStringExpected = String.format("Stairs[operation=%s,"
				+ "t0=1,a=2,b=3,c=4,h=5,steps=6]", op1);
		assertThat(stairs.toString(), equalTo(stairsToStringExpected));

		config.getLoadProfileEntities().add(stairs);

		Target target = new Target();
		target.setName("target1");
		target.setLoadPart(0.5d);

		config.getTargets().add(target);

		OneTime oneTime = new OneTime();
		oneTime.operation = op2;
		oneTime.targets.add(target);
		oneTime.t0 = 7;
		String oneTimeToStringExpected = String.format("OneTime[operation=%s,"
				+ "t0=7,targets=[%s]]", op2, target);
		assertThat(oneTime.toString(), equalTo(oneTimeToStringExpected));

		config.getLoadProfileEntities().add(oneTime);

		JAXBContext context = JAXBContext.newInstance(LoadProfileConfig.class);
		Marshaller m = context.createMarshaller();
		m.setAdapter(OperationAdapter.class, new OperationAdapter(asList(op1, op2)));
		m.setAdapter(ClientAdapter.class, new ClientAdapter(asList(cl1, cl2)));
		m.setAdapter(TargetAdapter.class, new TargetAdapter(asList(target)));
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		StringWriter sw = new StringWriter();
		m.marshal(config, sw);
		LOG.info("Marshalled LoadProfileConfig (as String): \n" + sw);

		Unmarshaller um = context.createUnmarshaller();
		um.setAdapter(OperationAdapter.class, new OperationAdapter(asList(op1, op2)));
		um.setAdapter(ClientAdapter.class, new ClientAdapter(asList(cl1, cl2)));
		um.setAdapter(TargetAdapter.class, new TargetAdapter(asList(target)));

		LoadProfileConfig configUnmarshalled = (LoadProfileConfig) um.unmarshal(new StringReader(sw.toString()));
		assertThat(configUnmarshalled.toString(), equalTo(config.toString()));
		LOG.info("UnMarshalled LoadProfileConfig: \n" + configUnmarshalled.toString().replaceAll(", ", ",\n\t"));
	}
}
