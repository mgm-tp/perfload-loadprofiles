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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.google.common.eventbus.EventBus;

/**
 * @author rnaegele
 */
public class ToolsDeleteLoadProfileEntityAction extends BaseAction {

	public ToolsDeleteLoadProfileEntityAction(final EventBus eventBus) {
		super("Delete element", "Delete load profile element", KeyEvent.VK_D, null, "delete.png", eventBus);
	}

	public class Event extends BaseEvent {
		public Event(final ActionEvent event) {
			super(event);
		}
	}
}
