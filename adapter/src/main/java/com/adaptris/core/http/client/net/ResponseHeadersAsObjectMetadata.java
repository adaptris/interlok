package com.adaptris.core.http.client.net;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.client.ResponseHeaderHandler;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Concrete implementation of {@link ResponseHeaderHandler} which adds all the HTTP headers from the
 * response as object metadata to the {@link AdaptrisMessage}.
 * 
 * <p>Because {@link HttpURLConnection} exposes header fields as a {@code List<String>}; this is the object that will be added to
 * object metadata for each header field. In most situations there will only be a single entry in the list, but that's just how
 * it is.
 * </p>
 * <p>This will include header fields where the key is {@code null}; this will end up as the string {@code "null"}. {@link
 * HttpURLConnection} exposes the HTTP status line (e.g. {@code 200 HTTP/1.1 OK} as a header field with no key so this will
 * generally be the object metadata associated with {@code "null"}.
 * </p>
 * @config http-response-headers-as-object-metadata
 * @author lchan
 * 
 */
@XStreamAlias("http-response-headers-as-object-metadata")
public class ResponseHeadersAsObjectMetadata implements ResponseHeaderHandler<HttpURLConnection> {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  private String metadataPrefix;

  public ResponseHeadersAsObjectMetadata() {

  }

  public ResponseHeadersAsObjectMetadata(String prefix) {
    this();
    setMetadataPrefix(prefix);
  }

  @Override
  public AdaptrisMessage handle(HttpURLConnection src, AdaptrisMessage msg) {
    addMetadata(src.getHeaderFields(), msg);
    return msg;
  }

  private String generateKey(String header) {
    return defaultIfEmpty(getMetadataPrefix(), "") + header;
  }

  public String getMetadataPrefix() {
    return metadataPrefix;
  }

  public void setMetadataPrefix(String metadataPrefix) {
    this.metadataPrefix = metadataPrefix;
  }


  private void addMetadata(Map<String, List<String>> headers, AdaptrisMessage reply) {
    for (String key : headers.keySet()) {
      List<String> list = headers.get(key);
      log.trace("key = " + key);
      log.trace("Values = " + list);
      reply.getObjectMetadata().put(generateKey(key), list);
    }
  }

}
