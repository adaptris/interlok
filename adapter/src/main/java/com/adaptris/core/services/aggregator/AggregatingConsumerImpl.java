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

package com.adaptris.core.services.aggregator;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.LifecycleHelper;


/**
 * Base class for {@link com.adaptris.core.services.aggregator.AggregatingConsumer} implementations.
 * 
 * @author lchan
 * 
 */
public abstract class AggregatingConsumerImpl<E extends AggregatingConsumeService> implements AggregatingConsumer<E> {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  @NotNull
  @Valid
  private MessageAggregator messageAggregator;
  @NotNull
  @Valid
  private ConsumeDestinationGenerator destination;

  public AggregatingConsumerImpl() {
  }

  /**
   * @return the messageHandler
   */
  public MessageAggregator getMessageAggregator() {
    return messageAggregator;
  }

  /**
   * Set the message handler that controls how the correlated message is merged into the original.
   * 
   * @param cmh the messageHandler to set
   */
  public void setMessageAggregator(MessageAggregator cmh) {
    if (cmh == null) throw new IllegalArgumentException("message-aggregator is null");
    this.messageAggregator = cmh;
  }

  public void init() throws CoreException {
    if (destination == null) {
      throw new CoreException("Null Destination");
    }
    if (getMessageAggregator() == null) {
      throw new CoreException("Null Message Aggregator");
    }
  }


  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
  }

  public ConsumeDestinationGenerator getDestination() {
    return destination;
  }

  /**
   * Set the destination generator.
   * 
   * @param cd the destination generator.
   */
  public void setDestination(ConsumeDestinationGenerator cd) {
    this.destination = cd;
  }

  protected static void rethrowServiceException(Exception e) throws ServiceException {
    if (e instanceof ServiceException) {
      throw (ServiceException) e;
    }
    else {
      throw new ServiceException(e);
    }
  }

  protected void start(AdaptrisComponent ac) throws ServiceException {
    try {
      LifecycleHelper.init(ac);
      LifecycleHelper.start(ac);
    }
    catch (CoreException e) {
      throw new ServiceException(e);
    }
  }

  protected void stop(AdaptrisComponent ac) {
    LifecycleHelper.stop(ac);
    LifecycleHelper.close(ac);
  }
}
