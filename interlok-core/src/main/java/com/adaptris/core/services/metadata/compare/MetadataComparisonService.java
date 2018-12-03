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

package com.adaptris.core.services.metadata.compare;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link com.adaptris.core.Service} that compares two items of metadata.
 * 
 * <p>
 * Sometimes you just want to compare two metadata values and store the result against a 3rd metadata key. Well this does that.
 * </p>
 * 
 * @config metadata-comparison-service
 * 
 * 
 */
@XStreamAlias("metadata-comparison-service")
@AdapterComponent
@ComponentProfile(summary = "Compare two metadata values and store the result against a 3rd metadata key", tag = "service,metadata")
@DisplayOrder(order = {"firstKey", "secondKey", "comparator"})
public class MetadataComparisonService extends ServiceImp {

  @NotBlank
  private String firstKey;
  @NotBlank
  private String secondKey;
  @NotNull
  @Valid
  private MetadataComparator comparator;

  public MetadataComparisonService() {
    super();
  }

  public MetadataComparisonService(String first, String second, MetadataComparator mc) {
    this();
    setFirstKey(first);
    setSecondKey(second);
    setComparator(mc);
  }

  public void doService(AdaptrisMessage msg) throws ServiceException {
    msg.addMetadata(getComparator().compare(msg.getMetadata(getFirstKey()), msg.getMetadata(getSecondKey())));
  }

  @Override
  protected void initService() throws CoreException {
    try {
      Args.notBlank(getFirstKey(), "firstKey");
      Args.notBlank(getSecondKey(), "secondKey");
      Args.notNull(getComparator(), "comparator");
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void closeService() {

  }

  @Override
  public void prepare() throws CoreException {
  }


  public String getFirstKey() {
    return firstKey;
  }

  public void setFirstKey(String key) {
    this.firstKey = Args.notBlank(key, "firstKey");;
  }

  public String getSecondKey() {
    return secondKey;
  }

  public void setSecondKey(String key) {
    this.secondKey = Args.notBlank(key, "secondKey");

  }

  public MetadataComparator getComparator() {
    return comparator;
  }

  public void setComparator(MetadataComparator mc) {
    this.comparator = Args.notNull(mc, "comparator");
  }

}
