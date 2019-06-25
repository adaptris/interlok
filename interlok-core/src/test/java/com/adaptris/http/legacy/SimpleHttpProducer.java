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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.http.HttpProducer;
import com.adaptris.http.Http;
import com.adaptris.http.HttpClientTransport;
import com.adaptris.http.HttpException;
import com.adaptris.http.HttpHeaders;
import com.adaptris.http.HttpMessage;
import com.adaptris.http.HttpMessageFactory;
import com.adaptris.http.HttpSession;
import com.adaptris.util.stream.StreamUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Producer that uses the config HTTP method to send to the remote server.
 * <p>
 * This producer implementation is a partial HTTP producer implementation that does not satisfy all the requirements of a HTTP/1.0
 * client. It does not support connections via proxy servers. You should look to be using {@link JdkHttpProducer}. The only
 * situation where this producer should be used is where SSL is a requirement, but the certificates do not match the standard model.
 * These could include certificates that are not signed by a standard Certificate Authority like Verisign/Thawte, or where the
 * certificates are not fit for purpose (the intended use is not as a server certificate).
 * </p>
 * <p>
 * The additional-headers configuration can be used to set the Content-Type header, along with any custom headers that are required.
 * </p>
 * <p>
 * This producer only supports the RFC2617-Basic authentication scheme for username and passwords. If Digest authentication is
 * required, then it should be generated manually, and placed directly into the additional-headers configuration.
 * </p>
 * <p>
 * <strong>Requires an HTTP license</strong>
 * </p>
 *
* <p>
 * In the adapter configuration file this class is aliased as <b>simple-http-producer</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>

 * @see HttpsProduceConnection
 * @see HttpProduceConnection
 * @deprecated since 2.9.0 use {@link com.adaptris.core.http.JdkHttpProducer} instead
 */
@Deprecated
@XStreamAlias("simple-http-producer")
public class SimpleHttpProducer extends HttpProducer {

  // private ProduceDestination defaultDestination;
  private long socketTimeout = Http.DEFAULT_SOCKET_TIMEOUT;
  private String method = "POST";

  /**
   * @see com.adaptris.core.AdaptrisMessageProducerImp#AdaptrisMessageProducerImp()
   *
   *
   */
  public SimpleHttpProducer() {
    super();
    setMethod("POST");
  }

  @Override
  protected long defaultTimeout() {
    return socketTimeout;
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageProducerImp #request(AdaptrisMessage,
   *      ProduceDestination, long)
   */
  @Override
  protected AdaptrisMessage doRequest(AdaptrisMessage msg,
                                      ProduceDestination destination,
                                      long timeout) throws ProduceException {

    AdaptrisMessage reply = defaultIfNull(getMessageFactory()).newMessage();
    OutputStream out = null;

    try {
      HttpClientConnection c = retrieveConnection(HttpClientConnection.class);
      HttpClientTransport client = c.initialiseClient(destination
          .getDestination(msg));
      client.setMethod(getMethod());
      HttpMessage m = HttpMessageFactory.getDefaultInstance().create();
      applyHeaders(getAdditionalHeaders(msg), m);
      if (getContentTypeKey() != null && msg.containsKey(getContentTypeKey())) {
        m.getHeaders().put(Http.CONTENT_TYPE,
            msg.getMetadataValue(getContentTypeKey()));
      }
      if (getAuthorisation() != null) {
        m.getHeaders().put(Http.AUTHORIZATION, getAuthorisation());
      }
      if (getEncoder() != null) {
        getEncoder().writeMessage(msg, m);
      }
      else {
        out = m.getOutputStream();
        out.write(msg.getPayload());
        out.flush();
      }
      HttpSession httpSession = client.send(m, new Long(timeout).intValue(),
 handleRedirection());
      readResponse(httpSession, reply);
      httpSession.close();
    }
    catch (HttpException e) {
      throw new ProduceException(e);
    }
    catch (IOException e) {
      throw new ProduceException(e);
    }
    catch (CoreException e) {
      throw new ProduceException(e);
    }
    return reply;
  }

  /**
   * Add any configured header information.
   *
   * @param m the message.
   */
  protected static void applyHeaders(Properties p, HttpMessage m) {
    HttpHeaders hdr = m.getHeaders();
    for (Iterator i = p.keySet().iterator(); i.hasNext();) {
      String key = (String) i.next();
      hdr.put(key, p.getProperty(key));
    }
    // Bug#2555
    // for (String key : p.stringPropertyNames()) {
    // hdr.put(key, p.getProperty(key));
    // }
  }

  /**
   * Set the socket timeout for activity.
   * <p>
   * If the post operation takes longer than the time in ms, then the message if
   * considered to have failed.
   * </p>
   * <p>
   * This value will be overridden by any configuration on the workflow if it is
   * request reply
   * </p>
   *
   * @param ms the socket timeout in ms.
   */
  public void setSocketTimeout(long ms) {
    socketTimeout = ms;
  }

  /**
   * Get the socket timeout.
   *
   * @return the timeout in ms.
   */
  public long getSocketTimeout() {
    return socketTimeout;
  }

  @Override
  public void prepare() throws CoreException {
  }

  private void readResponse(HttpSession httpReply, AdaptrisMessage reply)
      throws IOException, CoreException {
    int responseCode = httpReply.getResponseLine().getResponseCode();
    if (!ignoreServerResponseCode()) {
      if (responseCode < 200 || responseCode > 299) {
        throw new ProduceException("Failed to send payload, got "
            + responseCode);
      }
    }
    if (getEncoder() != null) {
      copy(getEncoder().readMessage(httpReply), reply);
    }
    else {
      OutputStream out = reply.getOutputStream();
      InputStream in = httpReply.getResponseMessage().getInputStream();
      StreamUtil.copyStream(in, out);
      reply.addMetadata(new MetadataElement(
          CoreConstants.HTTP_PRODUCER_RESPONSE_CODE, String
              .valueOf(responseCode)));
      out.close();
      in.close();
    }
    return;
  }

  /**
   * @return the method
   */
  public String getMethod() {
    return method;
  }

  /**
   * Set the method used to send data
   *
   * @param s the method to set, default POST
   */
  public void setMethod(String s) {
    method = s;
  }

}
