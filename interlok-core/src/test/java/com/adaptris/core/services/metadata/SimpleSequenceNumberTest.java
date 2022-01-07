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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.SequenceNumber.OverflowBehaviour;
import com.adaptris.core.services.SequenceNumberServiceExample;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("deprecation")
public class SimpleSequenceNumberTest extends SequenceNumberServiceExample {

  private static final String SERVICE_PROPERTY_KEY = "SimpleSequenceNumberService.next";
  private static final String DEFAULT_NUMBER_FORMAT = "000000000";
  private static final String DEFAULT_METADATA_KEY = "sequence_number";
  private static final String KEY_BASEDIR = "SimpleSequenceNumberTest.basedir";


  @Before
  public void setUp() throws Exception {
    new File(PROPERTIES.getProperty(KEY_BASEDIR)).mkdirs();
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.deleteQuietly(new File(PROPERTIES.getProperty(KEY_BASEDIR)));
  }

  @Test
  public void testSetNumberFormat() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    assertEquals("0", service.getSequenceNumber().getNumberFormat());
    try {
      service.getSequenceNumber().setNumberFormat(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals("0", service.getSequenceNumber().getNumberFormat());
    service.getSequenceNumber().setNumberFormat("00");
    assertEquals("00", service.getSequenceNumber().getNumberFormat());
  }

  @Test
  public void testSetOverflowBehaviour() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    assertNull(service.getSequenceNumber().getOverflowBehaviour());
    service.getSequenceNumber().setOverflowBehaviour(null);
    assertNull(service.getSequenceNumber().getOverflowBehaviour());
    service.getSequenceNumber().setOverflowBehaviour(OverflowBehaviour.Continue);
    assertEquals(OverflowBehaviour.Continue, service.getSequenceNumber().getOverflowBehaviour());
  }

