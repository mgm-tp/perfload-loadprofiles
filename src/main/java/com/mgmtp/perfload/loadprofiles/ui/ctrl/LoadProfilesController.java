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
package com.mgmtp.perfload.loadprofiles.ui.ctrl;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;

import java.io.File;
import java.util.Collection;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import ca.odell.glazedlists.EventList;

import com.mgmtp.perfload.loadprofiles.generation.LoadCurveCalculator;
import com.mgmtp.perfload.loadprofiles.model.Client;
import com.mgmtp.perfload.loadprofiles.model.LoadCurve;
import com.mgmtp.perfload.loadprofiles.model.LoadCurveAssignment;
import com.mgmtp.perfload.loadprofiles.model.LoadTestConfiguration;
import com.mgmtp.perfload.loadprofiles.model.Operation;
import com.mgmtp.perfload.loadprofiles.model.Target;
import com.mgmtp.perfload.loadprofiles.model.jaxb.ClientAdapter;
import com.mgmtp.perfload.loadprofiles.model.jaxb.OperationAdapter;
import com.mgmtp.perfload.loadprofiles.model.jaxb.TargetAdapter;
import com.mgmtp.perfload.loadprofiles.ui.model.LoadProfileConfig;
import com.mgmtp.perfload.loadprofiles.ui.model.LoadProfileEntity;
import com.mgmtp.perfload.loadprofiles.ui.model.OneTime;
import com.mgmtp.perfload.loadprofiles.ui.model.Settings;
import com.mgmtp.perfload.loadprofiles.ui.util.GraphPointsCalculator;
import com.mgmtp.perfload.loadprofiles.ui.util.IsOneTimePredicate;
import com.mgmtp.perfload.loadprofiles.ui.util.IsStairsPredicate;
import com.mgmtp.perfload.loadprofiles.ui.util.LoadProfileEntityToOneTimeFunction;
import com.mgmtp.perfload.loadprofiles.ui.util.LoadProfileEntityToStairsFunction;
import com.mgmtp.perfload.loadprofiles.ui.util.Point;
import java.util.List;

/**
 * @author rnaegele
 */
@Singleton
public class LoadProfilesController {

	private EventList<LoadProfileEntity> treeItems;
	private EventList<Operation> operations;
	private EventList<Target> targets;
	private EventList<Client> clients;

	/**
	 * @return the treeItems
	 */
	public EventList<LoadProfileEntity> getTreeItems() {
		return treeItems;
	}

	/**
	 * @param treeItems
	 *            the treeItems to set
	 */
	public void setTreeItems(final EventList<LoadProfileEntity> treeItems) {
		this.treeItems = treeItems;
	}

	/**
	 * @return the operations
	 */
	public EventList<Operation> getOperations() {
		return operations;
	}

	/**
	 * @param operations
	 *            the operations to set
	 */
	public void setOperations(final EventList<Operation> operations) {
		this.operations = operations;
	}

	/**
	 * @return the targets
	 */
	public EventList<Target> getTargets() {
		return targets;
	}

	/**
	 * @param targets
	 *            the targets to set
	 */
	public void setTargets(final EventList<Target> targets) {
		this.targets = targets;
	}

	/**
	 * @return the client
	 */
	public EventList<Client> getClients() {
		return clients;
	}

	/**
	 * @param clients
	 *            the client to set
	 */
	public void setClients(final EventList<Client> clients) {
		this.clients = clients;
	}

	public void addOrUpdateLoadProfileEntity(final LoadProfileEntity lpe) {
		int index = treeItems.indexOf(lpe);
		if (index >= 0) {
			// set it again, so the tree is updated
			treeItems.set(index, lpe);
		} else {
			treeItems.add(lpe);
		}
	}

	/**
	 * Resets all tree items in order to update the tree.
	 */
	public void updateTreeItems() {
		for (ListIterator<LoadProfileEntity> it = treeItems.listIterator(); it.hasNext();) {
			it.set(it.next());
		}
	}

	public void checkCurveCreationPossible() {
		checkState(!operations.isEmpty() && !targets.isEmpty() && !clients.isEmpty(),
				"Please configure operations, targets, and clients\nbefore adding curve assignments.");
	}

