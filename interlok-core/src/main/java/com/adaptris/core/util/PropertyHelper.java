package com.adaptris.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyHelper {
  /**
   * Get a subset of the configuration properties.
   *
   * @param prefix the key that must exist in the property key for it to belong.
   * @param p the properties
   * @return a Properties object containing matching keys and values.
   */
  public static Properties getPropertySubset(Properties p, String prefix) {
    return getPropertySubset(p, prefix, false);
  }

  /**
   * Get a subset of the configuration properties.
   *
   * @param prefix the key that must exist in the property key for it to belong.
   * @param p the properties
   * @param ignoreCase whether or not to ignore the case of the prefix and property key
   * @return a Properties object containing matching keys and values.
   */
  public static Properties getPropertySubset(Properties p, String prefix, boolean ignoreCase) {
    Properties tmp = new Properties();
    String propPrefix = ignoreCase ? prefix.toUpperCase() : prefix;
    for (Object keyObj : p.keySet()) {
      String key = keyObj.toString();
      String propKey = ignoreCase ? key.toUpperCase() : key;
      if (propKey.startsWith(propPrefix)) {
        tmp.setProperty(key, p.getProperty(key));
      }
    }
    return tmp;

  }

  /**
   * Convenience method to get a property value ignoring the case.
   *
   * <p>
   * While we could legitimately override the get/set/getProperty/setProperty methods and convert all the keys to a single case;
   * this would not be appropriate for the properties that we will convert into system properties. So we have a static method to
   * iterate through all the keys of the property doing a case insensitive match. It's not terribly performant, but performance
   * isn't a consideration at this point.
   * </p>
   *
   * @param p the properties you want to query.
   * @param key the key
   * @param defaultValue the default value if key is not matched.
   * @return the property value.
   */
  public static String getPropertyIgnoringCase(Properties p, String key, String defaultValue) {
    String value = defaultValue;
    for (Object keyObj : p.keySet()) {
      String propKey = keyObj.toString();
      if (propKey.equalsIgnoreCase(key)) {
        value = p.getProperty(propKey);
      }
    }
    return value;
  }

  /**
   * Convenience method to turn a Properties into a Map<String, String>
   * 
   * @param p the properties, if null, will return an empty map
   * @return a map
   */
  public static Map<String, String> asMap(Properties p) {
    Map<String, String> result = new HashMap<String, String>();
    if (p == null) {
      return result;
    }
    for (String key : p.stringPropertyNames()) {
      result.put(key, p.getProperty(key));
    }
    return result;
  }

  /**
   * @see #getPropertyIgnoringCase(Properties, String, String)
   */
  public static String getPropertyIgnoringCase(Properties p, String key) {
    return getPropertyIgnoringCase(p, key, null);
  }

  /**
   * Convenience method to load a set of properties from a file.
   * 
   * @param file the file.
   * @return a possibly empty set of properties.
   */
  public static Properties loadQuietly(final File file) {
    return loadQuietly(() -> {
      return new FileInputStream(file);
    });
  }

  /**
   * Convenience method to load a set of properties from a resource on the classpath.
   * 
   * @param resource the resource that will be found via
   *          {@code Thread.currentThread().getContextClassLoader().getResourceAsStream(String)}.
   * @return a possibly empty set of properties.
   */
  public static Properties loadQuietly(final String resource) {
    return loadQuietly(() -> {
      return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    });
  }

  /**
   * Convenience method to load a set of properties from a URL
   * 
   * @param resource the resource
   * @return a possibly empty set of properties.
   */
  public static Properties loadQuietly(final URL resource) {
    return loadQuietly(() -> {
      return resource.openStream();
    });
  }

  /**
   * Convenience method to load a set of properties from an InputStream
   * 
   * @param resource the resource
   * @return a possibly empty set of properties.
   */
  public static Properties loadQuietly(final InputStream in) {
    return loadQuietly(() -> {
      return in;
    });
  }

  /**
   * Convenience method to load a set of properties from an InputStream
   * 
   * @param e the {@link PropertyInputStream} instance (probably a lambda expression).
   * @return a possibly empty set of properties.
   */
  public static Properties loadQuietly(PropertyInputStream e) {
    Properties result = new Properties();
    try {
      result = load(e);
    } catch (Exception ignored) {
    }
    return result;
  }

  /**
   * Convenience method to load a set of properties from an InputStream
   * 
   * @param e the {@link PropertyInputStream} instance (probably a lambda expression).
   * @return the properties loaded from the input stream.
   * @throws IOException any IO exceptions reading the input stream.
   */
  public static Properties load(PropertyInputStream e) throws IOException {
    Properties result = new Properties();
    try (InputStream in = e.openStream()) {
      result.load(in);
    }
    return result;
  }

  @FunctionalInterface
  public interface PropertyInputStream {
    InputStream openStream() throws IOException;
  }
}
