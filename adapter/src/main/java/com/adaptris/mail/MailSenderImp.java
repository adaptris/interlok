package com.adaptris.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentDisposition;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.lang.ArrayUtils;

public abstract class MailSenderImp implements MailSender {

  private static final String DEFAULT_CONTENT_TYPE = "text/plain";

  private Properties sessionProperties;
  private String encoding = null;
  protected Session session;
  protected MimeMessage message;
  private List<Attachment> attachments;
  private byte[] emailBody;
  private String bodyContentType;

  protected MailSenderImp() {
    attachments = new ArrayList<Attachment>();
    sessionProperties = new Properties();
  }

  // Purely for testing.
  Properties getSessionProperties() {
    return sessionProperties;
  }

  /**
   * Add a property to the set of properties used by the Session.
   *
   * @param key the key
   * @param value the value.
   */
  @Override
  public void addSessionProperty(String key, String value) {
    sessionProperties.setProperty(key, value);
  }

  /**
   * Remove a property from the set of properties used by the Session.
   *
   * @param key
   */
  @Override
  public void removeSessionProperty(String key) {
    if (sessionProperties.containsKey(key)) {
      sessionProperties.remove(key);
    }
  }

  @Override
  public void startSession() throws MailException {
    session = Session.getInstance(sessionProperties);
    newMessage();
  }

  @Override
  public void newMessage() throws MailException {
    checkSession();
    message = new MimeMessage(session);
    attachments.clear();
  }

  @Override
  public void addMailHeader(String key, String value) throws MailException {
    checkSession();
    try {
      message.addHeader(key, value);
    }
    catch (MessagingException e) {
      throw new MailException(e);
    }
  }

  /**
   * Remove a mail header.
   *
   * @param key
   */
  @Override
  public void removeMailHeader(String key) throws MailException {
    checkSession();
    try {
      message.removeHeader(key);
    }
    catch (MessagingException e) {
      throw new MailException(e);
    }
  }

  /**
   * Add some names to the TO list.
   *
   * @param address an array of InternetAddress[] representing email addresses
   * @throws AddressException if the addresses could not be parsed.
   */
  @Override
  public void addTo(InternetAddress[] address) throws MailException {
    checkSession();
    try {
      message.addRecipients(Message.RecipientType.TO, address);
    }
    catch (MessagingException e) {
      throw new MailException(e);
    }
  }

  /**
   * Add some names to the TO list.
   *
   * @param address the email address name@company.com
   * @throws AddressException if the addresses could not be parsed.
   */
  @Override
  public void addTo(String address) throws MailException {
    try {
      addTo(InternetAddress.parse(address));
    }
    catch (AddressException e) {
      throw new MailException(e);
    }
  }

  /**
   * Add some names to the CC list.
   *
   * @param address an array of InternetAddress[] representing email addresses
   * @throws AddressException if the addresses could not be parsed.
   */
  @Override
  public void addCarbonCopy(InternetAddress[] address) throws MailException {
    checkSession();
    try {
      message.addRecipients(Message.RecipientType.CC, address);
    }
    catch (MessagingException e) {
      throw new MailException(e);
    }
  }

  /**
   * Add some names to the CC list.
   *
   * @param address the email address name@company.com
   * @throws AddressException if the addresses could not be parsed.
   */
  @Override
  public void addCarbonCopy(String address) throws MailException {
    try {
      addCarbonCopy(InternetAddress.parse(address));
    }
    catch (AddressException e) {
      throw new MailException(e);
    }
  }

  /**
   * Add some names to the BCC list.
   *
   * @param address an array of InternetAddress[] representing email addresses
   * @throws AddressException if the addresses could not be parsed.
   */
  @Override
  public void addBlindCarbonCopy(InternetAddress[] address) throws MailException {
    checkSession();
    try {
      message.addRecipients(Message.RecipientType.BCC, address);
    }
    catch (MessagingException e) {
      throw new MailException(e);
    }
  }

  /**
   * Add a name to the BCC list.
   *
   * @param address email address name@company.com
   * @throws AddressException if the addresses could not be parsed.
   */
  @Override
  public void addBlindCarbonCopy(String address) throws MailException {
    try {
      addBlindCarbonCopy(InternetAddress.parse(address));
    }
    catch (AddressException e) {
      throw new MailException(e);
    }
  }

  /**
   * Set the sender.
   *
   * @param from email address name@company.com
   * @throws AddressException if the addresses could not be parsed.
   */
  @Override
  public void setFrom(String from) throws MailException {
    try {
      setFrom(new InternetAddress(from));
    }
    catch (AddressException e) {
      throw new MailException(e);
    }
  }

  /**
   * Set the sender.
   *
   * @param from InternetAddress container for name@company.com
   * @throws AddressException if the addresses could not be parsed.
   */
  @Override
  public void setFrom(InternetAddress from) throws MailException {
    checkSession();
    try {
      message.setFrom(from);
    }
    catch (MessagingException e) {
      throw new MailException(e);
    }
  }

