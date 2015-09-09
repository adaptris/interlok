package com.adaptris.core.jms;

import static com.adaptris.core.jms.MetadataHandler.isReserved;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Translates between <code>AdaptrisMessage</code> and <code>javax.jms.MapMessage</code>.
 * </p>
 * 
 * @config map-message-translator
 * @license BASIC
 */
@XStreamAlias("map-message-translator")
public final class MapMessageTranslator extends MessageTypeTranslatorImp {

  @NotNull
  @NotBlank
  private String keyForPayload;
  @AdvancedConfig
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
    if (getKeyForPayload() == null || "".equals(getKeyForPayload())) {
      throw new CoreException("key for the payload == null");
    }
    super.init();
  }

  /**
   * <p>
   * Translates an {@link AdaptrisMessage} into a MapMessage.
   * </p>
   *
   * @param msg the <code>AdaptrisMessage</code> to translate
   * @return a new <code>MapMessage</code>
   * @throws JMSException
   */
  public Message translate(AdaptrisMessage msg) throws JMSException {
    MapMessage jmsMsg = session.createMapMessage();
    jmsMsg.setString(getKeyForPayload(), msg.getStringPayload());
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
   * Translates a MapMessage into an {@link AdaptrisMessage}.
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
    keyForPayload = s;
  }


  public Boolean getTreatMetadataAsPartOfMessage() {
    return treatMetadataAsPartOfMessage;
  }

  /**
   * When translating from AdaptrisMessage to MapMessage treat AdaptrisMessage metadata as part of the MapMessage.
   * <p>
   * Setting this to true does not means that {@link #setMoveMetadata(Boolean)} is ignored; if that field is set to true, then all
   * metadata is still added as JMS Properties
   * </p>
   *
   * @param b true to force AdaptrisMessage metadata to be treated as part of the MapMessage rather as standard JMS properties.
   */
  public void setTreatMetadataAsPartOfMessage(Boolean b) {
    treatMetadataAsPartOfMessage = b;
  }

  boolean treatMetadataAsPartOfMessage() {
    return getTreatMetadataAsPartOfMessage() != null ? getTreatMetadataAsPartOfMessage().booleanValue() : false;
  }
}
