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

package com.adaptris.core.services.splitter;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.Service;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.StubMessageFactory;
import com.adaptris.interlok.util.CloseableIterable;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BytesSplitterTest extends SplitterCase {

  private AdaptrisMessage msg;
  private MockMessageProducer producer;
  private BasicMessageSplitterService service;

  @BeforeEach
  public void setUp() throws Exception {
    msg = createBytesMessageInput(new byte[50*1024*1024]);
    producer = new MockMessageProducer();
    service = createBasic(new BytesSplitter());
    service.setProducer(producer);
  }

  @Override
  protected BytesSplitter createSplitterForTests() {
    return new BytesSplitter();
  }

  @Test
  public void testSetMessageFactory() throws Exception {
    MessageSplitterImp splitter = createSplitterForTests();
    assertNull(splitter.getMessageFactory());
    assertEquals(DefaultMessageFactory.class, splitter.selectFactory(new DefaultMessageFactory().newMessage()).getClass());

    splitter.setMessageFactory(new StubMessageFactory());
    assertEquals(StubMessageFactory.class, splitter.getMessageFactory().getClass());
    assertEquals(StubMessageFactory.class, splitter.selectFactory(new DefaultMessageFactory().newMessage()).getClass());

    splitter.setMessageFactory(null);
    assertEquals(DefaultMessageFactory.class, splitter.selectFactory(new DefaultMessageFactory().newMessage()).getClass());
    assertEquals(StubMessageFactory.class, splitter.selectFactory(new StubMessageFactory().newMessage()).getClass());
  }

  @Test
  public void testDefaultSplit() throws Exception {
    BytesSplitter s = new BytesSplitter();
    List<AdaptrisMessage> msgs = toList(s.splitMessage(msg));
    assertEquals(1, msgs.size());
  }

  @Test
  public void testRetainAllSplit() throws Exception {
    BytesSplitter s = new BytesSplitter();
    s.setRetainedSplits(BytesSplitter.RETAIN_ALL_SPLITS);
    List<AdaptrisMessage> msgs = toList(s.splitMessage(msg));
    assertEquals(5, msgs.size());
  }

  @Test
  public void testChunkSplit() throws Exception {
    BytesSplitter s = new BytesSplitter();
    s.setRetainedSplits(BytesSplitter.RETAIN_ALL_SPLITS);
    s.setSplitUnit(BytesSplitter.SplitUnit.CHUNK.name());
    s.setSplitSize(5);
    List<AdaptrisMessage> msgs = toList(s.splitMessage(msg));
    assertEquals(5, msgs.size());
    msgs.forEach(msg -> assertEquals(10*1024*1024, msg.getSize()));
  }

  @Test
  public void testChunkSplitRounding() throws Exception {
    BytesSplitter s = new BytesSplitter();
    s.setRetainedSplits(BytesSplitter.RETAIN_ALL_SPLITS);
    s.setSplitUnit(BytesSplitter.SplitUnit.CHUNK.name());
    s.setSplitSize(3);
    List<AdaptrisMessage> msgs = toList(s.splitMessage(msg));
    assertEquals(3, msgs.size());
    assertEquals(17476267, msgs.get(0).getSize());
    assertEquals(17476267, msgs.get(1).getSize());
    assertEquals(17476266, msgs.get(2).getSize());

  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-BytesSplitter";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null; // over-rides retrieveServices below instead
  }

  @Override
  protected List<Service> retrieveObjectsForSampleConfig() {
    return createExamples(new BytesSplitter());
  }


}
