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
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.ListIterator;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@code ServiceCollection} with an ordered list of {@link Service}s.
 * 
 * @config service-list
 */
@XStreamAlias("service-list")
@AdapterComponent
@ComponentProfile(summary = "A collection of services", tag = "service,base")
@DisplayOrder(order = {"allowForwardSearch", "restartAffectedServiceOnException"})
public class ServiceList extends ServiceListBase {

  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean allowForwardSearch;

  private transient LinkedHashMap<String, Integer> serviceIndex = new LinkedHashMap<>();
  private transient boolean listIsSearchable;

  public ServiceList() {
    super();
  }

  public ServiceList(Collection<Service> serviceList) {
    this();
    setServices(new ArrayList<>(serviceList));
  }

  public ServiceList(Service... serviceList) {
    this(Arrays.asList(serviceList));
  }

  @Override
  protected void doInit() throws CoreException {
    super.doInit();
    listIsSearchable = forwardSearch();
    if (listIsSearchable) {
      serviceIndex.clear();
      for (int i = 0; i < this.size(); i++) {
        Service s = get(i);
        serviceIndex.put(s.getUniqueId(), i);
      }
      if (serviceIndex.size() != this.size()) {
        // missing or duplicate ID, so lets disable allowSkip
        log.warn("Attempt to enable forward-search capability, missing/duplicate uniqueIds, disabling forward-search");
        listIsSearchable = false;
      }
    }
  }


  @Override
  protected void applyServices(AdaptrisMessage msg) throws ServiceException {
    ListIterator<Service> itr = this.listIterator();
    while (itr.hasNext()) {
      Service service = itr.next();
      String serviceName = friendlyName(service);
      if (haltProcessing(msg)) {
        break;
      }
      msg.setNextServiceId("");
      log.debug("Executing doService on [{}]", serviceName);
      try {
        service.doService(msg);
        msg.addEvent(service, true);
        // Should we resolveNext *regardless of exception?*
        itr = resolveNext(itr, msg.getNextServiceId());
      } catch (Exception e) {
        // add fail event
        msg.addEvent(service, false);
        handleException(service, msg, e);
      }
    }
  }


  /**
   * @return whether or not forward-search is allowed.
   */
  public Boolean getAllowForwardSearch() {
    return allowForwardSearch;
  }

  /**
   * Allow services to specify the {@code next service} in a forward search mode only.
   * 
   * <p>
   * If set to true, then the service-list will act like a limited {@link BranchingServiceCollection}. It will
   * check {@link AdaptrisMessage#getNextServiceId()}, and search the remaining services for that service-id. If found it will
   * execute that service (after clearing the next service id). Searches are forward only so you cannot jump backwards through the
   * service-list.
   * </p>
   * 
   * @param b true to allow limited skipping of (forward only) services based on {@link AdaptrisMessage#setNextServiceId(String)};
   *        default is null(true).
   */
  public void setAllowForwardSearch(Boolean b) {
    this.allowForwardSearch = b;
  }

  boolean forwardSearch() {
    return BooleanUtils.toBooleanDefaultIfNull(getAllowForwardSearch(), true);
  }

  private ListIterator<Service> resolveNext(ListIterator<Service> current, String serviceId) {
    ListIterator<Service> result = current;
    if (!listIsSearchable) {
      return result;
    }
    if (!isBlank(serviceId)) {
      Integer i = serviceIndex.get(serviceId);
      if (i != null && i >= current.nextIndex()) {
        result = listIterator(i);
        log.trace("Skipping to [{}]", serviceId);
      } else {
        log.trace("Cannot branch to [{}], no skipping", serviceId);
      }
    }
    return result;
  }
}
