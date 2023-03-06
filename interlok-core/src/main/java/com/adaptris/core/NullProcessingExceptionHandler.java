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

package com.adaptris.core;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* Null implementation of Processing Exceptions.
*
* @config null-processing-exception-handler
*
* @author lchan
*
*/
@JacksonXmlRootElement(localName = "null-processing-exception-handler")
@XStreamAlias("null-processing-exception-handler")
@AdapterComponent
@ComponentProfile(summary = "The default NO-OP exception handler", tag = "error-handling,base")
public class NullProcessingExceptionHandler extends RootProcessingExceptionHandler {

public NullProcessingExceptionHandler() {
super();
}

public void handleProcessingException(AdaptrisMessage msg) {
msg.getObjectHeaders().put(CoreConstants.OBJ_METADATA_MESSAGE_FAILED, true);
notifyParent(msg);
}

public void registerWorkflow(Workflow w) {
}

@Override
public void init() throws CoreException {
}

@Override
public void start() throws CoreException {
}

@Override
public synchronized void stop() {
}

@Override
public synchronized void close() {
}

@Override
public void prepare() throws CoreException {
}

public boolean hasConfiguredBehaviour() {
return false;
}

}
