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

package com.mgmtp.perfload.loadprofiles.generation;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.mgmtp.perfload.loadprofiles.model.Client;
import com.mgmtp.perfload.loadprofiles.model.LoadCurve;
import com.mgmtp.perfload.loadprofiles.model.LoadCurveAssignment;
import com.mgmtp.perfload.loadprofiles.model.LoadEvent;
import com.mgmtp.perfload.loadprofiles.model.LoadTestConfiguration;
import com.mgmtp.perfload.loadprofiles.model.Operation;
import com.mgmtp.perfload.loadprofiles.model.Target;
import com.mgmtp.perfload.loadprofiles.util.PlotFileCreator;

/**
 * Test the generation of load events for a load test defined by a load test configuration.
 * 
 * @author mvarendo
 */
public class EventDistributorTest {
	private static final Logger log = LoggerFactory.getLogger(EventDistributorTest.class);

	@Test
	public void testEventGenerationFromLoadCurveAndConfiguration() throws IOException {
		String loadCurveName1 = "Treppe";
		String loadCurveName2 = "Treppe";
		LoadTestConfiguration loadTestConfiguration = createLoadTestConfiguration(loadCurveName1, loadCurveName2);
		double stepWidth = 1.;
		double stepHeight = 0.25;
		int nStep = 4;
		LoadCurve loadCurve1 = generateTiltedStair(loadCurveName1, stepWidth, stepHeight, nStep);
		/*
		 * double startUpTime = 1.; double levelWidth = 7.; double shutdownTime = 1.; double
		 * levelHeight = 1.; LoadCurve loadCurve2 = generateOneLevel(loadCurveName2, startUpTime,
		 * levelWidth, shutdownTime, levelHeight);
		 */
		LoadCurve loadCurve2 = generateTiltedStair(loadCurveName2, stepWidth, stepHeight, nStep);
		List<LoadCurve> loadCurves = newArrayList(loadCurve1, loadCurve2);

		addScaledLoadCurvesToAssignments(loadTestConfiguration, loadCurves);

		List<Operation> operations = loadTestConfiguration.getOperations();
		int nOperation = operations.size();

		int nClient = loadTestConfiguration.getClients().size();

		// create and distribute the events
		List<LoadEvent> clientEventList = EventDistributor.createClientEventList(loadTestConfiguration);

		// plot the load curves
		for (int iOperation = 0; iOperation < nOperation; iOperation++) {
			PlotFileCreator.createLoadCurvePlot(createPlotFile(loadCurves.get(iOperation).getName()), loadCurves.get(iOperation));
		}

		// plot the event distributions for all clients
		PlotFileCreator.createPlot(createPlotFile("EventDistribution_AllClients"), clientEventList,
				loadTestConfiguration.getLoadCurveAssignments(), nClient, LoadCurveCalculator.timeUnit_minute);

		for (int iClient = 0; iClient < nClient; iClient++) {
			PlotFileCreator.createPlot(createPlotFile("EventDistribution_Client" + iClient), clientEventList,
					loadCurves.get(iClient), iClient, LoadCurveCalculator.timeUnit_minute);
		}

		// create Histograms and plots for all operations. The result should
		// follow the given load curve of the operation.
		for (int iOperation = 0; iOperation < nOperation; iOperation++) {
			PlotFileCreator.createOperationHistogram(createPlotFile("EventDistributionHistogram_"
					+ loadCurves.get(iOperation).getName()), clientEventList,
					loadCurves.get(iOperation).getName(), 19, 0., 9.5, LoadCurveCalculator.timeUnit_minute);
		}

		// create histograms and plots for all clients to verify the distribution on clients
		for (int iClient = 0; iClient < nClient; iClient++) {
			String clientId = Integer.toString(iClient);
			PlotFileCreator.createClientHistogram(createPlotFile("EventDistributionHistogram_Client_" + clientId),
					clientEventList, iClient, 19, 0., 9.5, LoadCurveCalculator.timeUnit_minute);
		}

		// create the result List
		EventDistributor
				.writeEventListForPerfLoadClientsToFile(createEventListFile("EventDistributorTest"),
						"# EventDistributorTest " + new Date(), clientEventList);

	}

