/*
 * $RCSfile: SocketProducer.java,v $
 * $Revision: 1.11 $
 * $Date: 2009/03/27 12:23:42 $
 * $Author: lchan $
 */
package com.adaptris.core.socket;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

import java.net.Socket;
import java.util.Map;

import org.hibernate.validator.constraints.NotBlank;
import org.perf4j.aop.Profiled;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageProducerImp;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.RequestReplyProducerImp;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Message Producer implemention for TCP.
 * 
 * @config socket-producer
 * 
 * @license STANDARD
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("socket-producer")
public class SocketProducer extends RequestReplyProducerImp {

  @NotBlank
  private String protocolImplementation;
  private static final long DEFAULT_TIMEOUT = 300000;

  @Override
  public boolean isEnabled(License l) {
    return l.isEnabled(LicenseType.Standard);
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
   * @see AdaptrisMessageProducerImp#produce(AdaptrisMessage,ProduceDestination)
   */
  @Override
  @Profiled(tag = "{$this.getClass().getSimpleName()}.produce()", logger = "com.adaptris.perf4j.socket.TimingLogger")
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
  @Profiled(tag = "{$this.getClass().getSimpleName()}.produce()", logger = "com.adaptris.perf4j.socket.TimingLogger")
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
      Map m = msg.getObjectMetadata();
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
      msg.getObjectMetadata().remove(CoreConstants.SOCKET_OBJECT_KEY);
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
