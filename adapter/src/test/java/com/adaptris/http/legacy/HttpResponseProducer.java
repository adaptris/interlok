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

package com.adaptris.http.legacy;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Iterator;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.http.Http;
import com.adaptris.http.HttpHeaders;
import com.adaptris.http.HttpMessage;
import com.adaptris.http.HttpSession;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.stream.StreamUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of <code>AdaptrisMessageProducer</code> that modifies the
 * <code>HttpSession</code> object metadata.
 * <p>
 * This producer does not actually produce the message in question, and can be
 * considered analagous to the HttpResponseService in that it can be used to
 * control the response sent back to a client sending data via a
 * <code>GenericConsumer</code> concrete implementation.
 * </p>
 * <p>
 * The payload of the <code>AdaptrisMessage</code> is set as the body of the
 * HTTP Response. Additional headers can be set by configuring the associated
 * <code>additional-headers</code> object.
 * </p>
 * <p>
 * The primary use for this producer is as a secondary producer in the message
 * error handler, allowing an HTTP error to be returned to the client if a
 * message fails.
 * </p>
 * <p>
 * <strong>Requires an HTTP license</strong>
 * </p>
 *
* <p>
 * In the adapter configuration file this class is aliased as <b>http-response-producer</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 
 * @see HttpSession
 * @see HttpResponseService
 * @see HttpConsumerImp
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("http-response-producer")
@DisplayOrder(order = {"httpResponseCode", "httpResponseText", "contentTypeKey"})
public class HttpResponseProducer extends ProduceOnlyProducerImp {

  private int httpResponseCode;
  private String httpResponseText;

  @NotNull
  @Valid
  @AutoPopulated
  private KeyValuePairSet headers;
  private String contentTypeKey = null;

  /**
   * @see Object#Object()
   *
   */
  public HttpResponseProducer() {
    super();
    httpResponseCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
    httpResponseText = "Internal Error";
    setAdditionalHeaders(new KeyValuePairSet());
    setContentTypeKey(null);
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageProducerImp#produce(AdaptrisMessage,
   *      ProduceDestination)
   */
  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination destination)
      throws ProduceException {
    HttpSession session = (HttpSession) msg.getObjectMetadata().get(
        CoreConstants.HTTP_SESSION_KEY);
    InputStream in = null;

    try {
      if (session == null) {
        return;
      }
      HttpMessage m = session.getResponseMessage();
      if (msg.getSize() > 0) {
        in = msg.getInputStream();
        StreamUtil.copyStream(in, m.getOutputStream());
      }
      HttpHeaders h = m.getHeaders();
      for (Iterator i = headers.getKeyValuePairs().iterator(); i.hasNext();) {
        KeyValuePair k = (KeyValuePair) i.next();
        h.put(k.getKey(), k.getValue());
      }
      if (getContentTypeKey() != null && msg.containsKey(getContentTypeKey())) {
        h.put(Http.CONTENT_TYPE, msg.getMetadataValue(getContentTypeKey()));
      }

      session.getResponseLine().setResponseCode(httpResponseCode);
      session.getResponseLine().setResponseMessage(httpResponseText);

    }
    catch (IOException e) {
      throw new ProduceException(e);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#start()
   */
  @Override
  public void start() throws CoreException {
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#stop()
   */
  @Override
  public void stop() {
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#close()
   */
  @Override
  public void close() {
  }

  @Override
  public void prepare() throws CoreException {
  }

  /**
   * Get the headers to be sent to the client.
   *
   * @return the headers
   */
  public KeyValuePairSet getAdditionalHeaders() {
    return headers;
  }

  /**
   * Set the headers to be sent to the client.
   *
   * @param h the headers.
   */
  public void setAdditionalHeaders(KeyValuePairSet h) {
    headers = h;
  }

  /**
   * Get the HTTP Response code.
   *
   * @return the http response code.
   */
  public int getHttpResponseCode() {
    return httpResponseCode;
  }

  /**
   * Set the HTTP Response code.
   * <p>
   * Valid HTTP response codes are beyond the scope of this javadoc. You should
   * read the appropriate internet RFC
   * </p>
   *
   * @see java.net.HttpURLConnection
   * @param c
   */
  public void setHttpResponseCode(int c) {
    httpResponseCode = c;
  }

  /**
   * Get the text to be sent as the short descriptive text associated with the
   * response code.
   *
   * @return the response text
   */
  public String getHttpResponseText() {
    return httpResponseText;
  }

  /**
   * Set The text to send as the short descriptive text associated with the
   * response code.
   *
   * @param text the text.
   */
  public void setHttpResponseText(String text) {
    httpResponseText = text;
  }

  /**
   * Get the metadata key from which to extract the metadata.
   *
   * @return the contentTypeKey
   */
  public String getContentTypeKey() {
    return contentTypeKey;
  }

  /**
   * Set the content type metadata key that will be used to extract the Content
   * Type.
   * <p>
   * In the event that this metadata key exists, it will be used in preference
   * to any configured content-type in the additionalHeaders field
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
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer("[");
    sb.append(this.getClass().getName());
    sb.append(",additionalHeaders=").append(getAdditionalHeaders());
    sb.append(",responseCode=").append(getHttpResponseCode());
    sb.append(",responseText=").append(getHttpResponseText());
    sb.append("]");
    return sb.toString();
  }
}
