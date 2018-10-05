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

package com.adaptris.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.util.stream.StreamUtil;
import com.adaptris.util.text.mime.PartSelector;

/**
 * Helper class to parse a MimeMessage retrieved from a mailbox.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public class MessageParser {

  private transient Logger logR = LoggerFactory.getLogger(this.getClass());
  private MimeMessage msg = null;
  private byte[] message = null;
  private ArrayList<Attachment> attachments;
  private Iterator<Attachment> attachmentIterator = null;

  private MessageParser() {
    attachments = new ArrayList<Attachment>();
  }

  /**
   * Constructor.
   * 
   * @param toParse the MimeMessage we want to parse
   * @throws MailException wrapping any underlying exception.
   */
  public MessageParser(MimeMessage toParse, PartSelector partSelector) throws MailException {
    this();
    try {
      parse(toParse, partSelector);
    }
    catch (Exception e) {
      throw new MailException(e);
    }
  }

  /**
   * Constructor.
   * 
   * @param toParse the MimeMessage we want to parse
   * @throws MailException wrapping any underlying exception.
   */
  public MessageParser(MimeMessage toParse) throws MailException {
    this(toParse, null);
  }

  /**
   * Return the Message.
   * 
   * @return the message, or null if no message was sent as part of the email.
   */
  public byte[] getMessage() {
    return message;
  }

  /**
   * Return the message id.
   * 
   * @return the message id
   * @throws MailException wrapping any underlying exception.
   */
  public String getMessageId() throws MailException {

    String id = null;
    try {
      id = msg.getMessageID();
    }
    catch (Exception e) {
      throw new MailException(e);
    }
    return id;
  }

  /**
   * Does this message have attachments
   * 
   * @return true if the message contains attachments
   * @throws MailException wrapping any underlying exception.
   */
  public boolean hasAttachments() throws MailException {

    boolean rc = false;
    try {
      if (logR.isTraceEnabled()) {
        logR.trace("Message " + msg.getMessageID() + " has "
            + attachments.size() + " attachments");
      }
      rc = (attachments.size() > 0);
    }
    catch (Exception e) {
      throw new MailException(e);
    }
    return rc;
  }

  /**
   * Return the number of attachments in this message.
   * 
   * @return the number of attachments.
   */
  public int numberOfAttachments() {
    return attachments.size();
  }

  /**
   * Are there any more attachments to retrieve.
   * 
   * @return true if there are remaining attachments
   */
  public boolean hasMoreAttachments() {
    if (attachmentIterator == null) {
      attachmentIterator = attachments.iterator();
    }
    return attachmentIterator.hasNext();
  }

  /**
   * Get the next attachment.
   * 
   * @return the next attachment as an array of bytes.
   */
  public Attachment nextAttachment() {
    if (attachmentIterator == null) {
      attachmentIterator = attachments.iterator();
    }
    return attachmentIterator.next();
  }

  /**
   * Parse the underlying MimeMessage.
   * 
   * @param m	the MimeMessage
   * @param ps	the PartSelector, null => use whole message
   */
  private void parse(MimeMessage m, PartSelector ps) throws MessagingException, IOException {
    this.msg = m;

    Object o = msg.getContent();
    
    //If there is a part selector but the message is not MimeMultipart
    //then ignore this message
    if (ps == null || o instanceof MimeMultipart){
      if (o instanceof String) {
        message = ((String) o).getBytes();
      }
      else if (o instanceof InputStream) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtil.copyStream((InputStream) o, out);
        out.flush();
        message = out.toByteArray();
      }
      else if (o instanceof MimeMultipart) {
        parseMultipart((MimeMultipart) o, ps);
      }
      else {
        throw new MessagingException("Cannot handle unknown type :-"
            + o.getClass());
      }
    }
  }

  private void parseMultipart(MimeMultipart m, PartSelector ps)
      throws MessagingException, IOException {
    
    if (ps == null){
      // Loop through all the body parts
      // It is an assumption that there will only be one "message" in the
      // mime-multi-part, the rest of the the multipart being
      // attachments.
      for (int i = 0; i < m.getCount(); i++) {
        processBodyPart((MimeBodyPart) m.getBodyPart(i));
      }
    }
    else{
      for (MimeBodyPart part : ps.select(m)){
        processBodyPart(part);
      }
    }
  }
  
  private void processBodyPart(MimeBodyPart part) throws MessagingException, IOException{
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    StreamUtil.copyStream(part.getInputStream(), out);

    if ((part.getFileName() != null) && (!part.getFileName().equals(""))){
      attachments.add(new Attachment(out.toByteArray(), part.getFileName(), part.getContentType(), "base64"));
    }
    else if (message == null) {
      message = out.toByteArray();
    }
  }
}
