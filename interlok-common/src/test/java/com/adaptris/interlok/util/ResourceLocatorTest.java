package com.adaptris.interlok.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileDeleteStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.adaptris.core.management.webserver.JettyServerManager;

public class ResourceLocatorTest extends ResourceLocator {
  private static FileCleaningTracker cleaner = new FileCleaningTracker();
  private static String relativePath = "./build/tmp";
  private static File relativeFile = new File(relativePath);

  @Test
  public void testToURL() throws Exception {
    assertNotNull(toURL("./config/jetty.xml"));
    assertNotNull(toURL("file:////absolute/path/to/jetty/jetty.xml"));
    assertNotNull(toURL("file:///c:/absolute/path/to/jetty.xml"));
    assertNotNull(toURL("file://localhost/c:/absolute/path/to/jetty.xml"));
    assertNotNull(toURL("\\absolute\\path\\to\\jetty.xml"));
    assertNotNull(toURL("https://github.com/adaptris/interlok"));
    assertNotNull(toURL(JettyServerManager.DEFAULT_JETTY_XML));
  }

  public void testToURL_URISyntax() throws Exception {
    Assertions.assertThrows(URISyntaxException.class, () -> {
      toURL("file://localhost/c:/Program Files/Microsoft/Teams");
    });
  }

  @Test
  public void testToURL_Relative() throws Exception {
    relativeFile.mkdirs();
    trackFile(relativeFile, this);
    File tempFile = createTrackedFile(this, relativeFile);
    URL url = toURL("file://localhost/" + relativePath + "/" + tempFile.getName());
    try (InputStream in = url.openStream()) {
    }
    URL url2 = toURL("file:///" + relativePath + "/" + tempFile.getName());
    try (InputStream in = url2.openStream()) {
    }
  }


  @Test
  public void testLocalResolver() throws Exception {
    File tempFile = createTrackedFile(this);

    assertTrue(LocalResource.Filesystem.canFind(tempFile.getCanonicalPath()));
    assertNotNull(LocalResource.Filesystem.resolve(tempFile.getCanonicalPath()));
    assertFalse(LocalResource.Filesystem.canFind(JettyServerManager.DEFAULT_JETTY_XML));
  }


  @Test
  public void testClasspathResolver() throws Exception {
    File tempFile = createTrackedFile(this);

    assertFalse(LocalResource.Classpath.canFind(tempFile.getCanonicalPath()));
    assertTrue(LocalResource.Classpath.canFind(JettyServerManager.DEFAULT_JETTY_XML));
    assertNotNull(LocalResource.Classpath.resolve(JettyServerManager.DEFAULT_JETTY_XML));
  }


  private static File createTrackedFile(Object tracker, File dir) throws IOException {
    File f = File.createTempFile("ResourceLocatorTest", "", dir);
    return trackFile(f, tracker);
  }

  private static File createTrackedFile(Object tracker) throws IOException {
    return createTrackedFile(tracker, null);
  }

  private static File trackFile(File f, Object tracker) {
    cleaner.track(f, tracker, FileDeleteStrategy.FORCE);
    return f;
  }
}
