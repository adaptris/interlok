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

package com.adaptris.core.http.jetty;

import static org.apache.commons.io.IOUtils.copy;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.http.server.ConfiguredStatusProvider;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link com.adaptris.core.AdaptrisMessageProducer} writes to the {@code HttpServletResponse} object metadata
 * provided by the Jetty engine.
 * 
 * @config jetty-standard-response-producer
 * 
 * @author lchan
 *
 */
@XStreamAlias("jetty-standard-response-producer")
@AdapterComponent
@ComponentProfile(summary = "Write and commit the HTTP Response", tag = "producer,http,https", recommended = {NullConnection.class})
@DisplayOrder(order = {"sendPayload", "flushBuffer", "forwardConnectionException"})
public class StandardResponseProducer extends ResponseProducerImpl {

  public StandardResponseProducer() {
    super();
  }

  public StandardResponseProducer(HttpStatus status) {
    this();
    setStatusProvider(new ConfiguredStatusProvider(status));
  }

  @Override
  public void init() throws CoreException {}

  @Override
  public void start() throws CoreException {}

  @Override
  public void stop() {}

  @Override
  public void close() {}

  @Override
  public void prepare() throws CoreException {}


  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
    HttpServletResponse response = (HttpServletResponse) msg.getObjectHeaders().get(CoreConstants.JETTY_RESPONSE_KEY);

    try {
      if (response == null) {
        log.debug("No HttpServletResponse in object metadata, nothing to do");
        return;
      }
      getResponseHeaderProvider().addHeaders(msg, response);
      String contentType = getContentTypeProvider().getContentType(msg);
      response.setContentType(contentType);
      response.setStatus(getStatus(msg).getCode());
      commitResponse(msg, response);
    } catch (Exception e) {
      throw ExceptionHelper.wrapProduceException(e);
    }
  }

  private void commitResponse(AdaptrisMessage msg, HttpServletResponse response) throws ProduceException {
    try {
      handlePayload(msg, response);
      if (flushBuffers()) {
        response.flushBuffer();
      }
    }
    catch (IOException | CoreException e) {
      if (forwardConnectionException()) {
        throw ExceptionHelper.wrapProduceException(e);
      }
      else {
        log.trace("Failed to commit response to HTTP; client disconnected?");
      }
    }
    
  }

  private void handlePayload(AdaptrisMessage msg, HttpServletResponse response) throws CoreException, IOException {
    if (sendPayload()) {
      if (getEncoder() != null) {
        getEncoder().writeMessage(msg, response);
      } else {
        if (msg.getSize() > 0) {
          try (InputStream in = new BufferedInputStream(msg.getInputStream())) {
            copy(in, response.getOutputStream());
          }
        }
      }
    }
  }

}
