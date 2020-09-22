/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.http;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.Iterator;
import java.util.Properties;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ProduceException;
import com.adaptris.core.RequestReplyProducerImp;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.password.Password;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.Conversion;

/**
 * Abstract base class for all Http producer classes.
 *
 * @author lchan
 *
 */
@Deprecated
public abstract class HttpProducer extends RequestReplyProducerImp {

  protected static final int ONE_MEG = 1024 * 1024;
  protected static final int TWO_MEG = ONE_MEG * 2;
  protected static final int FOUR_MEG = TWO_MEG * 2;

  @NotNull
  @AutoPopulated
  private KeyValuePairSet additionalHeaders;
  private String userName = null;
  @InputFieldHint(style = "PASSWORD", external = true)
  private String password = null;
  @AdvancedConfig
  private String contentTypeKey = null;

  @AdvancedConfig
  @Deprecated
  private Boolean sendMetadataAsHeaders;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean ignoreServerResponseCode;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean handleRedirection;
  @AdvancedConfig
  @Deprecated
  private String sendMetadataRegexp;

  @NotNull
  @AutoPopulated
  @Valid
  private MetadataFilter metadataFilter;

  private transient String authString = null;
  private transient PasswordAuthentication passwordAuth;

  public HttpProducer() {
    super();
    setAdditionalHeaders(new KeyValuePairSet());
    setContentTypeKey(null);
    setMetadataFilter(new RemoveAllMetadataFilter());
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
  }

  @Override
  public void init() throws CoreException {
    try {
      if (getUserName() != null && getPassword() != null) {
        authString = buildBasicRfc2617(getUserName(), Password.decode(ExternalResolver.resolve(getPassword())));
        passwordAuth = new PasswordAuthentication(userName, Password.decode(ExternalResolver.resolve(getPassword())).toCharArray());
      }
      if (sendMetadataAsHeaders()) {
        if (getSendMetadataRegexp() == null && getMetadataFilter() instanceof RemoveAllMetadataFilter) {
          log.warn("No Metadata Regular expression configured, ignoring sendMetadataAsHeaders=true");
          setSendMetadataAsHeaders(Boolean.FALSE);
        }
        else {
          if (getSendMetadataRegexp() != null && getMetadataFilter() instanceof RemoveAllMetadataFilter) {
            log.trace("Overriding metadata-filter with filter based on {}", getSendMetadataRegexp());
            RegexMetadataFilter filter = new RegexMetadataFilter();
            filter.addIncludePattern(getSendMetadataRegexp());
            setMetadataFilter(filter);
          }
        }
      }
    }
    catch (Exception e) {
      throw new CoreException(e);
    }

  }

  @Override
  public void doProduce(AdaptrisMessage msg, String dest) throws ProduceException {
    doRequest(msg, dest, defaultTimeout());
  }

  /**
   * Set the RFC 2617 username.
   *
   * @param s the user name
   */
  public void setUserName(String s) {
    userName = s;
  }

  /**
   * Set the RFC 2617 password.
   * <p>
   * In additional to plain text passwords, the passwords can also be encoded using the appropriate {@link com.adaptris.security.password.Password}
   * </p>
   *
   * @param s the password
   */
  public void setPassword(String s) {
    password = s;
  }

  /**
   * Get the username.
   *
   * @return username
   */
  public String getUserName() {
    return userName;
  }

  /**
   * Get the password.
   *
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * Set any additional information that will be sent as part of the HTTP headers.
   * <p>
   * The key part of the <code>KeyValuePair</code> forms the header tag, the value forms the data.
   *
   * @param kps the content type.
   */
  public void setAdditionalHeaders(KeyValuePairSet kps) {
    additionalHeaders = kps;
  }

  /**
   * Get any additional header information.
   *
   * @return the content type.
   */
  public KeyValuePairSet getAdditionalHeaders() {
    return additionalHeaders;
  }

  /**
   * Specify whether to automatically handle redirection.
   *
   * @param b true or false.
   */
  public void setHandleRedirection(Boolean b) {
    handleRedirection = b;
  }

  protected boolean handleRedirection() {
    return handleRedirection != null ? handleRedirection.booleanValue() : false;
  }

  /**
   * Get the handle redirection flag.
   *
   * @return true or false.
   */
  public Boolean getHandleRedirection() {
    return handleRedirection;
  }

  /**
   * Get the metadata key from which to extract the content-type.
   *
   * @return the contentTypeKey
   */
  public String getContentTypeKey() {
    return contentTypeKey;
  }

  /**
   * Set the content type metadata key that will be used to extract the Content Type.
   * <p>
   * In the event that this metadata key exists, it will be used in preference to any configured content-type in the
   * additionalHeaders field
   * </p>
   *
   * @see #getAdditionalHeaders()
   * @see #setAdditionalHeaders(KeyValuePairSet)
   * @param s the contentTypeKey to set
   */
  public void setContentTypeKey(String s) {
    contentTypeKey = s;
  }

  /**
   * Get the currently configured flag for ignoring server response code.
   *
   * @return true or false
   * @see #setIgnoreServerResponseCode(Boolean)
   */
  public Boolean getIgnoreServerResponseCode() {
    return ignoreServerResponseCode;
  }

  protected boolean ignoreServerResponseCode() {
    return ignoreServerResponseCode != null ? ignoreServerResponseCode.booleanValue() : false;
  }

