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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@linkplain ServiceCollection} that creates a new clone of {@linkplain com.adaptris.core.AdaptrisMessage} for each configured
 * service.
 * <p>
 * The expected use case for this {@linkplain ServiceCollection} is that you have a number of services that have to process exactly
 * the same message (e.g. transforming XML to an HTML representation as well as CSV).
 * </p>
 * <p>
 * If you have a list of services that require the same clone to be used; then use a nested {@link ServiceList} to wrap all the
 * required {@linkplain Service} implementations that require it (e.g. Transforming to HTML and emailing the result of the
 * transform)
 * </p>
 * <p>
 * If there are services configured after this {@linkplain ServiceCollection} implementation then they will process the message in
 * its original form.
 * </p>
 *
 * @config clone-message-service-list
 *
 */
@XStreamAlias("clone-message-service-list")
@AdapterComponent
@ComponentProfile(summary = "A collection of services where each service gets a new copy of the message", tag = "service,base")
@DisplayOrder(order = {"restartAffectedServiceOnException", "overrideMetadata", "overrideMetadataFilter"})
public class CloneMessageServiceList extends ServiceListBase {

  @AdvancedConfig
  @Valid
  private MetadataFilter overrideMetadataFilter;

  public CloneMessageServiceList() {
    super();
  }

  public CloneMessageServiceList(Collection<Service> list) {
    this();
    setServices(new ArrayList<>(list));
  }

  public CloneMessageServiceList(Service... list) {
    this(Arrays.asList(list));
  }

  @Override
  protected void applyServices(AdaptrisMessage msg) throws ServiceException {
    for (Service service : getServices()) {
      try {
        AdaptrisMessage clonedMessage = (AdaptrisMessage) msg.clone();
        if (haltProcessing(clonedMessage)) {
          break;
        }
        service.doService(clonedMessage);
        log.debug("service [{}] applied", friendlyName(service));
        captureMetadata(msg, clonedMessage, selectFilter());
      }
      catch (CloneNotSupportedException e) {
        throw new ServiceException(e);
      }
      catch (Exception e) {
        handleException(service, msg, e);
      }
    }
  }

  private void captureMetadata(AdaptrisMessage srcMsg, AdaptrisMessage cloned, MetadataFilter filter) {
    // since the default is now RemoteAll; this will be an empty collection
    MetadataCollection filtered = filter.filter(cloned);
    filtered.forEach((e) -> {
      srcMsg.addMetadata(e);
    });
    if (filtered.size() > 0) {
      log.trace("Metadata keys copied : {}", filtered.stream().map(e -> e.getKey()).collect(Collectors.toList()));
    }
  }

  public MetadataFilter getOverrideMetadataFilter() {
    return overrideMetadataFilter;
  }

  /**
   * Specify the {@link com.adaptris.core.AdaptrisMessage} metadata keys that will be overridden in
   * the original message.
   * 
   * @param mf the filter defaults to {@link RemoveAllMetadataFilter} if not specified (which will
   *        mean no metadata is preserved).
   * @see MetadataFilter
   */
  public void setOverrideMetadataFilter(MetadataFilter mf) {
    overrideMetadataFilter = mf;
  }

  private MetadataFilter selectFilter() {
    return ObjectUtils.defaultIfNull(getOverrideMetadataFilter(), new RemoveAllMetadataFilter());
  }

}
