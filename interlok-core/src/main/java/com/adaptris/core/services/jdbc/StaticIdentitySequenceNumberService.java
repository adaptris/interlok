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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* Implementation of AbstractJdbcSequenceNumberService where the identity is statically configured.
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
* @config jdbc-sequence-number-service
* @author lchan
*
*/
@JacksonXmlRootElement(localName = "jdbc-sequence-number-service")
@XStreamAlias("jdbc-sequence-number-service")
@AdapterComponent
@ComponentProfile(summary = "Create a sequence number using a database, the sequence number is associated with a static value",
tag = "service,metadata,jdbc,sequence")
@DisplayOrder(order = {"connection", "metadataKey", "identify", "numberFormat", "selectStatement", "updateStatement",
"insertStatement", "resetStatement", "alwaysReplaceMetadata"})
public class StaticIdentitySequenceNumberService extends AbstractJdbcSequenceNumberService {

private String identity;

public String getIdentity() {
return identity;
}

/**
* Set the identity that will be used as part of the standard SQL statements.
*
* @param id the identiy, the default is null which means that SQL statements are assumed to not require parameters.
*/
public void setIdentity(String id) {
identity = id;
}

@Override
protected String getIdentity(AdaptrisMessage msg) {
return identity;
}

}
