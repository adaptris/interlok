package com.adaptris.core.management;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;


/**
 * <p>
 * Used to filter jars and zips for inclusion in the classpath.
 * </p>
 */
final class JarFilter implements FilenameFilter {

  private static final String[] JAR_EXTENSIONS =
  {
      ".jar", ".zip"
  };
  private static final Map FILTERS = new HashMap();
  private transient String regexp;

  private JarFilter() {
  }

  private JarFilter(String nameFilter) {
    this();
    regexp = nameFilter;
  }


  @Override
  public boolean accept(File file, String s) {

    if (s != null) {
      int i = JAR_EXTENSIONS.length;
      s = s.toLowerCase();
      for (int j = 0; j < i; j++) {
        if (s.matches(regexp) && s.endsWith(JAR_EXTENSIONS[j])) {
          return true;
        }
      }
    }
    return false;
  }

  public static FilenameFilter getDefaultInstance() {
    return getInstance(".*");
  }

  public static FilenameFilter getInstance(String filter) {
    FilenameFilter f = (FilenameFilter) FILTERS.get(filter);
    if (f == null) {
      f = new JarFilter(filter);
      FILTERS.put(filter, f);
    }
    return f;
  }
}
