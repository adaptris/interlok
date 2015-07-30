/*
 * $RCSfile: $
 * $Revision: $
 * $Date: $
 * $Author: $
 */
package com.adaptris.core.socket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.util.stream.UnbufferedLineInputStream;

/**
 * Simple Socket Protocol Implementation.
 * Basic protocol is
 * <ul>
 * <li>crlf terminated documents (i.e each line is a single document)</li>
 * <li>Responses are either 200 (OK) or 500 (error) roughly matching HTTP</li>
 * </ul>
 * @author lchan
 * @author $Author: $
 */
public class SimpleProtocol implements Protocol {

  private transient Log logR = LogFactory.getLog(this.getClass());
  private byte[] document = new byte[0];
  private byte[] reply = new byte[0];
  private boolean docReceived;
  private boolean docSent;
  private Socket socket;

  /**
   * @see com.adaptris.core.socket.Protocol#getReceivedAsBytes()
   */
  public byte[] getReceivedAsBytes() throws IOException, IllegalStateException {
    if (!docReceived) {
      throw new IllegalStateException(
          "Called out of sync, receiveDocumentFirst");
    }
    return document;
  }

  /**
   * @see com.adaptris.core.socket.Protocol#getReceivedAsStream()
   */
  public InputStream getReceivedAsStream() throws IOException,
      IllegalStateException {
    if (!docReceived) {
      throw new IllegalStateException(
          "Called out of sync, receiveDocument First");
    }
    return new ByteArrayInputStream(document);
  }

  /**
   * @see com.adaptris.core.socket.Protocol#getReplyAsBytes()
   */
  public byte[] getReplyAsBytes() throws IOException, IllegalStateException {
    if (!docSent) {
      throw new IllegalStateException("Called out of sync, sendDocument first");
    }
    return reply;
  }

  /**
   * @see com.adaptris.core.socket.Protocol#getReplyAsStream()
   */
  public InputStream getReplyAsStream() throws IOException,
      IllegalStateException {
    if (!docSent) {
      throw new IllegalStateException("Called out of sync, sendDocument first");
    }
    return new ByteArrayInputStream(reply);
  }

  /**
   * @see com.adaptris.core.socket.Protocol#receiveDocument()
   */
  public void receiveDocument() throws IOException {
    UnbufferedLineInputStream in = new UnbufferedLineInputStream(socket.getInputStream());
    String doc = in.readLine();
    logR.trace("receiveDocument Got Document [" + doc + "]");
    document = doc.getBytes();
    docReceived = true;
  }

  /**
   * @see com.adaptris.core.socket.Protocol#receiveDocumentError()
   */
  public void receiveDocumentError() throws IOException, IllegalStateException {
    PrintStream out = new PrintStream(socket.getOutputStream(), true);
    out.print("500\r\n");
    out.flush();
  }

  /**
   * @see com.adaptris.core.socket.Protocol#receiveDocumentSuccess()
   */
  public void receiveDocumentSuccess() throws IOException,
      IllegalStateException {
    PrintStream out = new PrintStream(socket.getOutputStream(), true);
    out.print("200\r\n");
    out.flush();

  }

  /**
   * @see com.adaptris.core.socket.Protocol#sendDocument(byte[])
   */
  public void sendDocument(byte[] bytes) throws IOException {
    PrintStream out = new PrintStream(socket.getOutputStream(), true);
    out.print(new String(bytes) + "\r\n");
    out.flush();
    UnbufferedLineInputStream in = new UnbufferedLineInputStream(socket.getInputStream());
    String doc = in.readLine();
    logR.trace("SendDocument Got Reply [" + doc + "]");
    reply = doc.getBytes();
    docSent = true;
  }

  /**
   * @see com.adaptris.core.socket.Protocol#setSocket(java.net.Socket)
   */
  public void setSocket(Socket s) {
    this.socket = s;
  }

  /**
   * @see com.adaptris.core.socket.Protocol#wasSendSuccess()
   */
  public boolean wasSendSuccess() throws IllegalStateException {
    return "200".equals(new String(reply));
  }

}