	private void addScaledLoadCurvesToAssignments(final LoadTestConfiguration loadTestConfiguration,
			final List<LoadCurve> loadCurves) {
		for (LoadCurve loadCurve : loadCurves) {
			LoadCurveCalculator.transformToHours(loadCurve);
		}

		List<LoadCurveAssignment> loadCurveAssignments = loadTestConfiguration.getLoadCurveAssignments();
		for (LoadCurveAssignment loadCurveAssignment : loadCurveAssignments) {
			String loadCurveName = loadCurveAssignment.getLoadCurveName();
			boolean found = false;
			for (LoadCurve loadCurve : loadCurves) {
				if (loadCurve.getName().equalsIgnoreCase(loadCurveName)) {
					LoadCurve clonedLoadCurve = loadCurve.clone();
					LoadCurve scaledLoadCurve = LoadCurveCalculator.scaleLoadCurve(
							clonedLoadCurve, loadCurveAssignment.getLoadCurveScaling());
					loadCurveAssignment.setLoadCurve(scaledLoadCurve);
					found = true;
					break;
				}
			}
			checkState(found, "No matching load curve found for load curve: " + loadCurveName);
		}
	}

	/**
	 * Test the distribution of events of different types with different client loads to clients
	 * with different power. To verify, that the distribution mechanism is OK, histograms of the
	 * results together with plot data (plottable by Excel) are created.
	 */
	@Test
	public void testCreateClientEventLists() throws IOException {
		String name0 = "Registration";
		String name1 = "Get a product";
		LoadTestConfiguration loadTestConfiguration = createLoadTestConfiguration(name0, name1);

		List<LoadCurve> loadCurves = generateLoadCurves(name0, name1);
		EventDistributor.addScaledLoadCurvesToAssignments(loadTestConfiguration, loadCurves);

		List<Operation> operations = loadTestConfiguration.getOperations();
		int nOperation = operations.size();

		List<Client> clients = loadTestConfiguration.getClients();
		int nClient = clients.size();

		// distribute the events
		List<LoadEvent> clientEventList = EventDistributor.createClientEventList(loadTestConfiguration);

		// plot the load curves
		for (int iOperation = 0; iOperation < nOperation; iOperation++) {
			PlotFileCreator.createLoadCurvePlot(createPlotFile(loadCurves.get(iOperation).getName()), loadCurves.get(iOperation));
		}

		// plot the event distributions for all clients
		PlotFileCreator.createPlot(createEventListFile("EventDistribution_AllClients"), clientEventList,
				loadTestConfiguration.getLoadCurveAssignments(), nClient, LoadCurveCalculator.timeUnit_minute);

		for (int iClient = 0; iClient < nClient; iClient++) {
			PlotFileCreator.createPlot(createEventListFile("EventDistribution_Client" + iClient), clientEventList,
					loadCurves.get(iClient), iClient, LoadCurveCalculator.timeUnit_minute);
		}

		// create Histograms and plots for all operations. The result should
		// follow the given load curve of the operation.
		for (int iOperation = 0; iOperation < nOperation; iOperation++) {
			PlotFileCreator.createOperationHistogram(
					createEventListFile("EventDistributionHistogram_" + loadCurves.get(iOperation).getName()),
					clientEventList, operations.get(iOperation).getName(), 19, 0., 9.5, LoadCurveCalculator.timeUnit_minute);
		}

		// create histograms and plots for all clients to verify the distribution on clients
		for (int iClient = 0; iClient < nClient; iClient++) {
			String clientId = Integer.toString(iClient);
			PlotFileCreator.createClientHistogram(createEventListFile("EventDistributionHistogram_ClientLoadForClient_"
					+ clientId), clientEventList, iClient, 19, 0., 9.5, LoadCurveCalculator.timeUnit_minute);
		}

		// create the result List
		EventDistributor.writeEventListForPerfLoadClientsToFile(createEventListFile("EventDistributorTest"), "foo",
				clientEventList);
	}

