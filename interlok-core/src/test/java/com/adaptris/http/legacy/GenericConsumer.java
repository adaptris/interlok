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

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.http.HttpException;
import com.adaptris.http.HttpHeaders;
import com.adaptris.http.HttpMessage;
import com.adaptris.http.HttpRequest;
import com.adaptris.http.HttpResponse;
import com.adaptris.http.HttpSession;
import com.adaptris.util.stream.StreamUtil;

/**
 * This is the standard class that receives documents via HTTP. *
 *
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class GenericConsumer extends HttpConsumerImp {

  private boolean preserveParameters; // defaults to false

  /**
   * @see HttpConsumerImp#handleRequest(HttpSession)
   */
  @Override
  @SuppressWarnings("deprecation")
  protected AdaptrisMessage handleRequest(HttpSession httpSession)
      throws IOException, IllegalStateException, HttpException {

    HttpRequest request = httpSession.getRequestLine();
    HttpMessage message = httpSession.getRequestMessage();
    HttpHeaders header = message.getHeaders();

    HttpResponse response = httpSession.getResponseLine();

    AdaptrisMessage result = null;
    OutputStream out = null;
    try {
      // By default we only accept post methods
      if (request.getMethod().equalsIgnoreCase(getMethod())) {
        response.setResponseCode(HttpURLConnection.HTTP_OK);
        response.setResponseMessage("OK");
        if (getEncoder() != null) {
          result = getEncoder().readMessage(message);
        } else {
          result = defaultIfNull(getMessageFactory()).newMessage();
          out = result.getOutputStream();
          StreamUtil.copyStream(message.getInputStream(), out, header
              .getContentLength());
        }
        result.addObjectHeader(CoreConstants.HTTP_SESSION_KEY, httpSession);
        addParamsAsMetadata(httpSession, result);
      }
      else {
        response.setResponseCode(HttpURLConnection.HTTP_BAD_METHOD);
        response.setResponseMessage("Method Not Allowed");
      }
    }
    catch (CoreException e) {
      throw new HttpException(e);
    } finally {
      IOUtils.closeQuietly(out);
    }

    return result;
  }

  /**
   * <p>
   * Adds parameters as <code>AdaptrisMessage</code> metadata.
   * </p>
   */
  private void addParamsAsMetadata(HttpSession session, AdaptrisMessage msg) {

    if (getPreserveParameters()) {
      Map params = session.getRequestLine().getParameters();
      Iterator itr = params.keySet().iterator();

      while (itr.hasNext()) {
        String key = (String) itr.next();
        String value = (String) params.get(key);

        if ("".equals(key) || "".equals(value)) {
          log.info("unable to add metadata with empty key and / or value");
        }
        else {
          msg.addMetadata(key, value);
        }
      }
    }
  }

  /**
   * <p>
   * Returns the HTTP method to accept.
   * </p>
   *
   * @return the HTTP method to accept
   */
  protected abstract String getMethod();

  /**
   * <p>
   * Returns setParametersAsMetadata.
   * </p>
   *
   * @return setParametersAsMetadata
   */
  public boolean getPreserveParameters() {
    return preserveParameters;
  }

  /**
   * <p>
   * If true HTTP paramters that are passed as part of the URI will be set as
   * <code>AdaptrisMessage</code> metadata.
   * </p>
   *
   * @param b true if params should be preserved
   */
  public void setPreserveParameters(boolean b) {
    preserveParameters = b;
  }

  /**
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer("[");
    sb.append(this.getClass().getName());
    sb.append(",preserveParameters=").append(getPreserveParameters());
    sb.append("]");
    return sb.toString();
  }
}
