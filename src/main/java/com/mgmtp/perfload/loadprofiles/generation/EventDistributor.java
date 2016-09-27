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
package com.mgmtp.perfload.loadprofiles.generation;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.loadprofiles.model.BaseLoadProfileEvent;
import com.mgmtp.perfload.loadprofiles.model.Client;
import com.mgmtp.perfload.loadprofiles.model.LoadCurve;
import com.mgmtp.perfload.loadprofiles.model.LoadCurveAssignment;
import com.mgmtp.perfload.loadprofiles.model.LoadEvent;
import com.mgmtp.perfload.loadprofiles.model.LoadEventComparator;
import com.mgmtp.perfload.loadprofiles.model.LoadTestConfiguration;
import com.mgmtp.perfload.loadprofiles.model.MarkerEvent;
import com.mgmtp.perfload.loadprofiles.model.Operation;
import com.mgmtp.perfload.loadprofiles.model.Target;

/**
 * Distribute loadtest events to clients balancing the load on each client according to the relative
 * power of the given clients and taking the relative load of each operation (caused on the client
 * by this operation) into account.
 * 
 * @author mvarendo
 */
public class EventDistributor {
	private static final Logger log = LoggerFactory.getLogger(EventDistributor.class);

	private static final String eventSeparator = ";";

