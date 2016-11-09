package com.adaptris.core.management.classloader;

import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChildFirstClassLoader extends URLClassLoader {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  public ChildFirstClassLoader(final URL[] urls, final ClassLoader parent) {
    super(urls, parent);
  }

  @Override
  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public Class loadClass(final String name) throws ClassNotFoundException {
    return loadClass(name, false);
  }

  @Override
  @SuppressWarnings({
    "rawtypes", "unchecked", "unused"
  })
  protected Class loadClass(final String name, final boolean resolve) throws ClassNotFoundException {

    log.info("Attempting to load class " + name);

    // First, check if the class has already been loaded
    Class clas = findLoadedClass(name);

    // if not loaded, search the local (child) resources
    if (clas == null) {
      log.info("Class " + name + " not already loaded");
      try {
        clas = findClass(name);
      } catch (final ClassNotFoundException e) {
        // ignore
      }
    }

    // if we could not find it, delegate to parent
    // Note that we don't attempt to catch any ClassNotFoundException
    if (clas == null) {
      log.info("Class " + name + " still not loaded; trying parent/system classloader ");
      if (getParent() != null) {
        clas = getParent().loadClass(name);
      } else {
        clas = getSystemClassLoader().loadClass(name);
      }
    }
    log.info("Class " + name + " loaded as " + clas);

    if (resolve) {
      resolveClass(clas);
    }

    return clas;
  }
}
