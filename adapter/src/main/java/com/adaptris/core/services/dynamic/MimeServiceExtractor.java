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

import javax.mail.internet.MimeBodyPart;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.MimeHelper;
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
public class MimeServiceExtractor implements ServiceExtractor {

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
    InputStream in = null;
    try {
      Args.notNull(getSelector(), "selector");
      MimeBodyPart part = selector.select(MimeHelper.createBodyPartIterator(m));
      if (part != null) {
        out = new ByteArrayOutputStream();
        in = part.getInputStream();
        IOUtils.copy(in, out);
      }
      else {
        throw new ServiceException("Could not select a part");
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    finally {
      IOUtils.closeQuietly(out);
      IOUtils.closeQuietly(in);
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
