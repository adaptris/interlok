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

import static com.adaptris.core.fs.FsHelper.createFileReference;
import static com.adaptris.core.fs.FsHelper.createUrlFromString;
import static com.adaptris.core.fs.FsMessageProducerTest.BASE_KEY;
import static com.adaptris.core.fs.FsMessageProducerTest.BASE_TEMP_DIR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MimeEncoder;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.fs.FsProducerExample;

public class LargeFsMessageProducerTest extends FsProducerExample {

  private static final String DEFAULT_DEST = "/tgt";

  private File baseDir, tempDir, destDir;


  @BeforeEach
  public void setUp() throws Exception {
    baseDir = createFileReference(createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    destDir = createFileReference(createUrlFromString(PROPERTIES.getProperty(BASE_KEY) + DEFAULT_DEST, true));
    tempDir = createFileReference(createUrlFromString(PROPERTIES.getProperty(BASE_TEMP_DIR), true));
  }

  @AfterEach
  public void tearDown() throws Exception {
    // delete contents of destination...
    FileUtils.deleteQuietly(baseDir);
    FileUtils.deleteQuietly(tempDir);
    FileUtils.deleteQuietly(destDir);
  }

  @Test
  public void testProduceWithDefaultMessageFactory() throws Exception {
    LargeFsProducer producer = create();
    start(producer);
    producer.produce(new DefaultMessageFactory().newMessage("dummy"));
    stop(producer);
  }

  @Test
  public void testProduce() throws Exception {
    LargeFsProducer producer = create();
    start(producer);
    producer.produce(new FileBackedMessageFactory().newMessage("dummy"));
    stop(producer);
  }

  @Test
  public void testProduceWithRenameTo() throws Exception {
    LargeFsProducer producer = create();
    producer.setUseRenameTo(true);
    start(producer);
    producer.produce(new FileBackedMessageFactory().newMessage("dummy"));
    stop(producer);
  }

  @Test
  public void testProduce_WithEncoder() throws Exception {
    LargeFsProducer producer = create();
    producer.setEncoder(new FileBackedMimeEncoder());
    try {
      start(producer);
      AdaptrisMessage msg = new FileBackedMessageFactory().newMessage("dummy");
      producer.produce(msg);
      FileBackedMimeEncoder encoder = new FileBackedMimeEncoder();
      encoder.setRetainUniqueId(true);
      File writtenFile = new File(destDir, msg.getUniqueId());
      AdaptrisMessage result = encoder.readMessage(writtenFile);
      assertEquals(msg.getUniqueId(), result.getUniqueId());
    } finally {
      stop(producer);
    }
  }

  @Test
  public void testProduce_WithUnsupportedEncoder() throws Exception {
    LargeFsProducer producer = create();
    producer.setEncoder(new MimeEncoder());
    try {
      start(producer);
      AdaptrisMessage msg = new FileBackedMessageFactory().newMessage("dummy");
      producer.produce(msg);
      FileBackedMimeEncoder encoder = new FileBackedMimeEncoder();
      encoder.setRetainUniqueId(true);
      File writtenFile = new File(destDir, msg.getUniqueId());
      // Won't work.
      AdaptrisMessage result = encoder.readMessage(writtenFile);
      fail();
    } catch (Exception expected) {

    } finally {
      stop(producer);
    }
  }

  @Test
  public void testProduceWithOverride() throws Exception {
    LargeFsProducer producer = create();
    start(producer);
    producer.produce(new FileBackedMessageFactory().newMessage("dummy"));
    producer.setBaseDirectoryUrl(PROPERTIES.getProperty(BASE_KEY));
    stop(producer);
  }

  @Test
  public void testProduceWithTempDir() throws Exception {
    String tempDir = PROPERTIES.getProperty(BASE_TEMP_DIR);
    LargeFsProducer producer = create();

    producer.setTempDirectory(tempDir);
    start(producer);
    producer.produce(new FileBackedMessageFactory().newMessage("dummy"));
    stop(producer);
    File f = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_TEMP_DIR), true));
    assertTrue(f.exists());
    assertTrue(f.isDirectory());
  }

  @Override
  protected StandaloneProducer retrieveObjectForSampleConfig() {
    LargeFsProducer producer =
        new LargeFsProducer().withBaseDirectoryUrl("file:///path/to/directory");
    StandaloneProducer result = new StandaloneProducer(producer);
    return result;
  }

  private LargeFsProducer create() {
    // create producer
    LargeFsProducer producer = new LargeFsProducer();
    producer.setBaseDirectoryUrl((PROPERTIES.getProperty(BASE_KEY) + DEFAULT_DEST));
    producer.setCreateDirs(true);
    return producer;
  }

}
