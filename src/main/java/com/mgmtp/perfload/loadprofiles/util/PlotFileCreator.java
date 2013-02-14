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
package com.mgmtp.perfload.loadprofiles.util;

import static org.apache.commons.io.FileUtils.forceMkdir;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.loadprofiles.generation.LoadCurveCalculator;
import com.mgmtp.perfload.loadprofiles.model.LoadCurve;
import com.mgmtp.perfload.loadprofiles.model.LoadCurveAssignment;
import com.mgmtp.perfload.loadprofiles.model.LoadEvent;

/**
 * @author mvarendo
 */
public class PlotFileCreator {
	private static final Logger log = LoggerFactory.getLogger(PlotFileCreator.class);

	/**
	 * Create a time-rate-histogram of events of the given operation. The histograms are usually
	 * used for diagnostic purposes.
	 * 
	 * @param file
	 *            The plot file
	 * @param eventList
	 *            List of events from which the histogram is derived after applying a filter on the
	 *            operation
	 * @param operationName
	 *            Name of the operation, used as filter to use only events in the histogram, which
	 *            have this operation assigned.
	 * @param nBin
	 *            Number of bins of the resulting histogram.
	 * @param xLow
	 *            time value of the left boundary of the lowest bin of the resulting histogram
	 * @param xUp
	 *            time value of the right boundary of the highest bin of the resulting histogram
	 * @param timeUnitPlot
	 *            The time unit of the resulting plot
	 */
	public static void createOperationHistogram(final File file, final Collection<LoadEvent> eventList,
			final String operationName, final int nBin, final double xLow, final double xUp, final String timeUnitPlot)
			throws IOException {
		double timeScalingFactor = LoadCurveCalculator.getTimeScalingFactor(LoadCurveCalculator.timeUnit_hour, timeUnitPlot);

		double[] xhistoLow = new double[nBin];
		double[] xhistoUp = new double[nBin];
		int[] yhisto = new int[nBin];

		double delta = nBin / (xUp - xLow);
		for (int iBin = 0; iBin < nBin; iBin++) {
			xhistoLow[iBin] = iBin / delta + xLow;
			xhistoUp[iBin] = (iBin + 1) / delta + xLow;
		}
		for (LoadEvent event : eventList) {
			if (event.getOperation().getName().equals(operationName)) {
				double time = timeScalingFactor * event.getTime();
				int xBin = (int) Math.floor((time - xLow) * delta);
				if (xBin >= 0 && xBin < nBin) {
					yhisto[xBin]++;
				} else {
					log.warn("Value " + time + " outside range [" + xLow + ", " + xUp + ")");
				}
			}
		}

		// create plot data
		double[] x = new double[nBin * 2];
		double[] y = new double[nBin * 2];

		int plotBin = 0;
		for (int iBin = 0; iBin < nBin; iBin++) {
			x[plotBin] = xhistoLow[iBin];
			y[plotBin] = yhisto[iBin] / (xhistoUp[iBin] - xhistoLow[iBin]);
			plotBin++;
			x[plotBin] = xhistoUp[iBin];
			y[plotBin] = yhisto[iBin] / (xhistoUp[iBin] - xhistoLow[iBin]);
			plotBin++;
		}
		createPlot(file, x, y, "time " + timeUnitPlot, "Histogram " + operationName);
	}

