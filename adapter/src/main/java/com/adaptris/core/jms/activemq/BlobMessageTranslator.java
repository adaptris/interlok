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

package com.adaptris.core.jms.activemq;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.BlobMessage;
import org.apache.commons.io.IOUtils;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.jms.AutoConvertMessageTranslator;
import com.adaptris.core.jms.JmsUtils;
import com.adaptris.core.jms.MessageTypeTranslatorImp;
import com.adaptris.core.lms.FileBackedMessage;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Translates between {@link com.adaptris.core.AdaptrisMessage} and {@link org.apache.activemq.BlobMessage}.
 * </p>
 * <p>
 * In outbound scenarios (i.e. writing to ActiveMQ), there are two ways in which this translator
 * works with the {@link com.adaptris.core.AdaptrisMessage}.
 * </p>
 * <ol>
 * <li>If {@link #setMetadataUrlKey(String)} <strong>has been set</strong>, then the value
 * associated with that metadata key is used to create a URL object so that
 * {@link ActiveMQSession#createBlobMessage(URL)} can be used. If you are intending to use this,
 * then the data will already have been written to this URL (by the adapter or otherwise). If the
 * metadata key does not exist, then it assumes the next scenario.</li>
 * <li>If {@link #setMetadataUrlKey(String)} <strong>has not been set</strong>, then the contents of
 * the AdaptrisMessage object are used to form the BlobMessage, either by using
 * {@link ActiveMQSession#createBlobMessage(java.io.File)} or
 * {@link ActiveMQSession#createBlobMessage(InputStream)} depending on the underlying type of
 * AdaptrisMessage. If you are intending to use this, then you will have already configured ActiveMQ
 * / underlying OS platform (e.g. for WebDAV/FTP) appropriately for handling out of band transfers.
 * </li>
 * </ol>
 * <p>
 * For inbound scenarios, {@link BlobMessage#getInputStream()} is used, this may create additional
 * connections to remote servers from the machine where the adapter is running.
 * </p>
 * 
 * @config activemq-blob-message-translator
 * 
 */
@SuppressWarnings("deprecation")
@XStreamAlias("activemq-blob-message-translator")
@DisplayOrder(order = {"metadataUrlKey", "metadataFilter", "moveMetadata", "moveJmsHeaders", "reportAllErrors"})
public final class BlobMessageTranslator extends MessageTypeTranslatorImp {

  private String metadataUrlKey;
  private transient AutoConvertMessageTranslator fallback;

  public BlobMessageTranslator() {
    super();
    fallback = new AutoConvertMessageTranslator();
  }

  /**
   * @see #setMetadataUrlKey(String)
   *
   */
  public BlobMessageTranslator(String metadataKey) {
    this();
    setMetadataUrlKey(metadataKey);
  }

  /**
   * <p>
   * Translates an <code>AdaptrisMessage</code> into a <code>BlobMessage</code>.
   * </p>
   *
   * @param msg the <code>AdaptrisMessage</code> to translate
   * @return a new {@link org.apache.activemq.BlobMessage}
   * @throws JMSException on error.
   * @see ActiveMQSession#createBlobMessage(URL)
   * @see ActiveMQSession#createBlobMessage(java.io.File)
   * @see ActiveMQSession#createBlobMessage(InputStream)
   */
  @Override
  public Message translate(AdaptrisMessage msg) throws JMSException {
    BlobMessage jmsMsg = null;
    try {
      if (metadataUrlKey != null && msg.containsKey(metadataUrlKey)) {
        jmsMsg = ((ActiveMQSession) session).createBlobMessage(new URL(msg.getMetadataValue(metadataUrlKey)));
      }
      else {
        if (msg instanceof FileBackedMessage) {
          jmsMsg = ((ActiveMQSession) session).createBlobMessage(((FileBackedMessage) msg).currentSource());
        }
        else {
          jmsMsg = ((ActiveMQSession) session).createBlobMessage(msg.getInputStream());
        }
      }
    }
    catch (IOException e) {
      JmsUtils.rethrowJMSException("Failed to create BlobMessage", e);

    }
    return helper.moveMetadata(msg, jmsMsg);
  }

  /**
   * <p>
   * Translates a <code>BlobMessage</code> into an <code>AdaptrisMessage</code>
   * .
   * </p>
   *
   * @param msg the <code>BlobMessage</code> to translate
   * @return an <code>AdaptrisMessage</code>
   * @throws JMSException on exception
   */
  @Override
  public AdaptrisMessage translate(Message msg) throws JMSException {
    OutputStream out = null;
    InputStream in = null;
    AdaptrisMessage result = currentMessageFactory().newMessage();
    try {
      if (msg instanceof BlobMessage) {
        in = ((BlobMessage) msg).getInputStream();
        if (in == null) {
          log.warn("BlobMessage [" + msg.getJMSMessageID() + "] has no content");
        }
        else {
          out = result.getOutputStream();
          IOUtils.copy(in, out);
          out.flush();
        }
      }
      else {
        result = fallback.translate(msg);
      }
    }
    catch (IOException e) {
      JmsUtils.rethrowJMSException("Failed to translate BlobMessage", e);
    }
    finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }
    return helper.moveMetadata(msg, result);
  }

  @Override
  public void init() throws CoreException {
    super.init();
    init(fallback);
  }

  private void init(MessageTypeTranslatorImp m) throws CoreException {
    m.registerSession(currentSession());
    m.registerMessageFactory(currentMessageFactory());
    m.setMoveJmsHeaders(getMoveJmsHeaders());
    m.setMoveMetadata(getMoveMetadata());
    m.setReportAllErrors(getReportAllErrors());
    LifecycleHelper.init(m);
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(fallback);
    super.start();
  }

  @Override
  public void stop() {
    super.stop();
    LifecycleHelper.stop(fallback);
  }

  @Override
  public void close() {
    super.close();
    LifecycleHelper.close(fallback);
  }

  /**
   * Get the metadata key that will be used to generate the URL.
   *
   * @see ActiveMQSession#createBlobMessage(URL)
   * @return the metadata key.
   */
  public String getMetadataUrlKey() {
    return metadataUrlKey;
  }

  /**
   * Set the metadata key that will be used to generate the URL.
   * <p>
   * This setting only has effect when the framework invokes
   * {@link #translate(AdaptrisMessage)} (during the produce of the message).
   * </p>
   *
   * @see ActiveMQSession#createBlobMessage(URL)
   * @param metadataKey
   */
  public void setMetadataUrlKey(String metadataKey) {
    metadataUrlKey = metadataKey;
  }
}
