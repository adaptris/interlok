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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A decorator for <code>AdaptrisMessageProducer</code> which will use the configured ConnectionErrorHandler
 * to check if the exception is related to the connection and if so, handle it. Otherwise, the exception will
 * be handled as per the underlying producer's implementation.
 * </p>
 */
@NoArgsConstructor
@XStreamAlias("connection-error-handling-producer")
public class ConnectionErrorHandlingProducer implements AdaptrisMessageProducer {
    protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

    protected AdaptrisMessageProducer delegate;

    public AdaptrisMessageProducer getDelegate() {
        return delegate;
    }

    public void setDelegate(AdaptrisMessageProducer delegate) {
        this.delegate = delegate;
    }

    @Override
    public AdaptrisMessage request(AdaptrisMessage msg) throws ProduceException {
        return delegate.request(msg);
    }

    @Override
    public AdaptrisMessage request(AdaptrisMessage msg, long timeout) throws ProduceException {
       return delegate.request(msg, timeout);
    }

    @Override
    public void produce(AdaptrisMessage msg) throws ProduceException {
        delegate.produce(msg);
    }

    @Override
    public void registerConnection(AdaptrisConnection connection) {
        delegate.registerConnection(connection);
    }

    @Override
    public <T> T retrieveConnection(Class<T> type) {
        return delegate.retrieveConnection(type);
    }

    @Override
    public AdaptrisMessageEncoder getEncoder() {
        return delegate.getEncoder();
    }

    @Override
    public void setEncoder(AdaptrisMessageEncoder encoder) {
        delegate.setEncoder(encoder);
    }

    @Override
    public void handleConnectionException() throws CoreException {
        delegate.handleConnectionException();
    }

    @Override
    public byte[] encode(AdaptrisMessage msg) throws CoreException {
        return delegate.encode(msg);
    }

    @Override
    public AdaptrisMessage decode(byte[] bytes) throws CoreException {
        return delegate.decode(bytes);
    }

    @Override
    public AdaptrisMessageFactory getMessageFactory() {
        return delegate.getMessageFactory();
    }

    @Override
    public void setMessageFactory(AdaptrisMessageFactory f) {
        delegate.setMessageFactory(f);
    }

    @Override
    public String getUniqueId() {
        return delegate.getUniqueId();
    }

    @Override
    public void prepare() throws CoreException {
        delegate.prepare();
    }

    @Override
    public String createName() {
        return delegate.createName();
    }

    @Override
    public String createQualifier() {
        return delegate.createQualifier();
    }

    @Override
    public boolean isTrackingEndpoint() {
        return delegate.isTrackingEndpoint();
    }
}
