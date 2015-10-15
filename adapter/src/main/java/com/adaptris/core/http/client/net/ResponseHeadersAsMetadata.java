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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Concrete implementation of {@link com.adaptris.core.http.client.ResponseHeaderHandler} which adds all the HTTP headers from the
 * response as metadata to the {@link AdaptrisMessage}.
 *
 * <p>Because {@link java.net.HttpURLConnection} exposes headers as a {@code List<String>}; {@code #setMetadataSeparator(String)} is
 * used
 * as a separator between multiple items in the list to flatten the list into a single metadata value. The default value is the
 * tab character ("\t").</p>
 * <p>This will include header fields where the key is {@code null}; this will end up as the string {@code "null"}. {@link
 * java.net.HttpURLConnection} exposes the HTTP status line (e.g. {@code 200 HTTP/1.1 OK} as a header field with no key so this will
 * generally be what is associated with {@code "null"}.
 * </p>
 * 
 * @config http-response-headers-as-metadata
 * @author lchan
 * 
 */
@XStreamAlias("http-response-headers-as-metadata")
public class ResponseHeadersAsMetadata extends MetadataResponseHeaderImpl {

  private static final String DEFAULT_SEPARATOR_CHAR = "\t";

  @AdvancedConfig
  private String metadataSeparator;

  public ResponseHeadersAsMetadata() {

  }

  public ResponseHeadersAsMetadata(String prefix) {
    this(prefix, null);
  }

  public ResponseHeadersAsMetadata(String prefix, String separator) {
    this();
    setMetadataPrefix(prefix);
    setMetadataSeparator(separator);
  }


  protected void addMetadata(Map<String, List<String>> headers, AdaptrisMessage reply) {
    for (String key : headers.keySet()) {
      List<String> list = headers.get(key);
      String metadataValue = "";
      for (Iterator<String> i = list.iterator(); i.hasNext();) {
        metadataValue += i.next();
        if (i.hasNext()) {
          metadataValue += metadataSeparator();
        }
      }
      String metadataKey = generateKey(key);
      log.trace("Adding Metadata [{}: {}]", metadataKey, metadataValue);
      reply.addMetadata(generateKey(key), metadataValue);
    }
  }

  public String getMetadataSeparator() {
    return metadataSeparator;
  }

  /**
   * Set the separator to be used when multiple headers should be associated with the same key.
   * 
   * <p>
   * Because {@link java.net.HttpURLConnection} exposes headers as a {@code List<String>}; {@code #setMetadataSeparator(String)} is
   * used as a separator between multiple items in the list to flatten the list into a single metadata value. The default value is
   * the tab character ("\t").
   * </p>
   * 
   * @param s the separator (default if not specified is "\t");
   */
  public void setMetadataSeparator(String s) {
    this.metadataSeparator = s;
  }

  String metadataSeparator() {
    return getMetadataSeparator() != null ? getMetadataSeparator() : DEFAULT_SEPARATOR_CHAR;
  }
}
