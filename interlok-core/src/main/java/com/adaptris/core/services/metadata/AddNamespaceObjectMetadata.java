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

import javax.validation.Valid;
import javax.xml.namespace.NamespaceContext;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of {@link com.adaptris.core.Service} that adds a static NamespaceContext to object metadata for use by other services.
 * </p>
 * 
 * @config add-namespace-object-metadata
 * 
 * 
 */
@XStreamAlias("add-namespace-object-metadata")
@AdapterComponent
@ComponentProfile(summary = "Add an XML namespace as object metadata", tag = "service,metadata,xml")
@DisplayOrder(order = {"namespaceContext"})
public class AddNamespaceObjectMetadata extends ServiceImp {

  private static final String OBJECT_METADATA_KEY = SimpleNamespaceContext.class.getCanonicalName();

  @Valid
  private KeyValuePairSet namespaceContext;

  public AddNamespaceObjectMetadata() {
    super();
  }

  public AddNamespaceObjectMetadata(KeyValuePairSet elements) {
    this();
    setNamespaceContext(elements);
  }


  public void doService(AdaptrisMessage msg) throws ServiceException {
    NamespaceContext ctx = SimpleNamespaceContext.create(getNamespaceContext());
    if (ctx != null) msg.getObjectHeaders().put(OBJECT_METADATA_KEY, ctx);
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {

  }


  /**
   * @return the namespaceContext
   */
  public KeyValuePairSet getNamespaceContext() {
    return namespaceContext;
  }

  /**
   * Set the namespace context for resolving namespaces.
   * <ul>
   * <li>The key is the namespace prefix</li>
   * <li>The value is the namespace uri</li>
   * </ul>
   * 
   * @param kvps the namespace context
   * @see SimpleNamespaceContext#create(KeyValuePairSet)
   */
  public void setNamespaceContext(KeyValuePairSet kvps) {
    this.namespaceContext = kvps;
  }

  @Override
  public void prepare() throws CoreException {
  }


}