  @Test
  public void testSetAlwaysReplaceMetadata() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    assertNull(service.getSequenceNumber().getAlwaysReplaceMetadata());
    service.getSequenceNumber().setAlwaysReplaceMetadata(null);
    assertNull(service.getSequenceNumber().getAlwaysReplaceMetadata());
    service.getSequenceNumber().setAlwaysReplaceMetadata(Boolean.TRUE); // Not that this will mean anything @ runtime.
    assertEquals(Boolean.TRUE, service.getSequenceNumber().getAlwaysReplaceMetadata());
  }

  @Test
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

  @Test
  public void testSetMetadataKey() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    assertNull(service.getSequenceNumber().getMetadataKey());
    try {
      service.getSequenceNumber().setMetadataKey(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertNull(service.getSequenceNumber().getMetadataKey());
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
    assertEquals(DEFAULT_METADATA_KEY, service.getSequenceNumber().getMetadataKey());
  }

  @Test
  public void testSetMaximum() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    assertNull(service.getSequenceNumber().getMaximumSequenceNumber());
    service.getSequenceNumber().setMaximumSequenceNumber(null);
    assertNull(service.getSequenceNumber().getMaximumSequenceNumber());
    service.getSequenceNumber().setMaximumSequenceNumber(12L);
    assertEquals(new Long(12L), service.getSequenceNumber().getMaximumSequenceNumber());
  }

  @Test
  public void testInit() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    try {
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {
    }
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
    try {
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {
    }
    service.setSequenceNumberFile(new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath());
    LifecycleHelper.init(service);
  }

  @Test
  public void testDoService_NonExistentFile() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    service.setSequenceNumberFile(filename);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(2, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("1", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testDoService_FileExists() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 5);
    service.setSequenceNumberFile(filename);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(6, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("5", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testDoService_FileIsDirectory() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
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

  @Test
  public void testDoService_NoOverwriteNoMetadata() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    service.setSequenceNumberFile(filename);
    service.getSequenceNumber().setAlwaysReplaceMetadata(Boolean.FALSE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(2, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("1", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testDoService_MetadataExistsNoOverwrite() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    service.setSequenceNumberFile(filename);
    service.getSequenceNumber().setAlwaysReplaceMetadata(Boolean.FALSE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(DEFAULT_METADATA_KEY, "testDoService_MetadataExistsNoOverwrite");
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("testDoService_MetadataExistsNoOverwrite", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testDoService_MetadataExistsOverwrite() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    service.setSequenceNumberFile(filename);
    service.getSequenceNumber().setAlwaysReplaceMetadata(Boolean.TRUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(DEFAULT_METADATA_KEY, "testDoService_MetadataExistsNoOverwrite");
    execute(service, msg);
    assertEquals(2, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("1", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testDoService_OverflowBehaviourContinue() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 10);
    service.setSequenceNumberFile(filename);
    service.getSequenceNumber().setNumberFormat("0");
    service.getSequenceNumber().setOverflowBehaviour(OverflowBehaviour.Continue);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(11, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("10", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testDoService_OverflowBehaviourUndefinied() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 10);
    service.setSequenceNumberFile(filename);
    service.getSequenceNumber().setNumberFormat("0");
    service.getSequenceNumber().setOverflowBehaviour(null);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(11, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("10", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testDoService_OverflowBehaviourResetToOne() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 10);
    service.setSequenceNumberFile(filename);
    service.getSequenceNumber().setNumberFormat("0");
    service.getSequenceNumber().setOverflowBehaviour(OverflowBehaviour.ResetToOne);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(2, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("1", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testDoService_NumberFormatting() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    service.setSequenceNumberFile(filename);
    service.getSequenceNumber().setNumberFormat(DEFAULT_NUMBER_FORMAT);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(2, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("000000001", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testDoService_MaximumAndSetNotHit() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 10);
    service.setSequenceNumberFile(filename);
    service.getSequenceNumber().setMaximumSequenceNumber(12L);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(11, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("10", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testDoService_MaximumSetHit() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 13);
    service.setSequenceNumberFile(filename);
    service.getSequenceNumber().setMaximumSequenceNumber(12L);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(2, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("1", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testDoService_MaximumSetHitPropertyExceededMax() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 13);
    service.setSequenceNumberFile(filename);
    service.getSequenceNumber().setMaximumSequenceNumber(12L);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(2, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("1", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testDoService_MaximumAndNumberFormattingSetNotHit() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 10);
    service.setSequenceNumberFile(filename);
    service.getSequenceNumber().setNumberFormat("000");
    service.getSequenceNumber().setMaximumSequenceNumber(12L);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(11, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("010", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testDoService_MaximumAndNumberFormattingSetHit() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 12);
    service.setSequenceNumberFile(filename);
    service.getSequenceNumber().setNumberFormat("000");
    service.getSequenceNumber().setMaximumSequenceNumber(12L);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(1, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("012", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testDoService_MaximumAndNumberFormattingSetHitPropertyExceededMax() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 13);
    service.setSequenceNumberFile(filename);
    service.getSequenceNumber().setNumberFormat("000");
    service.getSequenceNumber().setMaximumSequenceNumber(12L);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(2, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("001", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testDoService_MaximumAndOverflowBehaviourResetToOne() throws Exception {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.getSequenceNumber().setMetadataKey(DEFAULT_METADATA_KEY);
    String filename = new File(PROPERTIES.getProperty(KEY_BASEDIR), new GuidGenerator().getUUID()).getCanonicalPath();
    createPropertyFile(filename, 10);
    service.setSequenceNumberFile(filename);
    service.getSequenceNumber().setNumberFormat("0");
    service.getSequenceNumber().setOverflowBehaviour(OverflowBehaviour.ResetToOne);
    service.getSequenceNumber().setMaximumSequenceNumber(12L);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals(2, getSequenceNumber(filename));
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("1", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    SimpleSequenceNumberService service = new SimpleSequenceNumberService();
    service.getSequenceNumber().setMetadataKey("The_Metadata_Key_Where_The_Sequence_Number_Will_Be_Stored");
    service.setSequenceNumberFile("/path/to/the/sequence/number/file");
    service.getSequenceNumber().setNumberFormat(DEFAULT_NUMBER_FORMAT);
    service.getSequenceNumber().setOverflowBehaviour(OverflowBehaviour.Continue);
    service.getSequenceNumber().setMaximumSequenceNumber(12L);
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
