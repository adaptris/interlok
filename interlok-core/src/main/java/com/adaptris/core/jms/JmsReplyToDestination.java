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

import static org.apache.commons.lang3.StringUtils.isEmpty;
import javax.jms.Destination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MessageDrivenDestination;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>ProduceDestination</code> which resolves the JMS Destination from the JMSReplyTo object metadata.
 * </p>
 * 
 * @config jms-reply-to-destination
 */
@XStreamAlias("jms-reply-to-destination")
@DisplayOrder(order = {"objectMetadataKey"})
public class JmsReplyToDestination implements MessageDrivenDestination {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @AdvancedConfig(rare = true)
  private String objectMetadataKey;

  // separator char here if required

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public JmsReplyToDestination() {
  }

  /**
   * Creates a new <code>ProduceDestination</code> based on object metadata
   * <p>
   * As this uses object metadata, the toString() method is called on the object
   * that is returned from metadata
   * </p>
   *
   * @param msg the message to process
   * @return the <code>String</code> destination name
   * @throws CoreException wrapping any underlying <code>Exception</code>s
   */
  @Override
  public String getDestination(AdaptrisMessage msg) throws CoreException {

    Object o = retrieveJmsDestination(msg);
    return o != null ? o.toString() : null;
  }

  /**
   *
   * Gets the JMS Destination as stored in object metadata.
   * @return The JMS Destination object
   * @param msg the AdaptrisMessage
   */
  public Destination retrieveJmsDestination(AdaptrisMessage msg)
      throws CoreException {
    Object result = null;
    String keyToUse = deriveMetadataKey();
    if (msg.getObjectHeaders().containsKey(keyToUse)) {
      result = msg.getObjectHeaders().get(keyToUse);
    }
    else {
      log.warn(keyToUse + " not found in object metadata");
    }
    log.debug("Destination [{}]", result);
    return (Destination) result;
  }

  private String deriveMetadataKey() {
    return isEmpty(getObjectMetadataKey()) ? JmsConstants.OBJ_JMS_REPLY_TO_KEY : getObjectMetadataKey();
  }

  public String getObjectMetadataKey() {
    return objectMetadataKey;
  }

  /**
   * Set the object metadata key that will be used to derive the destination.
   *
   * @param objectMetadataKey
   * @see JmsConstants#OBJ_JMS_REPLY_TO_KEY
   */
  public void setObjectMetadataKey(String objectMetadataKey) {
    this.objectMetadataKey = objectMetadataKey;
  }
}
