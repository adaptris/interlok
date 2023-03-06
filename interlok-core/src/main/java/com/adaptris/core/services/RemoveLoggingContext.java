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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.MDC;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
* Remove a mapped diagnostic context via {@link MDC#remove(String)}.
* <p>
* If you have a large number of workflows then it may be useful to use a mapped diagnostic context to provide additional
* information into your logfile.
* </p>
*
* @config remove-logging-context-service
*
*/
@JacksonXmlRootElement(localName = "remove-logging-context-service")
@XStreamAlias("remove-logging-context-service")
@AdapterComponent
@ComponentProfile(summary = "Remove a mapped diagnostic context for logging; useful for filtering", tag = "service,logging,debug")
@DisplayOrder(order =
{
"keys"
})
public class RemoveLoggingContext extends ServiceImp {

@InputFieldHint(expression = true)
@XStreamImplicit(itemFieldName = "key")
private List<String> keys = new ArrayList<>();

public RemoveLoggingContext() {
super();
}

public RemoveLoggingContext(String key) {
this();
setKeys(new ArrayList<>(Arrays.asList(key)));
}

@Override
public void doService(AdaptrisMessage msg) throws ServiceException {
try {
for(String key : getKeys()) {
MDC.remove(msg.resolve(key));
}
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

public List<String> getKeys() {
return keys;
}

/**
* Set the keys for the mapped diagnostic context.
*
* @param keys the key to set
*/
public void setKeys(List<String> keys) {
this.keys = keys;
}

}
