package com.adaptris.interlok.management;

import java.io.File;
import java.io.IOException;
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
	 * N.B. Both opendmk_jdmkrt_jar.jar and opendmk_jmxremote_optional_jar.jar
	 * should be added to the classpath on command line. Therefore the system
	 * classpath should have three entries, one of which is the path of this
	 * JAR, plus the two JARs mentioned above.
	 *
	 * @throws Exception
	 *           If anything bad happens.
	 */
	@SuppressWarnings("resource")
	public void boot() throws Exception {
		final ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
		URL[] urls = ((URLClassLoader)sysClassLoader).getURLs();
		String configDir = null;
		String libDir = null;

		for (final URL url : urls) {
			final File file = new File(url.getFile());
			if (file.isDirectory()) {
				configDir = normaliseClasspathEntry(file, "config");
				libDir = normaliseClasspathEntry(file, "lib");
				break;
			}
		}
		if (configDir == null || libDir == null) {
			throw new RuntimeException("Classpath not configured correctly!");
		}

		urls = new URL[]
				{
					new URL(configDir),
					new URL(libDir + "jetty-all.jar"),
					new URL(libDir),

					new URL(libDir + "geronimo-jms_1.1_spec.jar"),
					new URL(libDir + "geronimo-servlet_3.0_spec.jar"),

					new URL(libDir + "log4j-1.2-api.jar"),
					new URL(libDir + "log4j-api.jar"),
					new URL(libDir + "log4j-core.jar"),
					new URL(libDir + "log4j-slf4j-impl.jar"),
					new URL(libDir + "slf4j-api.jar"),

					new URL(libDir + "interlok-common.jar"),

					new URL(libDir + "opendmk_jdmkrt_jar.jar"),
					new URL(libDir + "opendmk_jmxremote_optional_jar.jar")
				};

		final URLClassLoader parentClassLoader = new URLClassLoader(urls, null);
		final URLClassLoader runtimeClassLoader = new URLClassLoader(new URL[] { new URL(libDir + "adp-core.jar") }, parentClassLoader);
		final Class<?> simpleBootstrap = runtimeClassLoader.loadClass("com.adaptris.core.management.SimpleBootstrap");
		simpleBootstrap.newInstance();
	}
	
	/**
	 * Depending on how this bootstrap class is loaded, figure out where
	 * the config and lib directories are.
	 *
	 * @param path
	 *          The current classpath entry.
	 * @param directory
	 *          The directory to look for.
	 *
	 * @return The correct path.
	 *
	 * @throws IOException
	 *           If something bad happened.
	 */
	private static String normaliseClasspathEntry(final File path, final String directory) throws IOException {
		File entry = new File(path.getPath() + "/../" + directory);
		if (entry.isDirectory()) {
			return "file:///" + entry.getCanonicalPath() + "/";
		}
		entry = new File(path.getPath() + "/" + directory);
		if (entry.isDirectory()) {
			return "file:///" + entry.getCanonicalPath() + "/";
		}
		return null;
	}

	/**
	 * Entry point.
	 *
	 * @param argv
	 *          Command line arguments; ignored.
	 *
	 * @throws Exception
	 *           If anything bad happens.
	 */
	public static void main(final String[] argv) throws Exception {
		new ClassLoaderBootstrap().boot();
	}
}
