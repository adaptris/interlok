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

package com.adaptris.core.ftp;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConnectedService;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.aggregator.AggregatingConsumeServiceImpl;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implentation of {@link com.adaptris.core.services.aggregator.AggregatingConsumeService} that allows you to consume a related
 * message via FTP based on some criteria.
 * 
 * @config aggregating-ftp-consume-service
 * 
 */
@JacksonXmlRootElement(localName = "aggregating-ftp-consume-service")
@XStreamAlias("aggregating-ftp-consume-service")
@AdapterComponent
@ComponentProfile(summary = "Allows you to aggregate messages from an FTP server", tag = "service,aggregation,ftp", recommended= {FileTransferConnection.class})
@DisplayOrder(order ={"connection", "consumer"})
public class AggregatingFtpConsumeService extends AggregatingConsumeServiceImpl<FileTransferConnection>
    implements ConnectedService {

  @NotNull
  @Valid
  private AggregatingFtpConsumer consumer;
  @NotNull
  @Valid
  private AdaptrisConnection connection;

  public AggregatingFtpConsumeService() {

  }

  public AggregatingFtpConsumeService(AdaptrisConnection conn, AggregatingFtpConsumer consumer) {
    this();
    setConnection(conn);
    setConsumer(consumer);
  }

  @Override
  protected void initService() throws CoreException {
    try {
      super.initService();
      Args.notNull(connection, "connection");
      Args.notNull(consumer, "consumer");
      LifecycleHelper.init(connection);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      start(consumer);
      consumer.aggregateMessages(msg, this);
    }
    finally {
      stop(consumer);
    }
  }

  /**
   * @return the fsConsumer
   */
  public AggregatingFtpConsumer getConsumer() {
    return consumer;
  }

  /**
   * @param fsConsumer the fsConsumer to set
   */
  public void setConsumer(AggregatingFtpConsumer fsConsumer) {
    this.consumer = fsConsumer;
  }


  @Override
  public void prepare() throws CoreException {
    LifecycleHelper.prepare(getConnection());
  }

  @Override
  public AdaptrisConnection getConnection() {
    return connection;
  }

  @Override
  public void setConnection(AdaptrisConnection connection) {
    this.connection = connection;
  }
}
