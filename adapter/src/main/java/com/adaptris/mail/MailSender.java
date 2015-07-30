package com.adaptris.mail;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * An interface for an email sender client
 *
 * The javax.mail MimeMessage and Session objects are used at least to hold message data although the client implementation may not
 * necessarily use javax.mail system, in which case a conversion will be required to store data in the MimeMessage object.
 *
 * Expected usage:- construct client sender object add/remove session properties start session call newMessage method call setFrom,
 * addTo/cc/Bcc, setSubject, setMessage, addAttachments as required call send
 *
 * @author Daniel
 *
 */
public interface MailSender {

  /**
   * Add a property to the set of properties used by the SMTP Session.
   *
   * @param key the key
   * @param value the value.
   */
  public void addSessionProperty(String key, String value);

  /**
   * Remove a property from the set of properties used by the SMTP Session.
   *
   * @param key the key
   */
  public void removeSessionProperty(String key);

  /**
   * Start session based on session properties This may be used to initialise the client prior to sending any mail
   */
  public void startSession() throws MailException;

  /**
   * Create a new message
   *
   * Must be called prior to setting recipient list, mail body etc.
   */
  public void newMessage() throws MailException;

  /**
   * Add a header to the mail header.
   *
   * @param key the key
   * @param value the value.
   */
  public void addMailHeader(String key, String value) throws MailException;

  /**
   * Remove a mail header.
   *
   * @param key the key
   */
  public void removeMailHeader(String key) throws MailException;

  /**
   * Add some names to the TO list.
   *
   * @param address an array of InternetAddress[] representing email addresses
   * @throws AddressException if the addresses could not be parsed.
   */
  public void addTo(InternetAddress[] address) throws MailException;

  /**
   * Add some names to the TO list.
   *
   * @param address the email address name@company.com
   * @throws AddressException if the addresses could not be parsed.
   */
  public void addTo(String address) throws MailException;

  /**
   * Add some names to the CC list.
   *
   * @param cc an array of InternetAddress[] representing email addresses
   * @throws AddressException if the addresses could not be parsed.
   */
  public void addCarbonCopy(InternetAddress[] cc) throws MailException;

  /**
   * Add some names to the CC list.
   *
   * @param cc the email address name@company.com
   * @throws AddressException if the addresses could not be parsed.
   */
  public void addCarbonCopy(String cc) throws MailException;

  /**
   * Add some names to the BCC list.
   *
   * @param bcc an array of InternetAddress[] representing email addresses
   * @throws AddressException if the addresses could not be parsed.
   */
  public void addBlindCarbonCopy(InternetAddress[] bcc) throws MailException;

  /**
   * Add a name to the BCC list.
   *
   * @param bcc email address name@company.com
   * @throws AddressException if the addresses could not be parsed.
   */
  public void addBlindCarbonCopy(String bcc) throws MailException;

  /**
   * Set the sender.
   *
   * @param from email address name@company.com
   * @throws AddressException if the addresses could not be parsed.
   */
  public void setFrom(String from) throws MailException;

  /**
   * Set the sender.
   *
   * @param from InternetAddress container for name@company.com
   * @throws AddressException if the addresses could not be parsed.
   */
  public void setFrom(InternetAddress from) throws MailException;

  /**
   * Set the subject of the email.
   *
   * @param s the subject.
   */
  public void setSubject(String s) throws MailException;

  /**
   * Add an attachment to the email.
   *
   * @param bytes the attachment;
   * @param filename the filename to use for the attachment.
   * @param type the content type to associate with this attachment
   * @param encoding of the mime body part for this attachment
   */
  public void addAttachment(byte[] bytes, String filename, String type, String encoding) throws MailException;

  /**
   * Add an attachment to the email.
   *
   * @param bytes the attachment;
   * @param filename the filename to use for the attachment.
   * @param type the content type to associate with this attachment
   */
  public void addAttachment(byte[] bytes, String filename, String type) throws MailException;

  /**
   * Add an attachment to the email.
   *
   * @param attachment object;
   */
  public void addAttachment(Attachment attachment) throws MailException;

  /**
   * Set the message and content type
   *
   * Content type maybe an arbitary string such as application/edi-x12, although if an appropriate <code>DataContentHandler</code>
   * is not installed, then the results can be undefined.
   *
   * @param bytes the message
   * @param contentType the content type.
   * @see javax.activation.DataContentHandler
   *
   */
  public void setMessage(byte[] bytes, String contentType) throws MailException;

  /**
   * Set the message
   *
   * @param bytes the message;
   *
   */
  public void setMessage(byte[] bytes) throws MailException;

  /**
   * Set the encoding type to use for the message type.
   * <p>
   * Valid types of encoding are those defined in RFC2045. They include "base64", "quoted-printable", "7bit", "8bit" and "binary".
   * In addition, "uuencode" is also supported.
   *
   * @param enc the encoding type to use.
   */
  public void setEncoding(String enc);

  /**
   * Send the email.
   *
   * @throws MailException wrapping any underlying exception
   */
  public void send() throws MailException;

}
