package com.adaptris.core.util;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.net.URL;

import org.apache.log4j.helpers.Loader;

import com.adaptris.core.Service;
import com.adaptris.core.StateManagedComponent;

/**
 * Utility for generating logging messages.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public final class LoggingHelper {
  
  private static final String GUID_PATTERN = "^[0-9a-f]{8}\\-[0-9a-f]{4}\\-[0-9a-f]{4}\\-[0-9a-f]{4}\\-[0-9a-f]{12}$";

  public static boolean log4jAvailable() {
    boolean rc = false;
    try {
      Class.forName("org.apache.log4j.xml.DOMConfigurator");
      rc = true;
    }
    catch (Exception e) {
      rc = false;
    }
    return rc;
  }

  public static String friendlyName(Service s) {
    if (s == null) {
      return "";
    }
    // return s.getClass().getSimpleName() + "(" + (isBlank(s.getUniqueId()) ? "" : s.getUniqueId()) + ")";
    return s.getClass().getSimpleName() + filterGuid(s.getUniqueId());
  }

  public static String friendlyName(StateManagedComponent comp) {
    if (comp == null) {
      return "";
    }
    // return comp.getClass().getSimpleName() + "(" + (isBlank(comp.getUniqueId()) ? "" : comp.getUniqueId()) + ")";
    return comp.getClass().getSimpleName() + filterGuid(comp.getUniqueId());
  }

  private static String filterGuid(String uid) {
    if (isBlank(uid) || uid.matches(GUID_PATTERN)) {
      return "";
    }
    return "(" + uid + ")";
  }

  public static URL findLog4jConfiguration() {
    String resource = System.getProperty("log4j.configuration", "log4j.xml");
    URL url = null;
    try {
      url = Loader.getResource(resource);
      if (url == null) {
        url = Loader.getResource("log4j.properties");
      }
    }
    catch (Exception ignored) {

    }
    return url;
  }

}
