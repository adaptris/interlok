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
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import com.adaptris.core.util.PropertyHelper;

/**
 * Class to report module version numbers.
 *
 */
public abstract class VersionReport {

  private static VersionReport instance = null;
  private static final String NO_BUILD_DATE = "Unspecified";
  private static final String NO_VERSION = "No Build-Version Information";
  private static final String NO_COMPONENT = "unknown";
  private static final String NO_GROUP = "unknown";

  private static final ComponentVersion UNSPECIFIED_VERSION = new ComponentVersion(new Properties(), NO_COMPONENT);
  private static final Pattern UNKONWN_ARTIFACT_PATTERN = Pattern.compile("jar:file:.*/(.*)!/META-INF.*");

  private static final String BUILD_DATE_KEY = "build.date";
  private static final String BUILD_INFO_KEY = "build.info";
  private static final String VERSION_KEY = "build.version";
  private static final String COMPONENT_KEY = "component.name";
  private static final String ARTIFACT_KEY = "artifactId";
  private static final String GROUP_KEY = "groupId";

  private static final String VERSION_FILE = "META-INF/adaptris-version";

  private transient Set<ComponentVersion> componentVersions;
  private transient ComponentVersion adapterVersion;

  private static final List<String> INTERLOK_NAMES =
      Collections.unmodifiableList(Arrays.asList("BASE INTERLOK", "INTERLOK CORE/BASE"));

  protected VersionReport() {
    buildReport();
  }

  /**
   * Get the singleton of the VersionReport.
   *
   * @return the VersionReport object
   */
  public synchronized static VersionReport getInstance() {
    instance = ObjectUtils.defaultIfNull(instance, new VersionReportImp());
    return instance;
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
    return new TreeSet<String>(componentVersions.stream().map(v -> v.toString()).collect(Collectors.toSet()));
  }

  /**
   * Get maven style artifact identifiers.
   *
   * @return version numbers of all available adaptris modules.
   */
  public Collection<String> getArtifactIdentifiers() {
    return new TreeSet<String>(componentVersions.stream().map(v -> v.artifactIdentifier()).collect(Collectors.toSet()));
  }

  protected Set<ComponentVersion> buildFromVersionFile() throws IOException {
    Enumeration<URL> versions = this.getClass().getClassLoader().getResources(VERSION_FILE);
    Set<ComponentVersion> result = new HashSet<ComponentVersion>();
    while (versions.hasMoreElements()) {
      ComponentVersion v = createVersionInfo(versions.nextElement());
      result.add(v);
    }
    return result;
  }

  private static ComponentVersion createVersionInfo(URL url) {
    return new ComponentVersion(PropertyHelper.loadQuietly(url), jarName(url.toString()));
  }

  // jar:file:/path/to/interlok-common-3.8-SNAPSHOT.jar!/META-INF/adaptris-version
  // into just interlok-common-3.8-SNAPSHOT.jar
  protected static String jarName(String stringifiedUrl) {
    Matcher m = UNKONWN_ARTIFACT_PATTERN.matcher(stringifiedUrl);
    if (m.matches()) {
      return m.group(1);
    }
    return stringifiedUrl;
  }

  protected void buildReport() {
    try {
      componentVersions = buildFromVersionFile();
      if (componentVersions.size() == 0) {
        componentVersions.add(UNSPECIFIED_VERSION);
      }
      adapterVersion =
          componentVersions.stream().filter(v -> isBaseInterlok(v.name)).findAny()
              .orElse(UNSPECIFIED_VERSION);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean isBaseInterlok(String v) {
    return INTERLOK_NAMES.contains(v.toUpperCase());
  }

  protected static class ComponentVersion {
    private transient String name, version, buildDate, artifactId, groupId, buildInfo;
    private transient String artifactIdentifier;
    private transient String nameAndVersion;
    private transient String versionOnly;

    protected ComponentVersion(Properties p, String defaultComponentName) {
      name = p.getProperty(COMPONENT_KEY, defaultComponentName);
      groupId = p.getProperty(GROUP_KEY, NO_GROUP);
      buildDate = p.getProperty(BUILD_DATE_KEY, NO_BUILD_DATE);
      artifactId = p.getProperty(ARTIFACT_KEY, defaultComponentName);
      version =  p.getProperty(VERSION_KEY, NO_VERSION);
      buildInfo = StringUtils.trimToEmpty(StringUtils.chomp(p.getProperty(BUILD_INFO_KEY, "")));
      if (!StringUtils.isEmpty(buildInfo)) {
        nameAndVersion = String.format("%1$s: %2$s(%3$s:%4$s)", name, version, buildDate, buildInfo);
        versionOnly = String.format("%1$s(%2$s:%3$s)", version, buildDate, buildInfo);
      } else {
        nameAndVersion = String.format("%1$s: %2$s(%3$s)", name, version, buildDate);
        versionOnly = String.format("%1$s(%2$s)", version, buildDate);
      }
      artifactIdentifier = String.format("%s:%s:%s", groupId, artifactId, version);
    }

    @Override
    public String toString() {
      return nameAndVersion;
    }

    public String artifactIdentifier() {
      return artifactIdentifier;
    }
  }

  private static class VersionReportImp extends VersionReport {

  }
}
