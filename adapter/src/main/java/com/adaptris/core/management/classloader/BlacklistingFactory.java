package com.adaptris.core.management.classloader;

import static com.adaptris.core.util.PropertyHelper.getPropertyIgnoringCase;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.management.BootstrapProperties;

/**
 * A blacklisting classloader factory.
 */
public class BlacklistingFactory implements ClassLoaderFactory {

	private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

	private final List<String> blacklisted;

	/**
	 * Create a new blacklisting factory that blacklists the classes
	 * specified in the bootstrap properties.
	 *
	 * @param p Bootstrap properties.
	 */
	public BlacklistingFactory(final BootstrapProperties p) {
		final String s = getPropertyIgnoringCase(p, "classloader.blacklist.filenames", "");
		blacklisted = Arrays.asList(s.split(","));
	}

	/**
	 * {@inheritDoc}.
	 */
	@SuppressWarnings("unused")
	@Override
	public URLClassLoader create(final ClassLoader parent) {
		final URL[] parentUrls = ((URLClassLoader)parent).getURLs();
		final List<URL> whitelist = new ArrayList<>();
		for (final URL url : parentUrls) {
			try {
				final String name = new File(url.toURI()).getName();
				if (blacklisted.contains(name)) {
					log.debug("Blacklisting " + name + " " + url);
				} else {
					log.trace("Whitelisting " + name);
					whitelist.add(url);
				}
			} catch (final URISyntaxException e) {
				// ignored
			}
		}
		return new URLClassLoader(whitelist.toArray(new URL[0]), this.getClass().getClassLoader().getParent());
	}
}
