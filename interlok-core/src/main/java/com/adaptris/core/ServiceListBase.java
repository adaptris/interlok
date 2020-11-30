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
package com.adaptris.core;

import static com.adaptris.core.CoreConstants.STOP_PROCESSING_KEY;
import static com.adaptris.core.CoreConstants.STOP_PROCESSING_VALUE;
import static com.adaptris.core.CoreConstants.shouldStopProcessing;

public abstract class ServiceListBase extends ServiceCollectionImp {

  public ServiceListBase() {
    super();
  }

  @Override
  protected void doClose() {
  }

  @Override
  protected void doInit() throws CoreException {
  }

  @Override
  protected void doStart() throws CoreException {
  }

  @Override
  protected void doStop() {
  }

  protected boolean haltProcessing(AdaptrisMessage msg) {
    if(shouldStopProcessing.apply(msg)) {
      log.trace("{}={} detected, halt processing", STOP_PROCESSING_KEY, STOP_PROCESSING_VALUE);
      return true;
    }
    return false;
  }
}
