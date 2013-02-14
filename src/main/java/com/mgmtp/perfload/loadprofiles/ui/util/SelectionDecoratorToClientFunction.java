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

import com.google.common.base.Function;
import com.mgmtp.perfload.loadprofiles.model.Client;
import com.mgmtp.perfload.loadprofiles.ui.model.SelectionDecorator;

/**
 * @author rnaegele
 */
public class SelectionDecoratorToClientFunction implements Function<SelectionDecorator, Client> {
	@Override
	public Client apply(final SelectionDecorator input) {
		return (Client) input.getBaseObject();
	}
}