	/**
	 * Distributes events from different operations with different client loads to Clients with
	 * different performance. The events from the given list of events, which must be sorted
	 * according to time (not verified in this method) are distributed onto processes within daemons
	 * on clients. All processes of all daemons of one client should get the same load The daemon is
	 * only introduced, since it exists in the configuration of perfload It is not necessary to
	 * balance the distribution of events between daemons. The load of all processes of one client
	 * and the total load per client is balanced by this algorithm. While looping over all events,
	 * the algorithm derives for each event which client has up to now the lowest total load,
	 * assigns the client load of the operation of this event to the client and then continues with
	 * the next event. The algorithm for the distribution on processes is analogous. This algorithn
	 * takes only the time sequence of the events into account, the time difference between the
	 * events is not considered. To correct for this, not the total load per client up to the time
	 * of the event to be distributed has to be taken into account, but the load distributed over
	 * time, taking into account, that after a certain time, the 'current load' of a client might be
	 * 0. I have no idea yet how to do this.
	 * 
	 * @param allEvents
	 *            ArrayList of all load events
	 * @return Lists of Events. For each event the client, daemon and process is now defined.
	 */
	private static List<LoadEvent> distributeEvents(final List<LoadEvent> allEvents,
			final LoadTestConfiguration loadTestConfiguration, final double totalWeightedNevent) {

		List<Client> clients = loadTestConfiguration.getClients();
		int numClients = clients.size();
		List<Operation> operations = loadTestConfiguration.getOperations();
		double[] relativeClientPower = getRelativeClientPower(loadTestConfiguration);

		// distribute events to processes and daemons of all clients
		ArrayList<LoadEvent> clientEventList = newArrayList();
		double[][] cumulatedProcessLoad = new double[numClients][];
		double[][] deficitProcessLoad = new double[numClients][];
		int[] processesPerClient = new int[numClients];

		for (int iClient = 0; iClient < numClients; iClient++) {
			processesPerClient[iClient] = 1 * clients.get(iClient).getNumProcesses();
			cumulatedProcessLoad[iClient] = new double[processesPerClient[iClient]];
			deficitProcessLoad[iClient] = new double[processesPerClient[iClient]];
		}
		double cumulatedClientLoad[] = new double[numClients];
		double desiredClientLoad[] = new double[numClients];
		double deficitClientLoad[] = new double[numClients];

		// log input parameter
		log.info("totalWeightedNevent = " + totalWeightedNevent);
		log.info("nClient = " + numClients);
		for (Client client : clients) {
			log.info("daemonId = " + client.getDaemonId() + " , nProcess = " + client.getNumProcesses()
					+ ", relativeClientPower = " + client.getRelativePower());
		}
		for (Operation operation : operations) {
			log.info("operation " + operation.getName() + " relativeClientLoad = " + operation.getRelativeClientLoad());
		}

		if (log.isDebugEnabled()) {
			log.debug("weight; cumulatedWeightedNevent;");
			for (int iClient = 0; iClient < numClients; iClient++) {
				log.debug("targetClientLoad[" + iClient + "]; ");
			}
			for (int iClient = 0; iClient < numClients; iClient++) {
				log.debug("deficitClientLoad[" + iClient + "]; ");
			}
			log.debug("iClientMax; OperationType; StartTime");
		}

		double cumulatedWeightedNevent = 0.;
		for (LoadEvent event : allEvents) {
			double weight = event.getOperation().getRelativeClientLoad();
			cumulatedWeightedNevent += weight;
			for (int iClient = 0; iClient < numClients; iClient++) {
				desiredClientLoad[iClient] = cumulatedWeightedNevent * relativeClientPower[iClient];
				deficitClientLoad[iClient] = desiredClientLoad[iClient] - cumulatedClientLoad[iClient];
			}

			int iClientMax = findHighestDeficit(deficitClientLoad);
			cumulatedClientLoad[iClientMax] += weight;
			double desiredProcessLoad = desiredClientLoad[iClientMax] / processesPerClient[iClientMax];
			for (int iProcess = 0; iProcess < processesPerClient[iClientMax]; iProcess++) {
				deficitProcessLoad[iClientMax][iProcess] = desiredProcessLoad - cumulatedProcessLoad[iClientMax][iProcess];
			}
			int iProcessMax = findHighestDeficit(deficitProcessLoad[iClientMax]);
			cumulatedProcessLoad[iClientMax][iProcessMax] += weight;

			event.setClientId(iClientMax);
			event.setProcessId(iProcessMax);
			event.setDaemonId(clients.get(iClientMax).getDaemonId());
			clientEventList.add(event);

			if (log.isDebugEnabled()) {
				log.debug(weight + "; " + cumulatedWeightedNevent + "; ");
				for (int iClient = 0; iClient < numClients; iClient++) {
					log.debug(desiredClientLoad[iClient] + "; ");
				}
				for (int iClient = 0; iClient < numClients; iClient++) {
					log.debug(deficitClientLoad[iClient] + "; ");
				}
				log.debug(iClientMax + "; " + event.getOperation().getName() + "; " + event.getTime());
			}
		}

		// numeric check
		double numericRelativeDifference = (cumulatedWeightedNevent - totalWeightedNevent) / totalWeightedNevent;
		log.info("Numeric relative difference cumulatedWeightedNevent - totalWeightedNevent " + numericRelativeDifference);

		// check residua for clients
		for (int iClient = 0; iClient < numClients; iClient++) {
			deficitClientLoad[iClient] = desiredClientLoad[iClient] - cumulatedClientLoad[iClient];
			log.info("Residuum Client " + iClient + ": " + deficitClientLoad[iClient]);
		}

		return clientEventList;
	}

