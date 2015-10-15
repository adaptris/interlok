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

package com.adaptris.core.services.dynamic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;

/**
 * <p>
 * Implementation of {@link ServiceStore} which uses an XML marshaller for Services.
 * </p>
 */
public abstract class MarshallServiceStore implements ServiceStore {

  private AdaptrisMarshaller marshaller;
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * Default constructor.
   * <ul>
   * <li>Default imp. class is <code>com.adaptris.core.ServiceList</code></li>
   * <ul>
   *
   * @throws CoreException
   */
  public MarshallServiceStore() throws CoreException {
  }

  /**
   * Gets the marshalling implementation.
   *
   * @return {@link AdaptrisMarshaller}
   */
  public AdaptrisMarshaller getMarshaller() {
    return marshaller;
  }

  /**
   * Sets the marshalling implementation.
   *
   * @param marshaller
   */
  public void setMarshaller(AdaptrisMarshaller marshaller) {
    this.marshaller = marshaller;
  }

  protected AdaptrisMarshaller currentMarshaller() throws CoreException {
    return getMarshaller() != null ? getMarshaller() : DefaultMarshaller.getDefaultMarshaller();
  }
}
