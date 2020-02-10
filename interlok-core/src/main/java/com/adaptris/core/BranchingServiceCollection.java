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
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ServiceCollection} which allows {@code branching} services to select the next service to apply.
 * 
 * @config branching-service-collection
 * 
 * 
 */
@XStreamAlias("branching-service-collection")
@AdapterComponent
@ComponentProfile(summary = "A Collection of services where the next service is determined dynamically",
    tag = "service,base,branching")
@DisplayOrder(order = {"firstServiceId", "restartAffectedServiceOnException"})
public class BranchingServiceCollection extends ServiceCollectionImp {

  @NotBlank
  private String firstServiceId;

  public BranchingServiceCollection() {
    super();
  }

  public BranchingServiceCollection(Collection<Service> list) {
    super(list);
  }

  @Override
  protected Service enforceRequirements(Service service) {
    Args.notNull(service, "service");
    Args.notBlank(service.getUniqueId(), "serviceUniqueId");
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
      log.debug("next service id [{}]", nextServiceId);
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
      // service can't be null at this point, because handleException will throw a ServiceException if so.
      if (service.isBranching()) { // lgtm [java/dereferenced-value-may-be-null]
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
    msg.setNextServiceId("");
  }


  private Service getService(String uniqueId) throws ServiceException {
    Service result = null;
    for (Service s : getServices()) {
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
    try {
      Args.notNull(getFirstServiceId(), "firstServiceId");
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
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
   * Returns the unique ID of the first {@link Service} to apply.
   * </p>
   *
   * @return the unique ID of the first {@link Service} to apply
   */
  public String getFirstServiceId() {
    return firstServiceId;
  }

  /**
   * <p>
   * Sets the unique ID of the first {@link Service} to apply.
   * </p>
   *
   * @param s the unique ID of the first {@link Service} to apply
   */
  public void setFirstServiceId(String s) {
    firstServiceId = Args.notBlank(s, "firstServiceId");
  }

  public BranchingServiceCollection withFirstServiceId(String s) {
    setFirstServiceId(s);
    return this;
  }
}
