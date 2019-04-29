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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * <code>Service</code> which information from the message payload and sets it as metadata. Multiple items of metadata may be set,
 * each with its own {@link RegexpMetadataQuery}.
 * </p>
 * 
 * @config regexp-metadata-service
 * 
 * 
 */
@XStreamAlias("regexp-metadata-service")
@AdapterComponent
@ComponentProfile(summary = "Extract data from the message via a regular expression and store it as metadata",
    tag = "service,metadata")
@DisplayOrder(order = {"regexpMetadataQueries", "addNullValues", "metadataLogger"})
public class RegexpMetadataService extends MetadataServiceImpl {

  @NotNull
  @AutoPopulated
  @Valid
  @XStreamImplicit(itemFieldName="regexp-metadata-query")
  private List<RegexpMetadataQuery> regexpMetadataQueries;

  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean addNullValues;

  public RegexpMetadataService() {
    regexpMetadataQueries = new ArrayList<RegexpMetadataQuery>();
  }

  public RegexpMetadataService(List<RegexpMetadataQuery> list) {
    this();
    setRegexpMetadataQueries(list);
  }

  @Override
  public void doService(AdaptrisMessage msg)
    throws ServiceException {

    String message = msg.getContent();
    List<MetadataElement> added = new ArrayList<>();
    try {
      for (RegexpMetadataQuery q : getRegexpMetadataQueries()) {
        MetadataElement elem = q.doQuery(message);
        if (!isEmpty(elem.getValue()) || addNullValues()) {
          msg.addMetadata(elem);
          added.add(elem);
        }
      }
    }
    catch (CoreException e) {
      throw new ServiceException(e);
    }
    logMetadata("Added metadata : {}", added);
  }

  /**
   * Adds a {@link RegexpMetadataQuery} to the list be applied.
   * 
   */
  public void addRegexpMetadataQuery(RegexpMetadataQuery query) {
    regexpMetadataQueries.add(Args.notNull(query, "regexp-metadata-query"));
  }

  public List<RegexpMetadataQuery> getRegexpMetadataQueries() {
    return regexpMetadataQueries;
  }

  /**
   * Sets the {@link List} of {@link RegexpMetadataQuery} instances that will be applied by this service.
   */
  public void setRegexpMetadataQueries(List<RegexpMetadataQuery> l) {
    regexpMetadataQueries = Args.notNull(l, "regexp-metadata-queries");
  }

  public Boolean getAddNullValues() {
    return addNullValues;
  }

  /**
   * If set to true then null values will be added as metadata in the event that a regular expression doesn't match but
   * {@link RegexpMetadataQuery#getAllowNulls()} is true.
   * 
   * @param b true to add possible null values to metadata; default is true.
   * @see RegexpMetadataQuery#setAllowNulls(Boolean)
   */
  public void setAddNullValues(Boolean b) {
    this.addNullValues = b;
  }

  boolean addNullValues() {
    return BooleanUtils.toBooleanDefaultIfNull(getAddNullValues(), true);
  }
}
