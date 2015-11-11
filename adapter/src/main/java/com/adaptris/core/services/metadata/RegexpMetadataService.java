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

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
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
public class RegexpMetadataService extends ServiceImp {

  @NotNull
  @AutoPopulated
  @Valid
  @XStreamImplicit(itemFieldName="regexp-metadata-query")
  private List<RegexpMetadataQuery> regexpMetadataQueries;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public RegexpMetadataService() {
    regexpMetadataQueries = new ArrayList<RegexpMetadataQuery>();
  }

  public RegexpMetadataService(List<RegexpMetadataQuery> list) {
    this();
    setRegexpMetadataQueries(list);
  }

  /**
   * <p>
   * Executes all configured <code>RegexpMetadataQuery</code>s on the
   * passed <code>AdaptrisMessage</code>.
   * </p><p>
   * @param msg the <code>AdaptrisMessage</code> to process
   * @throws ServiceException wrapping any underlying <code>Exception</code>
   */
  public void doService(AdaptrisMessage msg)
    throws ServiceException {

    String message = msg.getStringPayload();

    try {
      for (int i = 0; i < regexpMetadataQueries.size(); i++) {
        RegexpMetadataQuery q = regexpMetadataQueries.get(i);
        MetadataElement elem = q.doQuery(message);
        msg.addMetadata(elem);
      }
    }
    catch (CoreException e) {
      throw new ServiceException(e);
    }
  }

  /**
   * <p>
   * Adds a <code>RegexpMetadataQuery</code> to the <code>List</code> to
   * be applied.
   * </p>
   * @param query a <code>RegexpMetadataQuery</code> to apply
   */
  public void addRegexpMetadataQuery(RegexpMetadataQuery query) {
    regexpMetadataQueries.add(query);
  }

  /**
   * <p>
   * Returns the <code>List</code> of <code>RegexpMetadataQuery</code>s
   * that will be applied by this <code>Service</code>.
   * </p>
   * @return the <code>List</code> of <code>RegexpMetadataQuery</code>s
   * that will be applied by this <code>Service</code>
   */
  public List<RegexpMetadataQuery> getRegexpMetadataQueries() {
    return regexpMetadataQueries;
  }

  /**
   * <p>
   * Sets the <code>List</code> of <code>RegexpMetadataQuery</code>s
   * that will be applied by this <code>Service</code>.
   * </p>
   * @param l the <code>List</code> of <code>RegexpMetadataQuery</code>s
   * that will be applied by this <code>Service</code>
   */
  public void setRegexpMetadataQueries(List<RegexpMetadataQuery> l) {
    regexpMetadataQueries = l;
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {

  }

  @Override
  public void prepare() throws CoreException {
  }

}
