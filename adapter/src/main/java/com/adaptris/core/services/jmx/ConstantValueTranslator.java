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

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This implementation of @{link ValueTranslator} uses a static String value, that can be used as a
 * parameter for Jmx operations.
 * </p>
 * <p>
 * If your Jmx operation requires a different type of data other than String, simply set the type to
 * the fully qualified name of the required type. A conversion will then take place which will
 * require your your desired class to have a string constructor, or be a {@link java.util.Date},
 * where the value is the milliseconds.
 * </p>
 * <p>
 * You can also allow this constant value to change value. Any time this translator is used as a
 * result translator, should you set "allow-overwrite" to true, then the new value will be used for
 * further invocations. The default value is false;
 * </p>
 * 
 * @author amcgrath
 * @config jmx-constant-value-translator
 * @since 3.0.3
 */
@XStreamAlias("jmx-constant-value-translator")
public class ConstantValueTranslator implements ValueTranslator {
  
  @NotBlank
  private String value;
  
  @NotBlank
  @AutoPopulated
  private String type;
  
  @InputFieldDefault(value = "false")
  private Boolean allowOverwrite;
  
  public ConstantValueTranslator() {
    this.setType(DEFAULT_PARAMETER_TYPE);
  }

  public ConstantValueTranslator(String value, String type, Boolean allowOverwrite) {
    this();
    setValue(value);
    setType(type);
    setAllowOverwrite(allowOverwrite);
  }

  @Override
  public void setValue(AdaptrisMessage message, Object object) {
    if(this.getAllowOverwrite()) {
      if(this.getType().equals(Date.class.getName()))
        this.setValue(Long.toString(((Date) object).getTime()));
      else
        this.setValue(object.toString());
    }
  }
  
  @Override
  public Object getValue(AdaptrisMessage message) throws CoreException {
    return this.convert(this.getValue(), this.getType());
  }

  private Object convert(Object value, String type) throws CoreException {
    try {
      Class<?> clazz = Class.forName(type);
      if(type.equals(String.class.getName()))
        return value;
      if(type.equals(Date.class.getName()))
        return new Date(Long.parseLong((String)value));
      else
        return clazz.getConstructor(String.class).newInstance(value);
    } catch (Exception e) {
      throw new CoreException(e);
    } 
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String getType() {
    return this.type == null ? DEFAULT_PARAMETER_TYPE : this.type;
  }

  @Override
  public void setType(String type) {
    this.type = type;
  }

  public Boolean getAllowOverwrite() {
    return allowOverwrite;
  }

  /**
   * Whether or not the constant can change value.
   * <p>
   * You can also allow this constant value to change value. Any time this translator is used as a
   * result translator, should you set "allow-overwrite" to true, then the new value will be used
   * for further invocations. The default value is false;
   * </p>
   * 
   * @param allowOverwrite
   */
  public void setAllowOverwrite(Boolean allowOverwrite) {
    this.allowOverwrite = allowOverwrite;
  }

  boolean allowOverwrite() {
    return getAllowOverwrite() != null ? getAllowOverwrite().booleanValue() : false;
  }
}
