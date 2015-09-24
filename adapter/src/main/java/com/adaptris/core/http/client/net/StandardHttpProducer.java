package com.adaptris.core.http.client.net;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;
import static com.adaptris.core.http.HttpConstants.CONTENT_TYPE;

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
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default {@link HttpProducer} implementation.
 * 
 * @config standard-http-producer
 * @license BASIC
 * @author lchan
 *
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
    HttpURLConnection request = http;
    RequestMethod rm = getMethod(msg);
    http.setRequestMethod(rm.name());
    http.setInstanceFollowRedirects(handleRedirection());
    http.setDoInput(true);
    getRequestHeaderProvider().addHeaders(msg, http);
    http.setRequestProperty(CONTENT_TYPE, getContentTypeProvider().getContentType(msg));
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

  /**
   * @see com.adaptris.core.AdaptrisComponent#isEnabled(License)
   */
  @Override
  public boolean isEnabled(License l) throws CoreException {
    return l.isEnabled(LicenseType.Basic);
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
