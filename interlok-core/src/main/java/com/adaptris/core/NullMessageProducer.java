/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.adaptris.core;

import static com.adaptris.core.util.DestinationHelper.logWarningIfNotNull;

import javax.validation.Valid;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.Removal;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Null implementation of <code>AdaptrisMessageProducer</code>.
 * </p>
 *
 * @config null-message-producer
 */
@XStreamAlias("null-message-producer")
@AdapterComponent
@ComponentProfile(summary = "Default NO-OP producer implementation", tag = "producer,base",
recommended = {NullConnection.class})
public class NullMessageProducer extends AdaptrisMessageProducerImp {

  /**
   * The destination is ignored.
   *
   */
  @Deprecated
  @Getter
  @Setter
  @Valid
  @ConfigDeprecated(removalVersion = "4.0.0", message = "Destination has no meaning for a no-op producer", groups = Deprecated.class)
  private ProduceDestination destination;


  private transient boolean destWarning;

  public NullMessageProducer() {
    setMessageFactory(null);
  }

  @Override
  public AdaptrisMessage request(AdaptrisMessage msg) throws ProduceException {
    return null;
  }

  @Override
  public AdaptrisMessage request(AdaptrisMessage msg, long timeout) throws ProduceException {
    return null;
  }

  @Override
  @Deprecated
  @Removal(version = "4.0.0")
  public AdaptrisMessage request(AdaptrisMessage msg, ProduceDestination destination)
      throws ProduceException {

    return null;
  }

  @Override
  @Deprecated
  @Removal(version = "4.0.0")
  public AdaptrisMessage request(AdaptrisMessage msg, ProduceDestination destination, long timeout)
      throws ProduceException {
    return null;
  }

  @Override
  public void prepare() throws CoreException {
    logWarningIfNotNull(destWarning, () -> destWarning = true, getDestination(),
        "{} uses destination, it has no meaning", LoggingHelper.friendlyName(this));
  }

  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return null;
  }

  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
  }

  @Override
  @Deprecated
  @Removal(version = "4.0.0")
  public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
  }

}
