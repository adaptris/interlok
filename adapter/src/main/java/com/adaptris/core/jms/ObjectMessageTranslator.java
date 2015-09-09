package com.adaptris.core.jms;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Translates between <code>javax.jms.ObjectMessage</code>s and <code>AdaptrisMessage</code>s and vice versa.
 * </p>
 * 
 * @config object-message-translator
 * @license BASIC
 */
@XStreamAlias("object-message-translator")
public final class ObjectMessageTranslator extends MessageTypeTranslatorImp {

  /**
   * <p>
   * Translates by setting the <code>AdaptrisMessage.getPayload</code> as the
   * <code>Object</code> in the <code>ObjectMessage</code>.
   * </p>
   *
   * @param msg the <code>AdaptrisMessage</code> to translate
   * @return a new <code>ObjectMessage</code>
   * @throws JMSException
   */
  public Message translate(AdaptrisMessage msg) throws JMSException {
    ObjectMessage result = session.createObjectMessage();
    try {
      if (msg.getSize() > 0) {
        InputStream in = msg.getInputStream();
        ObjectInputStream object = new ObjectInputStream(in);
        result.setObject((Serializable) object.readObject());
        object.close();
        in.close();
      }
    }
    catch (Exception e) {
      JmsUtils.rethrowJMSException(e);
    }

    return helper.moveMetadata(msg, result);
  }

  /**
   * <p>
   * Translates a <code>ObjectMessage</code> into an
   * <code>AdaptrisMessage</code>.
   * </p>
   *
   * @param msg the <code>ObjectMessage</code> to translate
   * @return an <code>AdaptrisMessage</code>
   * @throws JMSException
   */
  public AdaptrisMessage translate(Message msg) throws JMSException {
    AdaptrisMessage result = currentMessageFactory().newMessage();
    Object payload = ((ObjectMessage) msg).getObject();
    try {
      if (payload != null) {
        OutputStream os = result.getOutputStream();
        ObjectOutputStream object = new ObjectOutputStream(os);
        object.writeObject(payload);
        object.close();
        os.close();
      }
    }
    catch (IOException e) {
      JmsUtils.rethrowJMSException(e);
    }
    return helper.moveMetadata(msg, result);
  }
}
