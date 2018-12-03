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

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.BooleanUtils;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Translates between <code>AdaptrisMessage</code> and <code>javax.jms.MapMessage</code>.
 * </p>
 * 
 * @config map-message-translator
 * 
 */
@XStreamAlias("map-message-translator")
@DisplayOrder(order = {"keyForPayload", "treatMetadataAsPartOfMessage", "metadataFilter", "moveMetadata", "moveJmsHeaders",
    "reportAllErrors"})
public final class MapMessageTranslator extends MessageTypeTranslatorImp {

  @NotNull
  @NotBlank
  private String keyForPayload;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean treatMetadataAsPartOfMessage;

  public MapMessageTranslator() {
    super();
    setTreatMetadataAsPartOfMessage(false);
  }

  public MapMessageTranslator(String key) {
    this();
    setKeyForPayload(key);
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
    try {
      Args.notBlank(getKeyForPayload(), "keyForPayload");
      super.init();
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  /**
   * <p>
   * Translates an {@link com.adaptris.core.AdaptrisMessage} into a MapMessage.
   * </p>
   *
   * @param msg the <code>AdaptrisMessage</code> to translate
   * @return a new <code>MapMessage</code>
   * @throws JMSException
   */
  public Message translate(AdaptrisMessage msg) throws JMSException {
    MapMessage jmsMsg = session.createMapMessage();
    jmsMsg.setString(getKeyForPayload(), msg.getContent());
    if (treatMetadataAsPartOfMessage()) {
      Set metadata = msg.getMetadata();
      for (Iterator itr = metadata.iterator(); itr.hasNext();) {
        MetadataElement element = (MetadataElement) itr.next();
        if (!isReserved(element.getKey())) {
          jmsMsg.setString(element.getKey(), element.getValue());
        }
      }
    }
    return helper.moveMetadata(msg, jmsMsg);

  }

  /**
   * <p>
   * Translates a MapMessage into an {@link com.adaptris.core.AdaptrisMessage}.
   * </p>
   *
   * @param msg the <code>MapMessage</code> to translate
   * @return an <code>AdaptrisMessage</code>
   * @throws JMSException
   */
  public AdaptrisMessage translate(Message msg) throws JMSException {
    MapMessage jmsMsg = (MapMessage) msg;
    AdaptrisMessage result = currentMessageFactory().newMessage(jmsMsg.getString(getKeyForPayload()));
    Enumeration e = jmsMsg.getMapNames();
    while (e.hasMoreElements()) {
      String mapName = (String) e.nextElement();
      if (!mapName.equals(getKeyForPayload())) {
        result.addMetadata(mapName, jmsMsg.getString(mapName));
      }
    }
    return helper.moveMetadata(msg, result);
  }

  /**
   * @return the keyForPayload
   * @see #setKeyForPayload(String)
   */
  public String getKeyForPayload() {
    return keyForPayload;
  }

  /**
   * Specify the key of the map message that will be associated with the AdaptrisMessage Payload.
   * <p>
   * Other keys will be added as metadata when translating to AdaptrisMessage; behaviour when
   * translating from AdaptrisMessage is defined by
   * {@link #setTreatMetadataAsPartOfMessage(Boolean)}.
   * </p>
   * 
   * @param s the keyForPayload to set
   */
  public void setKeyForPayload(String s) {
    keyForPayload = Args.notBlank(s, "keyForPayload");
  }


  public Boolean getTreatMetadataAsPartOfMessage() {
    return treatMetadataAsPartOfMessage;
  }

  /**
   * When translating from AdaptrisMessage to MapMessage treat AdaptrisMessage metadata as part of the MapMessage.
   * <p>
   * Setting this to true does not mean that {@link #setMetadataFilter(com.adaptris.core.metadata.MetadataFilter)} is ignored;
   * things may still be added as JMS Properties
   * </p>
   *
   * @param b true to force AdaptrisMessage metadata to be treated as part of the MapMessage rather as standard JMS properties.
   */
  public void setTreatMetadataAsPartOfMessage(Boolean b) {
    treatMetadataAsPartOfMessage = b;
  }

  boolean treatMetadataAsPartOfMessage() {
    return BooleanUtils.toBooleanDefaultIfNull(getTreatMetadataAsPartOfMessage(), false);
  }
}
