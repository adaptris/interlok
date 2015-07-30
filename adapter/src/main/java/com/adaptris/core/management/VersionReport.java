package com.adaptris.core.management;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Class to report module version numbers.
 * 
 * @config version-report
 * @author lchan
 * @author $Author: hfraser $
 */
@XStreamAlias("version-report")
public final class VersionReport {

  private static VersionReport instance = null;
  private static final String NO_BUILD_DATE = "Unspecified";
  private static final String NO_VERSION = "No Build-Version Information";
  private static final String NO_COMPONENT = "Unspecified Component";

  private static final String INTERLOK_NAME = "Base Interlok";
  private static final ComponentVersion UNSPECIFIED_VERSION = new ComponentVersion(INTERLOK_NAME, NO_VERSION, NO_BUILD_DATE);

  private static final String BUILD_DATE_KEY = "build.date";
  private static final String VERSION_KEY = "build.version";
  private static final String COMPONENT_KEY = "component.name";

  private static final String VERSION_FILE = "META-INF/adaptris-version";

  private transient Set<ComponentVersion> componentVersions;
  private transient ComponentVersion adapterVersion;

  private VersionReport() {
    adapterVersion = UNSPECIFIED_VERSION;
    buildReport();
  }

  /**
   * Get the singleton of the VersionReport.
   * 
   * @return the VersionReport object
   */
  public synchronized static VersionReport getInstance() {
    return instance = instance == null ? new VersionReport() : instance;
  }

  /**
   * Get the Adapter Build Version.
   * 
   * @return the adapter build version.
   */
  public String getAdapterBuildVersion() {
    return adapterVersion.versionOnly;
  }

  /**
   * Get a report on all the modules.
   * 
   * @return version numbers of all available adaptris modules.
   */
  public Collection<String> getReport() {
    Collection<String> versions = new HashSet<>();
    for (ComponentVersion v : componentVersions) {
      versions.add(v.toString());
    }
    return new TreeSet<String>(versions);
  }

  private Set<ComponentVersion> buildFromVersionFile() throws IOException {
    Enumeration<URL> versions = this.getClass().getClassLoader().getResources(VERSION_FILE);
    Set<ComponentVersion> result = new HashSet<ComponentVersion>();
    while (versions.hasMoreElements()) {
      ComponentVersion v = createVersionInfo(versions.nextElement());
      if (v != null) {
        result.add(v);
        if (v.name.equalsIgnoreCase(INTERLOK_NAME)) {
          adapterVersion = v;
        }
      }
    }
    return result;
  }

  private ComponentVersion createVersionInfo(URL url) throws IOException {
    Properties p = new Properties();
    ComponentVersion result = null;
    try (InputStream in = url.openStream()) {
      p.load(in);
      String buildDate = p.getProperty(BUILD_DATE_KEY, NO_BUILD_DATE);
      String version = p.getProperty(VERSION_KEY, NO_VERSION);
      String name = p.getProperty(COMPONENT_KEY, url.toString());
      result = new ComponentVersion(name, version, buildDate);
    }
    return result;
  }

  private void buildReport() {
    try {
      componentVersions = buildFromVersionFile();
      if (componentVersions.size() == 0) {
        componentVersions.add(UNSPECIFIED_VERSION);
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    return;
  }

  private static class ComponentVersion {
    private transient String name, version, buildDate;
    private transient String nameAndVersion;
    private transient String versionOnly;

    ComponentVersion(String name, String version, String buildDate) {
      nameAndVersion = String.format("%1$s: %2$s(%3$s)", name, version, buildDate);
      versionOnly = String.format("%1$s(%2$s)", version, buildDate);
      this.name = name;
      this.version = version;
      this.buildDate = buildDate;
    }

    public String toString() {
      return nameAndVersion;
    }
  }
}
