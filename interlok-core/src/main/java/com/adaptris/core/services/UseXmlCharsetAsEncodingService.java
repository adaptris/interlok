/*
 * Copyright 2018 Adaptris Ltd.
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

package com.adaptris.core.services;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Changes the character encoding associated with a message.
 * <p>
 * This service does nothing with the data, but simply changes the character encoding associated with the message using
 * {@link com.adaptris.core.AdaptrisMessage#setCharEncoding(String)} after parsing the XML via {@link XMLStreamReader}. If there is
 * no encoding specified via {@link XMLStreamReader#getCharacterEncodingScheme()} then no change occurs.
 * </p>
 * 
 * @config use-xml-charset-as-encoding
 * 
 * 
 */
@XStreamAlias("use-xml-charset-as-encoding")
@AdapterComponent
@ComponentProfile(summary = "Change the character encoding of a message based on the XML charset", tag = "service,xml,encoding")
public class UseXmlCharsetAsEncodingService extends ServiceImp {

  public UseXmlCharsetAsEncodingService() {
    super();
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {

    XMLInputFactory factory = createInputFactory();
    XMLStreamReader reader = null;
    try {
      try (InputStream in = msg.getInputStream()) {
        reader = factory.createXMLStreamReader(in);
        String encoding = reader.getCharacterEncodingScheme();
        if (!StringUtils.isBlank(encoding)) {
          msg.setContentEncoding(encoding);
        }
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    finally {
      closeQuietly(reader);
    }
  }

  @Override
  protected void initService() throws CoreException {
    // nothing to do.
  }

  @Override
  protected void closeService() {
    // nothing to do.
  }

  @Override
  public void prepare() throws CoreException {
    // nothing to do.
  }

  private static void closeQuietly(XMLStreamReader r) {
    try {
      if (r != null) {
        r.close();
      }
    }
    catch (Exception ignored) {

    }
  }

  private static XMLInputFactory createInputFactory() {
    XMLInputFactory factory = XMLInputFactory.newFactory();
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    return factory;
  }
}