	private File createPlotFile(final String name) {
		File file = new File("data/plots", "Plot_" + name + ".csv");
		file.getParentFile().mkdirs();
		return file;
	}

	private File createEventListFile(final String name) {
		File file = new File("data/eventlists", "EventList_" + name + ".perfload");
		file.getParentFile().mkdirs();
		return file;
	}

	private LoadTestConfiguration createLoadTestConfiguration(final String loadCurveName1, final String loadCurveName2) {
		LoadTestConfiguration loadTestConfiguration = new LoadTestConfiguration();
		loadTestConfiguration.setName("EventDistributorTest");

		String operationName1 = "LStB";
		String operationName2 = "UStVA";
		Client client1 = new Client();
		client1.setDaemonId(1);
		client1.setNumProcesses(5);
		client1.setRelativePower(0.5);
		loadTestConfiguration.getClients().add(client1);

		Client client2 = new Client();
		client2.setDaemonId(2);
		client2.setNumProcesses(5);
		client2.setRelativePower(0.5);
		loadTestConfiguration.getClients().add(client2);

		Operation operation1 = new Operation();
		operation1.setName(operationName1);
		operation1.setRelativeClientLoad(3.);
		loadTestConfiguration.getOperations().add(operation1);

		Operation operation2 = new Operation();
		operation2.setName(operationName2);
		operation2.setRelativeClientLoad(1.);
		loadTestConfiguration.getOperations().add(operation2);

		Target target1 = new Target();
		target1.setName("elsterltas01");
		target1.setLoadPart(0.5);

		Target target2 = new Target();
		target2.setName("elsterltas02");
		target2.setLoadPart(0.5);

		double[] loadPartForTargets = new double[2];
		loadPartForTargets[0] = 0.5;
		loadPartForTargets[1] = 0.5;

		LoadCurveAssignment loadCurveAssignement1 = new LoadCurveAssignment();
		loadCurveAssignement1.setOperationName(operationName1);
		loadCurveAssignement1.setLoadCurveName(loadCurveName1);
		loadCurveAssignement1.setLoadCurveScaling(500.);
		loadCurveAssignement1.getTargets().add(target1);
		loadCurveAssignement1.getTargets().add(target2);

		LoadCurveAssignment loadCurveAssignement2 = new LoadCurveAssignment();
		loadCurveAssignement2.setOperationName(operationName2);
		loadCurveAssignement2.setLoadCurveName(loadCurveName2);
		loadCurveAssignement2.setLoadCurveScaling(1000.);
		loadCurveAssignement2.getTargets().add(target1);
		loadCurveAssignement2.getTargets().add(target2);

		loadTestConfiguration.getLoadCurveAssignments().add(loadCurveAssignement1);
		loadTestConfiguration.getLoadCurveAssignments().add(loadCurveAssignement2);

		loadTestConfiguration.setDescription("Gemischter Test: " + operationName1 + " (" + loadCurveName1 +
				") and " + operationName2 + " (" + loadCurveName2 + ") mit Treppenlastkurve 2 Clients auf zwei Server.");

		List<Operation> operations = loadTestConfiguration.getOperations();
		List<LoadCurveAssignment> loadCurveAssignments = loadTestConfiguration.getLoadCurveAssignments();
		for (LoadCurveAssignment loadCurveAssignment : loadCurveAssignments) {
			String operationName = loadCurveAssignment.getOperationName();
			boolean found = false;
			for (Operation operation : operations) {
				if (operation.getName().equalsIgnoreCase(operationName)) {
					loadCurveAssignment.setOperation(operation);
					found = true;
					break;
				}
			}
			checkState(found, "No matching operation found for operation: " + operationName);
		}

		return loadTestConfiguration;
	}

