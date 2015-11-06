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

package com.adaptris.core.jms;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import javax.jms.JMSException;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ProducerSessionFactory} that creates a new session/producer based on message size.
 * 
 * <p>
 * This implementaton refreshes the session based whether an item of metadata evaluates to 'true'
 * </p>
 * 
 * @config jms-metadata-producer-session
 * @license STANDARD
 * @author lchan
 * 
 */
@XStreamAlias("jms-metadata-producer-session")
public class MetadataProducerSessionFactory extends ProducerSessionFactoryImpl {

  private static final String DEFAULT_METADATA_KEY = "newJmsSession";
  @NotBlank
  private String metadataKey;

  public MetadataProducerSessionFactory() {
    super();
    setMetadataKey(DEFAULT_METADATA_KEY);
  }

  public MetadataProducerSessionFactory(String key) {
    this();
    setMetadataKey(key);
  }


  @Override
  public ProducerSession createProducerSession(JmsProducerImpl producer, AdaptrisMessage msg)
      throws JMSException {
    boolean newSession = newSessionRequired(msg);
    if (newSession || session == null) {
      if (newSession) log.trace("Metadata Key {} is true, new Session", getMetadataKey());
      closeQuietly(session);
      session = createProducerSession(producer);
    }
    return session;
  }

  boolean newSessionRequired(AdaptrisMessage msg) {
    return msg.containsKey(getMetadataKey())
        && Boolean.valueOf(defaultIfEmpty(msg.getMetadataValue(getMetadataKey()), "false")).booleanValue();
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * Set the metadata key which will cause a session to be refreshed.
   * 
   * @param key the metadata key, defaults to 'newJmsSession'
   */
  public void setMetadataKey(String key) {
    this.metadataKey = Args.notEmpty(key, "metadata key");
  }

}
