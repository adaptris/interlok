package com.adaptris.core.services.splitter;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;

/**
 * Partial implementation of MessageSplitter that splits Strings based payloads.
 *
 */
public abstract class StringPayloadSplitter extends MessageSplitterImp {

  /**
   * @see MessageSplitter#splitMessage(AdaptrisMessage)
   *
   */
  @Override
  public List<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException {
    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    try {
      AdaptrisMessageFactory factory = selectFactory(msg);
      List<String> payloads = split(msg.getContent());
      for (String payload : payloads) {
        AdaptrisMessage splitMsg = factory.newMessage(payload, msg.getContentEncoding());
        copyMetadata(msg, splitMsg);
        result.add(splitMsg);
      }
      logR.trace("Split gave " + result.size() + " messages");
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
    return result;
  }

  /**
   * default split operation.
   *
   * @param messagePayload the string payload derived from the {@link AdaptrisMessage}
   * @return a list of strings that make up the split messages.
   * @throws Exception
   */
  protected abstract List<String> split(String messagePayload) throws Exception;
}
