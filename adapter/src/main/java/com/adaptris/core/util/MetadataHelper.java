package com.adaptris.core.util;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import com.adaptris.core.MetadataElement;

public final class MetadataHelper {

  public static Properties convertToProperties(Collection<MetadataElement> s) {
    Properties result = new Properties();
    for (MetadataElement e : s) {
      result.setProperty(e.getKey(), e.getValue());
    }
    return result;
  }

  public static Set<MetadataElement> convertFromProperties(Properties p) throws IOException {
    HashSet set = new HashSet();
    // Oh to be able to use stringPropertyNames which is a dirty jdk1.6 method.
    for (Iterator i = p.keySet().iterator(); i.hasNext();) {
      String key = (String) i.next();
      set.add(new MetadataElement(key, p.getProperty(key)));
    }
    return set;

  }
}
