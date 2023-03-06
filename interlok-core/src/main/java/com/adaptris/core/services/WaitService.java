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

package com.adaptris.core.services;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.TimeInterval;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* <p>
* Implementation of <code>Service</code> for testing which sleeps for a configurable period.
* </p>
*
* @config wait-service
*
*/
@JacksonXmlRootElement(localName = "wait-service")
@XStreamAlias("wait-service")
@AdapterComponent
@ComponentProfile(summary = "Delay processing", tag = "service")
@DisplayOrder(order = {"waitInterval", "randomize"})
public class WaitService extends ServiceImp {

private static final TimeInterval DEFAULT_WAIT = new TimeInterval(20L, TimeUnit.SECONDS);

private TimeInterval waitInterval;
@InputFieldDefault(value = "false")
private Boolean randomize;
@AdvancedConfig(rare = true)
@InputFieldDefault(value = "false")
private Boolean exceptionOnInterrupt;

/**
* <p>
* Creates a new instance.
* </p>
*/
public WaitService() {
}

public WaitService(TimeInterval wait) {
this(wait, null);
}

public WaitService(TimeInterval wait, Boolean randomize) {
this();
setWaitInterval(wait);
setRandomize(randomize);
}

public WaitService(String uniqueId) {
this();
setUniqueId(uniqueId);
}

/**
* <p>
* Waits for the configured number of milliseconds.
* </p>
*
* @param msg the message to apply service to
* @throws ServiceException wrapping any underlying <code>Exception</code>s
*/
@Override
public void doService(AdaptrisMessage msg) throws ServiceException {

try {
long waitMs = waitMs();
log.trace("Waiting for [{}] ms; waking up at approx. {}", waitMs,
DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis() + waitMs)));
Thread.sleep(waitMs);
}
catch (InterruptedException e) {
handleInterrupt(e);
}
}

@Override
protected void initService() {
}

@Override
protected void closeService() {
}

protected long waitMs() {
long maxWaitMs = TimeInterval.toMillisecondsDefaultIfNull(getWaitInterval(), DEFAULT_WAIT);
return randomizeWait() ? ThreadLocalRandom.current().nextLong(maxWaitMs) : maxWaitMs;
}

public TimeInterval getWaitInterval() {
return waitInterval;
}

/**
* Set how long to wait for.
*
* @param interval if not specified then the default is 20 seconds.
*/
public void setWaitInterval(TimeInterval interval) {
this.waitInterval = interval;
}


@Override
public void prepare() throws CoreException {
}

public Boolean getRandomize() {
return randomize;
}

/**
* Set to true to randomize the wait time between 0 and the value specified by {@link #setWaitInterval(TimeInterval)}
*
* @param b default null (false)
*/
public void setRandomize(Boolean b) {
this.randomize = b;
}

protected boolean randomizeWait() {
return BooleanUtils.toBooleanDefaultIfNull(getRandomize(), false);
}

/**
* @return the exceptionOnInterupt
*/
public Boolean getExceptionOnInterrupt() {
return exceptionOnInterrupt;
}

/**
* Whether or not to throw an exception if an {@link InterruptedException} happens.
*
* @param b the exceptionOnInterupt to set
* @since 3.6.6
*/
public void setExceptionOnInterrupt(Boolean b) {
this.exceptionOnInterrupt = b;
}

void handleInterrupt(InterruptedException e) throws ServiceException {
if (BooleanUtils.toBooleanDefaultIfNull(getExceptionOnInterrupt(), false)) {
throw ExceptionHelper.wrapServiceException(e);
}
}
}
