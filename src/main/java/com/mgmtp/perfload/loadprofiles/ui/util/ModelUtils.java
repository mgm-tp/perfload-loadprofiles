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
package com.mgmtp.perfload.loadprofiles.ui.util;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;
import static java.util.Arrays.fill;

import java.util.List;
import java.util.Set;

import com.mgmtp.perfload.loadprofiles.model.Client;
import com.mgmtp.perfload.loadprofiles.model.Operation;
import com.mgmtp.perfload.loadprofiles.model.Target;
import com.mgmtp.perfload.loadprofiles.ui.model.SelectionDecorator;

/**
 * @author rnaegele
 */
public class ModelUtils {
	public static void updateOperations(final List<Operation> operations, final List<Operation> oldOperations) {
		if (oldOperations.isEmpty()) {
			oldOperations.addAll(operations);
			return;
		}

		boolean[] deletions = new boolean[oldOperations.size()];
		fill(deletions, true);

		for (Operation op : operations) {
			boolean set = false;
			for (int i = 0; i < oldOperations.size(); ++i) {
				Operation oldOp = oldOperations.get(i);
				if (op.equals(oldOp)) {
					deletions[i] = false;
					// set again in order to trigger GUI update
					oldOperations.set(i, oldOp);
					set = true;
					break;
				}
			}
			if (!set) {
				oldOperations.add(op);
			}
		}
		for (int i = deletions.length - 1; i >= 0; --i) {
			if (deletions[i]) {
				oldOperations.remove(i);
			}
		}
	}

	public static void updateTargets(final List<Target> targets, final List<Target> oldTargets) {
		//		EventList<Target> oldTargets = controller.getTargets();
		if (oldTargets.isEmpty()) {
			oldTargets.addAll(targets);
			return;
		}

		boolean[] deletions = new boolean[oldTargets.size()];
		fill(deletions, true);

		for (Target target : targets) {
			boolean set = false;
			for (int i = 0; i < oldTargets.size(); ++i) {
				Target oldTarget = oldTargets.get(i);
				if (target.equals(oldTarget)) {
					deletions[i] = false;
					// set again in order to trigger GUI update
					oldTargets.set(i, oldTarget);
					set = true;
					break;
				}
			}
			if (!set) {
				oldTargets.add(target);
			}
		}
		for (int i = deletions.length - 1; i >= 0; --i) {
			if (deletions[i]) {
				oldTargets.remove(i);
			}
		}
	}

	public static void updateClients(final List<Client> clients, final List<Client> oldClients) {
		//		EventList<Client> oldClients = controller.getClients();
		if (oldClients.isEmpty()) {
			oldClients.addAll(clients);
			return;
		}

		boolean[] deletions = new boolean[oldClients.size()];
		fill(deletions, true);

		for (Client client : clients) {
			boolean set = false;
			for (int i = 0; i < oldClients.size(); ++i) {
				Client oldClient = oldClients.get(i);
				if (client.equals(oldClient)) {
					deletions[i] = false;
					// set again in order to trigger GUI update
					oldClients.set(i, oldClient);
					set = true;
					break;
				}
			}
			if (!set) {
				oldClients.add(client);
			}
		}
		for (int i = deletions.length - 1; i >= 0; --i) {
			if (deletions[i]) {
				oldClients.remove(i);
			}
		}
	}

	/**
	 * Updates checkbox states.
	 */
	public static void updateTargetDecorators(final List<SelectionDecorator> decoratedTargets,
			final List<SelectionDecorator> oneTimeDecoratedTargets, final List<Target> targets, final boolean oneTimeOnly) {
		Set<Target> checkedTargets = newHashSet(transform(filter(decoratedTargets, new SelectionDecoratorCheckedPredicate()),
				new SelectionDecoratorToTargetFunction()));

		if (!oneTimeOnly) {
			decoratedTargets.clear();
			for (Target target : targets) {
				SelectionDecorator sd = new SelectionDecorator(target, checkedTargets.contains(target));
				decoratedTargets.add(sd);
			}
		}

		Set<Target> checkedOneTimeTargets = newTreeSet(transform(
				filter(oneTimeDecoratedTargets, new SelectionDecoratorCheckedPredicate()),
				new SelectionDecoratorToTargetFunction()));

		oneTimeDecoratedTargets.clear();
		for (Target target : checkedTargets) {
			SelectionDecorator sd = new SelectionDecorator(target, checkedOneTimeTargets.contains(target));
			oneTimeDecoratedTargets.add(sd);
		}
	}

	/**
	 * Updates checkbox states.
	 */
	public static void updateClientDecorators(final List<SelectionDecorator> decoratedClients, final List<Client> clients) {
		Set<Client> checkedClients = newHashSet(transform(filter(decoratedClients, new SelectionDecoratorCheckedPredicate()),
				new SelectionDecoratorToClientFunction()));

		decoratedClients.clear();
		for (Client client : clients) {
			SelectionDecorator sd = new SelectionDecorator(client, checkedClients.contains(client));
			decoratedClients.add(sd);
		}
	}
}
