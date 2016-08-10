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

package com.adaptris.core;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.adaptris.annotation.DisplayOrder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * The default factory which returns implementations of <code>AdaptrisMessage</code>.
 * </p>
 * 
 * @config default-message-factory
 */
@XStreamAlias("default-message-factory")
@DisplayOrder(order = {"defaultCharEncoding"})
public class DefaultMessageFactory extends AdaptrisMessageFactory {

  private String defaultCharEncoding;

  public DefaultMessageFactory() {
    super();
  }

  @Override
  public AdaptrisMessage newMessage(byte[] payload, Set metadata) {
    AdaptrisMessage result = newMessage();
    result.setPayload(payload);
    result.setMetadata(metadata);

    return result;
  }

  @Override
  public AdaptrisMessage newMessage(byte[] payload) {
    AdaptrisMessage result = newMessage();
    result.setPayload(payload);

    return result;
  }

  @Override
  public AdaptrisMessage newMessage(String payload, Set metadata) {
    AdaptrisMessage result = newMessage();
    result.setContent(payload, getDefaultCharEncoding());
    result.setMetadata(metadata);

    return result;
  }

  @Override
  public AdaptrisMessage newMessage(String payload) {
    AdaptrisMessage result = newMessage();
    result.setContent(payload, getDefaultCharEncoding());
    return result;
  }

  @Override
  public AdaptrisMessage newMessage(String payload, String charEncoding,
                                    Set metadata)
      throws UnsupportedEncodingException {
    AdaptrisMessage result = newMessage();
    result.setContent(payload, charEncoding);
    result.setMetadata(metadata);

    return result;
  }

  @Override
  public AdaptrisMessage newMessage(String payload, String charEncoding)
      throws UnsupportedEncodingException {

    return newMessage(payload, charEncoding, null);
  }

  @Override
  public AdaptrisMessage newMessage(AdaptrisMessage source,
                                    Collection metadataKeysToPreserve)
      throws CloneNotSupportedException {
    AdaptrisMessage result = newMessage();
    result.setUniqueId(source.getUniqueId());
    for (Iterator i = metadataKeysToPreserve.iterator(); i.hasNext();) {
      String key = (String) i.next();
      if (source.headersContainsKey(key)) {
        result.addMetadata(key, source.getMetadataValue(key));
      }
    }
    MessageLifecycleEvent mle = result.getMessageLifecycleEvent();
    List<MleMarker> markers = source.getMessageLifecycleEvent().getMleMarkers();

    for (int i = 0; i < markers.size(); i++) {
      MleMarker marker = (MleMarker) ((MleMarker) markers.get(i)).clone();
      mle.addMleMarker(marker);
    }
    result.getObjectHeaders().putAll(source.getObjectHeaders());
    return result;
  }

  @Override
  public AdaptrisMessage newMessage() {
    AdaptrisMessage m = new DefaultAdaptrisMessageImp(uniqueIdGenerator(), this);
    if (!isEmpty(getDefaultCharEncoding())) {
      m.setContentEncoding(getDefaultCharEncoding());
    }
    return m;
  }

  /**
   * @return the defaultCharEncoding
   */
  @Override
  public String getDefaultCharEncoding() {
    return defaultCharEncoding;
  }

  /**
   * Set the default character encoding to be applied to the message upon
   * creation.
   * <p>
   * If not explicitly configured, then the platform default character encoding
   * will be used.
   * </p>
   *
   * @param s the defaultCharEncoding to set
   * @see AdaptrisMessage#setCharEncoding(String)
   */
  @Override
  public void setDefaultCharEncoding(String s) {
    defaultCharEncoding = s;
  }

}