  /**
   * Set the subject of the email.
   *
   * @param s the subject.
   */
  @Override
  public void setSubject(String s) throws MailException {
    checkSession();
    try {
      message.setSubject(s);
    }
    catch (MessagingException e) {
      throw new MailException(e);
    }
  }

  @Override
  public void addAttachment(byte[] bytes, String filename, String type, String encoding) throws MailException {
    addAttachment(new Attachment(bytes, filename, type, encoding));
  }
  
  /**
   * Add an attachment to the email.
   *
   * @param bytes the attachment;
   * @param filename the filename to use for the attachment.
   * @param type the content type to associate with this attachment
   */
  @Override
  public void addAttachment(byte[] bytes, String filename, String type) throws MailException {
    addAttachment(new Attachment(bytes, filename, type, "base64"));
  }

  @Override
  public void addAttachment(Attachment a) throws MailException {
    attachments.add(a);
  }

  private void addAttachmentsToMessage(MimeMultipart multipart) throws MailException {
    try {
      for (Attachment a : attachments) {
        MimeBodyPart part = create(a.getBytes(), new InternetHeaders(), a.getEncoding());
        part.setHeader(Mail.CONTENT_TYPE, a.getContentType());
        ContentDisposition cd = new ContentDisposition();
        cd.setDisposition(Mail.DISPOSITION_TYPE_ATTACHMENT);
        if (a.getFilename() != null) {
          cd.setParameter(Mail.DISPOSITION_PARAM_FILENAME, a.getFilename());
        }
        part.setDisposition(cd.toString());
        multipart.addBodyPart(part);
      }
    }
    catch (Exception e) {
      throw new MailException(e);
    }
  }

  /**
   * Set the message.
   *
   * @param bytes the message;
   */
  @Override
  public void setMessage(byte[] bytes) throws MailException {
    setMessage(bytes, DEFAULT_CONTENT_TYPE);
  }

  @Override
  public void setMessage(byte[] bytes, String contentType) throws MailException {
    emailBody = bytes;
    bodyContentType = contentType;

  }

  private void addEmailBody(MimeMultipart multipart) throws MailException {
    try {
      if (emailBody != null) {
        MimeBodyPart part = create(emailBody, new InternetHeaders(), encoding);
        part.setHeader(Mail.CONTENT_TYPE, bodyContentType);
        multipart.addBodyPart(part);
      }
    }
    catch (Exception e) {
      throw new MailException(e);
    }
  }

  /**
   * Set the encoding type to use for the message type.
   * <p>
   * Valid types of encoding are those defined in RFC2045. They include "base64", "quoted-printable", "7bit", "8bit" and "binary".
   * In addition, "uuencode" is also supported.
   *
   * @param enc the encoding type to use.
   */
  @Override
  public void setEncoding(String enc) {
    encoding = enc;
  }

  /**
   * Build the content of the MimeMessage from the constituent parts.
   */
  protected void buildContent() throws MailException {
    checkSession();
    try {
      if (ArrayUtils.isEmpty(message.getFrom())) {
        message.setFrom();
      }
      if (ArrayUtils.isEmpty(message.getAllRecipients())) {
        throw new MailException("Mail message has no recipients");
      }
      message.setSentDate(new Date());
      MimeMultipart body = new MimeMultipart();
      addAttachmentsToMessage(body);
      addEmailBody(body);
      message.setContent(body);
      message.saveChanges();
    }
    catch (MessagingException e) {
      throw new MailException(e);
    }
  }

  private MimeBodyPart create(byte[] bytes, InternetHeaders headers, String encoding) throws MessagingException, IOException {
    byte[] content = encode(bytes, headers, encoding);
    return new MimeBodyPart(headers, content);
  }

  /**
   * Return the encoded payload.
   * <p>
   * In the situation where we are directly creating the <code>MimeBodypart</code> with a set of InternetHeaders and a byte payload,
   * we have to encode the payload ourselves, as no encoding appears to be done when the body part is written. This is implied by
   * the documentation for MimeBodyPart.
   * </p>
   *
   * @return the bytes
   * @param uncodedPayload the payload in its raw form.
   * @param header the existing InternetHeaders so that the appropriate content transfer encoding header can be set.
   * @param encoding for this particular part of the message
   * @see MimeBodypart#MimeBodyPart(InternetHeaders, byte[])
   */
  private byte[] encode(byte[] unencoded, InternetHeaders header, String encoding) throws MessagingException, IOException {

    byte[] encoded = unencoded;
    if (encoding != null) {

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      OutputStream encodedOut = MimeUtility.encode(out, encoding);
      encodedOut.write(unencoded);
      encodedOut.flush();
      encodedOut.close();
      encoded = out.toByteArray();
      header.setHeader(Mail.CONTENT_ENCODING, encoding);
    }
    return encoded;
  }

  private void checkSession() throws MailException {
    if (session == null) {
      throw new MailException("Session not started");
    }
  }
}
