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
				configDir = "file:///" + new File(file.getPath() + "/../config/").getCanonicalPath();
				libDir = "file:///" + new File(file.getPath() + "/../lib/").getCanonicalPath();
				break;
			}
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
					
					new URL(libDir + "adp-common.jar")
				};

		final URLClassLoader parentClassLoader = new URLClassLoader(urls, null);
		final URLClassLoader runtimeClassLoader = new URLClassLoader(new URL[] { new URL(libDir + "adp-core.jar") }, parentClassLoader);
		final Class<?> simpleBootstrap = runtimeClassLoader.loadClass("com.adaptris.core.management.SimpleBootstrap");
		simpleBootstrap.newInstance();
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
