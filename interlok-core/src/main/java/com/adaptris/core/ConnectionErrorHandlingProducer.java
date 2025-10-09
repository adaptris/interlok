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
public class ConnectionErrorHandlingProducer implements AdaptrisMessageProducer {
    protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

    protected AdaptrisMessageProducer producer;

    @Override
    public AdaptrisMessage request(AdaptrisMessage msg) throws ProduceException {
        try {
            return producer.request(msg);
        } catch (ProduceException e) {
            maybeHandleException(e);
            throw e;
        }
    }

    @Override
    public AdaptrisMessage request(AdaptrisMessage msg, long timeout) throws ProduceException {
        try {
            return producer.request(msg, timeout);
        } catch (ProduceException e) {
            maybeHandleException(e);
            throw e;
        }
    }

    @Override
    public void produce(AdaptrisMessage msg) throws ProduceException {
        try {
            producer.produce(msg);
        } catch (ProduceException e) {
            maybeHandleException(e);
            throw e;
        }
    }

    protected void maybeHandleException(ProduceException e) throws ProduceException {
        AdaptrisConnection connection = retrieveConnection(AdaptrisConnection.class);
        if (connection != null) {
            ConnectionErrorHandler errorHandler = connection.connectionErrorHandler();
            if (errorHandler != null) {
                if (errorHandler.canHandleException(e)) {
                    try {
                        handleConnectionException();
                    } catch (Exception ex) {
                        log.warn(ex.getMessage(), e);
                    }
                }
                throw e;
            }
        }
    }

    @Override
    public void registerConnection(AdaptrisConnection connection) {
        producer.registerConnection(connection);
    }

    @Override
    public <T> T retrieveConnection(Class<T> type) {
        return producer.retrieveConnection(type);
    }

    @Override
    public AdaptrisMessageEncoder getEncoder() {
        return producer.getEncoder();
    }

    @Override
    public void setEncoder(AdaptrisMessageEncoder encoder) {
        producer.setEncoder(encoder);
    }

    @Override
    public void handleConnectionException() throws CoreException {
        producer.handleConnectionException();
    }

    @Override
    public byte[] encode(AdaptrisMessage msg) throws CoreException {
        return producer.encode(msg);
    }

    @Override
    public AdaptrisMessage decode(byte[] bytes) throws CoreException {
        return producer.decode(bytes);
    }

    @Override
    public AdaptrisMessageFactory getMessageFactory() {
        return producer.getMessageFactory();
    }

    @Override
    public void setMessageFactory(AdaptrisMessageFactory f) {
        producer.setMessageFactory(f);
    }

    @Override
    public String getUniqueId() {
        return producer.getUniqueId();
    }

    @Override
    public void prepare() throws CoreException {
        producer.prepare();
    }

    @Override
    public String createName() {
        return producer.createName();
    }

    @Override
    public String createQualifier() {
        return producer.createQualifier();
    }

    @Override
    public boolean isTrackingEndpoint() {
        return producer.isTrackingEndpoint();
    }
}
