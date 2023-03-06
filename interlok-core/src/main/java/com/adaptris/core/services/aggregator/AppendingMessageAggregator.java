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

package com.adaptris.core.services.aggregator;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import org.apache.commons.io.IOUtils;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.NoArgsConstructor;

/**
* Implementation of {@link MessageAggregator} that just appends payloads.
*
* <p>
* This simply iterates over each of the messages; and appends their payloads to the original message. No checking is done of the
* payloads; it is a raw append using a stream copy.
* </p>
*
* @config appending-message-aggregator
*/
@JacksonXmlRootElement(localName = "appending-message-aggregator")
@XStreamAlias("appending-message-aggregator")
@ComponentProfile(summary = "MessageAggregator that appends all payloads to the original", since = "3.9.1")
@DisplayOrder(order = {"overwriteMetadata"})
@NoArgsConstructor
public class AppendingMessageAggregator extends MessageAggregatorImpl {

@Override
public void joinMessage(AdaptrisMessage orig, Collection<AdaptrisMessage> toAggregate) throws CoreException {
aggregate(orig, toAggregate);
}

@Override
public void aggregate(AdaptrisMessage orig, Iterable<AdaptrisMessage> msgs)
throws CoreException {
try (OutputStream out = orig.getOutputStream()) {
try (InputStream in = orig.getInputStream()) {
IOUtils.copy(in, out);
}
for (AdaptrisMessage m : msgs) {
if (filter(m)) {
try (InputStream subIn = m.getInputStream()) {
IOUtils.copy(subIn, out);
}
overwriteMetadata(m, orig);
}
}
} catch (Exception e) {
throw ExceptionHelper.wrapCoreException(e);
}
}

}