	/**
	 * Generate a stair like load curve, which rises in tilted steps to a maximum value and then the
	 * decreases to 0. The form of the first step is (0.,0.), (stepWidth,stepHeight),
	 * (stepWidth+stepWidth, stepHeight) The next step has the same form and starts at the end of
	 * the first step. After the last step, the curve continues to ( (2*nStep + 1)*stepWidth, 0.)
	 * 
	 * @param name
	 *            Name of the load curve
	 * @param stepWidth
	 *            width of the increasing part of the step and of the flat part of the step. the
	 *            total length of one step is 2 times this value.
	 * @param stepHeight
	 *            The height of one step
	 * @param nStep
	 *            The number of steps.
	 * @return the generated one level load curve according to the given parameters
	 */
	private LoadCurve generateTiltedStair(final String name, final double stepWidth, final double stepHeight, final int nStep) {

		int nPoints = 2 * nStep + 2;
		double[] timeValues = new double[nPoints];
		double[] rateValues = new double[nPoints];
		for (int iPoint = 0; iPoint < nPoints; iPoint++) {
			timeValues[iPoint] = iPoint * stepWidth;
			if (iPoint == 0) {
				rateValues[iPoint] = 0.;
			} else if (iPoint == nPoints - 1) {
				rateValues[iPoint] = 0.;
			} else {
				int iLevel = (iPoint + 1) / 2;
				rateValues[iPoint] = iLevel * stepHeight;
			}
		}
		LoadCurve loadCurve = new LoadCurve();
		loadCurve.setTimeValues(timeValues);
		loadCurve.setRateValues(rateValues);
		loadCurve.setName(name);
		loadCurve.setTimeUnit(LoadCurveCalculator.timeUnit_minute);
		loadCurve.setRateUnit(LoadCurveCalculator.rateUnit_perHour);
		LoadCurveCalculator.fillStatisticsAndNormValuesOfLoadCurve(loadCurve);
		log.info("Number of events for load curve " + loadCurve.getName() + " is " + loadCurve.getNEvents());
		return loadCurve;
	}

	/**
	 * generates a load curve, which is used in the tests. Don't change the values of the load curve
	 * without changing the expected values in the different test methods.
	 */
	private List<LoadCurve> generateLoadCurves(final String name0, final String name1) {
		double[] timeValues0 = { 0., 0.5, 1.5, 2., 3., 3.5 };
		double[] rateValues0 = { 0., 30., 70., 90., 90., 0. };
		for (int i = 0; i < rateValues0.length; i++) {
			rateValues0[i] /= 90.;
		}
		double[] timeValues1 = { 0., 1.0, 2.0, 2.5, 3., 3.5 };
		double[] rateValues1 = { 0., 100., 100., 200., 200., 0. };
		for (int i = 0; i < rateValues1.length; i++) {
			rateValues0[i] /= 200.;
		}
		List<LoadCurve> loadCurves = newArrayListWithCapacity(2);

		LoadCurve loadCurve0 = new LoadCurve();
		loadCurve0.setTimeValues(timeValues0);
		loadCurve0.setRateValues(rateValues0);
		loadCurve0.setName(name0);
		loadCurve0.setTimeUnit(LoadCurveCalculator.timeUnit_minute);
		loadCurve0.setRateUnit(LoadCurveCalculator.rateUnit_perHour);
		loadCurves.add(loadCurve0);

		LoadCurve loadCurve1 = new LoadCurve();
		loadCurve1.setTimeValues(timeValues1);
		loadCurve1.setRateValues(rateValues1);
		loadCurve1.setName(name1);
		loadCurve1.setTimeUnit(LoadCurveCalculator.timeUnit_minute);
		loadCurve1.setRateUnit(LoadCurveCalculator.rateUnit_perHour);
		loadCurves.add(loadCurve1);

		LoadCurveCalculator.fillStatisticsAndNormValuesOfLoadCurve(loadCurve0);
		log.info("Number of events for load curve " + loadCurve0.getName() + " is " + loadCurve0.getNEvents());
		LoadCurveCalculator.fillStatisticsAndNormValuesOfLoadCurve(loadCurve1);
		log.info("Number of events for load curve " + loadCurve1.getName() + " is " + loadCurve1.getNEvents());
		return loadCurves;
	}
}
