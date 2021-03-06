package com.adaptris.core.http.oauth;

import java.io.Serializable;
import java.util.Date;

import com.adaptris.util.text.DateFormatUtil;

/**
 * Wrapper around an OAUTH token.
 *
 */
public class AccessToken implements Serializable {

  private static final long serialVersionUID = 2020042101L;
  private static final String DEFAULT_TOKEN_TYPE = "Bearer";

  private String type;
  private String token;
  private String expiry;
  private String refreshToken;

  /**
   * Calls {@link #AccessToken(String, String, long)} with {@code Bearer} as the type.
   * 
   * @deprecated since 3.10.1 with no replacement (use {@link #withExpiry(String)}) instead.
   */
  @Deprecated
  public AccessToken(String token, long expiry) {
    this(DEFAULT_TOKEN_TYPE, token, expiry);
  }

  /**
   * Calls {@link #AccessToken(String, String, long)} with {@code Bearer} as the type and {@code -1} as the expiry
   */
  public AccessToken(String token) {
    this(DEFAULT_TOKEN_TYPE, token);
  }

  /**
   * Calls {@link #AccessToken(String, String, long)} with {@code -1} as the expiry
   */
  public AccessToken(String type, String token) {
    this(type, token, -1);
  }

  /**
   * Create an Access Token.
   * 
   * @param type the token type (usually {@code 'Bearer'})
   * @param token the token itself.
   * @param expiry the expiry; if there is no expiry in milliseconds (absolute), then use -1
   * @deprecated since 3.10.1 with no replacement (use {@link #withExpiry(String)}) instead.
   */
  @Deprecated
  public AccessToken(String type, String token, long expiry) {
    setType(type);
    setToken(token);
    if (expiry != -1) {
      setExpiry(DateFormatUtil.format(new Date(expiry)));
    }
  }


  /**
   * Set the refresh token.
   * 
   */
  public AccessToken withRefreshToken(String s) {
    setRefreshToken(s);
    return this;
  }

  public String getType() {
    return type;
  }

  public void setType(String tokenType) {
    type = tokenType;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getExpiry() {
    return expiry;
  }

  public void setExpiry(String expiry) {
    this.expiry = expiry;
  }

  public AccessToken withExpiry(String s) {
    setExpiry(s);
    return this;
  }

  public AccessToken withExpiry(Date d) {
    return withExpiry(DateFormatUtil.format(d));
  }


  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

}
