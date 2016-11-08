package com.adaptris.core.management.classloader;

import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChildFirstClassLoader extends URLClassLoader {
  
  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  
  public ChildFirstClassLoader(URL[] urls) {
    super(urls);
  }

  public ChildFirstClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  }

  public void addURL(URL url) {
    super.addURL(url);
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Class loadClass(String name) throws ClassNotFoundException {
    return loadClass(name, false);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
    
    log.info("Attempting to load class " + name + " ; will  resolve " + resolve);
    
    // First, check if the class has already been loaded
    Class c = findLoadedClass(name);

    // if not loaded, search the local (child) resources
    if (c == null) {
      log.info("Class " + name + " not already loaded");
      try {
        for (URL url : getURLs()) {
          log.info("Searching " + url.toString());
        }
        c = findClass(name);
      } catch(ClassNotFoundException cnfe) {
        // ignore
        log.error("Class not found " + cnfe, cnfe);
      }
    }
    
    // if we could not find it, delegate to parent
    // Note that we don't attempt to catch any ClassNotFoundException
    if (c == null) {
      log.info("Class " + name + " still not loaded ; parent is " + getParent().toString());
      if (getParent() != null) {
        c = getParent().loadClass(name);
      } else {
        c = getSystemClassLoader().loadClass(name);
      }
    }
    log.info("Class " + name + " is " + c);

    if (resolve) {
      log.info("Resolving class " + c.getName());
      resolveClass(c);
    }

    return c;
  }
  
  public static void main(String[] args) throws Exception {
    ChildFirstClassLoader cfcl = new ChildFirstClassLoader(new URL[] { new URL("file:///C/Adaptris/Interlok3.4.0/webapps/adapter-web-gui.war") });
    Class c = cfcl.loadClass("org.jboss.logging.AbstractMdcLoggerProvider");
    System.out.println(c.getName());
    // jboss-logging-3.1.3.GA.jar
    // org\jboss\logging\
    // AbstractMdcLoggerProvider.class
  }

}
