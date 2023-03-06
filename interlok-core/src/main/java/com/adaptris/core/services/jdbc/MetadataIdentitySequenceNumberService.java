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

package com.adaptris.core.services.jdbc;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* Implementation of AbstractJdbcSequenceNumberService where the identity is derived from metadata.
* <p>
* The default database schema is assumed to be
*
* <pre>
* {@code
* CREATE TABLE SEQUENCES (ID VARCHAR(255) NOT NULL, SEQ_NUMBER INT)
* }
* </pre> The default SQL statements reflect this; and provided that a table called 'SEQUENCES' contains at least those two columns
* then it should work without any changes to the SQL statements. Be aware that all statements default to upper-case which will have
* an impact if your database is case-sensitive (such as MySQL on Linux).
* </p>
*
* @config jdbc-metadata-sequence-number-service
*
*
* @author lchan
*
*/
@JacksonXmlRootElement(localName = "jdbc-metadata-sequence-number-service")
@XStreamAlias("jdbc-metadata-sequence-number-service")
@AdapterComponent
@ComponentProfile(summary = "Create a sequence number using a database, the sequence number is associated with a metadata key",
tag = "service,metadata,jdbc,sequence")
@DisplayOrder(order = {"connection", "metadataKey", "identityMetadataKey", "numberFormat", "selectStatement", "updateStatement",
"insertStatement", "resetStatement", "alwaysReplaceMetadata"})
public class MetadataIdentitySequenceNumberService extends AbstractJdbcSequenceNumberService {

private static final String DEFAULT_IDENTITY_METADATA_KEY = "identity";
@NotNull
@AutoPopulated
@NotBlank
private String identityMetadataKey;

public MetadataIdentitySequenceNumberService() {
super();
setIdentityMetadataKey(DEFAULT_IDENTITY_METADATA_KEY);
}

@Override
public void doService(AdaptrisMessage msg) throws ServiceException {
super.doService(msg);
}

@Override
public String getIdentity(AdaptrisMessage msg) throws ServiceException {
if (!msg.headersContainsKey(getIdentityMetadataKey())) {
throw new ServiceException("Message does not contain identity metadata key - " + getIdentityMetadataKey());
}
return msg.getMetadataValue(getIdentityMetadataKey());
}

public String getIdentityMetadataKey() {
return identityMetadataKey;
}

/**
* Set the metadata key that contains the identity.
*
* @param identityMetadataKey the metadata key, default metadata key is 'identity'
*/
public void setIdentityMetadataKey(String identityMetadataKey) {
this.identityMetadataKey = identityMetadataKey;
}

}
