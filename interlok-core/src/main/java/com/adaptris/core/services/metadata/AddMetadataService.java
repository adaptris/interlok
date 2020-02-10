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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * Implementation of {@link com.adaptris.core.Service} that adds static metadata to be added to a message.
 * </p>
 * <p>
 * Each metadata-element is added in sequence to the message, overwriting any existing metadata.
 * </p>
 * <p>
 * Additional behaviour is possible based on the value portion of the metadata-element.
 * <ul>
 * <li>$UNIQUE_ID$ - add the messages unique id as metadata</li>
 * <li>$MSG_SIZE$ - add the messages current size as metadata</li>
 * </ul>
 * </p>
 * 
 * @config add-metadata-service
 * 
 * 
 */
@XStreamAlias("add-metadata-service")
@AdapterComponent
@ComponentProfile(summary = "Add Static Metadata to a Message", tag = "service,metadata")
@DisplayOrder(order = {"metadataElements", "overwrite", "metadataLogger"})
public class AddMetadataService extends MetadataServiceImpl {

  private static final String UNIQUE_ID_MNENOMIC = "$UNIQUE_ID$";
  private static final String FILE_SIZE_MNEMONIC = "$MSG_SIZE$";

  private static enum MethodHandler {
    UniqueId {
      @Override
      String getValue(AdaptrisMessage msg) {
        return msg.getUniqueId();
      }
    },
    MessageSize {
      @Override
      String getValue(AdaptrisMessage msg) {
        return "" + msg.getSize();
      }
    };
    abstract String getValue(AdaptrisMessage msg) throws ServiceException;
  }

  private static final String[] HANDLER_STRING_ARRAY =
  {
      UNIQUE_ID_MNENOMIC, FILE_SIZE_MNEMONIC
  };

  private static final MethodHandler[] HANDLER_ARRAY =
  {
      MethodHandler.UniqueId, MethodHandler.MessageSize
  };

  private static final Map<String, MethodHandler> HANDLERS;

  static {
    Map<String, MethodHandler> ht = new HashMap<String, MethodHandler>();
    for (int i = 0; i < HANDLER_STRING_ARRAY.length; i++) {
      ht.put(HANDLER_STRING_ARRAY[i], HANDLER_ARRAY[i]);
    }
    HANDLERS = Collections.unmodifiableMap(ht);
  }

  @XStreamImplicit
  @Valid
  @NotNull
  @AutoPopulated
  @AffectsMetadata
  private Set<MetadataElement> metadataElements;

  @InputFieldDefault(value = "true")
  private Boolean overwrite;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public AddMetadataService() {
    super();
    setMetadataElements(new HashSet<MetadataElement>());
  }

  public AddMetadataService(Collection<MetadataElement> elements) {
    this();
    setMetadataElements(new HashSet<MetadataElement>(elements));
  }

  public AddMetadataService(MetadataElement... elements) {
    this(new HashSet<MetadataElement>(Arrays.asList(elements)));
  }

  /**
   * <p>
   * Adds the configured metadata to the message.
   * </p>
   *
   * @param msg the message to process
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    Set<MetadataElement> addedMetadata = new HashSet<MetadataElement>();
    for (MetadataElement e : metadataElements) {
      MetadataElement addMe = build(e, msg);
      if (overwrite(msg, addMe.getKey())) {
        msg.addMetadata(addMe);
        addedMetadata.add(addMe);
      }
    }
    logMetadata("metadata added : {}", addedMetadata);
  }

  /**
   * <p>
   * Returns the <code>MetadataElement</code>s that will be added to the <code>AdaptrisMessage</code>.
   * </p>
   *
   * @return the <code>MetadataElement</code>s that will be added to the <code>AdaptrisMessage</code>
   */
  public Set<MetadataElement> getMetadataElements() {
    return metadataElements;
  }

  /**
   * <p>
   * Sets the <code>MetadataElement</code>s that will be added to the <code>AdaptrisMessage</code>.
   * </p>
   *
   * @param s the <code>MetadataElement</code>s that will be added to the <code>AdaptrisMessage</code>
   */
  public void setMetadataElements(Set<MetadataElement> s) {
    metadataElements = s;
  }

  /**
   * <p>
   * Adds a <code>MetadataElement</code>s to the <code>Set</code> that will be added to the <code>AdaptrisMessage</code>.
   * </p>
   *
   * @param element a <code>MetadataElement</code>s to add to the <code>AdaptrisMessage</code>
   */
  public void addMetadataElement(MetadataElement element) {
    metadataElements.add(element);
  }

  /**
   * <p>
   * Adds a <code>MetadataElement</code>s made up of the passed key and value to the <code>Set</code> that will be added to the
   * <code>AdaptrisMessage</code> .
   * </p>
   *
   * @param key the metadata key, may not be null or empty
   * @param value the metadata value
   */
  public void addMetadataElement(String key, String value) {
    metadataElements.add(new MetadataElement(key, value));
  }

  /**
   * @return the overwrite
   */
  public Boolean getOverwrite() {
    return overwrite;
  }

  /**
   * Whether or not to always overwrite metadata with the values configured.
   * 
   * @param b the overwrite to set, default is true.
   */
  public void setOverwrite(Boolean b) {
    this.overwrite = b;
  }

  private boolean overwrite() {
    return BooleanUtils.toBooleanDefaultIfNull(getOverwrite(), true);
  }

  protected boolean overwrite(AdaptrisMessage msg, String key) {
    boolean result = true;
    if (msg.headersContainsKey(key)) {
      if (!overwrite()) {
        result = false;
      }
    }
    return result;
  }

  // Always create a new MetadataElement as downstream services may actually modify the instance
  protected static MetadataElement build(MetadataElement e, AdaptrisMessage msg) throws ServiceException {
    if (HANDLERS.containsKey(e.getValue())) {
      return new MetadataElement(e.getKey(), HANDLERS.get(e.getValue()).getValue(msg));
    }
    return new MetadataElement(e.getKey(), e.getValue());
  }
}
