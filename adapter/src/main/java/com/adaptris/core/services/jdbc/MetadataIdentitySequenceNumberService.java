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

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of AbstractJdbcSequenceNumberService where the identity is derived from metadata.
 * <p>
 * The default database schema that is assumed to be
 * 
 * <pre>
 * {@code 
 * CREATE TABLE sequences (id VARCHAR(255) NOT NULL, seq_number INT)
 * }
 * </pre>
 * The default SQL statements reflect this; and provided that a table called 'sequences' contains at least those two columns then it
 * should work without any changes to the SQL statements.
 * </p>
 * 
 * @config jdbc-metadata-sequence-number-service
 * 
 * @license STANDARD
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-metadata-sequence-number-service")
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
    if (!msg.containsKey(getIdentityMetadataKey())) {
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
