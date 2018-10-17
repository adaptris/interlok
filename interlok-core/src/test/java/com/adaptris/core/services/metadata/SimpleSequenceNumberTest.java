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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.SequenceNumberServiceExample;
import com.adaptris.core.services.metadata.SimpleSequenceNumberService.OverflowBehaviour;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;

@SuppressWarnings("deprecation")
public class SimpleSequenceNumberTest extends SequenceNumberServiceExample {

  private static final String SERVICE_PROPERTY_KEY = "SimpleSequenceNumberService.next";
  private static final String DEFAULT_NUMBER_FORMAT = "000000000";
  private static final String DEFAULT_METADATA_KEY = "sequence_number";
  private static final String KEY_BASEDIR = "SimpleSequenceNumberTest.basedir";

  public SimpleSequenceNumberTest(String name) {
    super(name);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    new File(PROPERTIES.getProperty(KEY_BASEDIR)).mkdirs();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    FileUtils.deleteQuietly(new File(PROPERTIES.getProperty(KEY_BASEDIR)));
  }

  public void testSetNumberFormat() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    assertEquals("0", service.getNumberFormat());
    try {
      service.setNumberFormat(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals("0", service.getNumberFormat());
    service.setNumberFormat("00");
    assertEquals("00", service.getNumberFormat());
  }

  public void testSetOverflowBehaviour() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    assertNull(service.getOverflowBehaviour());
    service.setOverflowBehaviour(null);
    assertNull(service.getOverflowBehaviour());
    service.setOverflowBehaviour(OverflowBehaviour.Continue);
    assertEquals(OverflowBehaviour.Continue, service.getOverflowBehaviour());
  }

