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

package com.adaptris.core.services.metadata;

import static com.adaptris.core.services.metadata.ReadMetadataFromFilesystemTest.BASE_DIR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.EmptyFileNameCreator;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.ServiceException;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.services.metadata.WriteMetadataToFilesystem.OutputStyle;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;

public class WriteMetadataToFilesystemTest extends MetadataServiceExample {


  @Test
  public void testDestination() throws Exception {
    WriteMetadataToFilesystem service = new WriteMetadataToFilesystem();
    assertNull(service.getBaseUrl());
    assertNull(service.getDestination());
    try {
      LifecycleHelper.init(service);
      fail("Service initialised with a null destination and base URL");
    }
    catch (CoreException expected) {

    }
    service.setDestination(new ConfiguredProduceDestination("dest"));
    assertNotNull(service.getDestination());
    assertEquals("dest", service.getDestination().getDestination(new DefaultMessageFactory().newMessage()));
    try {
      service.setDestination(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertNotNull(service.getDestination());
    assertEquals("dest", service.getDestination().getDestination(new DefaultMessageFactory().newMessage()));
  }

  @Test
  public void testSetMetadataFilter() throws Exception {
    WriteMetadataToFilesystem service = new WriteMetadataToFilesystem();
    assertNull(service.getMetadataFilter());
    service.setMetadataFilter(new RegexMetadataFilter());
    assertEquals(RegexMetadataFilter.class, service.getMetadataFilter().getClass());
    service.setMetadataFilter(null);
    assertEquals(NoOpMetadataFilter.class, service.metadataFilter().getClass());
  }

  @Test
  public void testOverwriteIfExists() throws Exception {
    WriteMetadataToFilesystem service = new WriteMetadataToFilesystem();
    assertNull(service.getOverwriteIfExists());
    assertFalse(service.overwriteIfExists());
    service.setOverwriteIfExists(null);
    assertFalse(service.overwriteIfExists());
    service.setOverwriteIfExists(Boolean.TRUE);
    assertNotNull(service.getOverwriteIfExists());
    assertTrue(service.overwriteIfExists());
  }

  @Test
  public void testOutputStyle() throws Exception {
    WriteMetadataToFilesystem service = new WriteMetadataToFilesystem();
    assertNull(service.getOutputStyle());
    service.setOutputStyle(OutputStyle.Text);
    assertEquals(OutputStyle.Text, service.getOutputStyle());
  }

  @Test
  public void testFilenameCreator() throws Exception {
    WriteMetadataToFilesystem service = new WriteMetadataToFilesystem();
    assertEquals(FormattedFilenameCreator.class, service.filenameCreator().getClass());
    service.setFilenameCreator(new EmptyFileNameCreator());
    assertEquals(EmptyFileNameCreator.class, service.getFilenameCreator().getClass());
    service.setFilenameCreator(null);
    assertEquals(FormattedFilenameCreator.class, service.filenameCreator().getClass());
  }

  @Test
  public void testService_Default() throws Exception {
    String subDir = new GuidGenerator().getUUID().replaceAll(":", "").replaceAll("-", "");
    AdaptrisMessage msg = createMessage();
    WriteMetadataToFilesystem service = createService(subDir);
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_DIR), true));
    String propsFilename = parentDir.getCanonicalPath() + "/" + subDir + "/" + msg.getUniqueId();
    execute(service, msg);
    Properties p = readProperties(new File(propsFilename), false);
    assertTrue(p.containsKey("key5"));
    assertEquals("v5", p.getProperty("key5"));
  }

  @Test
  public void testService_OutputXml() throws Exception {
    String subDir = new GuidGenerator().getUUID().replaceAll(":", "").replaceAll("-", "");
    AdaptrisMessage msg = createMessage();
    WriteMetadataToFilesystem service = createService(subDir);
    service.setOutputStyle(OutputStyle.XML);
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_DIR), true));
    String propsFilename = parentDir.getCanonicalPath() + "/" + subDir + "/" + msg.getUniqueId();
    execute(service, msg);
    Properties p = readProperties(new File(propsFilename), true);
    assertTrue(p.containsKey("key5"));
    assertEquals("v5", p.getProperty("key5"));
  }

  @Test
  public void testService_OutputUnknown() throws Exception {
    String subDir = new GuidGenerator().getUUID().replaceAll(":", "").replaceAll("-", "");
    AdaptrisMessage msg = createMessage();
    WriteMetadataToFilesystem service = createService(subDir);
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_DIR), true));
    String propsFilename = parentDir.getCanonicalPath() + "/" + subDir + "/" + msg.getUniqueId();
    execute(service, msg);
    Properties p = readProperties(new File(propsFilename), false);
    assertTrue(p.containsKey("key5"));
    assertEquals("v5", p.getProperty("key5"));
  }

  @Test
  public void testService_Filter() throws Exception {
    String subDir = new GuidGenerator().getUUID().replaceAll(":", "").replaceAll("-", "");
    AdaptrisMessage msg = createMessage();
    WriteMetadataToFilesystem service = createService(subDir);
    RegexMetadataFilter filter = new RegexMetadataFilter();
    filter.addIncludePattern("alt_key.*");
    filter.addExcludePattern("^key.*");
    service.setMetadataFilter(filter);
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_DIR), true));
    String propsFilename = parentDir.getCanonicalPath() + "/" + subDir + "/" + msg.getUniqueId();
    execute(service, msg);
    Properties p = readProperties(new File(propsFilename), false);
    assertFalse(p.containsKey("key5"));
    assertTrue(p.containsKey("alt_key5"));
    assertEquals("av5", p.getProperty("alt_key5"));
  }

  @Test
  public void testService_OverwriteFalseFileNonExistent() throws Exception {
    String subDir = new GuidGenerator().getUUID().replaceAll(":", "").replaceAll("-", "");
    AdaptrisMessage msg = createMessage();
    WriteMetadataToFilesystem service = createService(subDir);
    service.setOverwriteIfExists(false);
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_DIR), true));
    String propsFilename = parentDir.getCanonicalPath() + "/" + subDir + "/" + msg.getUniqueId();
    execute(service, msg);
    Properties p = readProperties(new File(propsFilename), false);
    assertTrue(p.containsKey("key5"));
    assertEquals("v5", p.getProperty("key5"));
  }

  @Test
  public void testService_OverwriteFalseFileExists() throws Exception {
    String subDir = new GuidGenerator().getUUID().replaceAll(":", "").replaceAll("-", "");
    AdaptrisMessage msg = createMessage();
    WriteMetadataToFilesystem service = createService(subDir);
    service.setOverwriteIfExists(false);
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_DIR), true));
    String propsFilename = parentDir.getCanonicalPath() + "/" + subDir + "/" + msg.getUniqueId();
    createEmptyFile(new File(propsFilename));
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Test
  public void testService_OverwriteTrueFileNonExistent() throws Exception {
    String subDir = new GuidGenerator().getUUID().replaceAll(":", "").replaceAll("-", "");
    AdaptrisMessage msg = createMessage();
    WriteMetadataToFilesystem service = createService(subDir);
    service.setOverwriteIfExists(true);
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_DIR), true));
    String propsFilename = parentDir.getCanonicalPath() + "/" + subDir + "/" + msg.getUniqueId();
    execute(service, msg);
    Properties p = readProperties(new File(propsFilename), false);
    assertTrue(p.containsKey("key5"));
    assertEquals("v5", p.getProperty("key5"));
  }

  @Test
  public void testService_OverwriteTrueFileExists() throws Exception {
    String subDir = new GuidGenerator().getUUID().replaceAll(":", "").replaceAll("-", "");
    AdaptrisMessage msg = createMessage();
    WriteMetadataToFilesystem service = createService(subDir);
    service.setOverwriteIfExists(true);
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_DIR), true));
    String propsFilename = parentDir.getCanonicalPath() + "/" + subDir + "/" + msg.getUniqueId();
    createEmptyFile(new File(propsFilename));
    execute(service, msg);
    Properties p = readProperties(new File(propsFilename), false);
    assertTrue(p.containsKey("key5"));
    assertEquals("v5", p.getProperty("key5"));
  }

  private void createEmptyFile(File f) throws IOException {
    File parent = f.getParentFile();
    if (parent != null) {
      parent.mkdirs();
    }
    FileOutputStream out = new FileOutputStream(f);
    out.write(new byte[0]);
    out.close();

  }

  private AdaptrisMessage createMessage() {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    for (int i = 1; i <= 10; i++) {
      msg.addMetadata("key" + i, "v" + i);
      msg.addMetadata("alt_key" + i, "av" + i);
    }
    return msg;
  }

  private WriteMetadataToFilesystem createService(String subDir) {
    String baseString = PROPERTIES.getProperty(BASE_DIR);
    WriteMetadataToFilesystem service = new WriteMetadataToFilesystem(new ConfiguredProduceDestination(baseString + "/" + subDir));
    return service;
  }

  private Properties readProperties(File filename, boolean xml) throws IOException {
    Properties p = new Properties();
    try (InputStream in = new FileInputStream(filename)) {
      if (xml) {
        p.loadFromXML(in);
      }
      else {
        p.load(in);
      }
    }
    return p;
  }

  @Override
  protected WriteMetadataToFilesystem retrieveObjectForSampleConfig() {
    WriteMetadataToFilesystem service = new WriteMetadataToFilesystem();
    service.setDestination(new ConfiguredProduceDestination("file:////path/to/directory"));
    service.setOutputStyle(OutputStyle.Text);
    return service;
  }

}
