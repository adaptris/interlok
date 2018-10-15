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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageEOFException;

import org.apache.commons.io.IOUtils;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.stream.StreamUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Translates between <code>AdaptrisMessage</code> and <code>javax.jms.BytesMessages</code>.
 * </p>
 * 
 * @config bytes-message-translator
 * 
 */
@XStreamAlias("bytes-message-translator")
@DisplayOrder(order = {"metadataFilter", "moveMetadata", "moveJmsHeaders", "reportAllErrors"})
public class BytesMessageTranslator extends MessageTypeTranslatorImp {

  // Threshold of 50k size to switch to to using an OutputStream impl to copy stuff into the msg proper.
  private static final long STREAM_THRESHOLD = 1024 * 50;
  private transient long streamThreshold = STREAM_THRESHOLD;

  /**
   * <p>
   * Translates an <code>AdaptrisMessage</code> into a <code>BytesMessage</code>.
   * </p>
   * 
   * @param msg the <code>AdaptrisMessage</code> to translate
   * @return a new <code>BytesMessage</code>
   * @throws JMSException
   */
  public Message translate(AdaptrisMessage msg) throws JMSException {
    BytesMessage jmsMsg = session.createBytesMessage();
    if (msg.getSize() > streamThreshold()) {
      try {
        StreamUtil.copyAndClose(msg.getInputStream(), new BytesMessageOutputStream(jmsMsg)); // lgtm [java/output-resource-leak]
      }
      catch (IOException e) {
        throw JmsUtils.wrapJMSException(e);
      }
    }
    else {
      jmsMsg.writeBytes(msg.getPayload());
    }
    return helper.moveMetadata(msg, jmsMsg);
  }

  /**
   * <p>
   * Translates a <code>BytesMessage</code> into an <code>AdaptrisMessage</code>.
   * </p>
   * 
   * @param msg the <code>BytesMessage</code> to translate
   * @return an <code>AdaptrisMessage</code>
   * @throws JMSException
   */
  public AdaptrisMessage translate(Message msg) throws JMSException {
    AdaptrisMessage result = currentMessageFactory().newMessage();
    try (InputStream in = new BytesMessageInputStream((BytesMessage) msg); OutputStream out = result.getOutputStream()) {
      IOUtils.copy(in, out);
    }
    catch (IOException e) {
      throw JmsUtils.wrapJMSException(e);
    }
    return helper.moveMetadata(msg, result);
  }

  long streamThreshold() {
    return streamThreshold;
  }

  private class BytesMessageOutputStream extends OutputStream {
    private final BytesMessage myMsg;
    public BytesMessageOutputStream(BytesMessage message) {
      this.myMsg = message;
    }

    public void write(int b) throws IOException {
      try {
        myMsg.writeByte((byte) b);
      }
      catch (JMSException ex) {
        throw new IOException(ex);
      }
    }
  }

  private class BytesMessageInputStream extends InputStream {
    private final BytesMessage myMsg;

    BytesMessageInputStream(BytesMessage message) {
      this.myMsg = message;
    }

    @Override
    public int read() throws IOException {
      try {
        byte b = myMsg.readByte();
        return b >= 0 ? b : 256+b;
      }
      catch (MessageEOFException ex) {
        return -1;
      }
      catch (JMSException ex) {
        throw new IOException(ex);
      }
    }
  }
}
