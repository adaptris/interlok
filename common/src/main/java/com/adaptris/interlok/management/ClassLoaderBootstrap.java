package com.adaptris.interlok.management;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Adapter bootstrap process with hierarchical class loaders; especially for Jetty.
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
	 * @param adpCore File object that is adp-core.jar.
	 *
	 * @throws Exception
	 *           If anything bad happens.
	 */
	@SuppressWarnings("resource")
	public void boot(final File adpCore) throws Exception {
		final ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
		final URL[] urls = ((URLClassLoader)sysClassLoader).getURLs();
		for (final URL url : urls) {
			System.out.println("Adding " + url + " to classpath");
		}
		final URLClassLoader parentClassLoader = new URLClassLoader(urls, null);
		final URL adpCoreUrl = new URL("file:///" + adpCore.getAbsolutePath());
		System.out.println("Loading " + adpCoreUrl);
		final URLClassLoader runtimeClassLoader = new URLClassLoader(new URL[] { adpCoreUrl }, parentClassLoader);
		Thread.currentThread().setContextClassLoader(runtimeClassLoader);
		final Class<?> simpleBootstrap = Class.forName("com.adaptris.core.management.SimpleBootstrap", true, runtimeClassLoader);
		simpleBootstrap.getConstructor().newInstance();
	}

	/**
	 * Entry point.
	 *
	 * @param argv
	 *          Command line arguments; must include the path to adp-core.jar.
	 *
	 * @throws Exception
	 *           If anything bad happens.
	 */
	public static void main(final String[] argv) throws Exception {
		if (argv.length != 1) {
			System.err.println("Missing path to adp-core.jar");
		}
		new ClassLoaderBootstrap().boot(new File(argv[0]));
	}
}
