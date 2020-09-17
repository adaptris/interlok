package com.adaptris.core.http.oauth;

import static org.apache.commons.lang3.StringUtils.isBlank;
import javax.validation.constraints.NotBlank;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Write the token to metadata.
 * 
 * @config oauth-access-token-to-metadata
 */
@XStreamAlias("oauth-access-token-to-metadata")
@ComponentProfile(summary = "Write the OAUTH token to metadata", since = "3.10.1")
@DisplayOrder(order = {"tokenKey", "tokenExpiryKey", "refreshTokenKey"})
public class MetadataAccessTokenWriter implements AccessTokenWriter {

  @NotBlank
  @AffectsMetadata
  @InputFieldDefault(value = "Authorization")
  private String tokenKey;
  @AffectsMetadata
  @AdvancedConfig
  private String tokenExpiryKey;
  @AffectsMetadata
  @AdvancedConfig
  private String refreshTokenKey;


  public MetadataAccessTokenWriter() {
    setTokenKey("Authorization");
  }

  @Override
  public void apply(AccessToken token, AdaptrisMessage msg) {
    String tokenMetadataValue = String.format("%s %s", token.getType(), token.getToken());
    msg.addMessageHeader(getTokenKey(), tokenMetadataValue);
    if (!isBlank(getTokenExpiryKey()) && !isBlank(token.getExpiry())) {
      msg.addMessageHeader(getTokenExpiryKey(), token.getExpiry());
    }
    if (!isBlank(getRefreshTokenKey()) && !isBlank(token.getRefreshToken())) {
      msg.addMessageHeader(getRefreshTokenKey(), token.getRefreshToken());
    }
  }

  public MetadataAccessTokenWriter withTokenKey(String b) {
    setTokenKey(b);
    return this;
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

  public MetadataAccessTokenWriter withTokenExpiryKey(String b) {
    setTokenExpiryKey(b);
    return this;
  }

  public String getTokenExpiryKey() {
    return tokenExpiryKey;
  }

  /**
   * Set the metadata key for storing the expiry.
   * <p>
   * In some cases, there is no expiry date for a token, in which case, the metadata key will never be set even if configured.
   * Depending on how you have configured your expiry token, this might be an absolute ISO8601 date, or relative time in seconds.
   * </p>
   * 
   * @param key key.
   */
  public void setTokenExpiryKey(String key) {
    this.tokenExpiryKey = key;
  }

  public MetadataAccessTokenWriter withRefreshTokenKey(String refreshMetadataKey) {
    setRefreshTokenKey(refreshMetadataKey);
    return this;
  }

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
  public void setRefreshTokenKey(String key) {
    this.refreshTokenKey = key;
  }


}
