package com.adaptris.core.http.oauth;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ComponentLifecycle;

@FunctionalInterface
public interface AccessTokenWriter extends ComponentLifecycle {

  /**
   * Apply the token to the message.
   * 
   */
  void apply(AccessToken token, AdaptrisMessage msg);
}
