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

import javax.validation.Valid;

import org.apache.commons.io.IOUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageImp;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.common.PayloadStreamInputParameter;
import com.adaptris.core.common.PayloadStreamOutputParameter;
import com.adaptris.core.http.AdapterResourceAuthenticator;
import com.adaptris.core.http.ResourceAuthenticator;
import com.adaptris.core.http.client.RequestMethodProvider;
import com.adaptris.core.http.client.RequestMethodProvider.RequestMethod;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.config.DataOutputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default {@link HttpProducer} implementation that uses {@link HttpURLConnection} available in a standard java runtime.
 * 
 * <p>This is designed mostly as a drop-in replacement for {@link com.adaptris.core.http.JdkHttpProducer}. It uses the new
 * {@code com.adaptris.core.http.client} interfaces to manage request and response headers and also the {@link DataInputParameter}
 * and {@link DataOutputParameter} interfaces to source the HTTP body and to handle the HTTP response body respectively. Without
 * specific overrides for these new fields; the behaviour should be functionally equivalent and a {@link
 * com.adaptris.core.NullConnection} is the appropriate connection type.
 * </p>
 * 
 * <p>Note that configuring a {@link com.adaptris.core.AdaptrisMessageEncoder} instance will cause the {@link DataInputParameter}
 * and {@link DataOutputParameter} fields to be ignored.
 * </p>
 * @config standard-http-producer
 * 
 * @author lchan
 */
@XStreamAlias("standard-http-producer")
@AdapterComponent
@ComponentProfile(summary = "Make a HTTP request to a remote server using standard JRE components", tag = "producer,http,https")
public class StandardHttpProducer extends HttpProducer {

  protected static final Collection<RequestMethodProvider.RequestMethod> METHOD_ALLOWS_OUTPUT = Collections
      .unmodifiableCollection(Arrays.asList(new RequestMethod[] {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH}));

  private transient DataInputParameter<InputStream> defaultRequest = new PayloadStreamInputParameter();
  private transient DataOutputParameter<InputStream> defaultResponse = new PayloadStreamOutputParameter();

  @Valid
  @AdvancedConfig
  private DataInputParameter<InputStream> requestBody;
  @Valid
  @AdvancedConfig
  private DataOutputParameter<InputStream> responseBody;


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

  private void writeData(RequestMethod methodToUse, AdaptrisMessage src, HttpURLConnection dest)
      throws IOException, InterlokException {
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
      copyAndClose(requestBody().extract(src), dest.getOutputStream());
    }
  }


  private void handleResponse(HttpURLConnection http, AdaptrisMessage reply) throws IOException, InterlokException {
    int responseCode = http.getResponseCode();
    logHeaders("Response Information", http.getResponseMessage(), http.getHeaderFields().entrySet());
    log.trace("Content-Length is " + http.getContentLength());

    if (responseCode < 200 || responseCode > 299) {
      if (ignoreServerResponseCode()) {
        log.trace("Ignoring HTTP Reponse code {}", responseCode);
        responseBody().insert(http.getErrorStream(), reply);
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
        responseBody().insert(http.getInputStream(), reply);
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


  public DataInputParameter<InputStream> getRequestBody() {
    return requestBody;
  }



  /**
   * Set where the HTTP Request body is going to come from.
   * 
   * @param input the input; default is {@link PayloadStreamInputParameter} which is the only implementation currently.
   */
  public void setRequestBody(DataInputParameter<InputStream> input) {
    this.requestBody = Args.notNull(input, "data input");
  }

  private DataInputParameter<InputStream> requestBody() {
    return getRequestBody() != null ? getRequestBody() : defaultRequest;
  }

  public DataOutputParameter<InputStream> getResponseBody() {
    return responseBody;
  }

  /**
   * Set where the HTTP Response Body will be written to.
   * 
   * @param output the output; default is {@link PayloadStreamOutputParameter}.
   */
  public void setResponseBody(DataOutputParameter<InputStream> output) {
    this.responseBody = Args.notNull(output, "data output");;
  }


  private DataOutputParameter<InputStream> responseBody() {
    return getResponseBody() != null ? getResponseBody() : defaultResponse;
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
