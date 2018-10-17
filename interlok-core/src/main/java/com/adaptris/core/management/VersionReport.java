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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  private static final String NO_COMPONENT = "unknown";
  private static final String NO_GROUP = "unknown";

  private static final String INTERLOK_NAME = "Base Interlok";
  private static final ComponentVersion UNSPECIFIED_VERSION = new ComponentVersion(new Properties(), NO_COMPONENT);
  private static final Pattern UNKONWN_ARTIFACT_PATTERN = Pattern.compile("jar:file:.*/(.*)!/META-INF.*");

  private static final String BUILD_DATE_KEY = "build.date";
  private static final String VERSION_KEY = "build.version";
  private static final String COMPONENT_KEY = "component.name";
  private static final String ARTIFACT_KEY = "artifactId";
  private static final String GROUP_KEY = "groupId";

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

  /**
   * Get maven style artifact identifiers.
   * 
   * @return version numbers of all available adaptris modules.
   */
  public Collection<String> getArtifactIdentifiers() {
    Collection<String> versions = new HashSet<>();
    for (ComponentVersion v : componentVersions) {
      versions.add(v.artifactIdentifier());
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
      result = new ComponentVersion(p, jarName(url.toString()));
    }
    return result;
  }

  // jar:file:/path/to/interlok-common-3.8-SNAPSHOT.jar!/META-INF/adaptris-version
  // into just interlok-common-3.8-SNAPSHOT.jar
  private static String jarName(String stringifiedUrl) {
    Matcher m = UNKONWN_ARTIFACT_PATTERN.matcher(stringifiedUrl);
    if (m.matches()) {
      return m.group(1);
    }
    return stringifiedUrl;
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
  }

  private static class ComponentVersion {
    private transient String name, version, buildDate, artifactId, groupId;
    private transient String artifactIdentifier;
    private transient String nameAndVersion;
    private transient String versionOnly;

    ComponentVersion(Properties p, String defaultComponentName) {
      this.name = p.getProperty(COMPONENT_KEY, defaultComponentName);
      this.groupId = p.getProperty(GROUP_KEY, NO_GROUP);
      this.buildDate = p.getProperty(BUILD_DATE_KEY, NO_BUILD_DATE);
      this.artifactId = p.getProperty(ARTIFACT_KEY, defaultComponentName);
      this.version =  p.getProperty(VERSION_KEY, NO_VERSION);
      nameAndVersion = String.format("%1$s: %2$s(%3$s)", name, version, buildDate);
      versionOnly = String.format("%1$s(%2$s)", version, buildDate);
      artifactIdentifier = String.format("%s:%s:%s", groupId, artifactId, version);
    }

    public String toString() {
      return nameAndVersion;
    }
    
    public String artifactIdentifier() {
      return artifactIdentifier;
    }
  }
}
