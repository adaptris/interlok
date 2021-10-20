package com.adaptris.core.http.oauth;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simplified framework for retrieving OAUTH tokens from verious 3rd party resources (such as Salesforce, or Google).
 *
 *
 * @config get-oauth-token
 *
 */
@XStreamAlias("get-oauth-token")
@AdapterComponent
@ComponentProfile(summary = "Make a HTTP(s) request to an OAUTH server and retrieve an access token", tag = "service,http,https,oauth")
@DisplayOrder(order =
{
    "accessTokenBuilder", "accessTokenWriter"
})
public class GetOauthToken extends OauthTokenGetter {

  public GetOauthToken() {
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      AccessToken token = getAccessTokenBuilder().build(msg);
      tokenWriterToUse().apply(token, msg);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }
}
