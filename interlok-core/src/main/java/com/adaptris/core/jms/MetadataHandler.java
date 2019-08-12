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

import static com.adaptris.core.jms.JmsConstants.JMS_CORRELATION_ID;
import static com.adaptris.core.jms.JmsConstants.JMS_DELIVERY_MODE;
import static com.adaptris.core.jms.JmsConstants.JMS_DESTINATION;
import static com.adaptris.core.jms.JmsConstants.JMS_EXPIRATION;
import static com.adaptris.core.jms.JmsConstants.JMS_MESSAGE_ID;
import static com.adaptris.core.jms.JmsConstants.JMS_PRIORITY;
import static com.adaptris.core.jms.JmsConstants.JMS_REDELIVERED;
import static com.adaptris.core.jms.JmsConstants.JMS_REPLY_TO;
import static com.adaptris.core.jms.JmsConstants.JMS_TIMESTAMP;
import static com.adaptris.core.jms.JmsConstants.JMS_TYPE;
import static com.adaptris.core.jms.JmsConstants.MESSAGE_UNIQUE_ID_KEY;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;

/**
 * Class that abstracts the handling of AdaptrisMessage metadata and JMS Headers away from the MessageTypeTranslator.
 *
 * @author lchan
 * @author $Author: lchan $
 */
@SuppressWarnings("deprecation")
public class MetadataHandler {

  // List of Strings that correspond to the reserved JMS Properties.
  private static String[] RESERVED_JMS =
  {
      JMS_CORRELATION_ID, JMS_TYPE, JMS_TIMESTAMP, JMS_REPLY_TO, JMS_REDELIVERED, JMS_PRIORITY, JMS_MESSAGE_ID, JMS_EXPIRATION,
      JMS_DELIVERY_MODE, JMS_DESTINATION
  };

  private static final List<String> RESERVED_LIST = Arrays.asList(RESERVED_JMS);

  static enum JmsPropertyHandler {
    CorrelationId(JMS_CORRELATION_ID) {
      @Override
      String getValue(Message msg) throws JMSException {
        return msg.getJMSCorrelationID();
      }
    },

    Type(JMS_TYPE) {
      @Override
      String getValue(Message msg) throws JMSException {
        return msg.getJMSType();
      }

      @Override
      void copy(AdaptrisMessage in, Message out) throws JMSException {
        if (in.containsKey(getKey())) {
          String s = in.getMetadataValue(getKey());
          if (s != null && !"".equals(s)) {
            out.setJMSType(s);
          }
        }
      }
    },

    Timestamp(JMS_TIMESTAMP) {
      @Override
      String getValue(Message msg) throws JMSException {
        return "" + msg.getJMSTimestamp();
      }
    },
    ReplyTo(JMS_REPLY_TO) {
      @Override
      String getValue(Message in) throws JMSException {
        String result = "";
        if (in.getJMSReplyTo() != null) {
          if (in.getJMSReplyTo() instanceof javax.jms.Queue) {
            result = ((javax.jms.Queue) in.getJMSReplyTo()).getQueueName();
          }
          else if (in.getJMSReplyTo() instanceof javax.jms.Topic) {
            result = ((javax.jms.Topic) in.getJMSReplyTo()).getTopicName();
          }
        }
        return result;
      }
    },
    Redelivered(JMS_REDELIVERED) {
      @Override
      String getValue(Message msg) throws JMSException {
        return "" + msg.getJMSRedelivered();
      }
    },
    Priority(JMS_PRIORITY) {
      @Override
      String getValue(Message msg) throws JMSException {
        return "" + msg.getJMSPriority();
      }
    },
    MessageID(JMS_MESSAGE_ID) {
      @Override
      String getValue(Message msg) throws JMSException {
        return msg.getJMSMessageID();
      }
    },
    Expiration(JMS_EXPIRATION) {
      @Override
      String getValue(Message msg) throws JMSException {
        return "" + msg.getJMSExpiration();
      }
    },
    DeliveryMode(JMS_DELIVERY_MODE) {
      @Override
      String getValue(Message msg) throws JMSException {
        return "" + msg.getJMSDeliveryMode();
      }
    },
    Destination(JMS_DESTINATION) {
      @Override
      String getValue(Message msg) throws JMSException {
        String result = "";
        if (msg.getJMSDestination() instanceof javax.jms.Queue) {
          result = ((javax.jms.Queue) msg.getJMSDestination()).getQueueName();
        }
        else if (msg.getJMSDestination() instanceof javax.jms.Topic) {
          result = ((javax.jms.Topic) msg.getJMSDestination()).getTopicName();
        }
        return result;
      }
    };

    private String metadataKey;

    JmsPropertyHandler(String name) {
      metadataKey = name;
    }

