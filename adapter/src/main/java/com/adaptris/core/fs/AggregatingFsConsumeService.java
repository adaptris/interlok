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

package com.adaptris.core.fs;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.aggregator.AggregatingConsumeServiceImpl;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implentation of {@link com.adaptris.core.services.aggregator.AggregatingConsumeService} that allows you to consume a related message from a directory based on some
 * criteria.
 * 
 * @config aggregating-fs-consume-service
 * 
 */
@XStreamAlias("aggregating-fs-consume-service")
@AdapterComponent
@ComponentProfile(summary = "Allows you to aggregate messages from the filesystem", tag = "service,aggregation")
@DisplayOrder(order = {"fsConsumer"})
public class AggregatingFsConsumeService extends AggregatingConsumeServiceImpl<NullConnection> {

  @NotNull
  @Valid
  private AggregatingFsConsumer fsConsumer;

  public AggregatingFsConsumeService() {

  }

  @Override
  protected void initService() throws CoreException {
    super.initService();
    if (fsConsumer == null) throw new CoreException("FS Consumer is null");
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      start(fsConsumer);
      fsConsumer.aggregateMessages(msg, this);
    }
    finally {
      stop(fsConsumer);
    }
  }

  /**
   * @return the fsConsumer
   */
  public AggregatingFsConsumer getFsConsumer() {
    return fsConsumer;
  }

  /**
   * @param fsConsumer the fsConsumer to set
   */
  public void setFsConsumer(AggregatingFsConsumer fsConsumer) {
    this.fsConsumer = fsConsumer;
  }


  @Override
  public void prepare() throws CoreException {
    if (getFsConsumer() != null) {
      getFsConsumer().prepare();
    }
  }

}
