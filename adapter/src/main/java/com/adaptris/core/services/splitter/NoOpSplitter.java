package com.adaptris.core.services.splitter;

import java.util.Collections;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A {@link MessageSplitter} implementation that doesn't actually split.
 * 
 * <p>
 * Effectively, using this splitter implementation just returns the original message as the split message on a 1:1 basis
 * </p>
 * 
 * @config no-op-splitter
 */
@XStreamAlias("no-op-splitter")
public class NoOpSplitter implements MessageSplitter {

  public NoOpSplitter() {
  }

  @Override
  public List<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException {
    return Collections.singletonList(msg);
  }
}
