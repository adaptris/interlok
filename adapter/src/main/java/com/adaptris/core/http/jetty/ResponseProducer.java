package com.adaptris.core.http.jetty;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.perf4j.aop.Profiled;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageProducerImp;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.core.ServiceException;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.adaptris.util.stream.StreamUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of <code>AdaptrisMessageProducer</code> that modifies the <code>HttpServletResponse</code> object metadata
 * provided by the Jetty engine.
 * <p>
 * The payload of the <code>AdaptrisMessage</code> is set as the body of the HTTP Response. Additional headers can be set by
 * configuring the associated <code>additional-headers</code> object.
 * </p>
 * 
 * @config jetty-response-producer
 * @license BASIC
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("jetty-response-producer")
public class ResponseProducer extends ProduceOnlyProducerImp {
  private static final String DEFAULT_METADATA_REGEXP = "X-HTTP.*";
  private static final boolean DEFAULT_FORWARD_CONNECTION_EXCEPTION = false;
  
  private int httpResponseCode;
  @NotNull
  @AutoPopulated
  @AdvancedConfig
  private KeyValuePairSet additionalHeaders;
  @AdvancedConfig
  private String contentTypeKey = null;
  @AdvancedConfig
  private String sendMetadataRegexp;
  private Boolean sendPayload;
  @AdvancedConfig
  private Boolean sendMetadataAsHeaders;
  @AdvancedConfig
  private Boolean flushBuffer;
  @AdvancedConfig
  private Boolean forwardConnectionException;
  @NotNull
  @AutoPopulated
  @Valid
  @AdvancedConfig
  private MetadataFilter metadataFilter;

  public ResponseProducer() {
    super();
    httpResponseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    setAdditionalHeaders(new KeyValuePairSet());
    setContentTypeKey(null);
    setMetadataFilter(new RemoveAllMetadataFilter());
  }

  public ResponseProducer(int responseCode) {
    this();
    setHttpResponseCode(responseCode);
  }

