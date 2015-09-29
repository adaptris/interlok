package com.adaptris.core.http.client.net;

import java.util.List;
import java.util.Map;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Concrete implementation of {@link ResponseHeaderHandler} which adds all the HTTP headers from the
 * response as object metadata to the {@link AdaptrisMessage}.
 * 
 * <p>For each header field that exists, there will be a corresponding {@link URLConnectionHeaderField} in object metadata.
 * Note that {@link HttpURLConnection} exposes header fields as a {@code List<String>} so this is reflected in the {@link
 * URLConnectionHeaderField}. In most situations there will only be a single entry in the list.
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
public class ResponseHeadersAsObjectMetadata extends MetadataResponseHeaderImpl {

  public ResponseHeadersAsObjectMetadata() {

  }

  public ResponseHeadersAsObjectMetadata(String prefix) {
    this();
    setMetadataPrefix(prefix);
  }

  protected void addMetadata(Map<String, List<String>> headers, AdaptrisMessage reply) {
    for (String key : headers.keySet()) {
      List<String> list = headers.get(key);
      String metadataKey = generateKey(key);
      log.trace("Adding Object Metadata [{}: {}]", metadataKey, list);
      reply.getObjectMetadata().put(generateKey(key), new URLConnectionHeaderField(key, list));
    }
  }

}
