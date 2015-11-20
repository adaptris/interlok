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

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Null implementation of <code>AdaptrisMessageProducer</code>.
 * </p>
 * 
 * @config null-message-producer
 */
@XStreamAlias("null-message-producer")
public class NullMessageProducer extends AdaptrisMessageProducerImp {

  public NullMessageProducer() {
	  setMessageFactory(null);
  }

  public NullMessageProducer(ProduceDestination p) {
    this();
    setDestination(p);
  }

  /** @see com.adaptris.core.AdaptrisMessageProducer */
  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
    // do nothing...
  }

  /** @see com.adaptris.core.AdaptrisMessageProducer */
  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination overload)
    throws ProduceException {

    // do nothing...
  }

  /** @see com.adaptris.core.AdaptrisMessageProducer */
  @Override
  public void init() throws CoreException {
    // do nothing...
  }

  /** @see com.adaptris.core.AdaptrisMessageProducer */
  @Override
  public void start() throws CoreException {
    // do nothing...
  }

  /** @see com.adaptris.core.AdaptrisMessageProducer */
  @Override
  public void stop() {
    // do nothing...
  }

  /** @see com.adaptris.core.AdaptrisMessageProducer */
  @Override
  public void close() {
    // do nothing...
  }

  /** @see com.adaptris.core.AdaptrisMessageProducer */
  @Override
  public AdaptrisMessage request(AdaptrisMessage msg) throws ProduceException {
    return null; // do nothing...
  }

  /** @see com.adaptris.core.AdaptrisMessageProducer */
  @Override
  public AdaptrisMessage request(AdaptrisMessage msg, long timeout)
    throws ProduceException {

    return null; // do nothing...
  }

  /** @see com.adaptris.core.AdaptrisMessageProducer */
  @Override
  public AdaptrisMessage request(
    AdaptrisMessage msg,
    ProduceDestination destination)
    throws ProduceException {

    return null; // do nothing...
  }

  /** @see com.adaptris.core.AdaptrisMessageProducer */
  @Override
  public AdaptrisMessage request(
    AdaptrisMessage msg,
    ProduceDestination destination,
    long timeout)
    throws ProduceException {

    return null; // do nothing...
  }

  /** @see MessageEventGenerator */
  @Override
  public String createName() {
    return this.getClass().getName();
  }


  @Override
  public void prepare() throws CoreException {
  }

}
