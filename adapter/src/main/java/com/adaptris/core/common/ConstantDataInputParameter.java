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

package com.adaptris.core.common;

import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This {@code DataInputParameter} is used when you want to configure data directly in the Interlok configuration.
 * </p>
 * <p>
 * An example might be configuring the XPath expression directly in Interlok configuration used for the {@link
 * com.adaptris.core.services.xml.XPathService}.
 * </p>
 * 
 * @author amcgrath
 * @config constant-data-input-parameter
 * @license BASIC
 */
@XStreamAlias("constant-data-input-parameter")
public class ConstantDataInputParameter implements DataInputParameter<String> {

  private String value;
  
  public ConstantDataInputParameter() {
  }
  
  public ConstantDataInputParameter(String v) {
    this();
    setValue(v);
  }

  @Override
  public String extract(InterlokMessage m) throws InterlokException {
    return getValue();
  }



  public String getValue() {
    return value;
  }

  public void setValue(String v) {
    this.value = v;
  }


}
