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

package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageProducerImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.MleMarker;
import com.adaptris.core.ProduceException;
import com.adaptris.interlok.util.Args;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.IdGenerator;
import com.adaptris.util.PlainIdGenerator;

import java.util.ArrayList;
import java.util.List;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

/**
 * <p>
 * Mock implementation of <code>AdaptrisMessageProducer</code> for testing.
 * Produces messages to a List which can be retrieved, thus allowing messages to
 * be verified as split, etc., etc.
 * </p>
 */
public class MockNonStandardRequestReplyProducer extends
    AdaptrisMessageProducerImp {

  public static final String REPLY_METADATA_VALUE = "ReplyMetadataValue";
  public static final String REPLY_METADATA_KEY = "ReplyMetadataKey";
  private List producedMessages;

  private static IdGenerator uniqueIdGenerator;

  static {
    try {
      uniqueIdGenerator = new GuidGenerator();
    }
    catch (Exception e) {
      uniqueIdGenerator = new PlainIdGenerator();
    }
  }

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public MockNonStandardRequestReplyProducer() {
    producedMessages = new ArrayList();
  }

  @Override
  public void prepare() throws CoreException {
  }


  /**
   * <p>
   * Returns the internal store of produced messages.
   * </p>
   *
   * @return the internal store of produced messages
   */
  public List getProducedMessages() {
    return producedMessages;
  }

  // nothing to see below here...

  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
    request(msg);
  }

  @Override
  public AdaptrisMessage request(AdaptrisMessage msg) throws ProduceException {
    Args.notNull(msg, "message");
    AdaptrisMessage reply = defaultIfNull(getMessageFactory()).newMessage();
    reply.setUniqueId(msg.getUniqueId());
    log.trace("Produced [" + msg.getUniqueId() + "]");
    producedMessages.add(msg);
    reply.setPayload(msg.getPayload());
    reply.addMetadata(new MetadataElement(REPLY_METADATA_KEY, REPLY_METADATA_VALUE));
    reply.getMessageLifecycleEvent().addMleMarker(
        new MleMarker("DummyMarker", true, 99, uniqueIdGenerator.create(new Object())));
    return reply;
  }

  @Override
  public AdaptrisMessage request(AdaptrisMessage msg, long timeout) throws ProduceException {
    return request(msg);
  }

  protected long defaultTimeout() {
    return 0;
  }

  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return null;
  }
}
