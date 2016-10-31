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

import com.adaptris.annotation.*;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;
import com.adaptris.util.KeyValuePairSet;
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
public class CloneMessageServiceList extends ServiceCollectionImp {


  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean overrideMetadata;
  @AdvancedConfig
  private MetadataFilter overrideMetadataFilter;

  public CloneMessageServiceList() {
    super();
    overrideMetadataFilter = new NoOpMetadataFilter();
  }

  public CloneMessageServiceList(Collection<Service> list) {
    super(list);
    overrideMetadataFilter = new NoOpMetadataFilter();
  }

  @Override
  protected void applyServices(AdaptrisMessage msg) throws ServiceException {
    for (Service service : getServices()) {
      try {
        AdaptrisMessage clonedMessage = (AdaptrisMessage) msg.clone();
        service.doService(clonedMessage);
        log.debug("service [" + friendlyName(service) + "] applied");
        if(overrideMetadata()) {
          MetadataCollection filtered = getOverrideMetadataFilter().filter(clonedMessage);
          StringBuilder filteredKeys = new StringBuilder("Metadata keys copied:");
          for (MetadataElement e : filtered) {
            filteredKeys.append(" ");
            filteredKeys.append(e.getKey());
            msg.addMetadata(e);
          }
          log.trace(filteredKeys.toString());
        }
      }
      catch (CloneNotSupportedException e) {
        throw new ServiceException(e);
      }
      catch (Exception e) {
        this.handleException(service, msg, e);
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

  @Override
  public void prepare() throws CoreException {
    for (Service s : getServices()) {
      s.prepare();
    }
  }

  public MetadataFilter getOverrideMetadataFilter() {
    return overrideMetadataFilter;
  }

  /**
   * <p>
   * Specify the {@link com.adaptris.core.AdaptrisMessage} metadata keys that will be overridden in the original message.
   * </p>
   *
   * @param mf the filter defaults to {@link NoOpMetadataFilter}
   * @see MetadataFilter
   */
  public void setOverrideMetadataFilter(MetadataFilter mf) {
    if (mf == null) {
      throw new IllegalArgumentException("Filter may not be null");
    }
    overrideMetadataFilter = mf;
  }


  boolean overrideMetadata() {
    return getOverrideMetadata() == null ? false : overrideMetadata.booleanValue();
  }

  public Boolean getOverrideMetadata() {
    return overrideMetadata;
  }

  /**
   * <p>
   * Sets whether to override metadata from the cloned message back to original
   * message.
   * </p>
   *
   * @param b whether to override metadata from the cloned message to the original
   *          message (default false)
   */
  public void setOverrideMetadata(Boolean b) {
    overrideMetadata = b;
  }

}
