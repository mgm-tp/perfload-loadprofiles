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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import com.google.common.eventbus.EventBus;

/**
 * @author rnaegele
 */
public class FileOpenLoadProfileConfigAction extends BaseAction {

	public FileOpenLoadProfileConfigAction(final EventBus eventBus) {
		super("Open...", "Open load profile configuration...", KeyEvent.VK_O, KeyStroke.getKeyStroke(KeyEvent.VK_O,
				InputEvent.CTRL_DOWN_MASK),	"folder_page.png", eventBus);
	}

	public class Event extends BaseEvent {
		public Event(final ActionEvent event) {
			super(event);
		}
	}
}
