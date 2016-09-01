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
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * 
 * @config add-formatted-metadata-service
 */
@XStreamAlias("add-formatted-metadata-service")
@AdapterComponent
@ComponentProfile(summary = "Add a formatted metadata item to a Message", tag = "service,metadata")
@DisplayOrder(order = {"metadataKey", "formatString", "argumentMetadataKeys"})
public class AddFormattedMetadataService extends ServiceImp {

  @NotBlank
  private String formatString;
  @NotNull
  @AutoPopulated
  @XStreamImplicit(itemFieldName = "argument-metadata-key")
  private List<String> argumentMetadataKeys;
  @NotBlank
  @AffectsMetadata
  private String metadataKey;

  public AddFormattedMetadataService() {
    setArgumentMetadataKeys(new ArrayList<String>());
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    String formattedValue = String.format(formatString, resolveMetadata(msg));
    msg.addMessageHeader(getMetadataKey(), formattedValue);
  }

  private Object[] resolveMetadata(AdaptrisMessage msg) throws ServiceException {
    List<String> values = new ArrayList<>();
    for (String key : argumentMetadataKeys) {
      if (!msg.containsKey(key)) {
        throw new ServiceException("[" + key + "] does not exist as metadata");
      }
      values.add(msg.getMetadataValue(key));
    }
    return values.toArray(new Object[values.size()]);
  }

  @Override
  public void prepare() throws CoreException {}

  @Override
  protected void initService() throws CoreException {}

  @Override
  protected void closeService() {}

  /**
   * @return the formatString
   */
  public String getFormatString() {
    return formatString;
  }

  /**
   * @param formatString the formatString to set
   */
  public void setFormatString(String formatString) {
    this.formatString = Args.notBlank(formatString, "format-string");
  }

  /**
   * @return the argumentMetadataKeys
   */
  public List<String> getArgumentMetadataKeys() {
    return argumentMetadataKeys;
  }

  /**
   * @param l the argumentMetadataKeys to set
   */
  public void setArgumentMetadataKeys(List<String> l) {
    this.argumentMetadataKeys = Args.notNull(l, "metadata-keys");
  }

  /**
   * @return the metadatakey
   */
  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * @param metadatakey the metadatakey to set
   */
  public void setMetadataKey(String metadatakey) {
    this.metadataKey = Args.notNull(metadatakey, "metadata-key");
  }

}