	/**
	 * Create events according to the given load curve assignment and distribute the events onto the
	 * targets according to the given partition between targets. The distribution algorithm is
	 * analogous to the distribution of events to clients described in the method distributeEvents()
	 * in this class. The shift value is used to shift each event within its time interval. The time
	 * interval is the time for which the integral over the load curve increases from the last
	 * integer number of events to the next integer number of events. The value indicates the
	 * relative shift within this interval. A value of 0.5 puts the event at the time, where the
	 * integral value is equal to the last integer number plus 0.5. The shifting is used to be able
	 * to smooth out the distribution of events on small time scales. If the load of an operation
	 * following a load curve is created separately for parts of the load, then it is possible to
	 * shift the start time of each event to smooth out the distribution. Creating events for half
	 * the load of a load curve with a shift value of 0. and another list of events with a shift
	 * value of 0.5 and combining them is equal to creating a list of events with the full load and
	 * a shift value of 0.
	 * 
	 * @param loadCurveAssignment
	 *            The assignment of a load curve to targets
	 * @param shift
	 *            The shift of all events for the given assignment
	 * @return list of load events
	 */
	private static ArrayList<LoadEvent> createEvents(final LoadCurveAssignment loadCurveAssignment, final double shift) {

		LoadCurve loadCurve = loadCurveAssignment.getLoadCurve();
		List<Target> targets = loadCurveAssignment.getTargets();
		Operation operation = loadCurveAssignment.getOperation();

		String targetList = "";
		for (Target target : targets) {
			targetList += target.getName() + ", ";
		}
		log.info("Creating events according to loadCurve " + loadCurve.getName() +
				"\n\tof operation " + operation.getName() +
				"\n\tfor targets " + targetList);
		loadCurve.dump(log);

		int nEvents = (int) loadCurve.getNEvents();
		ArrayList<LoadEvent> events = new ArrayList<LoadEvent>(nEvents);

		// calculate events from load curve in given time interval
                double lastTn = 0.;
		for (int iEvent = 0; iEvent < nEvents; iEvent++) {
			double eventIndex = iEvent + shift;
			double Tn = LoadCurveCalculator.deriveStartTime(loadCurve, eventIndex);
			LoadEvent event = new LoadEvent(Tn, operation);
			events.add(iEvent, event);
                        if (iEvent > 0) {
                            if (Tn < lastTn) {
                                log.error("Event in wrong sequence iEvent"+iEvent+", lastTn "+lastTn+", Tn "+Tn);
                            }
                        }
                        lastTn = Tn;     
		}

		// assign events to targets according to load part for each targets
		double[] desiredTargetLoad = new double[targets.size()];
		double[] cumulatedTargetLoad = new double[targets.size()];
		double[] deficitTargetLoad = new double[targets.size()];
		double cumulatedNevent = 0.;
		for (int iEvent = 0; iEvent < events.size(); iEvent++) {
			LoadEvent event = events.get(iEvent);
			cumulatedNevent += 1.;
			for (int iTarget = 0; iTarget < targets.size(); iTarget++) {
				desiredTargetLoad[iTarget] = cumulatedNevent * targets.get(iTarget).getLoadPart();
				deficitTargetLoad[iTarget] = desiredTargetLoad[iTarget] - cumulatedTargetLoad[iTarget];
			}
			int iTargetMax = findHighestDeficit(deficitTargetLoad);
			cumulatedTargetLoad[iTargetMax] += 1.;
			event.setTarget(targets.get(iTargetMax));
		}

		return events;
	}

