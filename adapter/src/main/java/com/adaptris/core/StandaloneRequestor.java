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

import static com.adaptris.core.AdaptrisMessageImp.copyPayload;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * StandaloneProducer extension that allows request reply functionality within a service
 * 
 * @config standalone-requestor
 */
@XStreamAlias("standalone-requestor")
public class StandaloneRequestor extends StandaloneProducer {

  private static final TimeInterval DEFAULT_TIMEOUT = new TimeInterval(-1L, TimeUnit.MILLISECONDS);
  private TimeInterval replyTimeout;

  public StandaloneRequestor() {
    super();
  }

  public StandaloneRequestor(AdaptrisMessageProducer p) {
    super(p);
  }

  public StandaloneRequestor(AdaptrisConnection c, AdaptrisMessageProducer p) {
    super(c, p);
  }

  public StandaloneRequestor(AdaptrisConnection c, AdaptrisMessageProducer p, TimeInterval timeout) {
    super(c, p);
    setReplyTimeout(timeout);
  }

  @Override
  public void doService(AdaptrisMessage m) throws ServiceException {
    try {
      AdaptrisMessage reply;

      if (timeoutOverrideMs() == -1L) {
        reply = getProducer().request(m);
      }
      else {
        reply = getProducer().request(m, timeoutOverrideMs());
      }
      // It should be the case that RequestReplyProducerImp
      // now enforces the return type to be the same object that was passed in
      // I suppose we can't guarantee that ppl haven't implemented their
      // own.
      if (reply != m) {
        log.trace("Copying reply message into original message");
        copy(reply, m);
      }
    }
    catch (CoreException e) {
      throw new ServiceException(e);
    }
  }

  private void copy(AdaptrisMessage src, AdaptrisMessage dest)
      throws CoreException {
    try {
      dest.setContentEncoding(src.getContentEncoding());
      copyPayload(src, dest);
      dest.getObjectMetadata().putAll(src.getObjectMetadata());
      // Well the thing we shouldn't need to do is set the unique Id I guess.
      //
      dest.setUniqueId(src.getUniqueId());
      for (Object md : src.getMetadata()) {
        dest.addMetadata((MetadataElement) md);
      }
      for (Object marker : src.getMessageLifecycleEvent().getMleMarkers()) {
        dest.getMessageLifecycleEvent().addMleMarker((MleMarker) marker);
      }
    }
    catch (IOException e) {
      throw new CoreException(e);
    }
  }

  long timeoutOverrideMs() {
    return getReplyTimeout() != null ? getReplyTimeout().toMilliseconds() : DEFAULT_TIMEOUT.toMilliseconds();
  }

  public TimeInterval getReplyTimeout() {
    return replyTimeout;
  }

  /**
   * Set the timeout override for this request.
   *
   * @param timeoutOverride the override, default is -1, which will use the underlying producers default timeout.
   */
  public void setReplyTimeout(TimeInterval timeoutOverride) {
    this.replyTimeout = timeoutOverride;
  }
}
