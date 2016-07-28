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
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageImp;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.NullConnection;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.common.InputStreamWithEncoding;
import com.adaptris.core.common.PayloadStreamInputParameter;
import com.adaptris.core.common.PayloadStreamOutputParameter;
import com.adaptris.core.http.auth.AdapterResourceAuthenticator;
import com.adaptris.core.http.auth.ConfiguredUsernamePassword;
import com.adaptris.core.http.auth.HttpAuthenticator;
import com.adaptris.core.http.auth.NoAuthentication;
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
@ComponentProfile(summary = "Make a HTTP request to a remote server using standard JRE components", tag = "producer,http,https", recommended = {NullConnection.class})
@DisplayOrder(order = {"authenticator", "username", "password", "allowRedirect", "ignoreServerResponseCode", "methodProvider", "contentTypeProvider",
    "requestHeaderProvider", "requestBody", "responseHeaderHandler", "responseBody"})
public class StandardHttpProducer extends HttpProducer {

  private static final String PARAM_CHARSET = "charset";
  
  protected static final Collection<RequestMethodProvider.RequestMethod> METHOD_ALLOWS_OUTPUT = Collections
      .unmodifiableCollection(Arrays.asList(new RequestMethod[] {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH}));

  private transient DataInputParameter<InputStream> defaultRequest = new PayloadStreamInputParameter();
  private transient DataOutputParameter<InputStreamWithEncoding> defaultResponse = new PayloadStreamOutputParameter();

  @Valid
  @AdvancedConfig
  private DataInputParameter<InputStream> requestBody;
  @Valid
  @AdvancedConfig
  private DataOutputParameter<InputStreamWithEncoding> responseBody;

  private HttpAuthenticator authenticator = new NoAuthentication();

  public StandardHttpProducer() {
    super();
    Authenticator.setDefault(AdapterResourceAuthenticator.getInstance());
  }

  public StandardHttpProducer(ProduceDestination d) {
    this();
    setDestination(d);
  }

  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination dest) throws ProduceException {
    doRequest(msg, dest, defaultTimeout(), defaultIfNull(getMessageFactory()).newMessage());
  }
  
  @Override
  protected AdaptrisMessage doRequest(AdaptrisMessage msg, ProduceDestination destination, long timeout) throws ProduceException {
    return doRequest(msg, destination, timeout, msg);
  }

  private AdaptrisMessage doRequest(AdaptrisMessage msg, ProduceDestination destination, long timeout, AdaptrisMessage reply) throws ProduceException {
    // If deprecated username/password are set and no authenticator is configured, transparently create a static authenticator
    if (getAuthenticator() instanceof NoAuthentication && !isEmpty(getUsername())) {
      setAuthenticator(new ConfiguredUsernamePassword(getUsername(), getPassword()));
    }
    
    try {
      URL url = new URL(destination.getDestination(msg));
      authenticator.setup(url.toString(), msg);
      HttpURLConnection http = configure((HttpURLConnection) url.openConnection(), msg);
      authenticator.configureConnection(http);
      writeData(getMethod(msg), msg, http);
      handleResponse(http, reply);
    } catch (Exception e) {
      ExceptionHelper.rethrowProduceException(e);
    } finally {
      authenticator.close();
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
        responseBody().insert(new InputStreamWithEncoding(http.getErrorStream(), getContentEncoding(http)), reply);
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
        responseBody().insert(new InputStreamWithEncoding(http.getInputStream(), getContentEncoding(http)), reply);
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
  
  private String getContentEncoding(HttpURLConnection http) {
    if(http.getContentEncoding() != null) {
      return http.getContentEncoding();
    }
    
    // Parse Content-Type header for encoding
    try {
      ContentType contentType = new ContentType(http.getContentType());
      if(!isEmpty(contentType.getParameter(PARAM_CHARSET))) {
        return contentType.getParameter(PARAM_CHARSET);
      }
    } catch (ParseException e) {
      log.warn("Unable to parse Content-Type header \"{}\": {}", http.getContentType(), e.toString());
    }
    
    return null;
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

  public DataOutputParameter<InputStreamWithEncoding> getResponseBody() {
    return responseBody;
  }

  /**
   * Set where the HTTP Response Body will be written to.
   * 
   * @param output the output; default is {@link PayloadStreamOutputParameter}.
   */
  public void setResponseBody(DataOutputParameter<InputStreamWithEncoding> output) {
    this.responseBody = Args.notNull(output, "data output");
  }


  private DataOutputParameter<InputStreamWithEncoding> responseBody() {
    return getResponseBody() != null ? getResponseBody() : defaultResponse;
  }

  @Override
  public void prepare() throws CoreException {
  }

  public HttpAuthenticator getAuthenticator() {
    return authenticator;
  }

  /**
   * Set the authentication method to use for the HTTP request
   */
  public void setAuthenticator(HttpAuthenticator authenticator) {
    this.authenticator = authenticator;
  }
  
}
