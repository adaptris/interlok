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

import static com.adaptris.core.jms.MetadataHandler.isReserved;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Perform a best guess routine on the JMSMessage when translating to an AdaptrisMessage.
 * <p>
 * This handles the types {@linkplain TextMessage}, {@linkplain BytesMessage}, {@linkplain ObjectMessage} by delegating to the
 * correct {@linkplain MessageTypeTranslator} implementation. The mapping from {@linkplain MapMessage} to AdaptrisMessage is
 * simplistic; the name value pairs (assumed to be String (or convertable to String)) are set as AdaptrisMessage metadata, the
 * resulting payload is empty. The mapping from AdaptrisMessage to {@linkplain MapMessage} results in all metadata being mapped as
 * name value pairs in the MapMessage; the payload is ignored.
 * </p>
 * <p>
 * If this converter cannot find an appropriate translator then a very basic translation will be applied. This will NOT include any
 * payload translation. You will also see a warning in the logs to notify you a basic translation has been applied.
 * </p>
 * <p>
 * It is primarily a fallback translator to mitigate configuration errors.
 * </p>
 * 
 * @config auto-convert-message-translator
 * @license BASIC
 * @author lchan
 */
@SuppressWarnings("deprecation")
@XStreamAlias("auto-convert-message-translator")
public class AutoConvertMessageTranslator extends MessageTypeTranslatorImp {

  /**
   * javax.jms.Message types that are supported by this translator.
   *
   *
   */
  public static enum SupportedMessageType {
    /** Represents {@linkplain javax.jms.TextMessage} */
    Text {
      @Override
      MessageTypeTranslatorImp create(MessageTypeTranslatorImp parent) throws JMSException {
        return configure(parent, new TextMessageTranslator());
      }

      @Override
      boolean isSupported(Message m) {
        return m instanceof TextMessage;
      }
    },
    /** Represents {@linkplain javax.jms.BytesMessage} */
    Bytes {
      @Override
      MessageTypeTranslatorImp create(MessageTypeTranslatorImp parent) throws JMSException {
        return configure(parent, new BytesMessageTranslator());
      }

      @Override
      boolean isSupported(Message m) {
        return m instanceof BytesMessage;
      }
    },
    /** Represents {@linkplain javax.jms.ObjectMessage} */
    Object {
      @Override
      MessageTypeTranslatorImp create(MessageTypeTranslatorImp parent) throws JMSException {
        return configure(parent, new ObjectMessageTranslator());
      }

      @Override
      boolean isSupported(Message m) {
        return m instanceof ObjectMessage;
      }
    },
    /** Represents {@linkplain javax.jms.MapMessage} */
    Map {
      @Override
      MessageTypeTranslatorImp create(MessageTypeTranslatorImp parent) throws JMSException {
        return configure(parent, new NaiveMapMessageTranslator());
      }

      @Override
      boolean isSupported(Message m) {
        return m instanceof MapMessage;
      }

    };

    abstract MessageTypeTranslatorImp create(MessageTypeTranslatorImp parent) throws JMSException;

    abstract boolean isSupported(Message m) throws JMSException;

    private static MessageTypeTranslatorImp configure(MessageTypeTranslatorImp source, MessageTypeTranslatorImp dest)
        throws JMSException {
      dest.setMoveJmsHeaders(source.getMoveJmsHeaders());
      dest.setReportAllErrors(source.getReportAllErrors());
      dest.setMoveMetadata(source.getMoveMetadata());
      dest.registerMessageFactory(source.currentMessageFactory());
      dest.registerSession(source.currentSession());
      dest.setMetadataFilter(source.getMetadataFilter());
      return dest;
    }

    public static MessageTypeTranslatorImp createFallback(MessageTypeTranslatorImp parent) throws JMSException {
      return configure(parent, new BasicJavaxJmsMessageTranslator());
    }
  }

  private String jmsOutputType;
  
  /**
   * Default constructor.
   * <p>
   * <ul>
   * <li>jms-output-type is "Text"</li>
   * </ul>
   */
  public AutoConvertMessageTranslator() {
    super();
    setJmsOutputType(SupportedMessageType.Text.name());
  }

  public Message translate(AdaptrisMessage msg) throws JMSException {
    Message result = null;
    MessageTypeTranslator mt = null;

    try {
      for (SupportedMessageType mti : SupportedMessageType.values()) {
        if (mti.name().equalsIgnoreCase(getJmsOutputType())) {
          mt = mti.create(this);
          break;
        }
      }
      if (mt != null) {
        start(mt);
        result = mt.translate(msg);
      }
      else {
        mt = SupportedMessageType.createFallback(this);
        start(mt);
        result = mt.translate(msg);
      }
    }
    finally {
      stop(mt);
    }
    return result;
  }

  public AdaptrisMessage translate(Message msg) throws JMSException {
    AdaptrisMessage result = null;
    MessageTypeTranslator mt = null;
    try {
      for (SupportedMessageType mti : SupportedMessageType.values()) {
        if (mti.isSupported(msg)) {
          mt = mti.create(this);
          break;
        }
      }
      if (mt != null) {
        start(mt);
        log.trace("Converting [" + msg.getClass().getSimpleName() + "] using [" + mt.getClass().getSimpleName() + "]");
        result = mt.translate(msg);
      }
    }
    finally {
      stop(mt);
    }
    if (result == null) {
      mt = SupportedMessageType.createFallback(this);
      start(mt);
      result = mt.translate(msg);
    }
    return result;
  }

  public String getJmsOutputType() {
    return jmsOutputType;
  }

  /**
   * Set the javax.jms.Message implementation that this MessageTranslator
   * creates when converting from AdaptrisMessage.
   *
   * @param outputType the output type, one of Object, Text, Bytes, Map
   * @see SupportedMessageType
   */
  public void setJmsOutputType(String outputType) {
    jmsOutputType = outputType;
  }


  private static class NaiveMapMessageTranslator extends MessageTypeTranslatorImp {

    public Message translate(AdaptrisMessage msg) throws JMSException {
      MapMessage jmsMsg = session.createMapMessage();
      Set metadata = msg.getMetadata();
      for (Iterator i = metadata.iterator(); i.hasNext();) {
        MetadataElement element = (MetadataElement) i.next();
        if (!isReserved(element.getKey())) {
          jmsMsg.setString(element.getKey(), element.getValue());
        }
      }
      return helper.moveMetadata(msg, jmsMsg);

    }

    public AdaptrisMessage translate(Message msg) throws JMSException {
      AdaptrisMessage result = currentMessageFactory().newMessage();
      MapMessage jmsMsg = (MapMessage) msg;
      for (Enumeration e = jmsMsg.getMapNames(); e.hasMoreElements();) {
        String mapName = (String) e.nextElement();
        result.addMetadata(mapName, jmsMsg.getString(mapName));
      }
      return helper.moveMetadata(msg, result);
    }
  }
}
