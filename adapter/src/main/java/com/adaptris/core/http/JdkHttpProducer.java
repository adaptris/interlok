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

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;
import static com.adaptris.core.http.HttpConstants.CONTENT_TYPE;
import static com.adaptris.core.http.HttpConstants.DEFAULT_SOCKET_TIMEOUT;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.perf4j.aop.Profiled;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.http.client.RequestMethodProvider;
import com.adaptris.core.http.client.RequestMethodProvider.RequestMethod;
import com.adaptris.core.http.client.net.StandardHttpProducer;
import com.adaptris.core.util.Args;
import com.adaptris.util.stream.StreamUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Producer that uses the standard JDK Http implementation.
 * <p>
 * The additional-headers configuration can be used to set the Content-Type header, along with any custom headers that are required.
 * </p>
 * <p>
 * As the producer uses the standard JRE implementation of HTTP rather than the Adaptris HTTP library, it supports HTTP Proxies, and
 * does not make any use of the configured AdaptrisConnection; so any associated connection should be
 * {@link com.adaptris.core.NullConnection}
 * </p>
 * 
 * @config jdk-http-producer
 * 
 * @see HttpURLConnection
 * @deprecated since 3.0.6 use {@link com.adaptris.core.http.client.net.StandardHttpProducer} instead.
 */
@XStreamAlias("jdk-http-producer")
@Deprecated
@AdapterComponent
@ComponentProfile(summary = "Make a HTTP request to a remote server using standard JRE components", tag = "producer,http,https")
public class JdkHttpProducer extends HttpProducer {
  private static transient boolean warningLogged;

  // Methods that allow doOutputUse; might not be the full list, we should probably check HTTP 1.1 specification.
  private static final Collection<RequestMethodProvider.RequestMethod> METHOD_ALLOWS_OUTPUT =
      Collections.synchronizedCollection(Arrays.asList(new RequestMethod[] {RequestMethod.POST, RequestMethod.PUT}));

  @AutoPopulated
  @NotBlank
  private String method;

  @AdvancedConfig
  private Boolean replyHttpHeadersAsMetadata;
  @AdvancedConfig
  private String replyMetadataPrefix;

  public JdkHttpProducer() {
    super();
    setMethod(RequestMethod.POST.name());
    if (!warningLogged) {
      log.warn("[{}] is deprecated, use [{}] instead", this.getClass().getSimpleName(), StandardHttpProducer.class.getName());
      warningLogged = true;
    }
  }

  public JdkHttpProducer(ProduceDestination d) {
    this();
    setDestination(d);
  }


  @Override
  protected long defaultTimeout() {
    return DEFAULT_SOCKET_TIMEOUT;
  }

