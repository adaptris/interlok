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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* Takes a metadata value and converts it to lower case.
* <p>
* Each matching metadata key from {@link ReformatMetadata#getMetadataKeyRegexp()} will be changed to lowercase
* </p>
*
* @config metadata-value-to-lower-case
*
*
*/
@JacksonXmlRootElement(localName = "metadata-value-to-lower-case")
@XStreamAlias("metadata-value-to-lower-case")
@AdapterComponent
@ComponentProfile(summary = "Changes matching metadata into lowercase", tag = "service,metadata")
@DisplayOrder(order = {"metadataKeyRegexp", "metadataLogger"})
public class MetadataValueToLowerCase extends ReformatMetadata {

public MetadataValueToLowerCase() {
super();
}

@Override
public String reformat(String toChange, String msgCharset) {
return toChange.toLowerCase();
}

}
