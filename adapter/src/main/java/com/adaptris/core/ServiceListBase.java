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

import static com.adaptris.core.util.LoggingHelper.friendlyName;

public abstract class ServiceListBase extends ServiceCollectionImp {

  public ServiceListBase() {
    super();
  }

  @Override
  protected void applyServices(AdaptrisMessage msg) throws ServiceException {
    for (Service service : this) {
      try {
        String serviceName = friendlyName(service);

        /*
         * Check this before applying any services as it may have been set deep in the ServiceList hierarchy. If we don't check this
         * before applying a service, we'll be "randomly" applying one service at each service list, keeping the metadata and then
         * no longer apply the next ones. That's strange behaviour and contradicts what the javadoc says about
         * CoreConstants.STOP_PROCESSING_KEY
         */
        if (CoreConstants.STOP_PROCESSING_VALUE.equals(msg.getMetadataValue(CoreConstants.STOP_PROCESSING_KEY))) {
          log.trace("Service {} has added metadata which stops any further configured services being applied", serviceName);
          break;
        }

        service.doService(msg);
        msg.addEvent(service, true);
        log.debug("service [{}] applied", serviceName);
      }
      catch (Exception e) {
        // add fail event
        msg.addEvent(service, false);
        handleException(service, msg, e);
      }
    }
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

}
