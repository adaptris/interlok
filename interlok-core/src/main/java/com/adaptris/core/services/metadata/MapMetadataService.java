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

package com.adaptris.core.services.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairList;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Changes the value of a specific metadata key based on a regular expression match of the value associated with the key.
 * <p>
 * It supports the special syntax of {n} as the replacement value. This indicates that the corresponding match group should be used
 * as the replacement value.
 * </p>
 * <p>
 * There are similiarities between this service and {@link ReplaceMetadataValue} . This service could be easily replaced with
 * multiple instances of {@link ReplaceMetadataValue} and you should consider using that if you have to use match groups.
 * </p>
 * 
 * @config map-metadata-service
 * @see Pattern
 */
@XStreamAlias("map-metadata-service")
@AdapterComponent
@ComponentProfile(summary = "Change a metadata value based on a regular expression match", tag = "service,metadata")
@DisplayOrder(order = {"metadataKey", "metadataKeyMap", "metadataLogger"})
public class MapMetadataService extends MetadataServiceImpl {
  private static final String MATCH_GROUP_REGEX = "\\{([0-9]+)\\}";
  @NotBlank
  @AffectsMetadata
  private String metadataKey;
  @AutoPopulated
  @NotNull
  @Valid
  private KeyValuePairList metadataKeyMap;

  public MapMetadataService() {
    setMetadataKeyMap(new KeyValuePairList());
  }

  /**
   * @see com.adaptris.core.Service#doService(AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    if (metadataKey == null || !msg.headersContainsKey(metadataKey)) {
      log.debug("Message does not contain metadatakey [" + metadataKey + "]");
      return;
    }
    String metadataValue = msg.getMetadataValue(metadataKey);
    metadataValue = metadataValue == null ? "" : metadataValue;
    List<MetadataElement> mapped = new ArrayList<>();
    for (Iterator i = getMetadataKeyMap().getKeyValuePairs().iterator(); i.hasNext();) {
      KeyValuePair k = (KeyValuePair) i.next();
      if (metadataValue.matches(k.getKey())) {
        String newMetadataValue = doSubstitution(metadataValue, k, msg);
        MetadataElement e = new MetadataElement(metadataKey, newMetadataValue);
        msg.addMetadata(e);
        mapped.add(e);
        break;
      }
    }
    logMetadata("Modified Metadata : {}", mapped);
  }

  private String doSubstitution(String metadataValue, KeyValuePair kvp, AdaptrisMessage msg) {
    String result = msg.resolve(kvp.getValue());
    Pattern keyPattern = Pattern.compile(MATCH_GROUP_REGEX);
    Matcher keyMatcher = keyPattern.matcher(result);
    if (keyMatcher.matches()) {
      int group = Integer.parseInt(keyMatcher.group(1));
      Pattern p = Pattern.compile(kvp.getKey());
      Matcher m = p.matcher(metadataValue);
      if (m.matches()) {
        result = m.group(group);
      }
    }
    return result;
  }

  /**
   * <p>
   * Returns a {@link KeyValuePairList} in which the key is the regular
   * expression to match the metadata value against, and the value is the
   * replacement value.
   * </p>
   * <p>
   * It supports the special syntax of {n} which indicates that the
   * corresponding match group should be used as the replacement value
   * </p>
   *
   * @return a {@link KeyValuePairList}
   */
  public KeyValuePairList getMetadataKeyMap() {
    return metadataKeyMap;
  }

  /**
   * <p>
   * Sets a {@link KeyValuePairList} in which the key is the regular expression to match the metadata value against, and the value
   * is the replacement value (the replacement value may be an expression a-la {@code %message{metadataKey}}.
   * </p>
   *
   * @param m a {@link KeyValuePairList}
   */
  public void setMetadataKeyMap(KeyValuePairList m) {
    this.metadataKeyMap = Args.notNull(m, "metadataKeyMap");
  }

  /**
   * @return the metadataKey
   */
  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * @param s the metadataKey to set
   */
  public void setMetadataKey(String s) {
    this.metadataKey = s;
  }

}
