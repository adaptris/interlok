package com.adaptris.core.management.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.management.BootstrapProperties;

import static com.adaptris.core.util.PropertyHelper.getPropertyIgnoringCase;

public class BlacklistingFactory implements ClassLoaderFactory {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private List<String> blacklisted;

  public BlacklistingFactory(BootstrapProperties p) {
    String s = getPropertyIgnoringCase(p, "classloader.blacklist.filenames", "");
    blacklisted = Arrays.asList(s.split(","));
  }

  public URLClassLoader create(ClassLoader parent) {
    URL[] parentUrls = ((URLClassLoader) parent).getURLs();
    List<URL> whitelist = new ArrayList<>();
    for (URL url : parentUrls) {
      String path = url.getPath();
      int lastSlash = path.lastIndexOf(File.separator);
      if (lastSlash == -1) {
        lastSlash = path.lastIndexOf('/');
      }
      String name = path.substring(lastSlash + 1);
      if (blacklisted.contains(name)) {
        log.debug("Blacklisting " + name);
      } else {
        whitelist.add(url);
      }
    }
    try {
      return new ChildFirstClassLoader(new URL[] { new URL("file:///C/Users/mcgratha/work/odin-interlok/packager/build/openfield/lib/adp-core.jar"), new URL("file:///C/Adaptris/Interlok3.4.0/webapps/adapter-web-gui.war") }/*whitelist.toArray(new URL[0])*/, parent);
    } catch (MalformedURLException e) {
      log.error(e.getMessage());
      return null;
    }
  }
}
