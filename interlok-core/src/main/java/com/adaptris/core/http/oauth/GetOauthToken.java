package com.adaptris.core.http.oauth;

import org.apache.commons.lang3.StringUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.validation.constraints.ConfigDeprecated;
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

  @AffectsMetadata
  @InputFieldDefault(value = "Authorization")
  @Deprecated
  @ConfigDeprecated(removalVersion = "3.12.0", message = "Use a access-token-writer instead", groups = Deprecated.class)
  private String tokenKey;
  @AffectsMetadata
  @AdvancedConfig
  @Deprecated
  @ConfigDeprecated(removalVersion = "3.12.0", message = "Use a access-token-writer instead", groups = Deprecated.class)
  private String tokenExpiryKey;
  @AffectsMetadata
  @AdvancedConfig
  @Deprecated
  @ConfigDeprecated(removalVersion = "3.12.0", message = "Use a access-token-writer instead", groups = Deprecated.class)
  private String refreshTokenKey;

  private transient boolean warningLogged = false;

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

  @Override
  protected AccessTokenWriter tokenWriterIfNull() {
    if (getAccessTokenWriter() == null) {
      LoggingHelper.logWarning(warningLogged, () -> warningLogged = true,
          "Use of token-key/token-expiry/refresh-key is deprecated; use an access-token-writer instead");
      return new MetadataAccessTokenWriter().withTokenKey(StringUtils.defaultIfBlank(getTokenKey(), "Authorization"))
          .withTokenExpiryKey(getTokenExpiryKey())
          .withRefreshTokenKey(getRefreshTokenKey());
    }
    return super.tokenWriterIfNull();
  }

  public GetOauthToken withTokenKey(String b) {
    setTokenKey(b);
    return this;
  }


  @Deprecated
  @ConfigDeprecated(removalVersion = "3.12.0", message = "Use a access-token-writer instead", groups = Deprecated.class)
  public String getTokenKey() {
    return tokenKey;
  }

  /**
   * Set the metadata to store the token against.
   *
   * @param key the key.
   */
  @Deprecated
  @Removal(version = "3.12.0", message = "Use a access-token-writer instead")
  public void setTokenKey(String key) {
    tokenKey = Args.notBlank(key, "tokenMetadataKey");
  }


  @Deprecated
  @Removal(version = "3.12.0", message = "Use a access-token-writer instead")
  public GetOauthToken withTokenExpiryKey(String b) {
    setTokenExpiryKey(b);
    return this;
  }

  @Deprecated
  @ConfigDeprecated(removalVersion = "3.12.0", message = "Use a access-token-writer instead", groups = Deprecated.class)
  public String getTokenExpiryKey() {
    return tokenExpiryKey;
  }

  /**
   * Set the metadata key for storing the expiry
   * <p>
   * In some cases, there is no expiry date for a token, in which case, the metadata key will never be set even if configured.
   * </p>
   *
   * @param key key.
   */
  @Deprecated
  @Removal(version = "3.12.0", message = "Use a access-token-writer instead")
  public void setTokenExpiryKey(String key) {
    tokenExpiryKey = Args.notBlank(key, "tokenExpiryKey");
  }

  @Deprecated
  @Removal(version = "3.12.0", message = "Use a access-token-write instead")
  public GetOauthToken withRefreshTokenKey(String refreshMetadataKey) {
    setRefreshTokenKey(refreshMetadataKey);
    return this;
  }

  @Deprecated
  @ConfigDeprecated(removalVersion = "3.12.0", message = "Use a access-token-write instead", groups = Deprecated.class)
  public String getRefreshTokenKey() {
    return refreshTokenKey;
  }

  /**
   * Set the metadata key for storing the refresh token.
   * <p>
   * In some cases, there is no refresh token, in which case, the metadata key will never be set even if configured.
   * </p>
   *
   * @param key key.
   */
  @Deprecated
  @Removal(version = "3.12.0", message = "Use a access-token-write instead")
  public void setRefreshTokenKey(String key) {
    refreshTokenKey = key;
  }
}
