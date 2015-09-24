package com.adaptris.core.http.client.net;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;

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
 * @config http-response-headers-as-metadata
 * @author lchan
 * 
 */
@XStreamAlias("http-response-headers-as-metadata")
public class ResponseHeadersAsMetadata implements ResponseHeaderHandler<HttpURLConnection> {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  private String metadataPrefix;

  public ResponseHeadersAsMetadata() {

  }

  public ResponseHeadersAsMetadata(String prefix) {
    this();
    setMetadataPrefix(prefix);
  }

  @Override
  public AdaptrisMessage handle(HttpURLConnection src, AdaptrisMessage msg) {
    addReplyMetadata(src.getHeaderFields(), msg);
    return msg;
  }

  private String generateKey(String header) {
    return defaultIfEmpty(getMetadataPrefix(), "") + defaultIfEmpty(header, "");
  }

  public String getMetadataPrefix() {
    return metadataPrefix;
  }

  public void setMetadataPrefix(String metadataPrefix) {
    this.metadataPrefix = metadataPrefix;
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
      String metadataKey = generateKey(key);
      if (!isBlank(metadataKey)) {
        reply.addMetadata(metadataKey, metadataValue);
      }
    }
  }

}
