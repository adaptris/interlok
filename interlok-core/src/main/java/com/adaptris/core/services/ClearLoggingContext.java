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

import org.slf4j.MDC;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* Remove all mapped diagnostic context via {@link MDC#clear()}.
*
*
* @config clear-logging-context-service
*
*/
@JacksonXmlRootElement(localName = "clear-logging-context-service")
@XStreamAlias("clear-logging-context-service")
@AdapterComponent
@ComponentProfile(summary = "Remove all mapped diagnostic contexts", tag = "service,logging,debug", since = "3.10.0")
public class ClearLoggingContext extends ServiceImp {

public ClearLoggingContext() {
super();
}

@Override
public void doService(AdaptrisMessage msg) throws ServiceException {
try {
for (String key : MDC.getCopyOfContextMap().keySet())
{
MDC.remove(key);
}
//      MDC.clear();
}
catch (IllegalArgumentException | IllegalStateException e) {
throw ExceptionHelper.wrapServiceException(e);
}
}

@Override
protected void initService() throws CoreException {
}

@Override
protected void closeService() {
}

@Override
public void prepare() throws CoreException {
}
}
