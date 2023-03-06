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
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.MessageHelper;
import com.adaptris.util.TimeInterval;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* StandaloneProducer extension that allows request reply functionality within a service
*
* @config standalone-requestor
*/
@JacksonXmlRootElement(localName = "standalone-requestor")
@XStreamAlias("standalone-requestor")
@AdapterComponent
@ComponentProfile(summary = "Perform a synchronous request operation, storing the reply in the message", tag = "service")
@DisplayOrder(order = {"connection", "producer", "replyTimeout"})
public class StandaloneRequestor extends StandaloneProducer {

private static final long DEFAULT_TIMEOUT = -1L;
private TimeInterval replyTimeout;

public StandaloneRequestor() {
super();
}

public StandaloneRequestor(AdaptrisMessageProducer p) {
super(p);
}

public StandaloneRequestor(AdaptrisConnection c, AdaptrisMessageProducer p) {
super(c, p);
}

public StandaloneRequestor(AdaptrisConnection c, AdaptrisMessageProducer p, TimeInterval timeout) {
super(c, p);
setReplyTimeout(timeout);
}

@Override
public void doService(AdaptrisMessage m) throws ServiceException {
try {
AdaptrisMessage reply;

if (timeoutOverrideMs() == -1L) {
reply = getProducer().request(m);
}
else {
reply = getProducer().request(m, timeoutOverrideMs());
}
// It should be the case that RequestReplyProducerImp
// now enforces the return type to be the same object that was passed in
// I suppose we can't guarantee that ppl haven't implemented their
// own.

if (reply != null && m != null && reply != m) {
log.trace("Copying reply message into original message");
copy(reply, m);
}
}
catch (Exception e) {
throw ExceptionHelper.wrapServiceException(e);
}
}

private void copy(AdaptrisMessage src, AdaptrisMessage dest) throws Exception {
dest.setContentEncoding(src.getContentEncoding());
MessageHelper.copyPayload(src, dest);
dest.getObjectHeaders().putAll(src.getObjectHeaders());
// Well the thing we shouldn't need to do is set the unique Id I guess.
//
dest.setUniqueId(src.getUniqueId());
for (Object md : src.getMetadata()) {
dest.addMetadata((MetadataElement) md);
}
for (Object marker : src.getMessageLifecycleEvent().getMleMarkers()) {
dest.getMessageLifecycleEvent().addMleMarker((MleMarker) marker);
}
}

long timeoutOverrideMs() {
return TimeInterval.toMillisecondsDefaultIfNull(getReplyTimeout(), DEFAULT_TIMEOUT);
}

public TimeInterval getReplyTimeout() {
return replyTimeout;
}

/**
* Set the timeout override for this request.
*
* @param timeoutOverride the override, default is -1, which will use the underlying producers default timeout.
*/
public void setReplyTimeout(TimeInterval timeoutOverride) {
replyTimeout = timeoutOverride;
}
}
