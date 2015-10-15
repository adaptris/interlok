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
 * 
 * @author gcsiki
 *
 */
public class WebServerManagementUtil {

	private static ServerManager serverManager;

	public static ServerManager getServerManager() {
		return serverManager;
	}

	public static void setServerManager(ServerManager _serverManager) {
		serverManager = _serverManager;
	}

}
