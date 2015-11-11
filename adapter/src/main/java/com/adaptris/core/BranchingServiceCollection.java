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

package com.adaptris.core;

import static com.adaptris.core.util.LoggingHelper.friendlyName;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>ServiceCollection</code> which allows appropriate 'branching' <code>Service</code>s to select the next
 * <code>Service</code> to apply.
 * </p>
 * <p>
 * BranchingServiceCollection does not currently support continue-on-fail.
 * </p>
 * 
 * @config branching-service-collection
 * 
 * 
 */
@XStreamAlias("branching-service-collection")
public class BranchingServiceCollection extends ServiceCollectionImp {

  private String firstServiceId;

  public BranchingServiceCollection() {
    super();
  }

  public BranchingServiceCollection(Collection<Service> list) {
    super(list);
  }

  @Override
  protected Service enforceRequirements(Service service) {
    if (service == null) {
      throw new IllegalArgumentException("null param");
    }
    if (StringUtils.isBlank(service.getUniqueId())) {
      throw new IllegalArgumentException("empty unique ID");
    }
    for (Service s : getServices()) {
      String existingId = s.getUniqueId();
      if (service.getUniqueId().equals(existingId)) {
        throw new IllegalArgumentException("duplicate Service unique ID [" + existingId + "]");
      }
    }
    return service;
  }

  @Override
  protected Collection<? extends Service> enforceRequirements(Collection<? extends Service> collection) {
    Map<String, Service> map = new HashMap<String, Service>();
    for (Service s : collection) {
      enforceRequirements(s);
      map.put(s.getUniqueId(), s);
    }
    if (map.size() != collection.size()) {
      throw new IllegalArgumentException("Duplicate Unique-ID's detected in collection");
    }
    return collection;
  }

  @Override
  protected void applyServices(AdaptrisMessage msg) throws ServiceException {
    String nextServiceId = firstServiceId;

    do {
      Service service = null;

      log.debug("next service id [" + nextServiceId + "]");

      try {
        service = this.getService(nextServiceId);
        service.doService(msg);

        msg.addEvent(service, true);
        log.debug("service [{}] applied", friendlyName(service));
      }
      catch (ServiceException e) {
        msg.addEvent(service, false);
        handleException(service, msg, e);
      }

      if (service.isBranching()) {
        nextServiceId = msg.getNextServiceId();
      }
      else {
        nextServiceId = CoreConstants.ENDPOINT_SERVICE_UNIQUE_ID;
      }
      if (StringUtils.isEmpty(nextServiceId)) {
        log.trace("NextServiceID is empty, implicit STOP");
        nextServiceId = CoreConstants.ENDPOINT_SERVICE_UNIQUE_ID;
      }
    }
    while (nextServiceId != CoreConstants.ENDPOINT_SERVICE_UNIQUE_ID);
  }

  /**
   * <p>
   * Obtains the <code>Service</code> for the passed <code>uniqueId</code>.
   * Throws <code>Exception</code> if there is no corresponding
   * <code>Service</code>.
   * </p>
   */
  private Service getService(String uniqueId) throws ServiceException {
    Service result = null;
    for (Service s : getServices()) {
      String existingId = s.getUniqueId();
      if (s.getUniqueId().equals(uniqueId)) {
        result = s;
        break;
      }
    }
    if (result == null) {
      throw new ServiceException("no Service with UID [" + uniqueId + "]");
    }
    return result;
  }

  @Override
  protected void doClose() {
  }

  @Override
  protected void doInit() throws CoreException {
    if (firstServiceId == null) {
      throw new CoreException("first service ID cannot be null");
    }
  }

  @Override
  protected void doStart() throws CoreException {
  }

  @Override
  protected void doStop() {
  }


  /**
   * <p>
   * Returns the unique ID of the first <code>Service</code> to apply.
   * </p>
   *
   * @return the unique ID of the first <code>Service</code> to apply
   */
  public String getFirstServiceId() {
    return firstServiceId;
  }

  /**
   * <p>
   * Sets the unique ID of the first <code>Service</code> to apply. May not be
   * null, empty or <code>CoreConstants.ENDPOINT_SERVICE_UNIQUE_ID</code>.
   * </p>
   *
   * @param string the unique ID of the first <code>Service</code> to apply
   */
  public void setFirstServiceId(String string) {
    if (string == null || "".equals(string)) {
      throw new IllegalArgumentException("null or empty param");
    }

    if (string.equals(CoreConstants.ENDPOINT_SERVICE_UNIQUE_ID)) {
      throw new IllegalArgumentException("first Service cannot be end point");
    }

    firstServiceId = string;
  }

  public void prepare() throws CoreException {
    for (Service s : getServices()) {
      s.prepare();
    }
  }

}