	/**
	 * Create a list of events containing events for each client according to the given load test
	 * configuration. The events are first created for each load curve assignment and then merged to
	 * one event list sorted by time. Then the events are distributed to clients (and daemons and
	 * processes). The algorithm for the distribution on clients and targets is described in the
	 * methods performing the task. The resulting event list contains events, which are sorted by
	 * time and contain defined values for the targets (server), the operation and the client (and
	 * daemon and process), which executes this operation against the given targets. The serialized
	 * form (.csv-file) of this list is used as an input file for the perfload clients.
	 * 
	 * @param loadTestConfiguration
	 *            configuration data of the load test
	 */
	public static List<LoadEvent> createClientEventList(final LoadTestConfiguration loadTestConfiguration) {

		verifyArguments(loadTestConfiguration);

		// convert load curve units to hours if necessary
		List<LoadCurveAssignment> loadCurveAssignments = loadTestConfiguration.getLoadCurveAssignments();
		for (LoadCurveAssignment loadCurveAssignment : loadCurveAssignments) {
			LoadCurve loadCurve = loadCurveAssignment.getLoadCurve();
			if (!LoadCurveCalculator.timeUnit_hour.equals(loadCurve.getTimeUnit())) {
				LoadCurveCalculator.transformToHours(loadCurve);
			}
		}

		int numAssignments = loadCurveAssignments.size();
		// derive relative shift for each assignment. The relative shifts are evenly distributed
		// from 0 to 1. The assignment with the highest client load gets the shift nearest to 0.5,
		// then the others are distributed around this value going from the highest client load to lowest.
		// This avoids that all clients start simultaneously.
		double shiftValues[] = new double[numAssignments];
		// implement the sorting here!
		log.warn("Sorting by relative client load for optimisation of shiftValues not yet implemented!");
		for (int iAssignment = 0; iAssignment < numAssignments; iAssignment++) {
			shiftValues[iAssignment] = (iAssignment + 0.5) / numAssignments;
		}

		// derive for all assignments an event list
		List<LoadEvent> allAssignmentsEventList = new ArrayList<LoadEvent>();

		for (int iAssignment = 0; iAssignment < numAssignments; iAssignment++) {
			LoadCurveAssignment loadCurveAssignment = loadCurveAssignments.get(iAssignment);
			allAssignmentsEventList.addAll(createEvents(loadCurveAssignment, shiftValues[iAssignment]));
		}

		Collections.sort(allAssignmentsEventList, new LoadEventComparator());

		// for numeric check
		double totalWeightedNevent = 0.;
		for (LoadCurveAssignment loadCurveAssignment : loadCurveAssignments) {
			LoadCurve loadCurve = loadCurveAssignment.getLoadCurve();
			totalWeightedNevent += loadCurve.getNEvents() * loadCurveAssignment.getOperation().getRelativeClientLoad();
		}

		//		double relativeOperationClientLoads[] = new double[numAssignments];
		//		for (int iAssignment = 0; iAssignment < numAssignments; iAssignment++) {
		//			LoadCurveAssignment loadCurveAssignment = loadCurveAssignments.get(iAssignment);
		//			relativeOperationClientLoads[iAssignment] = loadCurveAssignment.getOperation().getRelativeClientLoad();
		//		}
		List<LoadEvent> clientEventList = distributeEvents(allAssignmentsEventList, loadTestConfiguration, totalWeightedNevent);

		return clientEventList;
	}

