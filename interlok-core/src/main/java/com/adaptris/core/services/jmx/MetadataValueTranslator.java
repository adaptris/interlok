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

package com.adaptris.core.services.jmx;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This implementation of {@link ValueTranslator} will pull a metadata value from the
 * {@link com.adaptris.core.AdaptrisMessage} to be used as a Jmx operation parameter. Conversely we can also take a
 * string result from a Jmx operation call and create a new/overwrite an existing metadata item with
 * the new value.
 * </p>
 * <p>
 * If your Jmx operation requires a different type of data other than String, simply set the type to
 * the fully qualified name of the required type. A conversion will then take place which will
 * require your your desired class to have a string constructor, or be a {@link java.util.Date},
 * where the value is the milliseconds.
 * </p>
 * 
 * @author amcgrath
 * @config jmx-metadata-value-translator
 * @since 3.0.3
 */
@XStreamAlias("jmx-metadata-value-translator")
public class MetadataValueTranslator extends ValueTranslatorImp {
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @NotBlank
  private String metadataKey;
    
  public MetadataValueTranslator() {
  }

  public MetadataValueTranslator(String key, String type) {
    this();
    setMetadataKey(key);
    setType(type);
  }

  public Object getValue(AdaptrisMessage message) throws CoreException {
    if(!StringUtils.isEmpty(metadataKey))
      return this.convert(message.getMetadataValue(this.getMetadataKey()), this.getType());
    else
      log.warn("No metadata key set for metadata-value-translator, no translation occuring.");
    
    return null;
  }

  public void setValue(AdaptrisMessage message, Object value) {
    if(!StringUtils.isEmpty(metadataKey)) {
      if(this.getType().equals(Date.class.getName()))
        message.addMetadata(this.getMetadataKey(), Long.toString(((Date)value).getTime()));
      else
        message.addMetadata(this.getMetadataKey(), value.toString());
    }
    else
      log.warn("No metadata key set for metadata-value-translator, no translation occuring.");
  }
  
  private Object convert(String value, String type) throws CoreException {
    try {
      Class<?> clazz = Class.forName(type);
      if(type.equals(String.class.getName()))
        return value;
      if(type.equals(Date.class.getName()))
        return new Date(Long.parseLong(value));
      else
        return clazz.getConstructor(String.class).newInstance(value);
    } catch (Exception e) {
      throw new CoreException(e);
    } 
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String metadataKey) {
    this.metadataKey = Args.notBlank(metadataKey, "metadataKey");
  }

}
