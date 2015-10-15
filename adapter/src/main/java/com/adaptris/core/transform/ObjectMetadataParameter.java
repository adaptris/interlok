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

package com.adaptris.core.transform;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link XmlTransformParameter} implementation that makes object metadata available as transform parameters
 * <p>
 * Internally, the adapter uses String keys for object metadata; however this is not enforced for custom services, so behaviour may
 * vary depending on what custom components are in use as the key names may not be consistent or predictable.
 * </p>
 * 
 * @author lchan
 * @config xml-transform-object-parameter
 */
@XStreamAlias("xml-transform-object-parameter")
public class ObjectMetadataParameter implements XmlTransformParameter {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @NotBlank
  private String objectMetadataKeyRegexp;

  public ObjectMetadataParameter() {
  }


  public ObjectMetadataParameter(String regexp) {
    this();
    setObjectMetadataKeyRegexp(regexp);
  }

  @Override
  public Map createParameters(AdaptrisMessage msg, Map existingParams) throws ServiceException {
    if (isEmpty(getObjectMetadataKeyRegexp())) {
      throw new ServiceException("Object Metadata Key regexp is empty");
    }
    Pattern pattern = Pattern.compile(getObjectMetadataKeyRegexp());
    Map params = existingParams == null ? new HashMap() : new HashMap(existingParams);
    Map objMetadata = msg.getObjectMetadata();
    for (Object key : objMetadata.keySet()) {
      if (pattern.matcher(key.toString()).matches()) {
        params.put(key.toString(), objMetadata.get(key));
        log.trace("Adding object metadata against [{}]", key.toString());
      }
    }
    return params;
  }

  public String getObjectMetadataKeyRegexp() {
    return objectMetadataKeyRegexp;
  }

  public void setObjectMetadataKeyRegexp(String s) {
    if (isEmpty(s)) {
      throw new IllegalArgumentException("Empty regular expression");
    }
    this.objectMetadataKeyRegexp = s;
  }

}
