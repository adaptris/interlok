package com.adaptris.core.http.oauth;

import static org.apache.commons.lang3.StringUtils.isBlank;
import java.io.IOException;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
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
    "tokenKey", "accessTokenBuilder", "tokenExpiryKey"
})
public class GetOauthToken extends ServiceImp {

  @NotBlank
  @AffectsMetadata
  @InputFieldDefault(value = "Authorization")
  private String tokenKey;
  @AffectsMetadata
  @AdvancedConfig
  private String tokenExpiryKey;

  @NotNull
  @Valid
  private AccessTokenBuilder accessTokenBuilder;

  public GetOauthToken() {
    setTokenKey("Authorization");
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      AccessToken token = getAccessTokenBuilder().build(msg);
      String tokenMetadataValue = String.format("%s %s", token.getType(), token.getToken());
      msg.addMessageHeader(getTokenKey(), tokenMetadataValue);
      if (!isBlank(getTokenExpiryKey()) && !isBlank(token.getExpiry())) {
        msg.addMessageHeader(getTokenExpiryKey(), token.getExpiry());
      }
    }
    catch (CoreException | IOException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
    try {
      Args.notNull(getAccessTokenBuilder(), "accessTokenBuilder");
      LifecycleHelper.prepare(getAccessTokenBuilder());
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void initService() throws CoreException {
    LifecycleHelper.init(getAccessTokenBuilder());
  }

  @Override
  public void start() throws CoreException {
    super.start();
    LifecycleHelper.start(getAccessTokenBuilder());
  }

  @Override
  public void stop() {
    super.stop();
    LifecycleHelper.stop(getAccessTokenBuilder());
  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(getAccessTokenBuilder());
  }

  public String getTokenKey() {
    return tokenKey;
  }

  /**
   * Set the metadata to store the token against.
   * 
   * @param key the key.
   */
  public void setTokenKey(String key) {
    this.tokenKey = Args.notBlank(key, "tokenMetadataKey");
  }

  public String getTokenExpiryKey() {
    return tokenExpiryKey;
  }

  /**
   * Set the metadata key for storing the expiry date (ISO8601 style).
   * <p>
   * In some cases, there is no expiry date for a token, in which case, the metadata key will never be set even if configured.
   * </p>
   * 
   * @param key key.
   */
  public void setTokenExpiryKey(String key) {
    this.tokenExpiryKey = Args.notBlank(key, "tokenExpiryKey");
  }

  public AccessTokenBuilder getAccessTokenBuilder() {
    return accessTokenBuilder;
  }

  /**
   * Set the access token builder.
   * 
   * @param b the builder.
   */
  public void setAccessTokenBuilder(AccessTokenBuilder b) {
    this.accessTokenBuilder = Args.notNull(b, "accessTokenBuilder");
  }

}