  /**
   * Set whether to ignore the server response code.
   * <p>
   * In some cases, you may wish to ignore any server response code (such as 500) as this may return meaningful data that you wish
   * to use. If that's the case, make sure this flag is true. It defaults to false.
   * </p>
   * <p>
   * In all cases the metadata key {@link com.adaptris.core.CoreConstants#HTTP_PRODUCER_RESPONSE_CODE} is populated with the last
   * server response.
   * </p>
   *
   * @see com.adaptris.core.CoreConstants#HTTP_PRODUCER_RESPONSE_CODE
   * @param b true
   */
  public void setIgnoreServerResponseCode(Boolean b) {
    ignoreServerResponseCode = b;
  }

  /**
   * Get any additional items that need to be added as HTTP Headers.
   *
   * @param msg the AdaptrisMessage
   * @return a set of properties that contain the required items.
   * @see #setAdditionalHeaders(KeyValuePairSet)
   * @see #setMetadataFilter(MetadataFilter)
   */
  protected Properties getAdditionalHeaders(AdaptrisMessage msg) {
    Properties result = new Properties();
    for (Iterator i = additionalHeaders.getKeyValuePairs().iterator(); i.hasNext();) {
      KeyValuePair kp = (KeyValuePair) i.next();
      result.setProperty(kp.getKey(), kp.getValue());
    }
    MetadataCollection metadataSubset = getMetadataFilter().filter(msg);
    for (MetadataElement me : metadataSubset) {
      result.setProperty(me.getKey(), me.getValue());
    }
    return result;
  }

  /**
   * Get the RFC2617 authorisation string.
   *
   * @return the RFC2617 authorsation string or null if none specified.
   * @deprecated we should probably be using {@link #getPasswordAuthentication()} instead
   */
  @Deprecated
  protected String getAuthorisation() {
    return authString;
  }

  protected PasswordAuthentication getPasswordAuthentication() {
    return passwordAuth;
  }

  protected void copy(AdaptrisMessage src, AdaptrisMessage dest) throws IOException, CoreException {
    AdaptrisMessageImp.copyPayload(src, dest);
    dest.getObjectHeaders().putAll(src.getObjectHeaders());
    dest.setMetadata(src.getMetadata());
  }

  /**
   *
   * @return the sendMetadataAsHeaders
   * @deprecated since 3.0.2 use {@link #setMetadataFilter(MetadataFilter)} instead.
   */
  @Deprecated
  public Boolean getSendMetadataAsHeaders() {
    return sendMetadataAsHeaders;
  }

  @Deprecated
  protected boolean sendMetadataAsHeaders() {
    return sendMetadataAsHeaders != null ? sendMetadataAsHeaders.booleanValue() : false;
  }

  /**
   * Specify whether or not to send selected {@link com.adaptris.core.AdaptrisMessage} metadata as HTTP Headers or not.
   *
   * @param b the sendMetadataAsHeaders to set
   * @deprecated since 3.0.2 use {@link #setMetadataFilter(MetadataFilter)} instead.
   */
  @Deprecated
  public void setSendMetadataAsHeaders(Boolean b) {
    sendMetadataAsHeaders = b;
  }

  /**
   * @return the sendMetadataCriteria
   * @deprecated since 3.0.2 use {@link #setMetadataFilter(MetadataFilter)} instead.
   */
  @Deprecated
  public String getSendMetadataRegexp() {
    return sendMetadataRegexp;
  }

  /**
   * Specify the {@link com.adaptris.core.AdaptrisMessage} metadata keys that will be sent as HTTP Headers.
   * <p>
   * Any metadata keys that match this regular expression will be sent; the metadata key is the HTTP header name, the metadata value
   * becomes the HTTP header value.
   * </p>
   * <p>
   * Keys that match this regular expression will override any statically configured {@link #setAdditionalHeaders(KeyValuePairSet)}
   * entries
   * </p>
   *
   * @param regexp the regular expression; keys which match this expression will be sent as HTTP Headers
   * @see java.util.regex.Pattern
   * @deprecated use {@link #setMetadataFilter(MetadataFilter)} instead.
   */
  @Deprecated
  public void setSendMetadataRegexp(String regexp) {
    if (regexp == null) {
      throw new IllegalArgumentException("Illegal Metadata Regexp [" + regexp + "]");
    }
    sendMetadataRegexp = regexp;
  }

  public MetadataFilter getMetadataFilter() {
    return metadataFilter;
  }

  /**
   * Specify the {@link com.adaptris.core.AdaptrisMessage} metadata keys that will be sent as HTTP Headers.
   * <p>
   * Any metadata that is returned by this filter will be sent as HTTP headers. Any values that match will override any statically
   * configured {@link #setAdditionalHeaders(KeyValuePairSet)} entries
   * </p>
   *
   * @param metadataFilter the filter defaults to {@link RemoveAllMetadataFilter}
   * @see MetadataFilter
   */
  public void setMetadataFilter(MetadataFilter metadataFilter) {
    if (metadataFilter == null) {
      throw new IllegalArgumentException("Filter is null");
    }
    this.metadataFilter = metadataFilter;
  }

  /**
   * Build a RFC2617 basic authorisation string.
   *
   * @param user the user
   * @param password the password
   * @return the RFC2617 auth
   */
  private static String buildBasicRfc2617(String user, String password) {

    String authString = "";
    if (user != null && user.length() > 0) {

      String source = user + ":" + password;
      authString = "Basic " + Conversion.byteArrayToBase64String(source.getBytes());
    }
    return authString;
  }

}
