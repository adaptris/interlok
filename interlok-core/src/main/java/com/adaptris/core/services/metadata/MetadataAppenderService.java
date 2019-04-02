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
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Service to append multiple metadata keys together to form a new key.
 * <p>
 * If any value associated with a metadata key is null, then that value will be ignored.
 * </p>
 * 
 * @config metadata-appender-service
 * 
 * 
 */
@XStreamAlias("metadata-appender-service")
@AdapterComponent
@ComponentProfile(summary = "Concatenate various metadata values into one", tag = "service,metadata")
@DisplayOrder(order = {"appendKeys", "resultKey", "metadataLogger"})
public class MetadataAppenderService extends MetadataServiceImpl {

  @NotNull
  @AutoPopulated
  @XStreamImplicit(itemFieldName = "append-key")
  private List<String> appendKeys;
  @NotBlank
  @AutoPopulated
  @AffectsMetadata
  private String resultKey;

  /**
   * <p>
   * Creates a new instance.  Default key for result metatadata is
   * 'metadata-appender-service'.
   * </p>
   */
  public MetadataAppenderService() {
    setAppendKeys(new ArrayList<String>());
    resultKey = "metadata-appender-service"; // default
  }

  @Override
  public void doService(AdaptrisMessage msg) {
    StringBuffer result = new StringBuffer();
    for (String key : appendKeys) {
      if (msg.getMetadataValue(key) != null) {
        result.append(msg.getMetadataValue(key));
      }
    }
    MetadataElement e = new MetadataElement(resultKey, result.toString());
    logMetadata("Added {}", e);
    msg.addMetadata(e);
  }

  /**
   * <p>
   * Returns the <code>List</code> of metadata keys whose values are to be
   * appended.
   * </p>
   * @return the <code>List</code> of metadata keys whose values are to be
   * appended
   */
  public List<String> getAppendKeys() {
    return appendKeys;
  }

  /**
   * <p>
   * Sets the <code>List</code> of metadata keys whose values are to be
   * appended.
   * </p>
   * @param l the <code>List</code> of metadata keys whose values are to be
   * appended
   */
  public void setAppendKeys(List<String> l) {
    appendKeys = Args.notNull(l, "appendKeys");
  }

  /**
   * <p>
   * Adds a metadata key whose value to append.
   * </p>
   * @param string the metadata key whose value to append, may not be null or
   * empty.
   */
  public void addAppendKey(String string) {
    appendKeys.add(Args.notBlank(string, "key"));
  }

  /**
   * <p>
   * Returns the metadata key that the concatenated metadata values will be
   * stored against.
   * </p>
   * @return the metadata key that the concatenated metadata will be stored
   * against
   */
  public String getResultKey() {
    return resultKey;
  }

  /**
   * <p>
   * Sets the metadata key that the concatenated metadata values will be stored
   * against.
   * </p>
   * @param string the metadata key that the concatenated metadata values will
   * be stored against, may not be null or empty.
   */
  public void setResultKey(String string) {
    resultKey = Args.notBlank(string, "resultKey");
  }

}
