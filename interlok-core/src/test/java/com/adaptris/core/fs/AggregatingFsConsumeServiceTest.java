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

package com.adaptris.core.fs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.services.aggregator.AggregatingServiceExample;
import com.adaptris.core.services.aggregator.ConsumeDestinationFromMetadata;
import com.adaptris.core.services.aggregator.ConsumeDestinationGenerator;
import com.adaptris.core.services.aggregator.IgnoreOriginalMimeAggregator;
import com.adaptris.core.services.aggregator.MessageAggregator;
import com.adaptris.core.services.aggregator.ReplaceWithFirstMessage;
import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.core.util.MimeHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.text.mime.BodyPartIterator;

public class AggregatingFsConsumeServiceTest extends AggregatingServiceExample {

  protected static final String DATA_PAYLOAD = "Pack my box with five dozen liquor jugs";
  protected static final String INITIAL_PAYLOAD = "Glib jocks quiz nymph to vex dwarf";

  public AggregatingFsConsumeServiceTest(String name) {
    super(name);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testService() throws Exception {
    Object o = new Object();
    File tempFile = TempFileUtils.createTrackedFile(o);
    String url = "file://localhost/" + tempFile.getCanonicalPath().replaceAll("\\\\", "/");
    ConsumeDestinationGenerator cdg = createConsumeDestination(url, null);
    AggregatingFsConsumer afc = createConsumer(cdg, new ReplaceWithFirstMessage()).withCondition((msg) -> true);
    AggregatingFsConsumeService service = createAggregatingService(afc);
    File wipFile = new File(tempFile.getParent(), tempFile.getName() + afc.wipSuffix());
    try {
      writeDataMessage(tempFile);
      start(service);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage(INITIAL_PAYLOAD);
      service.doService(msg);
      assertNotSame(INITIAL_PAYLOAD, msg.getContent());
      assertEquals(DATA_PAYLOAD, msg.getContent());
      assertFalse(tempFile.exists());
      assertFalse(wipFile.exists());
    }
    finally {
      stop(service);
    }
  }

  public void testServiceNonDeleting() throws Exception {
    Object o = new Object();
    File tempFile = TempFileUtils.createTrackedFile(o);
    String url = "file://localhost/" + tempFile.getCanonicalPath().replaceAll("\\\\", "/");
    ConsumeDestinationGenerator cdg = createConsumeDestination(url, null);
    AggregatingFsConsumer afc = createConsumer(cdg, new ReplaceWithFirstMessage());
    afc.setDeleteAggregatedFiles(false);
    AggregatingFsConsumeService service = createAggregatingService(afc);
    File wipFile = new File(tempFile.getParent(), tempFile.getName() + afc.wipSuffix());
    try {
      writeDataMessage(tempFile);
      start(service);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage(INITIAL_PAYLOAD);
      service.doService(msg);
      assertNotSame(INITIAL_PAYLOAD, msg.getContent());
      assertEquals(DATA_PAYLOAD, msg.getContent());
      assertTrue(tempFile.exists());
      assertFalse(wipFile.exists());
    }
    finally {
      stop(service);
    }
  }

  public void testService_MultipleMessages() throws Exception {
    GuidGenerator o = new GuidGenerator();
    File tempDir = TempFileUtils.createTrackedDir(o);
    String url = "file://localhost/" + tempDir.getCanonicalPath().replaceAll("\\\\", "/");
    ConsumeDestinationGenerator cdg = createConsumeDestination(url, ".*");
    AggregatingFsConsumer afc = createConsumer(cdg, new IgnoreOriginalMimeAggregator());
    AggregatingFsConsumeService service = createAggregatingService(afc);

    try {
      writeDataMessage(tempDir, o.safeUUID());
      writeDataMessage(tempDir, o.safeUUID());
      start(service);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage(INITIAL_PAYLOAD);
      service.doService(msg);
      BodyPartIterator input = MimeHelper.createBodyPartIterator(msg);
      assertEquals(2, input.size());
    }
    finally {
      stop(service);
    }
  }

  private ConsumeDestinationFromMetadata createConsumeDestination(String dir, String filterExp) {
    ConsumeDestinationFromMetadata d = new ConsumeDestinationFromMetadata();
    d.setDefaultDestination(dir);
    d.setDefaultFilterExpression(filterExp);
    return d;
  }

  private AggregatingFsConsumeService createAggregatingService(AggregatingFsConsumer fsConsumer) {
    AggregatingFsConsumeService service = new AggregatingFsConsumeService();
    service.setFsConsumer(fsConsumer);
    return service;
  }

  private AggregatingFsConsumer createConsumer(ConsumeDestinationGenerator cdg, MessageAggregator aggr) {
    AggregatingFsConsumer consumer = new AggregatingFsConsumer();
    consumer.setDestination(cdg);
    consumer.setMessageAggregator(aggr);
    return consumer;
  }

  private void writeDataMessage(File directory, String filename) throws Exception {
    writeDataMessage(new File(directory, filename));
  }

  private void writeDataMessage(File file) throws Exception {
    try (PrintStream out = new PrintStream(new FileOutputStream(file), true)) {
      out.print(DATA_PAYLOAD);
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    AggregatingFsConsumeService service = new AggregatingFsConsumeService();
    ConsumeDestinationFromMetadata mfd = new ConsumeDestinationFromMetadata();
    mfd.setDestinationMetadataKey("aggrDir");
    mfd.setDefaultFilterExpression(".*\\*.xml");
    AggregatingFsConsumer consumer = new AggregatingFsConsumer(mfd);
    consumer.setMessageAggregator(new IgnoreOriginalMimeAggregator());
    service.setFsConsumer(consumer);
    return service;
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o)
        + "\n<!-- \n In the example here, you aggregate the contents of the directory specified by the metadata-key 'aggrDir'"
        + "\nmatching only files that correspond to the Perl pattern .*\\.xml. "
        + "\nThese are then aggregated into a single MIME Multipart message. The original message is ignored." + "\n-->\n";
  }
}
