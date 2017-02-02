/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.core;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.metadata.ElementFormatter;
import com.adaptris.core.metadata.ElementValueFormatter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Implementation of {@link ProduceDestination} that uses {@link String#format(String, Object...)} along with metadata to create a
 * dynamic destination.
 * <p>
 * The following rules will apply when you are constructing your formatted destination
 * <ul>
 * <li>The format is passed directly to {@link String#format(String, Object...)} with no checks.</li>
 * <li>Each of the values that are associated with the {@link #getMetadataKeys()} and {@link #getObjectMetadataKeys()} will be
 * passed into the formatter.</li>
 * <li>Object Metadata values are always listed <strong>after</strong> standard metadata values.</li>
 * <li>Standard metadata values will be defaulted to an empty string, if not present in the message</li>
 * <li>Object Metadata values will be passed as-is (i.e. they may be null), behaviour will be dependent on the format/locale</li>
 * <li>If the resulting destination is the empty string; then null is returned</li>
 * </ul>
 * </p>
 * <h2>Examples</h2>
 * <p>
 * The message in question has the metadata <code>{key1=archive, key2=orders}</code> and object metadata containing
 * <code>{timestamp=new Date(1335948092985)}</code> (the date is roughly "2012-05-02 09:41:23"). The configuration for this
 * destination contains <code>metadataKeys={key1, key2}, objectMetadataKeys={timestamp}</code>. then we could use to following
 * formats for the destination-format to generate output that matches our criteria
 * <ul>
 * <li>"/path/to/my/dir" would give us <strong>/path/to/my/dir</strong>; which is equivalent to configuring a
 * {@link ConfiguredProduceDestination}</li>
 * <li>"/%1$s/%2$s/%3$tF" would give us <strong>/archive/orders/2012-05-02</strong></li>
 * <li>"/%1$s/%3$tA/%2$s" would give us <strong>/archive/Wednesday/orders</strong></li>
 * </ul>
 * </p>
 *
 * @config formatted-metadata-destination
 * @see String#format(String, Object...)
 * @see java.util.Formatter
 */
@XStreamAlias("formatted-metadata-destination")
@DisplayOrder(order = {"destinationTemplate", "metadataKeys", "objectMetadataKeys"})
public class FormattedMetadataDestination implements MessageDrivenDestination {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  @XStreamImplicit(itemFieldName = "metadata-key")
  @NotNull
  private List<String> metadataKeys;
  @XStreamImplicit(itemFieldName = "object-metadata-key")
  @NotNull
  private List<String> objectMetadataKeys;

  @NotBlank
  private String destinationTemplate;

  private ElementFormatter elementFormatter;

  public FormattedMetadataDestination() {
    metadataKeys = new ArrayList<String>();
    objectMetadataKeys = new ArrayList<String>();
    destinationTemplate = "";
    elementFormatter = new ElementValueFormatter();
  }

  public void addMetadataKey(String s) {
    metadataKeys.add(s);
  }

  public List<String> getMetadataKeys() {
    return metadataKeys;
  }

  /**
   * Get the element formatter.
   * 
   * @return The element formatter.
   */
  public ElementFormatter getElementFormatter() {
    return elementFormatter;
  }

  /**
   * Set the element formatter.
   * 
   * @param elementFormatter The element formatter.
   */
  public void setElementFormatter(ElementFormatter elementFormatter) {
    this.elementFormatter = elementFormatter;
  }

  public void setMetadataKeys(List<String> l) {
    if (l == null) {
      throw new IllegalArgumentException("metadata keys may not be null");
    }
    metadataKeys = l;
  }

  public void addObjectMetadataKey(String s) {
    objectMetadataKeys.add(s);
  }

  public List<String> getObjectMetadataKeys() {
    return objectMetadataKeys;
  }

  public void setObjectMetadataKeys(List<String> l) {
    if (l == null) {
      throw new IllegalArgumentException("object metadata keys may not be null");
    }
    objectMetadataKeys = l;
  }

  @Override
  public String getDestination(AdaptrisMessage msg) throws CoreException {
    String destinationName = defaultIfEmpty(String.format(getDestinationTemplate(), createParams(msg)), null);
    log.debug("Dynamic destination [" + destinationName + "]");
    return destinationName;
  }

  private Object[] createParams(AdaptrisMessage msg) {
    List<Object> result = new ArrayList<>();
    for (String key : metadataKeys) {
      String param = "";
      if (msg.headersContainsKey(key)) {
        param = defaultIfEmpty(elementFormatter.format(msg.getMetadata(key)), "");
      }
      log.trace("Adding Metadata [{}]=[{}]", key, param);
      result.add(param);
    }
    Map<?, ?> objectMetadata = msg.getObjectHeaders();
    for (String key : objectMetadataKeys) {
      log.trace("Adding Object Metadata [{}]=[{}]", key, objectMetadata.get(key));
      result.add(objectMetadata.get(key));
    }
    return result.toArray();
  }

  public String getDestinationTemplate() {
    return destinationTemplate;
  }

  public void setDestinationTemplate(String s) {
    if (s == null) {
      throw new IllegalArgumentException("destination template may not be null");
    }
    destinationTemplate = s;
  }
}
