/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.http.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of {@link StatusEvaluator}.
 * 
 *
 */
public abstract class StatusEvaluatorImpl implements StatusEvaluator {

  private String serviceId;
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  public StatusEvaluatorImpl() {

  }

  /**
   * @return the serviceId
   */
  public String getServiceId() {
    return serviceId;
  }

  /**
   * @param s the serviceId to set, empty to stop branching.
   */
  public void setServiceId(String s) {
    this.serviceId = s;
  }

  @Override
  public String serviceId() {
    return serviceId;
  }

}
