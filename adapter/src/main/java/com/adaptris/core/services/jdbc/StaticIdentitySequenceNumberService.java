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

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of AbstractJdbcSequenceNumberService where the identity is statically configured.
 * <p>
 * The default database schema that is assumed to be
 * </p>
 * 
 * <pre>
 * {@code CREATE TABLE sequences (id VARCHAR(255) NOT NULL, seq_number INT)}
 * </pre>
 * 
 * The default SQL statements reflect this; and provided that a table called 'sequences' contains at least those two columns then it
 * should work without any changes to the SQL statements. </p>
 * 
 * @config jdbc-sequence-number-service
 * 
 * 
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-sequence-number-service")
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
