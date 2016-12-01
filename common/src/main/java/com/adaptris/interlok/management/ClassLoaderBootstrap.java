package com.adaptris.interlok.management;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * TODO
 */
public class ClassLoaderBootstrap {

	/**
	 * Default, empty constructor.
	 */
	private ClassLoaderBootstrap() {
	}

	/**
	 * Main boot method.
	 * 
	 * @throws Exception
	 */
	public void boot() throws Exception {
		/*
		 * put opendmk_jdmkrt_jar.jar and opendmk_jmxremote_optional_jar.jar on command line
		 */
		final URL[] urls = new URL[]
				{
					new URL("file:///C:/Adaptris/Interlok-3.4.0/config/"),
					new URL("file:///C:/Adaptris/Interlok-3.4.0/lib/jetty-all.jar"),
					new URL("file:///C:/Adaptris/Interlok-3.4.0/lib/"),
					
					new URL("file:///C:/Adaptris/Interlok-3.4.0/lib/geronimo-jms_1.1_spec.jar"),
					new URL("file:///C:/Adaptris/Interlok-3.4.0/lib/geronimo-servlet_3.0_spec.jar"),
					
					new URL("file:///C:/Adaptris/Interlok-3.4.0/lib/log4j-1.2-api.jar"),
					new URL("file:///C:/Adaptris/Interlok-3.4.0/lib/log4j-api.jar"),
					new URL("file:///C:/Adaptris/Interlok-3.4.0/lib/log4j-core.jar"),
					new URL("file:///C:/Adaptris/Interlok-3.4.0/lib/log4j-slf4j-impl.jar"),
					
					/* TODO: build this JAR with: WebServerManagementUtil, ...? */
					new URL("file:///C:/Adaptris/Interlok-3.4.0/lib/adp-util.jar")
				};
		final URLClassLoader parentClassLoader = new URLClassLoader(urls, null);

		final URLClassLoader runtimeClassLoader = new URLClassLoader(new URL[] { new URL("file:///C:/Adaptris/Interlok-3.4.0/lib/adp-core.jar") }, parentClassLoader);
		
		final Class<?> simpleBootstrap = runtimeClassLoader.loadClass("com.adaptris.core.management.SimpleBootstrap");
		simpleBootstrap.newInstance();
	}
	
	/**
	 * Entry point.
	 * 
	 * @param argv
	 * @throws Exception
	 */
	public static void main(final String[] argv) throws Exception {
		new ClassLoaderBootstrap().boot();
	}
}