	public LoadProfileConfig loadProfileConfig(final File file) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(LoadProfileConfig.class);

		Unmarshaller um = context.createUnmarshaller();
		um.setAdapter(OperationAdapter.class, new OperationAdapter(operations));
		um.setAdapter(ClientAdapter.class, new ClientAdapter(clients));
		um.setAdapter(TargetAdapter.class, new TargetAdapter(targets));
		um.setEventHandler(new ValidationEventHandler() {
			@Override
			public boolean handleEvent(final ValidationEvent event) {
				throw new RuntimeException(event.getLinkedException());
			}
		});
		return (LoadProfileConfig) um.unmarshal(file);
	}

	public void saveProfileConfig(final File file, final LoadProfileConfig lpc) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(LoadProfileConfig.class);

		Marshaller m = context.createMarshaller();
		m.setAdapter(OperationAdapter.class, new OperationAdapter(operations));
		m.setAdapter(ClientAdapter.class, new ClientAdapter(clients));
		m.setAdapter(TargetAdapter.class, new TargetAdapter(targets));
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		m.marshal(lpc, file);
	}

	public LoadTestConfiguration createLoadTestConfiguration(final LoadProfileConfig lpc,
			final Collection<Target> selectedTargets, final Collection<Client> selectedClients) {
		LoadTestConfiguration ltc = new LoadTestConfiguration();
		ltc.setName(lpc.getName());
		ltc.setDescription(lpc.getDescription());
		ltc.getClients().addAll(selectedClients);
		ltc.getOperations().addAll(operations);

		GraphPointsCalculator calc = new GraphPointsCalculator();
		Map<String, List<Point>> points = calc.calculatePoints(transform(filter(treeItems, new IsStairsPredicate()),
				new LoadProfileEntityToStairsFunction()));

		for (Entry<String, List<Point>> entry : points.entrySet()) {
			String operationName = entry.getKey();
			List<Point> operationPoints = entry.getValue();

			LoadCurveAssignment loadCurveAssignment = new LoadCurveAssignment();
			loadCurveAssignment.setOperationName(operationName);
			loadCurveAssignment.getTargets().addAll(selectedTargets);

			for (Operation op : operations) {
				if (op.getName().equals(operationName)) {
					loadCurveAssignment.setOperation(op);
					break;
				}
			}
			ltc.getLoadCurveAssignments().add(loadCurveAssignment);

			LoadCurve lc = new LoadCurve();
			lc.setTimeUnit(LoadCurveCalculator.timeUnit_minute);
			lc.setRateUnit(LoadCurveCalculator.rateUnit_perHour);
			lc.setName(operationName);
			loadCurveAssignment.setLoadCurveName(operationName);

			int size = operationPoints.size();
			double[] timeValues = new double[size];
			double[] rateValues = new double[size];
			int j = 0;
			for (Point point : operationPoints) {
				timeValues[j] = point.getX();
				rateValues[j] = point.getY();
				++j;
			}

			lc.setTimeValues(timeValues);
			lc.setRateValues(rateValues);

			loadCurveAssignment.setLoadCurve(lc);
		}

		return ltc;
	}

	/**
	 * @param lpc
	 *            the config to be validated
	 */
	public void validateProfileConfig(final LoadProfileConfig lpc, final Settings settings) {
		checkState(settings.getTargets().containsAll(lpc.getTargets()),
				"Load profile configuration contains non-existent targets.\nPlease check the current settings.");
		checkState(settings.getClients().containsAll(lpc.getClients()),
				"Load profile configuration contains non-existent clients.\nPlease check the current settings.");

		Collection<OneTime> oneTimes = transform(filter(lpc.getLoadProfileEntities(), new IsOneTimePredicate()),
				new LoadProfileEntityToOneTimeFunction());
		for (OneTime oneTime : oneTimes) {
			checkState(settings.getTargets().containsAll(oneTime.targets),
					"Load profile configuration contains non-existent targets.\nPlease check the current settings.");
		}
	}
}