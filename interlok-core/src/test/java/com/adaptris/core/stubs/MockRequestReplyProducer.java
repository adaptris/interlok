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

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;
import java.util.ArrayList;
import java.util.List;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ProduceException;
import com.adaptris.core.RequestReplyProducerImp;
import com.adaptris.interlok.util.Args;

/**
 * <p>
 * Mock implementation of <code>AdaptrisMessageProducer</code> for testing.
 * Produces messages to a List which can be retrieved, thus allowing messages to
 * be verified as split, etc., etc.
 * </p>
 */
public class MockRequestReplyProducer extends RequestReplyProducerImp {

  public static final String REPLY_METADATA_VALUE = "ReplyMetadataValue";
  public static final String REPLY_METADATA_KEY = "ReplyMetadataKey";
  private List producedMessages;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public MockRequestReplyProducer() {
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

  @Override
  protected long defaultTimeout() {
    return 0;
  }

  @Override
  protected AdaptrisMessage doRequest(AdaptrisMessage msg, String endpoint, long timeout)
      throws ProduceException {
    Args.notNull(msg, "message");
    AdaptrisMessage rm = defaultIfNull(getMessageFactory()).newMessage();
    log.trace("Produced [" + msg.getUniqueId() + "]");
    producedMessages.add(msg);
    rm.setPayload(msg.getPayload());
    rm.addMetadata(new MetadataElement(REPLY_METADATA_KEY, REPLY_METADATA_VALUE));
    return rm;
  }

  @Override
  protected void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException {
    doRequest(msg, endpoint, defaultTimeout());
  }

  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return null;
  }

}
