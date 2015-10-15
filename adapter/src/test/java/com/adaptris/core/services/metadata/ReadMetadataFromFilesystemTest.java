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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.EmptyFileNameCreator;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.services.metadata.ReadMetadataFromFilesystem.InputStyle;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;

public class ReadMetadataFromFilesystemTest extends MetadataServiceExample {

  public static final String BASE_DIR = "metadata.MetadataToFileSystem.baseDirUrl";

  public ReadMetadataFromFilesystemTest(String name) {
    super(name);
  }

  @Override
  public void setUp() {
  }

  public void testDestination() throws Exception {
    ReadMetadataFromFilesystem service = new ReadMetadataFromFilesystem();
    assertNull(service.getDestination());
    try {
      LifecycleHelper.init(service);
      fail("Service initialised with a null destination");
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

  public void testOverwriteExistingMetadata() throws Exception {
    ReadMetadataFromFilesystem service = new ReadMetadataFromFilesystem();
    assertNull(service.getOverwriteExistingMetadata());
    assertFalse(service.overwriteExistingMetadata());
    service.setOverwriteExistingMetadata(null);
    assertFalse(service.overwriteExistingMetadata());
    service.setOverwriteExistingMetadata(Boolean.TRUE);
    assertNotNull(service.getOverwriteExistingMetadata());
    assertTrue(service.overwriteExistingMetadata());
  }

  public void testInputStyle() throws Exception {
    ReadMetadataFromFilesystem service = new ReadMetadataFromFilesystem();
    assertNull(service.getInputStyle());
    service.setInputStyle(InputStyle.Text);
    assertEquals(InputStyle.Text, service.getInputStyle());
  }

  public void testFilenameCreator() throws Exception {
    ReadMetadataFromFilesystem service = new ReadMetadataFromFilesystem();
    assertEquals(FormattedFilenameCreator.class, service.getFileNameCreator().getClass());
    service.setFileNameCreator(new EmptyFileNameCreator());
    assertEquals(EmptyFileNameCreator.class, service.getFileNameCreator().getClass());
    try {
      service.setFileNameCreator(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(EmptyFileNameCreator.class, service.getFileNameCreator().getClass());
  }

  public void testService_Default() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    ReadMetadataFromFilesystem service = createService(subDir);
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_DIR), true));
    String propsFilename = parentDir.getCanonicalPath() + "/" + subDir + "/" + msg.getUniqueId();
    Properties p = createProperties();
    writeProperties(p, new File(propsFilename), false);
    execute(service, msg);
    assertTrue(msg.containsKey("key5"));
    assertEquals("v5", msg.getMetadataValue("key5"));
  }

  public void testService_InputXml() throws Exception {
    String subDir = new GuidGenerator().getUUID().replaceAll(":", "").replaceAll("-", "");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    ReadMetadataFromFilesystem service = createService(subDir);
    service.setInputStyle(InputStyle.XML);
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_DIR), true));
    String propsFilename = parentDir.getCanonicalPath() + "/" + subDir + "/" + msg.getUniqueId();
    Properties p = createProperties();
    writeProperties(p, new File(propsFilename), true);
    execute(service, msg);
    assertTrue(msg.containsKey("key5"));
    assertEquals("v5", msg.getMetadataValue("key5"));
  }

  public void testService_InputUnknown() throws Exception {
    String subDir = new GuidGenerator().getUUID().replaceAll(":", "").replaceAll("-", "");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    ReadMetadataFromFilesystem service = createService(subDir);
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_DIR), true));
    String propsFilename = parentDir.getCanonicalPath() + "/" + subDir + "/" + msg.getUniqueId();
    Properties p = createProperties();
    writeProperties(p, new File(propsFilename), false);
    execute(service, msg);
    assertTrue(msg.containsKey("key5"));
    assertEquals("v5", msg.getMetadataValue("key5"));
  }

  public void testService_NoFile() throws Exception {
    String subDir = new GuidGenerator().getUUID().replaceAll(":", "").replaceAll("-", "");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    ReadMetadataFromFilesystem service = createService(subDir);
    execute(service, msg);
    assertFalse(msg.containsKey("key5"));
  }

  public void testService_OverwriteExistingMetadata() throws Exception {
    String subDir = new GuidGenerator().getUUID().replaceAll(":", "").replaceAll("-", "");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    msg.addMetadata("key5", "MyValue");
    ReadMetadataFromFilesystem service = createService(subDir);
    service.setOverwriteExistingMetadata(true);
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_DIR), true));
    String propsFilename = parentDir.getCanonicalPath() + "/" + subDir + "/" + msg.getUniqueId();
    Properties p = createProperties();
    writeProperties(p, new File(propsFilename), false);
    execute(service, msg);
    assertTrue(msg.containsKey("key5"));
    assertEquals("v5", msg.getMetadataValue("key5"));
  }


  public void testService_DoNotOverwriteExistingMetadata() throws Exception {
    String subDir = new GuidGenerator().getUUID().replaceAll(":", "").replaceAll("-", "");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    msg.addMetadata("key5", "MyValue");
    ReadMetadataFromFilesystem service = createService(subDir);
    service.setOverwriteExistingMetadata(false);
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_DIR), true));
    String propsFilename = parentDir.getCanonicalPath() + "/" + subDir + "/" + msg.getUniqueId();
    Properties p = createProperties();
    writeProperties(p, new File(propsFilename), false);
    execute(service, msg);
    assertTrue(msg.containsKey("key5"));
    assertEquals("MyValue", msg.getMetadataValue("key5"));
  }

  private Properties createProperties() {
    Properties result = new Properties();
    for (int i = 1; i <= 10; i++) {
      result.setProperty("key" + i, "v" + i);
      result.setProperty("alt_key" + i, "av" + i);
    }
    return result;
  }

  private void writeProperties(Properties p, File filename, boolean xml) throws Exception {
    OutputStream out = null;
    try {
      File parent = filename.getParentFile();
      if (parent != null) {
        parent.mkdirs();
      }
      out = new FileOutputStream(filename);
      if (xml) {
        p.storeToXML(out, "");
      }
      else {
        p.store(out, "");
      }
    }
    finally {
      IOUtils.closeQuietly(out);
    }

  }

  private ReadMetadataFromFilesystem createService(String subDir) {
    String baseString = PROPERTIES.getProperty(BASE_DIR);
    ReadMetadataFromFilesystem service = new ReadMetadataFromFilesystem(new ConfiguredProduceDestination(baseString + "/" + subDir));
    return service;
  }

  @Override
  protected ReadMetadataFromFilesystem retrieveObjectForSampleConfig() {
    ReadMetadataFromFilesystem service = new ReadMetadataFromFilesystem();
    service.setDestination(new ConfiguredProduceDestination("file:////path/to/directory"));
    service.setInputStyle(InputStyle.Text);
    return service;
  }

}
