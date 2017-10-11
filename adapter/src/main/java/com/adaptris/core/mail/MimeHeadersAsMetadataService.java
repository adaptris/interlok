/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.mail;

import java.io.InputStream;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
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
 * Add headers from a mime message as metadata.
 * 
 * <p>
 * If you have previously used {@link RawMailConsumer} or similar, then you will have a MimeMessage that can be parsed for its
 * header information, if you have not previously used a {@link RawMailConsumer#setHeaderHandler(MailHeaderHandler)} at the point of
 * entry, then nows your chance.
 * </p>
 * 
 * @author lchan
 * @since 3.6.5
 */
@XStreamAlias("mime-headers-as-metadata-service")
@AdapterComponent
@ComponentProfile(summary = "Parse a MimeMessage and add its headers as metadata", tag = "service,mail,metadata")
@DisplayOrder(order =
{
    "handler"
})
public class MimeHeadersAsMetadataService extends ServiceImp {

  @NotNull
  @Valid
  @AutoPopulated
  private MetadataMailHeaders handler;

  private transient Session session;

  public MimeHeadersAsMetadataService() {
    setHandler(new MetadataMailHeaders());
    session = Session.getInstance(new Properties(), null);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      getHandler().handle(createMimeMessage(msg), msg);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  public MetadataMailHeaders getHandler() {
    return handler;
  }

  public void setHandler(MetadataMailHeaders headerHandler) {
    this.handler = Args.notNull(headerHandler, "handler");
  }

  private MimeMessage createMimeMessage(AdaptrisMessage msg) throws Exception {
    MimeMessage result = null;
    try (InputStream mimeMessageInput = msg.getInputStream()) {
      result = new MimeMessage(session, mimeMessageInput);
    }
    return result;
  }
}
