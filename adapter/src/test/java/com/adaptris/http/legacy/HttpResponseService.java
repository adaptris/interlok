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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.http.HttpSession;
import com.adaptris.util.stream.StreamUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>Service</code> which sets the payload of the passed <code>AdaptrisMessage</code> as the body of the HTTP
 * response, if the HTTP session is present in object metadata against a constant key. If the current payload is null or empty, the
 * HTTP response code is set to 204 NO CONTENT. In addition, AdaptrisMessage metadata may be configured to be preserved as HTTP
 * response headers.
 * </p>
 * <p>
 * This class will generally be used in conjunction with a <code>NullMessageProducer</code>.
 * </p>
 * <p>
 * Although this class provides similar functionality to <code>JmsReplyToWorkflow</code>, the main difference is that the reply here
 * is part of the reponse to the original request whereas in JMS data is sent as a new message rather than as part of the
 * acknowledgment, hence the different approach.
 * </p>
 * 
 * @config http-response-service
 */
@XStreamAlias("http-response-service")
public class HttpResponseService extends ServiceImp {

  @NotNull
  @AutoPopulated
  private List<String> metadataKeysToPreserve;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public HttpResponseService() {
    setMetadataKeysToPreserve(new ArrayList<String>());
  }

  /**
   * *
   *
   * @see com.adaptris.core.Service#doService
   *      (com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      HttpSession session = (HttpSession) msg.getObjectMetadata().get(
          CoreConstants.HTTP_SESSION_KEY);

      if (session != null) {
        if (msg.getSize() > 0) {
          InputStream in = msg.getInputStream();
          try {
            StreamUtil.copyStream(in, session.getResponseMessage()
                .getOutputStream());
          }
          finally {
            IOUtils.closeQuietly(in);
          }
          handleMetadata(msg, session);
        }
        else {
          session.getResponseLine().setResponseCode(
              HttpURLConnection.HTTP_NO_CONTENT);
          session.getResponseLine().setResponseMessage("No Content");
        }
      }
      else {
        log.warn("HttpSession not set as metadata");
      }
    }
    catch (ClassCastException e) {
      log.error("ignoring [" + msg.getObjectMetadata().getClass().getName()
          + "] metadata");
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
  }

  /**
   * <p>
   * Preserve configured metadata keys as HTTP reponse headers.
   * </p>
   */
  private void handleMetadata(AdaptrisMessage msg, HttpSession session) {
    Iterator itr = metadataKeysToPreserve.iterator();

    while (itr.hasNext()) {
      String key = (String) itr.next();
      String value = msg.getMetadataValue(key);
      log.trace("setting header key [" + key + "] value [" + value + "]");

      if (value != null && !"".equals(value)) {
        session.getResponseMessage().getHeaders().put(key, value);
      }
      else {
        log.warn("ignoring key [" + key + "] which returned [" + value + "]");
      }
    }
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public void init() throws CoreException {
    // na
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
    // na
  }

  /**
   * <p>
   * Returns a <code>List</code> of metadata keys to preserve as headers in the
   * HTTP response.
   * </p>
   *
   * @return a <code>List</code> of metadata keys to preserve as headers in the
   *         HTTP response
   */
  public List<String> getMetadataKeysToPreserve() {
    return metadataKeysToPreserve;
  }

  /**
   * <p>
   * Sets a <code>List</code> of metadata keys to preserve as headers in the
   * HTTP response.
   * </p>
   *
   * @param l a <code>List</code> of metadata keys to preserve as headers in the
   *          HTTP response
   */
  public void setMetadataKeysToPreserve(List<String> l) {
    metadataKeysToPreserve = l;
  }

  /**
   * <p>
   * Sets a metadata key to preserve as an HTTP response header. May not be null
   * or empty.
   * </p>
   *
   * @param s a metadata key to preserve as an HTTP response header
   */
  public void addMetadataKeyToPreserve(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("invalid param");
    }
    metadataKeysToPreserve.add(s);
  }
}
