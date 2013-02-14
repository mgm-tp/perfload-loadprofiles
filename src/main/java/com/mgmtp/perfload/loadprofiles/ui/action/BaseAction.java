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
package com.mgmtp.perfload.loadprofiles.ui.action;

import static com.google.common.base.Preconditions.checkArgument;

import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import com.google.common.eventbus.EventBus;

/**
 * Abstract super class for actions. {@link ActionEvent}s are automatically wrapped into a
 * {@link BaseEvent}, which must be implemented as an non-static inner class, and then posted to the
 * {@link EventBus} passed in via constructor.
 * 
 * @author rnaegele
 */
public abstract class BaseAction extends AbstractAction {

	private static final String ICON_PACKAGE = "com/famfamfam/silk/";

	private final EventBus eventBus;

	/**
	 * Creates a new action.
	 * 
	 * @param title
	 *            the action title
	 * @param eventBus
	 *            the Guava {@link EventBus} to post events to
	 * @see Action#MNEMONIC_KEY
	 * @see Action#SMALL_ICON
	 * @see Action#SHORT_DESCRIPTION
	 * @see Action#ACCELERATOR_KEY
	 */
	public BaseAction(final String title, final String description, final Integer mnemonicKey, final KeyStroke acceleratorKey,
			final String smallIconFileName, final EventBus eventBus) {
		super(title);

		putValue(SHORT_DESCRIPTION, description);
		if (smallIconFileName != null) {
			URL url = Thread.currentThread().getContextClassLoader().getResource(ICON_PACKAGE + smallIconFileName);
			putValue(SMALL_ICON, new ImageIcon(url));
		}
		if (mnemonicKey != null) {
			putValue(MNEMONIC_KEY, mnemonicKey);
		}
		if (acceleratorKey != null) {
			putValue(ACCELERATOR_KEY, acceleratorKey);
		}

		checkArgument(eventBus != null, "Parameter 'eventBus' must not be null.");
		this.eventBus = eventBus;
	}

	/**
	 * {@inheritDoc} Wraps the specified {@link ActionEvent} into a {@link BaseEvent}, which must be
	 * implemented as an non-static inner class, and posts the result to the internal event bus.
	 */
	@Override
	public final void actionPerformed(final ActionEvent e) {
		Class<?>[] classes = getClass().getDeclaredClasses();
		for (Class<?> clazz : classes) {
			if (BaseEvent.class.isAssignableFrom(clazz)) {
				try {
					// Inner classes need the surrounding class as first parameter in the 
					// constructor when instantiated by reflection.
					Constructor<?> constructor = clazz.getConstructor(getClass(), ActionEvent.class);
					Object event = constructor.newInstance(this, e);
					eventBus.post(event);
					break;
				} catch (InvocationTargetException ex) {
					throw new RuntimeException(ex.getCause());
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}
	}

	/**
	 * Base class for {@link ActionEvent} decorators.
	 */
	public abstract class BaseEvent {
		private final ActionEvent event;

		public BaseEvent(final ActionEvent event) {
			this.event = event;
		}

		/**
		 * @return the event
		 */
		public ActionEvent getEvent() {
			return event;
		}
	}
}
