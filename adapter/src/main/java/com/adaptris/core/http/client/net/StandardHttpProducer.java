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

package com.adaptris.core.http.client.net;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;
import static com.adaptris.core.http.HttpConstants.CONTENT_TYPE;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageImp;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.http.AdapterResourceAuthenticator;
import com.adaptris.core.http.ResourceAuthenticator;
import com.adaptris.core.http.client.RequestMethodProvider;
import com.adaptris.core.http.client.RequestMethodProvider.RequestMethod;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default {@link HttpProducer} implementation that uses {@link HttpURLConnection} available in a standard java runtime.
 * 
 * <p>This is designed mostly as a drop-in replacement for {@link com.adaptris.core.http.JdkHttpProducer} but uses the new
 * {@code com.adaptris.core.http.client} interfaces to manage request and response headers. The behaviour should be functionally
 * equivalent and a {@link com.adaptris.core.NullConnection} is the appropriate connection type.
 * </p>
 * 
 * @config standard-http-producer
 * 
 * @author lchan
 */
@XStreamAlias("standard-http-producer")
public class StandardHttpProducer extends HttpProducer {

  private static final Collection<RequestMethodProvider.RequestMethod> METHOD_ALLOWS_OUTPUT = Collections
      .unmodifiableCollection(Arrays.asList(new RequestMethod[] {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH}));

  public StandardHttpProducer() {
    super();
  }

  public StandardHttpProducer(ProduceDestination d) {
    this();
    setDestination(d);
  }

  @Override
  protected AdaptrisMessage doRequest(AdaptrisMessage msg, ProduceDestination destination, long timeout) throws ProduceException {
    AdaptrisMessage reply = defaultIfNull(getMessageFactory()).newMessage();
    HttpAuthenticator myAuth = null;
    try {
      URL url = new URL(destination.getDestination(msg));
      if (getPasswordAuthentication() != null) {
        myAuth = new HttpAuthenticator(url, getPasswordAuthentication());
        Authenticator.setDefault(AdapterResourceAuthenticator.getInstance());
        AdapterResourceAuthenticator.getInstance().addAuthenticator(myAuth);
      }
      HttpURLConnection http = configure((HttpURLConnection) url.openConnection(), msg);
      writeData(getMethod(msg), msg, http);
      handleResponse(http, reply);
    } catch (Exception e) {
      ExceptionHelper.rethrowProduceException(e);
    } finally {
      AdapterResourceAuthenticator.getInstance().removeAuthenticator(myAuth);
    }
    return reply;
  }

  private HttpURLConnection configure(HttpURLConnection http, AdaptrisMessage msg) throws Exception {
    RequestMethod rm = getMethod(msg);
    log.trace("HTTP Request Method is : [{}]", rm);
    http.setRequestMethod(rm.name());
    http.setInstanceFollowRedirects(handleRedirection());
    http.setDoInput(true);
    getRequestHeaderProvider().addHeaders(msg, http);
    String contentType = getContentTypeProvider().getContentType(msg);
    if (!isEmpty(contentType)) {
      http.setRequestProperty(CONTENT_TYPE, contentType);
    }
    return http;
  }

  private void writeData(RequestMethod methodToUse, AdaptrisMessage src, HttpURLConnection dest) throws IOException, CoreException {
    if (!METHOD_ALLOWS_OUTPUT.contains(methodToUse)) {
      if (src.getSize() > 0) {
        log.trace("Ignoring payload with use of {} method", methodToUse.name());
      }
      return;
    }
    dest.setDoOutput(true);
    if (getEncoder() != null) {
      getEncoder().writeMessage(src, dest);
    } else {
      copyAndClose(src.getInputStream(), dest.getOutputStream());
    }
  }


  private void handleResponse(HttpURLConnection http, AdaptrisMessage reply) throws IOException, CoreException {
    int responseCode = http.getResponseCode();
    logHeaders("Response Information", http.getResponseMessage(), http.getHeaderFields().entrySet());
    log.trace("Content-Length is " + http.getContentLength());

    if (responseCode < 200 || responseCode > 299) {
      if (ignoreServerResponseCode()) {
        log.trace("Ignoring HTTP Reponse code {}", responseCode);
        copyAndClose(http.getErrorStream(), reply.getOutputStream());
      } else {
        throw new ProduceException("Failed to send payload, got " + responseCode);
      }
    } else {
      if (getEncoder() != null) {
        AdaptrisMessage decodedReply = getEncoder().readMessage(http);
        AdaptrisMessageImp.copyPayload(decodedReply, reply);
        reply.getObjectMetadata().putAll(decodedReply.getObjectMetadata());
        reply.setMetadata(decodedReply.getMetadata());
      } else {
        copyAndClose(http.getInputStream(), reply.getOutputStream());
      }
    }
    getResponseHeaderHandler().handle(http, reply);
    reply.addMetadata(new MetadataElement(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE, String.valueOf(http.getResponseCode())));
  }

  private void copyAndClose(InputStream input, OutputStream out) throws IOException, CoreException {
    try (InputStream autoCloseIn = new BufferedInputStream(input); OutputStream autoCloseOut = new BufferedOutputStream(out)) {
      IOUtils.copy(autoCloseIn, autoCloseOut);
    }
  }

  @Override
  public void prepare() throws CoreException {
  }

  private class HttpAuthenticator implements ResourceAuthenticator {

    private URL url;
    private PasswordAuthentication auth;

    HttpAuthenticator(URL url, PasswordAuthentication auth) {
      this.url = url;
      this.auth = auth;
    }

    @Override
    public PasswordAuthentication authenticate(ResourceTarget target) {
      if (url.equals(target.getRequestingURL())) {
        log.trace("Using user={} to login to [{}]", auth.getUserName(), target.getRequestingURL());
        return auth;
      }
      return null;
    }
  }
}
