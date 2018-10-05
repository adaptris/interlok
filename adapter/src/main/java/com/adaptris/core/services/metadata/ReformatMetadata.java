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

import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;

/**
 * <p>
 * Implementation of <code>Service</code> that reformats matching metadata.
 * </p>
 * <p>
 * Each matching metadata key from {@link ReformatMetadata#getMetadataKeyRegexp()} will be taken and passed to concrete subclasses
 * to modify.
 * </p>
 * 
 * 
 * @see ReformatDateService
 * @see TrimMetadataService
 * @see ReplaceMetadataValue
 */
public abstract class ReformatMetadata extends ServiceImp implements MetadataReformatter {

  @NotBlank
  @AffectsMetadata
  private String metadataKeyRegexp;

  public ReformatMetadata() {
  }

  public ReformatMetadata(String regexp) {
    this();
    setMetadataKeyRegexp(regexp);
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }


  /**
   * <p>
   * Adds the configured metadata to the message.
   * </p>
   *
   * @param msg the message to process
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    if (isBlank(getMetadataKeyRegexp())) {
      return;
    }
    try {
      Set<MetadataElement> metadata = msg.getMetadata();
      Set<MetadataElement> modifiedMetadata = new HashSet<MetadataElement>();
      for (MetadataElement e : metadata) {
        if (e.getKey().matches(metadataKeyRegexp)) {
          modifiedMetadata.add(new MetadataElement(e.getKey(), reformat(e.getValue(), msg)));
        }
      }
      log.trace("Modified metadata : " + modifiedMetadata);
      msg.setMetadata(modifiedMetadata);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }


  /**
   * @return the metadataKeyRegexp
   */
  public String getMetadataKeyRegexp() {
    return metadataKeyRegexp;
  }

  /**
   * Set the regular expression to match against.
   *
   * @param s the metadataKeyRegexp to set
   */
  public void setMetadataKeyRegexp(String s) {
    metadataKeyRegexp = s;
  }

  @Override
  public void prepare() throws CoreException {
  }


}
