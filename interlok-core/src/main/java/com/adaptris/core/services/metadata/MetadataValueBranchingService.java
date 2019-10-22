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

package com.adaptris.core.services.metadata;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.util.Iterator;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.KeyValuePairList;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of {@link com.adaptris.core.Service} which sets the unique ID of the next <code>Service</code> to apply based on
 * values of {@link com.adaptris.core.AdaptrisMessage} metadata.
 * </p>
 * <p>
 * It concatenates the values stored against the keys in {@link #getMetadataKeys()} and uses this String as a key to look up the
 * {@link com.adaptris.core.Service} to apply in {@link #getMetadataToServiceIdMappings()}. In most use cases you would only
 * configure a single metadata key to branch on rather than multiple keys.
 * </p>
 * 
 * @config metadata-value-branching-service
 * 
 */
@XStreamAlias("metadata-value-branching-service")
@AdapterComponent
@ComponentProfile(summary = "Perform a branch based on a match of a metadata value", tag = "service,branching",
    branchSelector = true)
@DisplayOrder(order = {"metadataKeys", "valueMatcher", "metadataToServiceIdMappings", "defaultServiceId"})
public class MetadataValueBranchingService extends MetadataBranchingServiceImp {

  @NotNull
  @Valid
  private MetadataValueMatcher valueMatcher = null;
  @NotNull
  @AutoPopulated
  private KeyValuePairList metadataToServiceIdMappings;

  public MetadataValueBranchingService() {
    metadataToServiceIdMappings = new KeyValuePairList();
  }


  @Override
  protected void initService() throws CoreException {
    try {
      super.initService();
      Args.notNull(getValueMatcher(), "valueMatcher");
      Args.notNull(getMetadataToServiceIdMappings(), "metadataToServiceMappings");
      if (BooleanUtils.and(new boolean[] {getValueMatcher() instanceof UseKeyAsServiceIdValueMatcher,
          getMetadataToServiceIdMappings().size() > 0})) {

        log.warn("{} configured with metadata-to-service-id-mappings; mappings will be ignored",
            UseKeyAsServiceIdValueMatcher.class.getSimpleName());
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }


  /**
   * <p>
   * Obtains the value associated with each configured metadata key, concatenates these keys, then looks up and sets the
   * <code>Service</code> unique ID associated with this concatenated key.
   * </p>
   * <p>
   * If any of the configured keys return null, nothing is appended to the result key.
   * </p>
   * <p>
   * If no ID is stored against the created key, the default ID will be returned if one is configured. Otherwise a
   * <code>ServiceException</code> is thrown
   *
   * @see com.adaptris.core.Service#doService(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    String serviceKey = createServiceKey(msg);
    String nextServiceId = valueMatcher.getNextServiceId(serviceKey, getMetadataToServiceIdMappings());

    if (nextServiceId == null) {
      if (isEmpty(getDefaultServiceId())) {
        throw new ServiceException("no ServiceId configured against key [" + serviceKey + "] and no default ServiceId configured");
      }
      nextServiceId = getDefaultServiceId();
      log.debug("Using default ServiceId : {}", getDefaultServiceId());
    }
    msg.setNextServiceId(nextServiceId);
    // logging is in BranchingServiceCollection
  }

  private String createServiceKey(AdaptrisMessage msg) {
    StringBuffer result = new StringBuffer();
    for (Iterator itr = getMetadataKeys().iterator(); itr.hasNext();) {
      String value = msg.getMetadataValue((String) itr.next());
      if (value != null) {
        result.append(value);
      }
    }
    return result.toString();
  }

  // getters & sets

  public KeyValuePairList getMetadataToServiceIdMappings() {
    return metadataToServiceIdMappings;
  }

  /**
   * Set the list of mappings between metadata / service-id
   *
   * @param mappings the mapping to add
   */
  public void setMetadataToServiceIdMappings(KeyValuePairList mappings) {
    metadataToServiceIdMappings = Args.notNull(mappings, "mappings");
  }

  /**
   *
   * @return the valueMatcher
   */
  public MetadataValueMatcher getValueMatcher() {
    return valueMatcher;
  }

  /**
   * Set the matching implementation for matching metadata values.
   *
   * @param mvm the valueMatcher implementation to use. Using {@link EqualsValueMatcher} or {@link IgnoresCaseValueMatcher} will
   *          replicate the previous functionality
   */
  public void setValueMatcher(MetadataValueMatcher mvm) {
    valueMatcher = Args.notNull(mvm, "metadataValueMatcher");
  }
}
