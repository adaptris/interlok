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

import static com.adaptris.util.text.mime.MimeConstants.HEADER_CONTENT_ENCODING;
import static com.adaptris.util.text.mime.MimeConstants.HEADER_CONTENT_TYPE;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeUtility;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.text.mime.MultiPartOutput;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* {@link MessageAggregator} implementation that creates a new mime part for each message that needs
* to be joined up.
*
* <p>
* The pre-split message is always treated as the first part of the resulting multipart message; the
* payloads from the split messages form the second and subsequent parts. f an explicit Content-Id
* (expression or otherwise) has been specified then this will be resolved and used as that parts
* {@code Content-ID} otherwise the split message's unique-id will be used. If the same
* {@code Content-ID} is re-used for multiple split messages then results are undefined. The most
* likely situation is that parts will be lost and only one preserved.
* </p>
* <p>
* Note that the first part's <code>Content-Id</code> will always be the original messages
* unique-id. Also, if the original message was a Multipart message, then this will be added as a
* single part to the resulting multipart message (giving you a nested multipart as the first part).
* </p>
* <p>
* As a result of this join operation, the message will be marked as MIME encoded using
* {@link com.adaptris.core.CoreConstants#MSG_MIME_ENCODED} metadata.
* </p>
*
* @config mime-aggregator
* @see CoreConstants#MSG_MIME_ENCODED
* @author lchan
*
*/
@JacksonXmlRootElement(localName = "mime-aggregator")
@XStreamAlias("mime-aggregator")
@DisplayOrder(order = {"encoding", "mimeContentSubType", "mimeHeaderFilter", "overwriteMetadata",
"partContentId", "partContentType", "partHeaderFilter"})
@ComponentProfile(summary = "Aggregator implementation that creates a new mime part for each message that needs to be joined up")
public class MimeAggregator extends MessageAggregatorImpl {

private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
private static final String DEFAULT_SUB_TYPE = "mixed";

@Pattern(regexp = "base64|quoted-printable|uuencode|x-uuencode|x-uue|binary|7bit|8bit")
@AdvancedConfig
private String encoding;
@InputFieldHint(expression = true)
@InputFieldDefault(value = DEFAULT_SUB_TYPE)
private String mimeContentSubType;

@AdvancedConfig
@InputFieldHint(expression = true)
@InputFieldDefault(value = "built from the appropriate message id")
private String partContentId;
@AdvancedConfig(rare = true)
@InputFieldHint(expression = true)
@InputFieldDefault(value = DEFAULT_CONTENT_TYPE)
private String partContentType;
@Valid
@InputFieldDefault(value = "RemoveAllMetadata")
@AdvancedConfig(rare = true)
private MetadataFilter partHeaderFilter;
@Valid
@InputFieldDefault(value = "RemoveAllMetadata")
@AdvancedConfig(rare = true)
private MetadataFilter mimeHeaderFilter;

@Override
public void joinMessage(AdaptrisMessage original, Collection<AdaptrisMessage> messages) throws CoreException {
aggregate(original, messages);
}

@Override
public void aggregate(AdaptrisMessage original, Iterable<AdaptrisMessage> msgs)
throws CoreException {
try {
MultiPartOutput output = createInitialPart(original);
for (AdaptrisMessage m : msgs) {
if (filter(m)) {
output.addPart(createBodyPart(m), contentId(m));
overwriteMetadata(m, original);
}
}
try (OutputStream out = original.getOutputStream()) {
output.writeTo(out);
}
original.addMetadata(CoreConstants.MSG_MIME_ENCODED, Boolean.TRUE.toString());
} catch (Exception e) {
throw ExceptionHelper.wrapCoreException(e);
}
}

protected MimeBodyPart createBodyPart(AdaptrisMessage msg) throws MessagingException, IOException {
InternetHeaders hdrs = new InternetHeaders();
byte[] encodedData = encodeData(msg.getPayload(), getEncoding(), hdrs);
hdrs.addHeader(HEADER_CONTENT_TYPE, contentType(msg));
MetadataCollection metadata = partHeaderFilter().filter(msg);
metadata.forEach((e) -> {
hdrs.addHeader(e.getKey(), e.getValue());
});
return new MimeBodyPart(hdrs, encodedData);
}

protected MultiPartOutput createInitialPart(AdaptrisMessage original) throws MessagingException, IOException {
MultiPartOutput output =
new MultiPartOutput(original.getUniqueId(), mimeContentSubType(original));
MetadataCollection metadata = mimeHeaderFilter().filter(original);
metadata.forEach((e) -> {
output.setHeader(e.getKey(), e.getValue());
});
output.addPart(createBodyPart(original), contentId(original));
return output;
}

/**
* @return the encoding
*/
public String getEncoding() {
return encoding;
}

/**
* Set the encoding to be used for the content.
*
* @param s the encoding to set, defaults to no-encoding (null)
*/
public void setEncoding(String s) {
encoding = s;
}

private static byte[] encodeData(byte[] data, String encoding, InternetHeaders hdrs) throws MessagingException, IOException {
if (!isBlank(encoding)) {
hdrs.setHeader(HEADER_CONTENT_ENCODING, encoding);
}
try (ByteArrayOutputStream out = new ByteArrayOutputStream(); OutputStream encodedOut = MimeUtility.encode(out, encoding)) {
encodedOut.write(data);
return out.toByteArray();
}
}

public String getMimeContentSubType() {
return mimeContentSubType;
}

public void setMimeContentSubType(String s) {
mimeContentSubType = s;
}

public String getPartContentId() {
return partContentId;
}

/**
* Set the content type for each part.
*
* @param s the content id; supports the {@code %message{}} syntax to resolve metadata.
*/
public void setPartContentId(String s) {
partContentId = s;
}

public String getPartContentType() {
return partContentType;
}

/**
* Set the content-type for each part.
*
* @param s the content-type; supports the {@code %message{}} syntax to resolve metadata.
*/
public void setPartContentType(String s) {
partContentType = s;
}

public MetadataFilter getPartHeaderFilter() {
return partHeaderFilter;
}

/**
* Set a metadata filter which will be applied to generate headers for each part.
*
* @param filter the filter; defaults to {@link RemoveAllMetadataFilter} if not
*        specified.
*/
public void setPartHeaderFilter(MetadataFilter filter) {
partHeaderFilter = filter;
}


public MetadataFilter getMimeHeaderFilter() {
return mimeHeaderFilter;
}

/**
* Set a metadata filter which be applied to generate the root level mime headers.
*
* @param filter
*/
public void setMimeHeaderFilter(MetadataFilter filter) {
mimeHeaderFilter = filter;
}

public <T extends MimeAggregator> T withPartHeaderFilter(MetadataFilter filter) {
setPartHeaderFilter(filter);
return (T) this;
}

public <T extends MimeAggregator> T withMimeHeaderFilter(MetadataFilter filter) {
setMimeHeaderFilter(filter);
return (T) this;
}


public <T extends MimeAggregator> T withMimeContentSubType(String s) {
setMimeContentSubType(s);
return (T) this;
}

public <T extends MimeAggregator> T withPartContentId(String s) {
setPartContentId(s);
return (T) this;
}

public <T extends MimeAggregator> T withPartContentType(String s) {
setPartContentType(s);
return (T) this;
}

public <T extends MimeAggregator> T withEncoding(String s) {
setEncoding(s);
return (T) this;
}

protected MetadataFilter partHeaderFilter() {
return ObjectUtils.defaultIfNull(getPartHeaderFilter(), new RemoveAllMetadataFilter());
}

protected MetadataFilter mimeHeaderFilter() {
return ObjectUtils.defaultIfNull(getMimeHeaderFilter(), new RemoveAllMetadataFilter());
}

protected String mimeContentSubType(AdaptrisMessage msg) {
String sub = msg.resolve(getMimeContentSubType());
return defaultIfBlank(sub, DEFAULT_SUB_TYPE);
}

protected String contentType(AdaptrisMessage msg) {
return defaultIfBlank(msg.resolve(getPartContentType()), DEFAULT_CONTENT_TYPE);
}

protected String contentId(AdaptrisMessage msg) {
return defaultIfBlank(msg.resolve(getPartContentId()), msg.getUniqueId());
}


}
