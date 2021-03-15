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


/**
 * <p>
 * Constants used in this package.
 * </p>
 */
public final class JmsConstants {

  /**
   * Key used to store the JMSCorrelationID header property.
   *
   */
  public static final String JMS_CORRELATION_ID = "JMSCorrelationID";

  /**
   * <p>
   * Key used to store JMS reply to <code>javax.jms.Destination</code> against
   * as AdaptrisMessage <code>Object</code> metadata.
   * </p>
   */
  public static final String OBJ_JMS_REPLY_TO_KEY = "JMSReplyTo";

  /**
   * Key used to store the JMSType header property.
   *
   */
  public static final String JMS_TYPE = "JMSType";

  /**
   * Key used to store the JMSTimestamp header property.
   *
   */
  public static final String JMS_TIMESTAMP = "JMSTimestamp";

  /**
   * Key used to store the JMSReplyTo header property.
   *
   */
  public static final String JMS_REPLY_TO = OBJ_JMS_REPLY_TO_KEY;

  /**
   * Key used to store the JMSRedelivered header property.
   *
   */
  public static final String JMS_REDELIVERED = "JMSRedelivered";

  /**
   * Key used to store the JMSPriority header property.
   *
   */
  public static final String JMS_PRIORITY = "JMSPriority";

  /**
   * Key used to store the JMSMessageID header property.
   *
   */
  public static final String JMS_MESSAGE_ID = "JMSMessageID";

  /**
   * Key used to store the JMSExpiration header property.
   * <p>
   * If this metadata key is used, then the JMSExpiration should either be <br />
   * <ol>
   * <li>A long value that defines the time in milliseconds at which the message
   * expires.</li>
   * <li>A String in the format "yyyy-MM-dd'T'HH:mm:ssZ"</li>
   * </ol>
   * <br />
   * This resulting date be used to calculate the appropriate time to live.
   * </p>
   */
  public static final String JMS_EXPIRATION = "JMSExpiration";

  /**
   * Key used to store the JMSDestination header property.
   *
   */
  public static final String JMS_DESTINATION = "JMSDestination";

  /**
   * Key used to store the JMSDeliveryMode header property.
   *
   */
  public static final String JMS_DELIVERY_MODE = "JMSDeliveryMode";

  /**
   * Key used to store a specific reply to when using a producer.
   * <p>
   * If this metadata key is populated, then it will be used to create a JMSReplyTo whenever a message is produced to JMS.
   * </p>
   * 
   */
  public static final String JMS_ASYNC_STATIC_REPLY_TO = "JMSAsyncStaticReplyTo";

}
