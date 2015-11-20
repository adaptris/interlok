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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.internet.ContentType;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>Service</code> which parses the incoming HTTP Request parameters and converts all request parameters into
 * Metadata.
 * </p>
 * <p>
 * If the client is sending data to the adapter, it is possible that the client will send data as though it were a standard Html
 * Form post. In situations like that, then the payload is a number of URLEncoded key value pairs. This service can be used to
 * convert all the Request parameters into metadata.
 * </p>
 * <p>
 * If the <code>parameterForPayload</code> field is configured and present in the request parameters, then this will become the
 * message payload, otherwise the payload remains unchanged.
 * </p>
 * <p>
 * In all cases, the metadata value associated with <code>contentTypeKey</code> will be matched against the configured
 * <code>contentTypeValue</code> If the values match, then the payload is converted, otherwise no action is performed on the
 * payload. If there is a charset parameter supplied with the content type metadata, then this charset will be used to decode the
 * parameters, and used as the CharEncoding on the AdaptriMessage
 * </p>
 * 
 * @config http-request-parameter-converter-service
 * 
 * @author lchan
 */
@XStreamAlias("http-request-parameter-converter-service")
public class RequestParameterConverterService extends ServiceImp {
  public static final String DEFAULT_CONTENT_TYPE_KEY = HttpConstants.CONTENT_TYPE;
  public static final String DEFAULT_CONTENT_TYPE_VALUE = HttpConstants.WWW_FORM_URLENCODE;

  private static final String CONTENT_TYPE_CHARSET = "charset";
  private String parameterForPayload;
  @NotBlank
  @AutoPopulated
  private String contentTypeKey;
  @NotBlank
  @AutoPopulated
  private String contentTypeValue;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public RequestParameterConverterService() {
    setContentTypeKey(DEFAULT_CONTENT_TYPE_KEY);
    setContentTypeValue(DEFAULT_CONTENT_TYPE_VALUE);
  }

  @Override
  public void prepare() throws CoreException {
  }

  /**
   * *
   *
   * @see com.adaptris.core.Service#doService
   *      (com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    String charset = "UTF-8";
    try {
      if (!msg.headersContainsKey(contentTypeKey)) {
        log.debug(contentTypeKey + " not found in metadata, ignoring");
        return;
      }
      String ct = msg.getMetadataValue(contentTypeKey);
      ContentType contentType = new ContentType(ct);

      if (!getContentTypeValue().equalsIgnoreCase(contentType.getBaseType())) {
        log.debug(getContentTypeValue() + " does not match [" + ct
            + "] found in metadata, ignoring");
        return;
      }
      if (contentType.getParameter(CONTENT_TYPE_CHARSET) != null) {
        charset = contentType.getParameter(CONTENT_TYPE_CHARSET);
      }
      Properties p = formDataToProperties(msg.getContent(), charset);
      for (Iterator i = p.entrySet().iterator(); i.hasNext();) {
        Map.Entry e = (Map.Entry) i.next();
        if (getParameterForPayload() != null
            && getParameterForPayload().equalsIgnoreCase((String) e.getKey())) {
          msg.setContent((String) e.getValue(), charset);
        }
        else {
          msg.addMetadata((String) e.getKey(), (String) e.getValue());
        }
      }
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
  }

  private Properties formDataToProperties(String payload, String charset)
      throws UnsupportedEncodingException {
    Properties result = new Properties();
    StringTokenizer st = new StringTokenizer(payload, "&");
    while (st.hasMoreTokens()) {
      String s = st.nextToken();
      StringTokenizer kp = new StringTokenizer(s, "=");
      String key = kp.nextToken();
      String value = "true";
      if (kp.hasMoreTokens()) {
        value = URLDecoder.decode(kp.nextToken(), charset);
      }
      result.put(key, value);
    }
    return result;
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  public String getParameterForPayload() {
    return parameterForPayload;
  }

  /**
   * Set the request parameter the value of which will become the Payload of the
   * Adaptris Message
   *
   * @param s
   */
  public void setParameterForPayload(String s) {
    parameterForPayload = s;
  }

  public String getContentTypeKey() {
    return contentTypeKey;
  }

  /**
   * Set the metadata key for finding out the content-type.
   *
   * @param s the metadata key.
   */
  public void setContentTypeKey(String s) {
    contentTypeKey = s;
  }

  public String getContentTypeValue() {
    return contentTypeValue;
  }

  /**
   * Set the value that the content-type must match.
   *
   * @param s the value.
   */
  public void setContentTypeValue(String s) {
    contentTypeValue = s;
  }

}