  /**
   * @see AdaptrisMessageProducerImp#produce(AdaptrisMessage,
   *      ProduceDestination)
   */
  @Override
  @Profiled(tag = "{$this.getClass().getSimpleName()}.produce()", logger = "com.adaptris.perf4j.http.jetty.TimingLogger")
  public void produce(AdaptrisMessage msg, ProduceDestination destination)
      throws ProduceException {
    HttpServletResponse response = (HttpServletResponse) msg
        .getObjectMetadata().get(CoreConstants.JETTY_RESPONSE_KEY);
    InputStream in = null;

    try {
      if (response == null) {
        log.debug("No HttpServletResponse in object metadata, nothing to do");
        return;
      }
      for (Iterator i = additionalHeaders.getKeyValuePairs().iterator(); i.hasNext();) {
        KeyValuePair k = (KeyValuePair) i.next();
        response.addHeader(k.getKey(), k.getValue());
      }

      MetadataCollection metadataSubset = getMetadataFilter().filter(msg);
      for (MetadataElement me : metadataSubset) {
        response.addHeader(me.getKey(), me.getValue());
      }

      if (getContentTypeKey() != null && msg.containsKey(getContentTypeKey())) {
        response.setContentType(msg.getMetadataValue(getContentTypeKey()));
      }
      response.setStatus(getHttpResponseCode());
      if (sendPayload()) {
        if (getEncoder() != null) {
          getEncoder().writeMessage(msg, response);
        }
        else {
          if (msg.getSize() > 0) {
            in = msg.getInputStream();
            try {
              StreamUtil.copyStream(in, response.getOutputStream());
            } catch (IOException ioe) {
              // if we catch an IOE here, chances are the connection is down and there isn't much we can do.
              log.error("Cannot send the response, the connection appears to be down, either a timeout or the client has closed the connection.");
              if(forwardConnectionExceptions())
                throw ioe;
            }
          }
        }
      }
      else {
        log.trace("Ignoring Payload");
      }
      if (flushBuffers()) {
        response.flushBuffer();
      }
    }
    catch (Exception e) {
      throw new ProduceException(e);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
  }
  
  private boolean forwardConnectionExceptions() {
    return this.getForwardConnectionException() == null ? DEFAULT_FORWARD_CONNECTION_EXCEPTION : this.getForwardConnectionException();
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
    if (sendMetadataAsHeaders()) {
      if (getSendMetadataRegexp() == null && getMetadataFilter() instanceof RemoveAllMetadataFilter) {
        log.warn("No Metadata Regular expression configured, ignoring sendMetadataAsHeaders=true");
        setSendMetadataAsHeaders(Boolean.FALSE);
      }
      else {
        if (getSendMetadataRegexp() != null && getMetadataFilter() instanceof RemoveAllMetadataFilter) {
          log.trace("Overriding metadata-filter with filter based on {}", getSendMetadataRegexp());
          RegexMetadataFilter filter = new RegexMetadataFilter();
          filter.addIncludePattern(getSendMetadataRegexp());
          setMetadataFilter(filter);
        }
      }
    }
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

  /**
   *
   * @see com.adaptris.core.AdaptrisComponent#isEnabled(License)
   */
  @Override
  public boolean isEnabled(License l) throws CoreException {
    return l.isEnabled(LicenseType.Basic);
  }

  /**
   * Get the headers to be sent to the client.
   *
   * @return the headers
   */
  public KeyValuePairSet getAdditionalHeaders() {
    return additionalHeaders;
  }

  /**
   * Set the headers to be sent to the client.
   *
   * @param h the headers.
   */
  public void setAdditionalHeaders(KeyValuePairSet h) {
    additionalHeaders = h;
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
   * @see HttpServletResponse
   * @param c
   */
  public void setHttpResponseCode(int c) {
    httpResponseCode = c;
  }

  /**
   * Get the metadata key from which to extract the content type.
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
    sb.append(",additionalHeaders=[").append(getAdditionalHeaders());
    sb.append("],responseCode=").append(getHttpResponseCode());
    sb.append("]]");
    return sb.toString();
  }

  /**
   * @return the sendPayload
   */
  public Boolean getSendPayload() {
    return sendPayload;
  }

  /**
   * Whether or not to send the {@link AdaptrisMessage#getPayload()} as part of the reply.
   *
   * @param b the sendPayload to set defaults true.
   */
  public void setSendPayload(Boolean b) {
    sendPayload = b;
  }

  boolean sendPayload() {
    return getSendPayload() != null ? getSendPayload().booleanValue() : true;
  }

  /**
   * Whether or not to send {@link AdaptrisMessage#getMetadata()} as a standard HTTP Header
   * 
   * @return the sendMetadataAsHeaders
   * @deprecated since 3.0.2 use {@link #setMetadataFilter(MetadataFilter)} instead.
   */
  @Deprecated
  public Boolean getSendMetadataAsHeaders() {
    return sendMetadataAsHeaders;
  }

  /**
   * Specify whether or not to send selected {@link AdaptrisMessage} metadata as HTTP Headers or not.
   * 
   * @param b the sendMetadataAsHeaders to set defaults false.
   * @deprecated since 3.0.2 use {@link #setMetadataFilter(MetadataFilter)} instead.
   */
  @Deprecated
  public void setSendMetadataAsHeaders(Boolean b) {
    sendMetadataAsHeaders = b;
  }

  @Deprecated
  boolean sendMetadataAsHeaders() {
    return getSendMetadataAsHeaders() != null ? getSendMetadataAsHeaders().booleanValue() : false;
  }

  /**
   * @return the sendMetadataCriteria
   * @deprecated since 3.0.2 use {@link #setMetadataFilter(MetadataFilter)} instead.
   */
  @Deprecated
  public String getSendMetadataRegexp() {
    return sendMetadataRegexp;
  }

  /**
   * Specify the {@link AdaptrisMessage} metadata keys that will be sent as HTTP Headers.
   * <p>
   * Any metadata keys that match this regular expression will be sent; the metadata key is the HTTP header name, the metadata value
   * becomes the HTTP header value.
   * </p>
   * <p>
   * Keys that match this regular expression will override any statically configured {@link #setAdditionalHeaders(KeyValuePairSet)}
   * entries
   * </p>
   * 
   * @param regexp the regular expression; keys which match this expression will be sent as HTTP Headers (default
   *          <code>X-HTTP.*</code>);
   * @see java.util.regex.Pattern
   * @deprecated since 3.0.2 use {@link #setMetadataFilter(MetadataFilter)} instead.
   */
  @Deprecated
  public void setSendMetadataRegexp(String regexp) {
    if (regexp == null) {
      throw new IllegalArgumentException("Illegal Metadata Regexp [" + regexp
          + "]");
    }
    sendMetadataRegexp = regexp;
  }

  public Boolean getFlushBuffer() {
    return flushBuffer;
  }

  /**
   * Whether or not to execute {@link ServletResponse#flushBuffer()} at the end of the produce method.
   *
   * @param b (defaults true).
   */
  public void setFlushBuffer(Boolean b) {
    flushBuffer = b;
  }

  boolean flushBuffers() {
    return getFlushBuffer() != null ? getFlushBuffer().booleanValue() : true;
  }

  public Boolean getForwardConnectionException() {
    return forwardConnectionException;
  }

  /**
   * Set to true to throw an exception if producing the response fails.
   * 
   * <p>
   * When producing the reply to a client; it may be that they have already terminated the connection. By default client
   * disconnections will not generate a {@link ServiceException} so normal processing continues. Set this to be true if you want
   * error handling to be triggered in this situation.
   * </p>
   * 
   * @param b true to throw a ServiceException if producing the response fails., default null (false).
   */
  public void setForwardConnectionException(Boolean b) {
    this.forwardConnectionException = b;
  }

  public MetadataFilter getMetadataFilter() {
    return metadataFilter;
  }

  /**
   * Specify the {@link AdaptrisMessage} metadata keys that will be sent as HTTP Headers.
   * <p>
   * Any metadata that is returned by this filter will be sent as HTTP headers. Any values that match will override any statically
   * configured {@link #setAdditionalHeaders(KeyValuePairSet)} entries
   * </p>
   * 
   * @param metadataFilter the filter.
   * @see MetadataFilter
   * @since 3.0.2
   */
  public void setMetadataFilter(MetadataFilter metadataFilter) {
    if (metadataFilter == null) {
      throw new IllegalArgumentException("Filter is null");
    }
    this.metadataFilter = metadataFilter;
  }
}