  @Override
  @Profiled(tag = "{$this.getClass().getSimpleName()}.request()", logger = "com.adaptris.perf4j.http.TimingLogger")
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
      HttpURLConnection http = (HttpURLConnection) url.openConnection();
      http.setRequestMethod(methodToUse(msg).name());
      http.setInstanceFollowRedirects(handleRedirection());
      http.setDoInput(true);
      // ProxyUtil.applyBasicProxyAuthorisation(http);
      addHeaders(msg, http);
      if (getContentTypeKey() != null && msg.containsKey(getContentTypeKey())) {
        http.setRequestProperty(CONTENT_TYPE, msg.getMetadataValue(getContentTypeKey()));
      }
      // if (getAuthorisation() != null) {
      // http.setRequestProperty(AUTHORIZATION, getAuthorisation());
      // }
      // logHeaders("Request Information", "Request Method : " + http.getRequestMethod(), http.getRequestProperties().entrySet());
      sendMessage(msg, http);
      readResponse(http, reply);
      // logHeaders("Response Information", http.getResponseMessage(), http.getHeaderFields().entrySet());
    }
    catch (IOException e) {
      throw new ProduceException(e);
    }
    catch (CoreException e) {
      if (e instanceof ProduceException) {
        throw (ProduceException) e;
      }
      else {
        throw new ProduceException(e);
      }
    } finally {
      if (myAuth != null) {
        AdapterResourceAuthenticator.getInstance().removeAuthenticator(myAuth);
      }
    }
    return reply;
  }

  private void sendMessage(AdaptrisMessage src, HttpURLConnection dest) throws IOException, CoreException {
    RequestMethod methodToUse = methodToUse(src);
    if (!METHOD_ALLOWS_OUTPUT.contains(methodToUse)) {
      if (src.getSize() > 0) {
        log.trace("Ignoring payload with use of {} method", methodToUse.name());
      }
      return;
    }
    OutputStream out = null;
    InputStream in = null;
    try {
      dest.setDoOutput(true);
      if (getEncoder() != null) {
        getEncoder().writeMessage(src, dest);
      }
      else {
        if (src.getSize() < TWO_MEG) {
          out = dest.getOutputStream();
          out.write(src.getPayload());
          out.flush();
        }
        else {
          out = new BufferedOutputStream(dest.getOutputStream(), FOUR_MEG);
          in = new BufferedInputStream(src.getInputStream(), FOUR_MEG);
          IOUtils.copy(in, out);
        }
      }
    }
    finally {
      IOUtils.closeQuietly(out);
      IOUtils.closeQuietly(in);
    }
  }

  private void readResponse(HttpURLConnection http, AdaptrisMessage reply) throws IOException, CoreException {
    int responseCode = http.getResponseCode();
    logHeaders("Response Information", http.getResponseMessage(), http.getHeaderFields().entrySet());
    log.trace("Content-Length is " + http.getContentLength());

    if (responseCode < 200 || responseCode > 299) {
      if (ignoreServerResponseCode()) {
        log.trace("Ignoring HTTP Reponse code {}", responseCode);
        processErrorReply(http, reply);
        return;
      }
      else {
        throw new ProduceException("Failed to send payload, got " + responseCode);
      }
    }
    if (getEncoder() != null) {
      copy(getEncoder().readMessage(http), reply);
    }
    else {
      processReply(http, reply);
    }
  }

  private void processErrorReply(HttpURLConnection http, AdaptrisMessage reply) throws IOException, CoreException {
    InputStream error = null;
    OutputStream out = null;
    try {
      if (http.getContentLength() < TWO_MEG) {
        error = http.getErrorStream();
        out = reply.getOutputStream();
      }
      else {
        out = new BufferedOutputStream(reply.getOutputStream(), FOUR_MEG);
        error = new BufferedInputStream(http.getErrorStream(), FOUR_MEG);
      }
      StreamUtil.copyStream(error, out);
      if (httpHeadersAsMetadata()) {
        addReplyMetadata(http.getHeaderFields(), reply);
      }
    }
    finally {
      IOUtils.closeQuietly(error);
      IOUtils.closeQuietly(out);
    }
    reply.addMetadata(new MetadataElement(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE, String.valueOf(http.getResponseCode())));
  }

  private void processReply(HttpURLConnection http, AdaptrisMessage reply) throws IOException, CoreException {
    InputStream in = null;
    OutputStream out = null;
    try {
      if (http.getContentLength() < TWO_MEG) {
        in = http.getInputStream();
        out = reply.getOutputStream();
      }
      else {
        out = new BufferedOutputStream(reply.getOutputStream(), FOUR_MEG);
        in = new BufferedInputStream(http.getInputStream(), FOUR_MEG);
      }
      StreamUtil.copyStream(in, out);
      if (httpHeadersAsMetadata()) {
        addReplyMetadata(http.getHeaderFields(), reply);
      }
    }
    finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }
    reply.addMetadata(new MetadataElement(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE, String.valueOf(http.getResponseCode())));
  }

  private void addReplyMetadata(Map<String, List<String>> headers, AdaptrisMessage reply) {
    for (String key : headers.keySet()) {
      List<String> list = headers.get(key);
      log.trace("key = " + key);
      log.trace("Values = " + list);
      String metadataValue = "";
      for (Iterator<String> i = list.iterator(); i.hasNext();) {
       metadataValue += i.next();
       if (i.hasNext()) {
         metadataValue += "\t";
       }
      }
      if (key == null) {
        if (getReplyMetadataPrefix() != null) {
          reply.addMetadata(getReplyMetadataPrefix(), metadataValue);
        }
      } else {
        reply.addMetadata(getReplyMetadataPrefix() != null ? getReplyMetadataPrefix() + key : key, metadataValue);
      }
    }
  }

  @Override
  public void prepare() throws CoreException {
  }


  private void logHeaders(String header, String message, Set headers) {
    if (log.isTraceEnabled()) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      PrintWriter p = new PrintWriter(out);
      p.println(header);
      p.println(message);
      for (Iterator i = headers.iterator(); i.hasNext();) {
        Map.Entry e = (Map.Entry) i.next();
        p.println(e.getKey() + ": " + e.getValue());
      }
      p.flush();
      p.close();
      log.trace(out.toString());
    }
  }

  private void addHeaders(AdaptrisMessage msg, URLConnection c) {
    Properties p = getAdditionalHeaders(msg);
    for (Iterator i = p.keySet().iterator(); i.hasNext();) {
      String key = (String) i.next();
      c.addRequestProperty(key, p.getProperty(key));
    }
    // Bug#2555
    // for (String key : p.stringPropertyNames()) {
    // c.addRequestProperty(key, p.getProperty(key));
    // }
  }

  public String getMethod() {
    return method;
  }

  /**
   * Set the HTTP method to be used.
   *
   * @param s the method.
   */
  public void setMethod(String s) {
    this.method = Args.notNull(s, "Method");
  }


  RequestMethod methodToUse(AdaptrisMessage msg) {
    return RequestMethod.valueOf(getMethod().toUpperCase());
  }


  public Boolean getReplyHttpHeadersAsMetadata() {
    return replyHttpHeadersAsMetadata;
  }

  /**
   * Whether or not to use all the headers associated with the HTTP Response as
   * standard metadata items
   *
   * @param b true to record all http headers in the response as metadata,
   *          default false.
   */
  public void setReplyHttpHeadersAsMetadata(Boolean b) {
    replyHttpHeadersAsMetadata = b;
  }

  private boolean httpHeadersAsMetadata() {
    return replyHttpHeadersAsMetadata != null ? replyHttpHeadersAsMetadata.booleanValue() : false;
  }

  public String getReplyMetadataPrefix() {
    return replyMetadataPrefix;
  }

  /**
   * Set the prefix that will be prepended to any metadata elements generated
   * because {@link #setReplyHttpHeadersAsMetadata(Boolean)} has been set to true.
   *
   * @param prefix
   */
  public void setReplyMetadataPrefix(String prefix) {
    replyMetadataPrefix = prefix;
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
