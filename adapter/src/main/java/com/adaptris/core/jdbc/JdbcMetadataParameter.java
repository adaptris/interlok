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

package com.adaptris.core.jdbc;

import static org.apache.commons.lang.StringUtils.isEmpty;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.jdbc.ParameterValueType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Stored Procedure parameter implementation, can be used for all IN, INOUT and OUT Stored Procedure parameters.
 * <p>
 * If this implementation is used for an IN or an INOUT parameter, then the metadata will be pulled from the {@link AdaptrisMessage}
 * and used as the parameter value. If this implementation is used for an OUT or an INOUT parameter, then the value of the matching
 * parameter after the Stored Procedure has run, will be reapplied into the {@link AdaptrisMessage} as metadata. You will simply set
 * the metadataKey, to both retrieve a value or to set a new value as above.
 * </p>
 * <p>
 * Additionally you will set one or both of "name" and/or "order". "name" will map this parameter to a Stored Procedure parameter
 * using the Stored Procedures method signature. "order" will map this parameter according to the parameter number using the Stored
 * Procedures method signature. Note that the "order" starts from 1 and not 0, so the first parameter would be order 1. You will
 * also need to set the data type of the parameter; you may use any of the string types defined in {@link ParameterValueType}
 * </p>
 * 
 * @config jdbc-metadata-parameter
 * @author Aaron McGrath
 * 
 */
@XStreamAlias("jdbc-metadata-parameter")
public class JdbcMetadataParameter extends NullableParameter {

  private String metadataKey;
  
  @Override
  public Object applyInputParam(AdaptrisMessage msg) throws JdbcParameterException {
    this.checkMetadataKey();
    
    if(!msg.containsKey(this.getMetadataKey()))
      throw new JdbcParameterException("Metadata does not exist for key: " + this.getMetadataKey());
    
    return normalize(msg.getMetadataValue(getMetadataKey()));
  }

  @Override
  public void applyOutputParam(Object dbValue, AdaptrisMessage msg) throws JdbcParameterException {
    this.checkMetadataKey();
    
    msg.addMetadata(this.getMetadataKey(), normalize(JdbcParameterUtils.objectToString(dbValue)));
  }

  protected void checkMetadataKey() throws JdbcParameterException {
    if(isEmpty(metadataKey))
      throw new JdbcParameterException("Metadata key has not been set for " + this.getClass().getName());
  }
  
  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String metadataKey) {
    this.metadataKey = metadataKey;
  }

}
