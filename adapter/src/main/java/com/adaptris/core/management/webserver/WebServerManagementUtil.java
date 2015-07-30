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
