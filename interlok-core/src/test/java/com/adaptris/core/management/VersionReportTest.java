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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class VersionReportTest {


  @Test
  public void testSingleton() throws Exception {
    VersionReport report = VersionReport.getInstance();
    VersionReport r2 = VersionReport.getInstance();
    assertSame(report, r2);
  }

  @Test
  public void testBuildVersion() throws Exception {
    VersionReport report = VersionReport.getInstance();
    assertNotNull(report.getAdapterBuildVersion());
    assertNotSame("No Build-Version Information", report.getAdapterBuildVersion());
  }

  @Test
  public void testGetReport() throws Exception {
    VersionReport report = VersionReport.getInstance();
    Collection<String> components = report.getReport();
    // Should be at least core+common+apt
    assertTrue(components.size() > 1);
  }

  @Test
  public void testGetArtifactIdentifiers() throws Exception {
    VersionReport report = VersionReport.getInstance();
    Collection<String> components = report.getArtifactIdentifiers();
    // Should be at least core+common+apt
    assertTrue(components.size() > 1);
  }

  @Test
  public void testJarName() throws Exception {
    String url = "jar:file:/path/to/interlok-common-3.8-SNAPSHOT.jar!/META-INF/adaptris-version";
    assertEquals("interlok-common-3.8-SNAPSHOT.jar", VersionReport.jarName(url));
    String url2 = "/META-INF/adaptris-version";
    assertEquals("/META-INF/adaptris-version", VersionReport.jarName(url2));
  }

  @Test(expected = RuntimeException.class)
  public void testBuildReport_Broken() throws Exception {
    new BrokenVersionReport();
  }

  @Test
  public void testBuildReport_Empty() throws Exception {
    Collection<String> report = new EmptyVersionReport().getReport();
    assertEquals(1, report.size());
  }

  private class BrokenVersionReport extends VersionReport {
    @Override
    protected Set<ComponentVersion> buildFromVersionFile() throws IOException {
      throw new IOException();
    }
  }

  private class EmptyVersionReport extends VersionReport {
    @Override
    protected Set<ComponentVersion> buildFromVersionFile() throws IOException {
      return new HashSet<>();
    }
  }
}
