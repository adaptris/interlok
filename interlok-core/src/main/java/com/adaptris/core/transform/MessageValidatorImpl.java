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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.CoreException;

public abstract class MessageValidatorImpl implements MessageValidator {

  @Deprecated
  @Removal(version = "3.9.0")
  private String uniqueId;
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @Override
  public void prepare() throws CoreException {
  }


  @Override
  public void init() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {

  }

  @Override
  public void close() {

  }


  /**
   * Not required as this component doesn't need to extend {@link AdaptrisComponent}
   * 
   * @deprecated since 3.6.3
   */
  @Deprecated
  @Removal(version = "3.9.0")
  public String getUniqueId() {
    return uniqueId;
  }


  /**
   * Not required as this component doesn't need to extend {@link AdaptrisComponent}
   * 
   * @deprecated since 3.6.3
   */
  @Deprecated
  @Removal(version = "3.9.0")
  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

}
