package com.adaptris.core.util;

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
   * @see #getPropertyIgnoringCase(Properties, String, String)
   */
  public static String getPropertyIgnoringCase(Properties p, String key) {
    return getPropertyIgnoringCase(p, key, null);
  }

}