	/**
	 * Create a time-rate-histogram of events of the given client. The histograms are usually used
	 * for diagnostic purposes.
	 * 
	 * @param file
	 *            The plot file
	 * @param eventList
	 *            List of events from which the histogram is derived after applying a filter on the
	 *            operation
	 * @param clientId
	 *            Id of the client, used as filter to use only events in the histogram, which have
	 *            this clientId assigned.
	 * @param nBin
	 *            Number of bins of the resulting histogram.
	 * @param xLow
	 *            time value of the left boundary of the lowest bin of the resulting histogram
	 * @param xUp
	 *            time value of the right boundary of the highest bin of the resulting histogram
	 * @param timeUnitPlot
	 *            The time unit of the resulting plot
	 */
	public static void createClientHistogram(final File file, final Collection<LoadEvent> eventList, final int clientId,
			final int nBin, final double xLow, final double xUp, final String timeUnitPlot) throws IOException {
		double timeScalingFactor = LoadCurveCalculator.getTimeScalingFactor(LoadCurveCalculator.timeUnit_hour, timeUnitPlot);

		double[] xhistoLow = new double[nBin];
		double[] xhistoUp = new double[nBin];
		int[] yhisto = new int[nBin];

		double delta = nBin / (xUp - xLow);
		for (int iBin = 0; iBin < nBin; iBin++) {
			xhistoLow[iBin] = iBin / delta + xLow;
			xhistoUp[iBin] = (iBin + 1) / delta + xLow;
		}
		for (LoadEvent event : eventList) {
			if (event.getClientId() == clientId) {
				double time = timeScalingFactor * event.getTime();
				int xBin = (int) Math.floor((time - xLow) * delta);
				if (xBin >= 0 && xBin < nBin) {
					yhisto[xBin] += event.getOperation().getRelativeClientLoad();
				} else {
					log.warn("Value " + time + " outside range [" + xLow + ", " + xUp + ")");
				}
			}
		}

		// create plot data
		double[] x = new double[nBin * 2];
		double[] y = new double[nBin * 2];

		int plotBin = 0;
		for (int iBin = 0; iBin < nBin; iBin++) {
			x[plotBin] = xhistoLow[iBin];
			y[plotBin] = yhisto[iBin] / (xhistoUp[iBin] - xhistoLow[iBin]);
			plotBin++;
			x[plotBin] = xhistoUp[iBin];
			y[plotBin] = yhisto[iBin] / (xhistoUp[iBin] - xhistoLow[iBin]);
			plotBin++;
		}
		createPlot(file, x, y, "time " + timeUnitPlot, "Histogram Client " + clientId);
	}

	/**
	 * derive the id of an operation (position of the operation within the given array of operation
	 * names.
	 * 
	 * @param operationName
	 *            Name of operation, for which the id has to be derived.
	 * @param operationNames
	 *            Array of operation names
	 */
	private static int getOperationId(final String operationName, final String[] operationNames) {
		for (int i = 0; i < operationNames.length; i++) {
			if (operationName.equals(operationNames[i])) {
				return i;
			}
		}
		throw new IllegalArgumentException("Operation " + operationName + " not in " + operationNames);
	}

