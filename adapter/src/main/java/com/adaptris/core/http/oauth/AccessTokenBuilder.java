package com.adaptris.core.http.oauth;

import java.io.IOException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.CoreException;

public interface AccessTokenBuilder extends ComponentLifecycle {

  /**
   * Build the access token.
   * 
   * @param msg
   * @return
   */
  AccessToken build(AdaptrisMessage msg) throws IOException, CoreException;
}
