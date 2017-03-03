package com.adaptris.interlok.management;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Adapter bootstrap process with hierarchical class loaders; especially for Jetty.
 */
public class ClassLoaderBootstrap {

	/**
	 * Standard bootstrap class.
	 */
	private static final String STANDARD_BOOTSTRAP = "com.adaptris.core.management.StandardBootstrap";
	
	/**
	 * Default, empty constructor.
	 */
	private ClassLoaderBootstrap() {
	}

	/**
	 * Main boot method.
	 *
	 * @param adpCore URI that points to adp-core.jar.
	 *
	 * @throws Exception
	 *           If anything bad happens.
	 */
	public void boot(final URI adpCore) throws Exception {
		final ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
		URL[] urls = ((URLClassLoader)sysClassLoader).getURLs();
		for (final URL url : urls) {
			System.out.println("Adding " + url + " to parent class loader class path");
		}

		try (final URLClassLoader parentClassLoader = new URLClassLoader(urls, null)) {
			System.out.println("Loading ADP core from " + adpCore);
			urls = new URL[] { adpCore.toURL() };
			try (final URLClassLoader runtimeClassLoader = new URLClassLoader(urls, parentClassLoader)) {
				Thread.currentThread().setContextClassLoader(runtimeClassLoader);

				System.out.println("Invoking standard boot strap : " + STANDARD_BOOTSTRAP);
				final Class<?> standardBootstrap = Class.forName(STANDARD_BOOTSTRAP, true, runtimeClassLoader);
				final Constructor<?> constructor = standardBootstrap.getConstructor(String[].class);
				final Method boot = standardBootstrap.getMethod("boot");

				boot.invoke(constructor.newInstance((Object)new String[0]));
			}
		}
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
			throw new IllegalArgumentException("Expected path to adp-core.jar; received " + argv);
		}
		final File adpCore = new File(argv[0]);
		if (!(adpCore.exists() && adpCore.isFile())) {
			throw new FileNotFoundException("Invalid adp-core.jar [" + argv[0] + "]");
		}

		new ClassLoaderBootstrap().boot(adpCore.toURI());
	}
}