  public void testSetAlwaysReplaceMetadata() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    assertNull(service.getAlwaysReplaceMetadata());
    service.setAlwaysReplaceMetadata(null);
    assertNull(service.getAlwaysReplaceMetadata());
    service.setAlwaysReplaceMetadata(Boolean.TRUE); // Not that this will mean anything @ runtime.
    assertEquals(Boolean.TRUE, service.getAlwaysReplaceMetadata());
  }

  public void testSetSequenceNumberFile() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    assertNull(service.getSequenceNumberFile());
    try {
      service.setSequenceNumberFile(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertNull(service.getSequenceNumberFile());
    service.setSequenceNumberFile("00");
    assertEquals("00", service.getSequenceNumberFile());
  }

  public void testSetMetadataKey() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    assertNull(service.getMetadataKey());
    try {
      service.setMetadataKey(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertNull(service.getMetadataKey());
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    assertEquals(DEFAULT_METADATA_KEY, service.getMetadataKey());
  }

  public void testSetMaximum() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    assertNull(service.getMaximumSequenceNumber());
    service.setMaximumSequenceNumber(null);
    assertNull(service.getMaximumSequenceNumber());
    service.setMaximumSequenceNumber(12L);
    assertEquals(new Long(12L), service.getMaximumSequenceNumber());
  }

  public void testInit() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    try {
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {
    }
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    try {
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {
    }
    service.setSequenceNumberFile(new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath());
    LifecycleHelper.init(service);
  }

  public void testDoService_NonExistentFile() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    service.setSequenceNumberFile(filename);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(2, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("1", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  public void testDoService_FileExists() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 5);
    service.setSequenceNumberFile(filename);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(6, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("5", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  public void testDoService_FileIsDirectory() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    File dir = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID());
    dir.mkdirs();
    service.setSequenceNumberFile(dir.getCanonicalPath());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  public void testDoService_NoOverwriteNoMetadata() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    service.setSequenceNumberFile(filename);
    service.setAlwaysReplaceMetadata(Boolean.FALSE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(2, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("1", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  public void testDoService_MetadataExistsNoOverwrite() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    service.setSequenceNumberFile(filename);
    service.setAlwaysReplaceMetadata(Boolean.FALSE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(DEFAULT_METADATA_KEY, "testDoService_MetadataExistsNoOverwrite");
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("testDoService_MetadataExistsNoOverwrite", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  public void testDoService_MetadataExistsOverwrite() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    service.setSequenceNumberFile(filename);
    service.setAlwaysReplaceMetadata(Boolean.TRUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(DEFAULT_METADATA_KEY, "testDoService_MetadataExistsNoOverwrite");
    execute(service, msg);
    assertEquals(2, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("1", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  public void testDoService_OverflowBehaviourContinue() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 10);
    service.setSequenceNumberFile(filename);
    service.setNumberFormat("0");
    service.setOverflowBehaviour(OverflowBehaviour.Continue);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(11, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("10", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  public void testDoService_OverflowBehaviourUndefinied() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 10);
    service.setSequenceNumberFile(filename);
    service.setNumberFormat("0");
    service.setOverflowBehaviour(null);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(11, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("10", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  public void testDoService_OverflowBehaviourResetToOne() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 10);
    service.setSequenceNumberFile(filename);
    service.setNumberFormat("0");
    service.setOverflowBehaviour(OverflowBehaviour.ResetToOne);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(2, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("1", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  public void testDoService_NumberFormatting() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    service.setSequenceNumberFile(filename);
    service.setNumberFormat(DEFAULT_NUMBER_FORMAT);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(2, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("000000001", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  public void testDoService_MaximumAndSetNotHit() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 10);
    service.setSequenceNumberFile(filename);
    service.setMaximumSequenceNumber(12L);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(11, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("10", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  public void testDoService_MaximumSetHit() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 13);
    service.setSequenceNumberFile(filename);
    service.setMaximumSequenceNumber(12L);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(2, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("1", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  public void testDoService_MaximumSetHitPropertyExceededMax() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 13);
    service.setSequenceNumberFile(filename);
    service.setMaximumSequenceNumber(12L);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(2, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("1", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  public void testDoService_MaximumAndNumberFormattingSetNotHit() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 10);
    service.setSequenceNumberFile(filename);
    service.setNumberFormat("000");
    service.setMaximumSequenceNumber(12L);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(11, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("010", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  public void testDoService_MaximumAndNumberFormattingSetHit() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 12);
    service.setSequenceNumberFile(filename);
    service.setNumberFormat("000");
    service.setMaximumSequenceNumber(12L);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(1, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("012", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  public void testDoService_MaximumAndNumberFormattingSetHitPropertyExceededMax() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 13);
    service.setSequenceNumberFile(filename);
    service.setNumberFormat("000");
    service.setMaximumSequenceNumber(12L);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(2, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("001", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  public void testDoService_MaximumAndOverflowBehaviourResetToOne() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 10);
    service.setSequenceNumberFile(filename);
    service.setNumberFormat("0");
    service.setOverflowBehaviour(OverflowBehaviour.ResetToOne);
    service.setMaximumSequenceNumber(12L);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(2, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("1", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.setMetadataKey("The_Metadata_Key_Where_The_Sequence_Number_Will_Be_Stored");
    service.setSequenceNumberFile("/path/to/the/sequence/number/file");
    service.setNumberFormat(DEFAULT_NUMBER_FORMAT);
    service.setOverflowBehaviour(OverflowBehaviour.Continue);
    service.setMaximumSequenceNumber(12L);
    return service;
  }

  private long getSequenceNumber(String filename) throws Exception {
    Properties p = new Properties();
    InputStream in = null;
    try {
      in = new FileInputStream(new File(filename));
      p.load(in);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
    return Long.parseLong(p.getProperty(SERVICE_PROPERTY_KEY));
  }

  private void createPropertyFile(String filename, int seq) throws Exception {
    Properties p = new Properties();
    p.setProperty(SERVICE_PROPERTY_KEY, String.valueOf(seq));
    OutputStream out = null;
    try {
      out = new FileOutputStream(new File(filename));
      p.store(out, "");
    }
    finally {
      IOUtils.closeQuietly(out);
    }
  }
}
