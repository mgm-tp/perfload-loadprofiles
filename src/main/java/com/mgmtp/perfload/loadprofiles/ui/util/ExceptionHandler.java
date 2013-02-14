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
package com.mgmtp.perfload.loadprofiles.ui.util;

import java.lang.Thread.UncaughtExceptionHandler;

import javax.inject.Singleton;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standard exception handler for the application. Must be set as default handler for the event
 * dispatch thread.
 * 
 * @author rnaegele
 */
@Singleton
public class ExceptionHandler implements UncaughtExceptionHandler {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void uncaughtException(final Thread t, final Throwable th) {
		handle(th);
	}

	/**
	 * Called on uncaught exceptions in modal dialogs.
	 */
	public void handle(final Throwable th) {
		if (!(th instanceof LoadProfileException)) {
			log.error(th.getMessage(), th);
		}
		JOptionPane.showMessageDialog(null, th.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	}
}
