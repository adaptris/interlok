/*
 * Copyright 2015 Adaptris Ltd.
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

package com.adaptris.core.management.webserver;

/**
 * Inteface for dynamic webserver management.
 * <p>
 * It contains a {@link ServerManager} which can handle the deployment management.
 * </p>
 * @deprecated since 4.3.0, {@link ServerManager} is a redundant interface; there are no other instances other than
 * {@link JettyServerManager} and assumption of the {@code javax.servlet} API isn't always appropriate (e.g.
 * Undertow / Quarkus).
 */
@Deprecated(since="4.3.0")
@SuppressWarnings({"removal"})
public class WebServerManagementUtil {

	/**
	 * @deprecated since 4.3.0 use {@link JettyServerManager#getInstance()} instead.
	 * @return a {@link JettyServerManager} instance.
	 */
	@Deprecated(since="4.3.0")
	public static ServerManager getServerManager() {
		return JettyServerManager.getInstance();
	}

}
