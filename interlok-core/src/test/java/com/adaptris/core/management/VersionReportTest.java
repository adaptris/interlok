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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

public class VersionReportTest {


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
    System.err.println(components);
    // Should be at least core+common+apt
    assertTrue(components.size() > 1);
  }

}
