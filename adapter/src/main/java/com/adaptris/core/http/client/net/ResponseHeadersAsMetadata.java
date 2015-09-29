package com.adaptris.core.http.client.net;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.client.ResponseHeaderHandler;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Concrete implementation of {@link ResponseHeaderHandler} which adds all the HTTP headers from the
 * response as metadata to the {@link AdaptrisMessage}.
 *
 * <p>This will include header fields where the key is {@code null}; this will end up as the string {@code "null"}. {@link
 * HttpURLConnection} exposes the HTTP status line (e.g. {@code 200 HTTP/1.1 OK} as a header field with no key so this will
 * generally be what is associated with {@code "null"}.
 * </p>
 * 
 * @config http-response-headers-as-metadata
 * @author lchan
 * 
 */
@XStreamAlias("http-response-headers-as-metadata")
public class ResponseHeadersAsMetadata implements ResponseHeaderHandler<HttpURLConnection> {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  private String metadataPrefix;

  public ResponseHeadersAsMetadata() {

  }

  public ResponseHeadersAsMetadata(String prefix) {
    this();
    setMetadataPrefix(prefix);
  }

  @Override
  public AdaptrisMessage handle(HttpURLConnection src, AdaptrisMessage msg) {
    addMetadata(src.getHeaderFields(), msg);
    return msg;
  }

  protected String generateKey(String header) {
    return defaultIfEmpty(getMetadataPrefix(), "") + header;
  }

  public String getMetadataPrefix() {
    return metadataPrefix;
  }

  public void setMetadataPrefix(String metadataPrefix) {
    this.metadataPrefix = metadataPrefix;
  }


  protected void addMetadata(Map<String, List<String>> headers, AdaptrisMessage reply) {
    for (String key : headers.keySet()) {
      List<String> list = headers.get(key);
      String metadataValue = "";
      for (Iterator<String> i = list.iterator(); i.hasNext();) {
        metadataValue += i.next();
        if (i.hasNext()) {
          metadataValue += "\t";
        }
      }
      String metadataKey = generateKey(key);
      log.trace("{}:{}", metadataKey, metadataValue);
      reply.addMetadata(generateKey(key), metadataValue);
    }
  }

}
