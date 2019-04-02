/*
 * Copyright 2019 Adaptris Ltd.
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

import java.util.Collection;

import javax.validation.Valid;

import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.MetadataLogger;
import com.adaptris.core.ServiceImp;

public abstract class MetadataServiceImpl extends ServiceImp {

  private static final MetadataLogger DEFAULT_LOGGER = (list) -> {
    return list.toString();
  };

  @AdvancedConfig
  @InputFieldDefault(value = "full key and value")
  @Valid
  private MetadataLogger metadataLogger;

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {

  }

  @Override
  public void prepare() throws CoreException {

  }

  protected void logMetadata(String logMsg, MetadataElement... elements) {
    log.trace(logMsg, metadataLogger().toString(elements));
  }

  protected void logMetadata(String logMsg, Collection<MetadataElement> list) {
    log.trace(logMsg, metadataLogger().toString(list));
  }

  public MetadataLogger getMetadataLogger() {
    return metadataLogger;
  }

  public void setMetadataLogger(MetadataLogger metadataLogger) {
    this.metadataLogger = metadataLogger;
  }

  public <T extends MetadataServiceImpl> T withMetadataLogger(MetadataLogger logger) {
    setMetadataLogger(logger);
    return (T) this;
  }

  private MetadataLogger metadataLogger() {
    return ObjectUtils.defaultIfNull(getMetadataLogger(), DEFAULT_LOGGER);
  }
}