	/**
	 * Write the List of load test events for a perfLoadClient The list contains following values: -
	 * start time of the operation in milliseconds since start of the load test - name of the
	 * operation (i.e. UStVA) - name of the host on which this operation is triggered - port of the
	 * host over which the operation is triggered - number of daemon executing this test
	 * (=identifier of operation) - number of the process of the daemon (here always 1) The format
	 * of the file is one load test event per line, arguments separated by the given eventSeparator.
	 * 
	 * @param file
	 *            The events file
	 * @param eventList
	 *            List of load test events
	 */
	public static void writeEventListForPerfLoadClientsToFile(final File file, final String headerLines,
			final List<? extends BaseLoadProfileEvent> eventList)
			throws IOException {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(file, "UTF-8");
			pw.println(headerLines);

			for (BaseLoadProfileEvent event : eventList) {
				if (event instanceof LoadEvent) {
					LoadEvent loadEvent = (LoadEvent) event;
					// convert time from hours to milliseconds
					long t = Math.round(loadEvent.getTime() * 60. * 60. * 1000.);
					String operation = loadEvent.getOperation().getName();
					String target = loadEvent.getTarget().getName();
					int daemonId = loadEvent.getDaemonId();
					int processId = loadEvent.getProcessId() + 1;
					pw.println(t + eventSeparator +
							operation + eventSeparator +
							target + eventSeparator +
							daemonId + eventSeparator +
							processId);
				} else if (event instanceof MarkerEvent) {
					MarkerEvent marker = (MarkerEvent) event;
					long t = Math.round(marker.getTime() * 60. * 60. * 1000.);
					pw.println(t + eventSeparator +
							"[[marker]]" + eventSeparator +
							marker.getName() + eventSeparator +
							marker.getType() + eventSeparator);
				}
			}
		} finally {
			IOUtils.closeQuietly(pw);
		}
	}

	// search for the maximum (currently the first found maximum is taken, a different
	// distribution algorithm might be useful
	private static int findHighestDeficit(final double deficitLoad[]) {
		double deficitMax = deficitLoad[0];
		int iMax = 0;
		for (int i = 1; i < deficitLoad.length; i++) {
			if (deficitLoad[i] > deficitMax) {
				deficitMax = deficitLoad[i];
				iMax = i;
			}
		}
		return iMax;
	}

	/**
	 * verifies the validity of the given arguments. Throws an IllegalArgumentException, if an
	 * argument is invalid. For load curves it is checked, that the array and its elements are not
	 * null. Additionally it is verified, that the time values of the points in the load curve are
	 * increasing.
	 */
	public static void verifyArguments(final LoadTestConfiguration loadTestConfiguration) {
		for (LoadCurveAssignment assignment : loadTestConfiguration.getLoadCurveAssignments()) {
			LoadCurve loadCurve = assignment.getLoadCurve();
			if (loadCurve == null) {
				throw new java.lang.IllegalArgumentException("load curve of assignment " + assignment + " is null");
			}
			double[] timeValues = loadCurve.getTimeValues();
			if (timeValues == null) {
				throw new java.lang.IllegalArgumentException("time values of load curve with name " + loadCurve.getName()
						+ " is null");
			}
			int nPoint = timeValues.length;
                        log.info("Verifying assignement "+assignment.getLoadCurveName()+":"+assignment.getOperationName()+" with "+nPoint+" points");
			for (int iPoint = 1; iPoint < nPoint; iPoint++) {
                            log.info("Time Value["+iPoint+"] = "+timeValues[iPoint]);
				if (timeValues[iPoint] < timeValues[iPoint - 1]) {
					throw new java.lang.IllegalArgumentException("In loadCurve of assignment " + assignment +
							"], timeValues[" + Integer.toString(iPoint - 1) + "] >= timeValues[" + iPoint +
							"], time value of lower point must be smaller than time value of upper point.");
				}
			}
		}
	}

	/**
	 * Get the relative client power of all clients in the load test configuration as an array of
	 * doubles. The sequence of the values in the array is according to their occurence in the
	 * clients definition of the load test configuration.
	 * 
	 * @param loadTestConfiguration
	 *            The load test configuration, for which the relative client power are returned
	 * @return Array of realtive client powers
	 */
	public static double[] getRelativeClientPower(final LoadTestConfiguration loadTestConfiguration) {
		List<Client> clients = loadTestConfiguration.getClients();
		int size = clients.size();
		double[] relativeClientPower = new double[size];
		for (int i = 0; i < size; i++) {
			relativeClientPower[i] = clients.get(i).getRelativePower();
		}
		return relativeClientPower;
	}

	/**
	 * In assignments of a load test configuration load curves are referenced by name. In this
	 * method the given load curves are scaled with the given scaling factor of the assignment and
	 * then added as a direct reference. Since load curves can be used in more than one assignment,
	 * load curves are cloned before they are scaled.
	 * 
	 * @param loadTestConfiguration
	 *            The load test configuration to which scaled load curves are be added
	 * @param loadCurves
	 *            The load curves to be added to the assignments
	 */
	public static void addScaledLoadCurvesToAssignments(final LoadTestConfiguration loadTestConfiguration,
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
					LoadCurve scaledLoadCurve = LoadCurveCalculator.scaleLoadCurve(clonedLoadCurve,
							loadCurveAssignment.getLoadCurveScaling());
					loadCurveAssignment.setLoadCurve(scaledLoadCurve);
					found = true;
					break;
				}
			}

			checkState(found, "No matching load curve found for load curve: " + loadCurveName);
		}
	}
}
