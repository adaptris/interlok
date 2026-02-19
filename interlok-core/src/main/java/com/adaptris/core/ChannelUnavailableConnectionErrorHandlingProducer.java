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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.BooleanUtils;

/**
 * <p>
 * A decorator for <code>AdaptrisMessageProducer</code> which will handle connection errors
 * by making the channel unavailable if the number of connection errors received reaches a
 * certain threshold.
 * </p>
 */
@NoArgsConstructor
@XStreamAlias("channel-unavailable-connection-error-handling-producer")
public class ChannelUnavailableConnectionErrorHandlingProducer extends ConnectionErrorHandlingProducer {
    protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

    protected Integer connectionErrorThreshold;
    protected String connectionErrorWaitDuration;
    protected Duration _connectionErrorWaitDuration;
    protected Integer connectionErrors = 0;
    private Boolean autoConfigureConnection = Boolean.TRUE;

    @Override
    public void prepare() throws CoreException {
        super.prepare();
        setConnectionErrorWaitDuration(connectionErrorWaitDuration);
    }

    public Integer getConnectionErrorThreshold() {
        return connectionErrorThreshold;
    }

    public void setConnectionErrorThreshold(Integer connectionErrorThreshold) {
        this.connectionErrorThreshold = connectionErrorThreshold;
    }

    public String getConnectionErrorWaitDuration() {
        return connectionErrorWaitDuration;
    }

    public void setConnectionErrorWaitDuration(String connectionErrorWaitDuration) {
        setConnectionErrorWaitDuration(Duration.parse(connectionErrorWaitDuration));
        this.connectionErrorWaitDuration = connectionErrorWaitDuration;
    }

    public void setConnectionErrorWaitDuration(Duration connectionErrorWaitDuration) {
        this._connectionErrorWaitDuration =connectionErrorWaitDuration;
    }

    public Duration connectionErrorWaitDuration() {
        if (_connectionErrorWaitDuration == null && connectionErrorWaitDuration != null) {
            setConnectionErrorWaitDuration(connectionErrorWaitDuration);
        }
        return _connectionErrorWaitDuration;
    }

    public Integer getConnectionErrors() {
        return connectionErrors;
    }

    protected void setConnectionErrors(Integer connectionErrors) {
        this.connectionErrors = connectionErrors;
    }

    public Boolean getAutoConfigureConnection() {
        return autoConfigureConnection;
    }

    public void setAutoConfigureConnection(Boolean autoConfigureConnection) {
        this.autoConfigureConnection = autoConfigureConnection;
    }

    @Override
    public AdaptrisMessage request(AdaptrisMessage msg) throws ProduceException {
        ProduceException failed = null;
        try {
            return super.request(msg);
        } catch (ProduceException ex) {
            failed = ex;
            throw ex;
        } finally {
            if (getAutoConfigureConnection() && failed == null) {
                setConnectionErrors(0);
                toggleChannelAvailability(true);
            }
        }
    }

    @Override
    public AdaptrisMessage request(AdaptrisMessage msg, long timeout) throws ProduceException {
        ProduceException failed = null;
        try {
            return super.request(msg, timeout);
        } catch (ProduceException ex) {
            failed = ex;
            throw ex;
        } finally {
            if (getAutoConfigureConnection() && failed == null) {
                setConnectionErrors(0);
                toggleChannelAvailability(true);
            }
        }
    }

    @Override
    public void produce(AdaptrisMessage msg) throws ProduceException {
        ProduceException failed = null;
        try {
            super.produce(msg);
        } catch (ProduceException ex) {
            failed = ex;
            throw ex;
        } finally {
            if (getAutoConfigureConnection() && failed == null) {
                setConnectionErrors(0);
                toggleChannelAvailability(true);
            }
        }
    }

    @Override
    public void handleConnectionException() throws CoreException {
        if (connectionErrors == null) setConnectionErrors(0);
        setConnectionErrors(connectionErrors + 1);
        log.debug("Consecutive connection errors encountered: {} threshold: {}", getConnectionErrors(), getConnectionErrorThreshold());
        if (getAutoConfigureConnection() && getConnectionErrors() >= getConnectionErrorThreshold()) {
            toggleChannelAvailability(false);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        toggleChannelAvailability(true);
                        ChannelUnavailableConnectionErrorHandlingProducer.super.handleConnectionException();
                    } catch (CoreException e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }, connectionErrorWaitDuration().toMillis());
        }
    }


    protected void toggleChannelAvailability(boolean available) {
        AdaptrisConnection connection = retrieveConnection(AdaptrisConnection.class);
        Set<StateManagedComponent> list = connection.retrieveExceptionListeners();
        for (StateManagedComponent c : list) {
            if (c instanceof Channel) {
                ((Channel) c).toggleAvailability(available);
            }
        }
    }


}
