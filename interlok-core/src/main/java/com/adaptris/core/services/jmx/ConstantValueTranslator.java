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
import javax.validation.constraints.NotBlank;
import com.adaptris.annotation.InputFieldHint;
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
 * 
 * @author amcgrath
 * @config jmx-constant-value-translator
 * @since 3.0.3
 */
@XStreamAlias("jmx-constant-value-translator")
public class ConstantValueTranslator extends ValueTranslatorImp {
  
  @NotBlank
  @InputFieldHint(expression = true)
  private String value;
  
  public ConstantValueTranslator() {
  }

  public ConstantValueTranslator(String value, String type) {
    this();
    setValue(value);
    setType(type);
  }

  @Override
  public void setValue(AdaptrisMessage message, Object object) {
    return;
  }
  
  @Override
  public Object getValue(AdaptrisMessage message) throws CoreException {
    return this.convert(message.resolve(this.getValue()), this.getType());
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
}
