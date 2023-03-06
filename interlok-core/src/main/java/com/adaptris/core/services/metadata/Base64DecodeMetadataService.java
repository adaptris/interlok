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

package com.adaptris.core.services.metadata;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* Base64 decods an item of metadata.
* <p>
* Each matching metadata key from {@link ReformatMetadata#getMetadataKeyRegexp()} will be decoded, and the value overwitten with
* the bytes after translating it into a String with the specified {@link AdaptrisMessage#getContentEncoding()}
* </p>
*
* @config metadata-base64-decode
*
*/
@JacksonXmlRootElement(localName = "metadata-base64-decode")
@XStreamAlias("metadata-base64-decode")
@AdapterComponent
@ComponentProfile(summary = "Base64 decode an item of metadata", tag = "service,metadata,base64")
@DisplayOrder(order = {"metadataKeyRegexp", "metadataLogger"})
public class Base64DecodeMetadataService extends Base64MetadataService {

public Base64DecodeMetadataService() {
super();
}

public Base64DecodeMetadataService(String regexp) {
super(regexp);
}

@Override
public String reformat(String s, String charEncoding) throws Exception {
return toString(style().decoder().decode(s), charEncoding);
}

private String toString(byte[] metadataValue, String charset) throws UnsupportedEncodingException {
Charset cset = Charset.defaultCharset();
if (!isEmpty(charset)) {
cset = Charset.forName(charset);
}
return cset.decode(ByteBuffer.wrap(metadataValue)).toString();
}

}
