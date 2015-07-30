package com.adaptris.core.runtime;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default null implementation of {@link MessageErrorDigester}.
 * 
 * @config null-message-error-digester
 * @author lchan
 * 
 */
@XStreamAlias("null-message-error-digester")
public class NullMessageErrorDigester extends MessageErrorDigesterImp {

  @Override
  public void digest(AdaptrisMessage message) {
  }

  @Override
  public int getTotalErrorCount() {
    return 0;
  }
}
