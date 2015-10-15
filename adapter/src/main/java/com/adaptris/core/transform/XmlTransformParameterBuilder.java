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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * {@link XmlTransformParameter} implementation allows multiple nested implementations.
 * 
 * @author lchan
 * @config xml-transform-parameter-builder
 */
@XStreamAlias("xml-transform-parameter-builder")
public class XmlTransformParameterBuilder implements XmlTransformParameter {

  @NotNull
  @AutoPopulated
  @Valid
  @XStreamImplicit(itemFieldName = "parameter-builder")
  private List<XmlTransformParameter> parameterBuilders;

  public XmlTransformParameterBuilder() {
    setParameterBuilders(new ArrayList<XmlTransformParameter>());
  }

  public XmlTransformParameterBuilder(XmlTransformParameter... builders) {
    this();
    parameterBuilders.addAll(Arrays.asList(builders));
  }

  @Override
  public Map createParameters(AdaptrisMessage msg, Map existingParams) throws ServiceException {
    Map result = existingParams;
    for (XmlTransformParameter p : getParameterBuilders()) {
      result = p.createParameters(msg, result);
    }
    return result;
  }

  public List<XmlTransformParameter> getParameterBuilders() {
    return parameterBuilders;
  }

  public void setParameterBuilders(List<XmlTransformParameter> p) {
    if (p == null) {
      throw new IllegalArgumentException("Parameters are null");
    }
    this.parameterBuilders = p;
  }

}