	/**
	 * Create a plot of the start times of load events for all given load curve assignements. This
	 * plot is normally used for diagnostic purposes.
	 * 
	 * @param file
	 *            The plot file
	 * @param eventList
	 *            List of events to be plotted.
	 * @param loadCurveAssignments
	 *            load curve assignements, by which the load events are grouped
	 * @param nClients
	 *            Number of clients
	 * @param timeUnitPlot
	 *            time unit of the plot.
	 */
	public static void createPlot(final File file, final Collection<LoadEvent> eventList,
			final List<LoadCurveAssignment> loadCurveAssignments, final int nClients, 
			final String timeUnitPlot) throws IOException {
		double timeScalingFactor = LoadCurveCalculator.getTimeScalingFactor(LoadCurveCalculator.timeUnit_hour, timeUnitPlot);

		int nAssignements = loadCurveAssignments.size();
		int[][] operationsOfType = new int[nAssignements][nClients];
		String[] operationNames = new String[nAssignements];
		for (int iAssignement = 0; iAssignement < nAssignements; iAssignement++) {
			operationNames[iAssignement] = loadCurveAssignments.get(iAssignement).getOperationName();
		}
		for (int iClient = 0; iClient < nClients; iClient++) {
			for (LoadEvent event : eventList) {
				if (event.getClientId() == iClient) {
					int operationId = getOperationId(event.getOperation().getName(), operationNames);
					operationsOfType[operationId][iClient]++;
				}
			}
		}
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(file, "UTF-8");

			Format format = NumberFormat.getNumberInstance();
			for (int iClient = 0; iClient < nClients; iClient++) {
				for (int iAssignement = 0; iAssignement < nAssignements; iAssignement++) {
					pw.println("Eventtime " + timeUnitPlot + "; Client " + iClient + " "
							+ loadCurveAssignments.get(iAssignement).getLoadCurve().getName() + " "
							+ operationNames[iAssignement]);
					log.info("Writing " + operationsOfType[iAssignement][iClient] + " operations of type "
							+ loadCurveAssignments.get(iAssignement).getOperationName() + " for client " + iClient);
					for (LoadEvent event : eventList) {
						if (event.getClientId() == iClient) {
							if (event.getOperation().getName().equals(operationNames[iAssignement])) {
								double x = timeScalingFactor * event.getTime();
								double y = LoadCurveCalculator.r(loadCurveAssignments.get(iAssignement).getLoadCurve(),
										event.getTime());
								pw.println(format.format(x) + "; " + format.format(y));
							}
						}
					}
					pw.println();
				}
			}
		} finally {
			IOUtils.closeQuietly(pw);
		}
	}

	/**
	 * Plot the events created from a load curve. The start times of the events are used for the
	 * x-axis, the value of the load curve at this time is used as the y-axis.
	 * 
	 * @param file
	 *            The plot file
	 * @param eventList
	 *            List of events to be plotted.
	 * @param loadCurve
	 *            The load curve, from which the events are derived.
	 * @param timeUnitPlot
	 *            Time unit of the plot.
	 */
	public static void createPlot(final File file, final Collection<LoadEvent> eventList,
			final LoadCurve loadCurve, final String timeUnitPlot) throws IOException {
		double[] x = new double[eventList.size()];
		double[] y = new double[eventList.size()];

		double timeScalingFactor = LoadCurveCalculator.getTimeScalingFactor(LoadCurveCalculator.timeUnit_hour, timeUnitPlot);

		int iEvent = 0;
		for (LoadEvent event : eventList) {
			x[iEvent] = timeScalingFactor * event.getTime();
			y[iEvent++] = LoadCurveCalculator.r(loadCurve, event.getTime());
		}
		createPlot(file, x, y, "start time " + timeUnitPlot, loadCurve.getName());
	}

	/**
	 * Plot the events created from a load curve, filtered by the given client id. The start times
	 * of the events are used for the x-axis, the value of the load curve at this time is used as
	 * the y-axis.
	 * 
	 * @param file
	 *            The plot file
	 * @param eventList
	 *            List of events to be plotted.
	 * @param loadCurve
	 *            The load curve, from which the events are derived.
	 * @param clientId
	 *            The id of the client used for filtering the load events.
	 * @param timeUnitPlot
	 *            Time unit of the plot.
	 */
	public static void createPlot(final File file, final Collection<LoadEvent> eventList, final LoadCurve loadCurve,
			final int clientId, final String timeUnitPlot) throws IOException {
		double[] x = new double[eventList.size()];
		double[] y = new double[eventList.size()];

		double timeScalingFactor = LoadCurveCalculator.getTimeScalingFactor(LoadCurveCalculator.timeUnit_hour, timeUnitPlot);

		int iEvent = 0;
		for (LoadEvent event : eventList) {
			if (event.getClientId() == clientId) {
				x[iEvent] = timeScalingFactor * event.getTime();
				y[iEvent++] = LoadCurveCalculator.r(loadCurve, event.getTime());
			}
		}
		createPlot(file, x, y, "start time " + timeUnitPlot, loadCurve.getName() + " client " + clientId);
	}

	public static void createLoadCurvePlot(final File file, final LoadCurve loadCurve) throws IOException {
		double[] x = loadCurve.getTimeValues();
		double[] y = loadCurve.getRateValues();
		createPlot(file, x, y, "time " + loadCurve.getTimeUnit(), loadCurve.getName() + " " + loadCurve.getRateUnit());
	}

	/**
	 * Create a plot in .csv-format of the given x and y values, starting with a headerline
	 * containing the given xText and yText.
	 */
	private static void createPlot(final File file, final double[] x, final double[] y, final String xText, final String yText)
			throws IOException {

		PrintWriter pw = null;
		try {
			forceMkdir(file.getParentFile());
			pw = new PrintWriter(file, "UTF-8");

			Format format = NumberFormat.getNumberInstance();
			pw.println(xText + "; " + yText);
			for (int iLine = 0; iLine < x.length; iLine++) {
				pw.println(format.format(x[iLine]) + "; " + format.format(y[iLine]));
			}
		} finally {
			IOUtils.closeQuietly(pw);
		}
	}

}