    String getKey() {
      return metadataKey;
    }

    void copy(Message in, AdaptrisMessage out) throws JMSException {
      out.addMetadata(getKey(), getValue(in));
    }

    void copy(AdaptrisMessage in, Message out) throws JMSException {
      // Purposefully empty.
    }

    abstract String getValue(Message msg) throws JMSException;
  }

  private transient MetadataHandlerContext context;

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  public MetadataHandler(MetadataHandlerContext hdrContext) {
    context = hdrContext;
  }

  private void reportException(String header, Exception e) {
    if (context.reportAllErrors()) {
      log.trace("Ignoring non-critical error accessing " + header, e);
    }
    else {
      log.trace("Ignoring non-critical error accessing " + header);
    }
  }

  /**
   * <p>
   * Moves metadata from a <code>javax.jms.Message</code> to a <code>AdaptrisMessage</code> if <code>moveMetadata</code> is
   * <code>true</code>.
   * </p>
   *
   * @param in the JMS <code>Message</code> to move metadata from
   * @param out the <code>AdaptrisMessage</code> to move metadata to
   * @return the <code>AdaptrisMessage</code> with metadata added
   * @throws JMSException
   */
  public final AdaptrisMessage moveMetadata(Message in, AdaptrisMessage out) throws JMSException {
    if (!(context.metadataFilter() instanceof RemoveAllMetadataFilter)) {
      MetadataCollection metadata = createMetadataCollection(in);
      MetadataCollection filtered = context.metadataFilter().filter(metadata);
      for (MetadataElement e : filtered) {
        out.addMetadata(e);
      }
    }
    if (!isEmpty(in.getStringProperty(MESSAGE_UNIQUE_ID_KEY))) {
      out.setUniqueId(in.getStringProperty(MESSAGE_UNIQUE_ID_KEY));
    }
    if (context.moveJmsHeaders()) {
      this.moveJmsHeaders(in, out);
    }

    return out;
  }

  private MetadataCollection createMetadataCollection(Message in) throws JMSException {
    MetadataCollection result = new MetadataCollection();
    Enumeration<?> props = in.getPropertyNames();
    while (props.hasMoreElements()) {
      String key = (String) props.nextElement();
      String value = in.getStringProperty(key); // converts all to Strings
      if (!isEmpty(value)) {
        result.add(new MetadataElement(key, value));
      }
      else {
        log.debug("ignoring null or empty metadata value against key [" + key + "]");
      }
    }
    return result;
  }

  private void moveJmsHeaders(Message in, AdaptrisMessage out) throws JMSException {

    for (JmsPropertyHandler h : JmsPropertyHandler.values()) {
      try {
        h.copy(in, out);
      }
      catch (Exception e) {
        reportException(h.getKey(), e);
      }
    }
    out.addObjectHeader(JMS_DESTINATION, in.getJMSDestination());
    out.addObjectHeader(JMS_REPLY_TO, in.getJMSReplyTo());
  }

  /**
   * <p>
   * Moves metadata from an <code>AdaptrisMessage</code> to a <code>javax.jms.Message</code> if <code>moveMetadata</code> is
   * <code>true</code>.
   * </p>
   *
   * @param in the <code>AdaptrisMessage</code> to move metadata from
   * @param out the JMS <code>Message</code> to move metadata to
   * @return the JMS <code>Message</code> with metadata added
   * @throws JMSException
   */
  public final Message moveMetadata(AdaptrisMessage in, Message out) throws JMSException {

    MetadataCollection metadataCollection = context.metadataFilter().filter(in);
    if (context.metadataConverters().size() == 0){
      new StringMetadataConverter().moveMetadata(metadataCollection, out);
    } else {
      for (MetadataConverter converter : context.metadataConverters()) {
        converter.moveMetadata(metadataCollection, out);
      }
    }
    out.setStringProperty(MESSAGE_UNIQUE_ID_KEY, in.getUniqueId());
    if (context.moveJmsHeaders()) {
      moveJmsHeaders(in, out);
    }
    return out;
  }

  // Move JMS Headers from AdaptrisMessage to the JMS Message
  // Technically the only one we need to handle is JMSType...
  private void moveJmsHeaders(AdaptrisMessage in, Message out) throws JMSException {
    for (JmsPropertyHandler h : JmsPropertyHandler.values()) {
      try {
        h.copy(in, out);
      }
      catch (Exception e) {
        reportException(h.getKey(), e);
      }
    }
  }

  /**
   * Is this key a reserved jms header.
   *
   * @param key the key.
   * @return true if the key is a reserved JMS Header.
   */
  public static boolean isReserved(String key) {
    return RESERVED_LIST.contains(key);
  }
}
