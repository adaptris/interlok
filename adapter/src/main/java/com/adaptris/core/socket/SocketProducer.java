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

package com.adaptris.core.socket;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

import java.net.Socket;
import java.util.Map;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.RequestReplyProducerImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Message Producer implemention for TCP.
 * 
 * @config socket-producer
 * 
 * 
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("socket-producer")
@AdapterComponent
@ComponentProfile(summary = "Write a arbitrary message to a socket", tag = "producer,socket,tcp",
    recommended = {ProduceConnection.class})
@DisplayOrder(order = {"protocolImplementation"})
public class SocketProducer extends RequestReplyProducerImp {

  @NotBlank
  private String protocolImplementation;
  private static final long DEFAULT_TIMEOUT = 300000;

  @Override
  public void prepare() throws CoreException {
  }


  /**
   *
   * @see com.adaptris.core.RequestReplyProducerImp#defaultTimeout()
   */
  @Override
  protected long defaultTimeout() {
    return DEFAULT_TIMEOUT;
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageProducerImp#produce(AdaptrisMessage,ProduceDestination)
   */
  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination destination)
      throws ProduceException {
    sendMessage(msg, destination, DEFAULT_TIMEOUT, false);
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
    ;
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#start()
   */
  @Override
  public void start() throws CoreException {
    ;
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#stop()
   */
  @Override
  public void stop() {
    ;
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#close()
   */
  @Override
  public void close() {
    ;
  }

  /**
   *
   * @see RequestReplyProducerImp#doRequest(AdaptrisMessage, ProduceDestination,
   *      long)
   */
  @Override
  public AdaptrisMessage doRequest(AdaptrisMessage msg,
                                ProduceDestination destination, long timeout)
      throws ProduceException {

    return sendMessage(msg, destination, timeout, true);
  }

  private AdaptrisMessage sendMessage(AdaptrisMessage msg,
                                   ProduceDestination dest, long timeout,
                                   boolean expectReply) throws ProduceException {

    Protocol p = null;
    AdaptrisMessage reply = defaultIfNull(getMessageFactory()).newMessage();
    Socket sock = null;
    try {
      String host = dest.getDestination(msg);
      Map m = msg.getObjectHeaders();
      // Use the object metadata socket if available.
      sock = m.containsKey(CoreConstants.SOCKET_OBJECT_KEY) ? (Socket) m.get(CoreConstants.SOCKET_OBJECT_KEY) : retrieveConnection(
          ProduceConnection.class).createSocket(host);
      sock.setSoTimeout(timeout >=0 ? new Long(timeout).intValue() : 0);
      p = (Protocol) Class.forName(protocolImplementation).newInstance();
      p.setSocket(sock);
      p.sendDocument(msg.getPayload());

      if (!p.wasSendSuccess()) {
        throw new Exception("Send of document [" + msg.getUniqueId()
            + "] failed");
      }
      if (expectReply) {
        reply.setPayload(p.getReplyAsBytes());
      }
    }
    catch (Exception e) {
      throw new ProduceException(e);
    }
    finally {
      msg.getObjectHeaders().remove(CoreConstants.SOCKET_OBJECT_KEY);
      if (sock != null) {
        try {
          sock.close();
        }
        catch (Exception ignored) {
          ;
        }
      }
    }
    return reply;
  }

  /**
   * Get the protocol implementation.
   *
   * @return the protocol implementation
   * @see Protocol
   */
  public String getProtocolImplementation() {
    return protocolImplementation;
  }

  /**
   * Set the protocol implementation.
   *
   * @param string the protocol implementation
   * @see Protocol
   */
  public void setProtocolImplementation(String string) {
    protocolImplementation = string;
  }

}
