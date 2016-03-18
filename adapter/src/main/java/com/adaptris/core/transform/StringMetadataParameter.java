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
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link XmlTransformParameter} implementation that filters metadata making matches available as String parameters.
 * 
 * @author lchan
 * @config xml-transform-string-parameter
 */
@XStreamAlias("xml-transform-string-parameter")
@DisplayOrder(order = {"metadataFilter"})
public class StringMetadataParameter implements XmlTransformParameter {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @NotNull
  @Valid
  private RegexMetadataFilter metadataFilter;

  public StringMetadataParameter() {
    setMetadataFilter(new RegexMetadataFilter());
  }

  public StringMetadataParameter(String[] include, String[] exclude) {
    this();
    getMetadataFilter().setIncludePatterns(new ArrayList<String>(Arrays.asList(include)));
    getMetadataFilter().setExcludePatterns(new ArrayList<String>(Arrays.asList(exclude)));
  }

  @Override
  public Map createParameters(AdaptrisMessage msg, Map existingParams) {
    MetadataCollection metadataToInclude = metadataFilter.filter(msg);
    if (metadataToInclude.size() == 0) {
      return existingParams;
    }
    Map params = existingParams == null ? new HashMap() : new HashMap(existingParams);

    for (MetadataElement e : metadataToInclude) {
      params.put(e.getKey(), e.getValue());
    }
    log.trace("Stylesheet parameters: {}", params);
    return params;
  }

  public RegexMetadataFilter getMetadataFilter() {
    return metadataFilter;
  }

  public void setMetadataFilter(RegexMetadataFilter rmf) {
    if (rmf == null) {
      throw new IllegalArgumentException("Metadata filter is null");
    }
    this.metadataFilter = rmf;
  }

}
