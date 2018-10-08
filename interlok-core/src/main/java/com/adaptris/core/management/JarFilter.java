/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
