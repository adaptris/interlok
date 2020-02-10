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

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.validation.Valid;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.ManagedThreadFactory;

/**
 * <p>
 * Implementation of behaviour common to <code>AdaptrisMessageConsumer</code> and <code>AdaptrisMessageProducer</code>.
 * </p>
 */
public abstract class AdaptrisMessageWorkerImp implements AdaptrisMessageWorker {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private transient AdaptrisConnection connection;

  @AdvancedConfig
  @Valid
  private AdaptrisMessageEncoder encoder;
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "false")
  private Boolean isTrackingEndpoint;

  @AdvancedConfig
  private AdaptrisMessageFactory messageFactory;
  private String uniqueId;

  public AdaptrisMessageWorkerImp() {
  }


  /**
   * @see com.adaptris.core.AdaptrisMessageWorker #handleConnectionException()
   */
  @Override
  public void handleConnectionException() throws CoreException {
    if (hasActiveErrorHandler()) {
      // spin off exception handler Thread
      Thread thread = new ManagedThreadFactory(getClass().getSimpleName()).newThread(new Runnable() {
        @Override
        public void run() {
          retrieveConnection(AdaptrisConnection.class).connectionErrorHandler().handleConnectionException();
        }
      });
      thread.setName("Connection Exc: " + Thread.currentThread().getName());
      log.trace("Handling Connection Exception");
      thread.start();
    }
  }

  private boolean hasActiveErrorHandler() {
    AdaptrisConnection c = retrieveConnection(AdaptrisConnection.class);
    return c != null && c.connectionErrorHandler() != null;
  }

  @Override
  public String createName() {
    return this.getClass().getName();
  }

  @Override
  public String createQualifier() {
    return defaultIfEmpty(getUniqueId(), "");
  }

  @Override
  public byte[] encode(AdaptrisMessage msg) throws CoreException {
    if (encoder != null) {
      registerEncoderMessageFactory();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      encoder.writeMessage(msg, out);
      return out.toByteArray();
    }
    return msg.getPayload();
  }

  @Override
  public AdaptrisMessage decode(byte[] bytes) throws CoreException {
    if (encoder != null) {
      registerEncoderMessageFactory();
      ByteArrayInputStream in = new ByteArrayInputStream(bytes);
      return encoder.readMessage(in);
    }
    return defaultIfNull(getMessageFactory()).newMessage(bytes);
  }

  // gets and sets...

  @Override
  public AdaptrisMessageEncoder getEncoder() {
    return encoder;
  }


  @Override
  public void setEncoder(AdaptrisMessageEncoder enc) {
    encoder = enc;
    registerEncoderMessageFactory();
  }

  @Override
  public <T> T retrieveConnection(Class<T> type) {
    return connection.retrieveConnection(type);
  }


  @Override
  public void registerConnection(AdaptrisConnection conn) {
    connection = conn;
  }

  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  public void setUniqueId(String s) {
    uniqueId = s;
  }

  public Boolean getIsTrackingEndpoint() {
    return isTrackingEndpoint;
  }

  @Override
  public boolean isTrackingEndpoint() {
    return BooleanUtils.toBooleanDefaultIfNull(getIsTrackingEndpoint(), false);
  }

  public void setIsTrackingEndpoint(Boolean b) {
    isTrackingEndpoint = b;
  }

  @Override
  public AdaptrisMessageFactory getMessageFactory() {
    return messageFactory;
  }

  @Override
  public void setMessageFactory(AdaptrisMessageFactory f) {
    messageFactory = f;
    registerEncoderMessageFactory();
  }

  public <T extends AdaptrisMessageWorkerImp> T withMessageFactory(AdaptrisMessageFactory f) {
    setMessageFactory(f);
    return (T) this;
  }

  protected void registerEncoderMessageFactory() {
    if (getEncoder() != null) {
      getEncoder().registerMessageFactory(defaultIfNull(getMessageFactory()));
    }
  }
}
