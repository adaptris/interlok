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

package com.adaptris.core.jms;

import javax.jms.Session;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
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
* Implentation of {@link com.adaptris.core.services.aggregator.AggregatingConsumeService} that allows you to consume a related message from a queue based on some
* criteria.
*
* @config aggregating-jms-consume-service
*
*/
@JacksonXmlRootElement(localName = "aggregating-jms-consume-service")
@XStreamAlias("aggregating-jms-consume-service")
@AdapterComponent
@ComponentProfile(summary = "Allows you to aggregate messages from a JMS Queue", tag = "service,aggregation,jms", recommended= {JmsConnection.class})
@DisplayOrder(order = {"connection", "jmsConsumer"})
public class AggregatingJmsConsumeService extends AggregatingConsumeServiceImpl<JmsConnection>
implements JmsActorConfig, ConnectedService {

@NotNull
@Valid
private AggregatingJmsConsumer jmsConsumer;
@NotNull
@Valid
private AdaptrisConnection connection;
private transient Session session;

private transient Logger myLogger = LoggerFactory.getLogger(this.getClass());

public AggregatingJmsConsumeService() {
}

@Override
protected void initService() throws CoreException {
try {
Args.notNull(connection, "connection");
Args.notNull(jmsConsumer, "jmsConsumer");
LifecycleHelper.init(connection);
session = getConnection().retrieveConnection(JmsConnection.class).createSession(false, configuredAcknowledgeMode());
}
catch (Exception e) {
throw ExceptionHelper.wrapCoreException(e);
}
}

@Override
public void start() throws CoreException {
super.start();
LifecycleHelper.start(connection);
}

@Override
public void stop() {
super.stop();
LifecycleHelper.stop(connection);
}

@Override
protected void closeService() {
LifecycleHelper.close(connection);
}

@Override
public void prepare() throws CoreException {
LifecycleHelper.prepare(getConnection());
LifecycleHelper.prepare(getJmsConsumer());
}

@Override
public void doService(AdaptrisMessage msg) throws ServiceException {
try {
start(jmsConsumer);
jmsConsumer.aggregateMessages(msg, this);
}
finally {
stop(jmsConsumer);
}
}

// Not used, only really used internally by OracleAqImplementation do handle some
// oracle specifics.
@Override
public MessageTypeTranslator configuredMessageTranslator() {
return null;
}

@Override
public int configuredAcknowledgeMode() {
return AcknowledgeMode.getMode(AcknowledgeMode.Mode.AUTO_ACKNOWLEDGE.name());
}

// Not used, only really used internally.
@Override
public CorrelationIdSource configuredCorrelationIdSource() {
return null;
}

// Not used, only really used internally.
@Override
public AdaptrisMessageListener configuredMessageListener() {
return null;
}

@Override
public Session currentSession() {
return session;
}

// Not used, only really used internally.
@Override
public Logger currentLogger() {
return myLogger;
}

// Not used, only really used internally.
@Override
public long rollbackTimeout() {
return 0;
}

public AggregatingJmsConsumer getJmsConsumer() {
return jmsConsumer;
}

public void setJmsConsumer(AggregatingJmsConsumer consumer) {
this.jmsConsumer = consumer;
}

public AdaptrisConnection getConnection() {
return connection;
}

public void setConnection(AdaptrisConnection connection) {
this.connection = connection;
}

@Override
public boolean isManagedTransaction() {
return false;
}

}
