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

package com.adaptris.core.services.mime;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

/**
 * Choose one or more mime parts from an existing multipart message and add them as metadata of the AdaptrisMessage. The mime payload
 * remains unchanged.
 *
 * @config mime-part-selector-service-to-metadata
 *
 */
@XStreamAlias("mime-part-selector-service-to-metadata")
@AdapterComponent
@ComponentProfile(summary = "Select mime-parts from the message and put them into metadata", since = "5.0.1", tag = "service,mime")
@DisplayOrder(order = { "selectors" })
public class MimePartSelectorToMetadata extends ServiceImp {

  /**
   * The list of mime part selectors and metadata keys to add the content to.
   */
  @Getter
  @Setter
  @NotNull
  @Valid
  private List<PartSelectorToMetadata> selectors = new ArrayList<>();

  public MimePartSelectorToMetadata() {
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    for (PartSelectorToMetadata partSelectorMetadata : getSelectors()) {
      partSelectorMetadata.apply(msg);
    }
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  @Override
  public void prepare() throws CoreException {
  }

}
