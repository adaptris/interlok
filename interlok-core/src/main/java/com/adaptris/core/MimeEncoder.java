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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.text.mime.BodyPartIterator;
import com.adaptris.util.text.mime.MultiPartOutput;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* <p>
* Implementation of <code>AdaptrisMessageEncoder</code> that stores <code>AdaptrisMessage</code> payload and metadata as a
* mime-encoded multipart message.
* </p>
* <p>
* The metadata is treated as a though it were a set of Properties, therefore using = as part of either the metadata key or data
* makes the behaviour undefined.
* </p>
* <p>
* By default the data is not encoded, however this behaviour can be overriden through use of the <code>setMetadataEncoding()</code>
* and <code>setPayloadEncoding()</code> methods.
* </p>
*
* @config mime-encoder
*/
@JacksonXmlRootElement(localName = "mime-encoder")
@XStreamAlias("mime-encoder")
@DisplayOrder(order = {"payloadEncoding", "metadataEncoding", "retainUniqueId"})
public class MimeEncoder extends MimeEncoderImpl<OutputStream, InputStream> {

public MimeEncoder() {
super();
}

public MimeEncoder(Boolean retainUniqueId, String metadataEncoding, String payloadEncoding) {
this();
setRetainUniqueId(retainUniqueId);
setMetadataEncoding(metadataEncoding);
setPayloadEncoding(payloadEncoding);
}

@Override
public void writeMessage(AdaptrisMessage msg, OutputStream target) throws CoreException {
try {
// Use the message unique id as the message id.
MultiPartOutput output = new MultiPartOutput(msg.getUniqueId());
output.addPart(payloadAsMimePart(msg), PAYLOAD_CONTENT_ID);
output.addPart(getMetadata(msg), getMetadataEncoding(), METADATA_CONTENT_ID);
if (msg.getObjectHeaders().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION)) {
output.addPart(asMimePart((Exception) msg.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION)),
EXCEPTION_CONTENT_ID);
}
output.writeTo(target);
target.flush();
} catch (Exception e) {
throw ExceptionHelper.wrapCoreException(e);
}
}

@Override
public AdaptrisMessage readMessage(InputStream source) throws CoreException {

try {
AdaptrisMessage msg = currentMessageFactory().newMessage();
BodyPartIterator input = new BodyPartIterator(source);
addPartsToMessage(input, msg);
return msg;
} catch (Exception e) {
throw ExceptionHelper.wrapCoreException(e);
}
}

/**
* Convenience method that is available so that existing underlying
* implementations are not broken due to the AdaptrisMessageEncoder interface
* change.
*
* @param msg the message to encode as a byte array.
*/
public byte[] encode(AdaptrisMessage msg) throws CoreException {
ByteArrayOutputStream out = new ByteArrayOutputStream();
writeMessage(msg, out);
return out.toByteArray();
}

/**
* Convenience method that is available so that existing underlying
* implementations are not broken due to the AdaptrisMessageEncoder interface
* change.
*
* @param bytes the bytes to decode.
* @return the AdaptrisMessage.
* @throws CoreException wrapping any underyling exception.
*/
public AdaptrisMessage decode(byte[] bytes) throws CoreException {
try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
return readMessage(in);
} catch (Exception e) {
throw new CoreException(e);
}
}
}
