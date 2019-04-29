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

package com.adaptris.core.services.dynamic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.mail.internet.MimeBodyPart;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.MimeHelper;
import com.adaptris.util.stream.StreamUtil;
import com.adaptris.util.text.mime.BodyPartIterator;
import com.adaptris.util.text.mime.PartSelector;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ServiceExtractor} implementation that uses a {@link PartSelector} to extract where the service is.
 * 
 * @config dynamic-mime-service-extractor
 * @author lchan
 * 
 */
@XStreamAlias("dynamic-mime-service-extractor")
@ComponentProfile(
    summary = "Select the service to executed based on a MIME selector on the message")
@DisplayOrder(order = {"selector"})
public class MimeServiceExtractor extends ServiceExtractorImpl {

  @NotNull
  @Valid
  private PartSelector selector;

  public MimeServiceExtractor() {

  }

  public MimeServiceExtractor(PartSelector selector) {
    this();
    setSelector(selector);
  }

  @Override
  public InputStream getInputStream(AdaptrisMessage m) throws ServiceException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      Args.notNull(getSelector(), "selector");
      try (BodyPartIterator itr = MimeHelper.createBodyPartIterator(m);
          OutputStream closeOut = out) {
        MimeBodyPart part = selector.select(itr);
        if (part != null) {
          StreamUtil.copyAndClose(part.getInputStream(), closeOut);
        } else {
          throw new ServiceException("Could not select a part");
        }
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    return new ByteArrayInputStream(out.toByteArray());
  }

  public PartSelector getSelector() {
    return selector;
  }

  /**
   * Set the {@link PartSelector} implementation to use.
   * 
   * @param selector the part selector.
   */
  public void setSelector(PartSelector selector) {
    this.selector = Args.notNull(selector, "selector");
  }

}
