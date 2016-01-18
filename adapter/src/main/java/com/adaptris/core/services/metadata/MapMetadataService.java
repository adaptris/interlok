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

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
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
 * 
 * 
 * @see Pattern
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("map-metadata-service")
@AdapterComponent
@ComponentProfile(summary = "Change a metadata value based on a regular expression match", tag = "service,metadata")
public class MapMetadataService extends ServiceImp {
  private static final String MATCH_GROUP_REGEX = "\\{([0-9]+)\\}";
  @NotBlank
  private String metadataKey;
  @AutoPopulated
  @NotNull
  private KeyValuePairList metadataKeyMap;

  public MapMetadataService() {
    setMetadataKeyMap(new KeyValuePairList());
  }

  /**
   * @see com.adaptris.core.Service#doService(AdaptrisMessage)
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    if (metadataKey == null || !msg.containsKey(metadataKey)) {
      log.debug("Message does not contain metadatakey [" + metadataKey + "]");
      return;
    }
    String metadataValue = msg.getMetadataValue(metadataKey);
    metadataValue = metadataValue == null ? "" : metadataValue;

    for (Iterator i = getMetadataKeyMap().getKeyValuePairs().iterator(); i.hasNext();) {
      KeyValuePair k = (KeyValuePair) i.next();
      if (metadataValue.matches(k.getKey())) {
        String newMetadataValue = doSubstitution(metadataValue, k);
        log.debug("Modifying value [" + metadataValue + "] to [" + newMetadataValue + "]");
        msg.addMetadata(metadataKey, newMetadataValue);
        break;
      }
    }
  }

  private String doSubstitution(String metadataValue, KeyValuePair kvp) {
    String result = kvp.getValue();
    Pattern keyPattern = Pattern.compile(MATCH_GROUP_REGEX);
    Matcher keyMatcher = keyPattern.matcher(kvp.getValue());
    if (keyMatcher.matches()) {
      int group = Integer.valueOf(keyMatcher.group(1)).intValue();
      Pattern p = Pattern.compile(kvp.getKey());
      Matcher m = p.matcher(metadataValue);
      if (m.matches()) {
        result = m.group(group);
      }
    }
    return result;
  }


  @Override
  protected void initService() throws CoreException {

  }

  @Override
  protected void closeService() {

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
   * Sets a {@link KeyValuePairList} in which the key is the regular expression
   * to match the metadata value against, and the value is the replacement
   * value.
   * </p>
   *
   * @param m a {@link KeyValuePairList}
   */
  public void setMetadataKeyMap(KeyValuePairList m) {
    if (m == null) {
      throw new IllegalArgumentException("Null Metadata Key Map");
    }
    this.metadataKeyMap = m;
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer(super.toString());
    result.append(this.getMetadataKeyMap());
    return result.toString();
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

  @Override
  public void prepare() throws CoreException {
  }

}
