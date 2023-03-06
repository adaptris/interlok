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

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.IdGenerator;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* Service implementation that generates a unique item of metadata.
*
* <p>
* Uses the configured {@link IdGenerator} instance to generate a unique value that is subsequently assigned to metadata. Note that
* this is not designed to replace the unique-id that is associated with an AdaptrisMessage, but is intended to be an additional way
* for you to generate unique ids that can be associated with a message.
* </p>
*
* @config generate-unique-metadata-value-service
*
*/
@JacksonXmlRootElement(localName = "generate-unique-metadata-value-service")
@XStreamAlias("generate-unique-metadata-value-service")
@AdapterComponent
@ComponentProfile(summary = "Generate a unique value and attach it as metadata", tag = "service,metadata")
@DisplayOrder(order = {"metadataKey", "generator", "metadataLogger"})
public class GenerateUniqueMetadataValueService extends MetadataServiceImpl {

@AffectsMetadata
private String metadataKey;
@NotNull
@AutoPopulated
private IdGenerator generator;

public GenerateUniqueMetadataValueService() {
this(null, new GuidGenerator());
}

public GenerateUniqueMetadataValueService(String metadataKey) {
this(metadataKey, new GuidGenerator());
}

public GenerateUniqueMetadataValueService(String metadataKey, IdGenerator generator) {
setMetadataKey(metadataKey);
setGenerator(generator);
}

@Override
public void doService(AdaptrisMessage msg) throws ServiceException {
String metadataKey = metadataKey(msg);
String metadataValue = getGenerator().create(msg);
MetadataElement e = new MetadataElement(metadataKey, metadataValue);
msg.addMetadata(e);
logMetadata("Added {}", e);
}

/**
* <p>
* Returns the metadata key whose value should be checked.
* </p>
*
* @return metadataKey the metadata key whose value should be checked
*/
public String getMetadataKey() {
return metadataKey;
}

/**
* Sets the metadata key whose which will store the new value.
*
* @param s the metadata key; if set to null, then a unique-key will be generated using the configured
*          {@link #setGenerator(IdGenerator)}.
*/
public void setMetadataKey(String s) {
metadataKey = s;
}

String metadataKey(AdaptrisMessage msg) {
return isEmpty(getMetadataKey()) ? getGenerator().create(msg) : getMetadataKey();
}

public IdGenerator getGenerator() {
return generator;
}

/**
* Set the generator to be used.
*
* @param idg the generator; default is {@link GuidGenerator}
*/
public void setGenerator(IdGenerator idg) {
generator = Args.notNull(idg, "generator");
}

}
