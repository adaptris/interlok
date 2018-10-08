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

package com.adaptris.core.lms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.oro.io.Perl5FilenameFilter;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultAdaptrisMessageImp;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.MimeEncoder;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.fs.FsConsumer;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.fs.FsMessageConsumerTest;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.text.mime.MultiPartOutput;

public class LargeFsConsumerTest extends FsMessageConsumerTest {

  private static final GuidGenerator GUID = new GuidGenerator();

  public LargeFsConsumerTest(java.lang.String testName) {
    super(testName);
  }

  public void testConsumeWithAlternateFactory() throws Exception {
    String subDir = GUID.safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    FsConsumer fs = createConsumer(subDir);
    fs.setResetWipFiles(false);
    fs.setMessageFactory(new DefaultMessageFactory());
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    int count = 10;
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File baseDir = new File(parentDir, subDir);
      baseDir.mkdirs();
      super.createFiles(baseDir, ".xml", count);
      start(sc);
      waitForMessages(stub, count);
      assertEquals(count, stub.getMessages().size());
      super.assertMessages(stub.getMessages(), count, baseDir.listFiles((FilenameFilter) new Perl5FilenameFilter(".*\\.xml")));
      assertDefaultMessageType(stub.getMessages());
    } finally {
      stop(sc);
      FileUtils.deleteQuietly(new File(parentDir, subDir));
    }
  }

  public void testConsume_WithEncoder() throws Exception {
    String subDir = GUID.safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    FsConsumer fs = createConsumer(subDir);
    fs.setResetWipFiles(false);
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    fs.setEncoder(new FileBackedMimeEncoder());
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    int count = 10;
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File baseDir = new File(parentDir, subDir);
      baseDir.mkdirs();
      createMimeFiles(baseDir, ".xml", count);
      start(sc);
      waitForMessages(stub, count);
      assertEquals(count, stub.getMessages().size());
      assertIgnoreSize(stub.getMessages(), count, baseDir.listFiles((FilenameFilter) new Perl5FilenameFilter(".*\\.xml")));
    } finally {
      stop(sc);
      // FileUtils.deleteQuietly(new File(parentDir, subDir));
    }
  }

  public void testConsume_WithUnsupportedEncoder() throws Exception {
    String subDir = GUID.safeUUID();
    MockMessageListener stub = new MockMessageListener(10);
    FsConsumer fs = createConsumer(subDir);
    fs.setResetWipFiles(false);
    fs.setPoller(new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
    fs.setEncoder(new MimeEncoder());
    StandaloneConsumer sc = new StandaloneConsumer(fs);
    sc.registerAdaptrisMessageListener(stub);
    int count = 10;
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File baseDir = new File(parentDir, subDir);
      baseDir.mkdirs();
      createMimeFiles(baseDir, ".xml", count);
      start(sc);
      waitForMessages(stub, count);
      assertEquals(count, stub.getMessages().size());
      assertIgnoreSize(stub.getMessages(), count, baseDir.listFiles((FilenameFilter) new Perl5FilenameFilter(".*\\.xml")));
    } finally {
      stop(sc);
      // FileUtils.deleteQuietly(new File(parentDir, subDir));
    }
  }

  @Override
  protected FsConsumer createConsumer() {
    return new LargeFsConsumer();
  }

  private void assertIgnoreSize(List<AdaptrisMessage> list, int count, File[] remaining) {
    assertEquals(count, list.size());
    assertEquals(0, remaining.length);
    for (AdaptrisMessage m : list) {
      assertTrue(m.containsKey(CoreConstants.ORIGINAL_NAME_KEY));
      assertTrue(m.containsKey(CoreConstants.FILE_LAST_MODIFIED_KEY));
    }
  }

  @Override
  protected void assertMessages(List<AdaptrisMessage> list, int count, File[] remaining) {
    super.assertMessages(list, count, remaining);
    for (AdaptrisMessage m : list) {
      assertTrue(m instanceof FileBackedMessage);
    }
  }

  protected void assertDefaultMessageType(List<AdaptrisMessage> list) {
    for (AdaptrisMessage m : list) {
      assertTrue(m instanceof DefaultAdaptrisMessageImp);
    }
  }

  @Override
  public void testConsumeIgnoresWip() throws Exception {
    // ignore this test as the wip files are now left behind, and
    // removed when the gc collects the message object
    ;
  }

  protected List<File> createMimeFiles(File baseDir, String ext, int count) throws Exception {
    List<File> result = new ArrayList<File>();
    baseDir.mkdirs();
    for (int i = 0; i < count; i++) {
      result.add(writeContent(File.createTempFile("FSC", ext, baseDir)));
    }
    return result;
  }

  private File writeContent(File file) throws Exception {
    MultiPartOutput output = new MultiPartOutput(GUID.getUUID());
    output.addPart("Hello World", "base64", "AdaptrisMessage/payload");
    output.addPart("# comment", "base64", "AdaptrisMessage/metadata");
    try (FileOutputStream out = new FileOutputStream(file)) {
      output.writeTo(out);
    }
    return file;
  }
}
