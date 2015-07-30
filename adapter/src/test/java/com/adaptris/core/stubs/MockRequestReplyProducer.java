/*
 * $RCSfile: MockRequestReplyProducer.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/03/27 12:26:23 $
 * $Author: lchan $
 */
package com.adaptris.core.stubs;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.RequestReplyProducerImp;
import com.adaptris.util.license.License;

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

  /**
   *
   * @see com.adaptris.core.AdaptrisComponent#isEnabled(License)
   */
  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
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

  /**
   * @see com.adaptris.core.AdaptrisMessageProducer#produce
   *      (com.adaptris.core.AdaptrisMessage,
   *      com.adaptris.core.ProduceDestination)
   */
  public void produce(AdaptrisMessage msg, ProduceDestination destination)
      throws ProduceException {
    request(msg, destination);
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  public void init() throws CoreException {
    // do nothing
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  public void start() throws CoreException {
    // do nothing
  }

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  public void stop() {
    // do nothing
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  public void close() {
    // do nothing - could empty List?
  }

  @Override
  protected long defaultTimeout() {
    return 0;
  }

  @Override
  protected AdaptrisMessage doRequest(AdaptrisMessage msg,
                                   ProduceDestination dest, long timeout)
      throws ProduceException {
    AdaptrisMessage rm = defaultIfNull(getMessageFactory()).newMessage();

    if (msg == null) {
      throw new ProduceException("param is null");
    }
    log.trace("Produced [" + msg.getUniqueId() + "]");
    producedMessages.add(msg);
    rm.setPayload(msg.getPayload());
    rm.addMetadata(
        new MetadataElement(REPLY_METADATA_KEY, REPLY_METADATA_VALUE));
    return rm;
  }

}
