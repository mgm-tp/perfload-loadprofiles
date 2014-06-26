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
package com.mgmtp.perfload.loadprofiles.ui;

import static com.google.common.io.Resources.getResource;

import java.awt.Cursor;
import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.common.base.Charsets;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.mgmtp.perfload.loadprofiles.ui.ctrl.LoadProfilesController;
import com.mgmtp.perfload.loadprofiles.ui.model.LoadProfileConfig;
import com.mgmtp.perfload.loadprofiles.ui.util.ExceptionHandler;

/**
 * @author rnaegele
 */
public class AppModule extends AbstractModule {

	private final File baseDir;

	public AppModule(final File baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	protected void configure() {
		bind(File.class).annotatedWith(ConfigDir.class).toInstance(new File(baseDir, "config"));
		bind(File.class).annotatedWith(SettingsDir.class).toInstance(new File(baseDir, "settings"));

		try {
			String appVersion = Resources.toString(getResource("com/mgmtp/perfload/loadprofiles/version.txt"), Charsets.UTF_8);
			bindConstant().annotatedWith(AppVersion.class).to(appVersion);
		} catch (IOException ex) {
			addError("Could not read app version from classpath");
		}

		bind(AppFrame.class);
		bind(LoadProfileConfig.class);
		bind(LoadProfilesController.class);
		bind(ExceptionHandler.class);

		MethodInterceptor methodInterceptor = new MethodInterceptor() {
			@Inject
			ExceptionHandler exceptionHandler;

			@Override
			public Object invoke(final MethodInvocation invocation) throws Throwable {
				AppFrame frame = (AppFrame) invocation.getThis();
				Cursor oldCursor = frame.getCursor();
				try {
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					return invocation.proceed();
				} catch (Throwable th) {
					exceptionHandler.handle(th);
					return null;
				} finally {
					frame.setCursor(oldCursor);
				}
			}
		};
		requestInjection(methodInterceptor);

		bindInterceptor(Matchers.subclassesOf(AppFrame.class), Matchers.annotatedWith(Subscribe.class), methodInterceptor);
	}
}
