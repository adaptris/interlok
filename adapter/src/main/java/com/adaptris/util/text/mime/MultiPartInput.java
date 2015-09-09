package com.adaptris.util.text.mime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.util.GuidGenerator;
import com.adaptris.util.IdGenerator;
import com.adaptris.util.stream.StreamUtil;

/**
 * Reading a mime multipart input stream.
 * <p>
 * This offers, by default, a simplified model for processing all the body parts iteratively, however access to the underlying
 * MimeBodyPart is available depending on the constructor that is used.
 * </p>
 * <p>
 * A multi-part mime payload would look something similar to
 * 
 * <pre>
 * {@code 
 *  Message-ID: db03b6ef-ffff-ffc0-019b-04e2b47a4d8e
 *  Mime-Version: 1.0
 *  Content-Type: multipart/mixed;
 *    boundary="----=_Part_1_33189144.1047351507632"
 *  Content-Length: 383
 *
 *  ------=_Part_1_33189144.1047351507632
 *  Content-Id: AdaptrisMessage/payload
 *
 *  This is the message 03/11/2003 01:57 PM
 *
 *  ------=_Part_1_33189144.1047351507632
 *  Content-Id: AdaptrisMessage/metadata
 *
 *  workflowId=loopback
 *  previousGuid=db03b6ef-ffff-ffc0-019b-04e2b47a4d8e
 *  emailmessageid=<200303110257.h2B2v9sC030299@localhost.localdomain>
 *
 *  ------=_Part_1_33189144.1047351507632--
 * }
 * </pre>
 * 
 * <p>
 * The Content-Length header is ignored for the purposes of parsing the multi-part mime message, the multipart is considered
 * finished, when the final mime boundary occurs. If Content-Length needs to be taken into account then a specific DataSource should
 * be used as the parameter to the constructor.
 * </p>
 */
public class MultiPartInput implements Enumeration, Iterator {

  private List<PartHolder> bodyParts;
  private MimeMultipart multipart;
  private Iterator bodyPartIterator;
  private transient Logger logR = LoggerFactory.getLogger(this.getClass());
  private DataSource dataSource;
  private boolean simpleIterator;

  private static IdGenerator idGenerator;

  static {
    idGenerator = new GuidGenerator();

  }

  private MultiPartInput() {
    bodyParts = new Vector<PartHolder>();
    simpleIterator = true;

  }

  /**
   * Constructor.
   *
   * @param in the Inputstream from which to parse the mime multi-part
   * @throws MessagingException if the bytes did not contain a valid
   *           MimeMultiPart
   * @throws IOException if there was an IOException
   * @throws MessagingException if an underlying javax.mail exception occurred
   * @see MultiPartInput#MultiPartInput(InputStream, boolean)
   */
  public MultiPartInput(InputStream in) throws IOException, MessagingException {
    this(in, true);
  }

  /**
   * Constructor.
   *
   * @param in the Inputstream from which to parse the mime multi-part
   * @param simplified whether the iterator / enumeration should simply return
   *          the content body as a byte array rather than a MimeBodyPart
   * @throws MessagingException if the bytes did not contain a valid
   *           MimeMultiPart
   * @throws IOException if there was an IOException
   * @throws MessagingException if the bytes did not contain a valid
   *           MimeMultiPart
   * @see MultiPartInput#MultiPartInput(DataSource, boolean)
   */
  public MultiPartInput(InputStream in, boolean simplified) throws IOException,
      MessagingException {
    this(new InputStreamDataSource(in), simplified);
  }

  /**
   * Constructor.
   *
   * @param bytes the byte array where the mime multi-part is.
   * @throws MessagingException if the bytes did not contain a valid
   *           MimeMultiPart
   * @throws IOException if there was an IOException
   * @throws MessagingException if the bytes did not contain a valid
   *           MimeMultiPart
   * @see MultiPartInput#MultiPartInput(byte[], boolean)
   */
  public MultiPartInput(byte[] bytes) throws IOException, MessagingException {
    this(bytes, true);
  }

  /**
   * Constructor.
   *
   * @param bytes the bytes from which to parse the mime multipart.
   * @param simplified whether the iterator / enumeration should simply return
   *          the content body as a byte array rather than a MimeBodyPart
   * @see InputStreamDataSource
   * @throws MessagingException if the bytes did not contain a valid
   *           MimeMultiPart
   * @throws IOException if there was an IOException
   */
  public MultiPartInput(byte[] bytes, boolean simplified) throws IOException,
      MessagingException {
    this(new ByteArrayInputStream(bytes), simplified);
  }

  /**
   * Constructor.
   *
   * @param ds the Datasource from which to parse the mime multipart.
   * @see InputStreamDataSource
   * @throws MessagingException if the bytes did not contain a valid
   *           MimeMultiPart
   * @throws IOException if there was an IOException
   * @throws MessagingException if the bytes did not contain a valid
   *           MimeMultiPart
   * @see MultiPartInput#MultiPartInput(DataSource, boolean)
   */
  public MultiPartInput(DataSource ds) throws IOException, MessagingException {
    this(ds, true);
  }

  /**
   * Constructor.
   *
   * @param ds the Datasource from which to parse the mime multipart.
   * @param simplified whether the iterator / enumeration should simply return
   *          the content body as a byte array rather than a MimeBodyPart
   * @see InputStreamDataSource
   * @throws MessagingException if the bytes did not contain a valid
   *           MimeMultiPart
   * @throws IOException if there was an IOException
   */
  public MultiPartInput(DataSource ds, boolean simplified) throws IOException,
      MessagingException {
    this();
    dataSource = ds;
    simpleIterator = simplified;
    initialise();
  }

  /**
   * Get a BodyPart based on the id.
   * <p>
   * Although it is unlikely that the Content-Id will re-occur across a mime
   * multi-part, this is possible, so use of this method may not return the
   * expected body part.
   * </p>
   *
   * @param id the defining content-id.
   * @return the underlying MimeBodyPart specified by the contentid or null if
   *         the contentId is not present.
   */
  public MimeBodyPart getBodyPart(String id) {
    MimeBodyPart result = null;
    PartHolder tmp = new PartHolder(id);

    if (bodyParts.contains(tmp)) {
      result = ((PartHolder) bodyParts.get(bodyParts.indexOf(tmp))).getPart();
    }
    return result;
  }

  /**
   * Get a BodyPart based on the it's position within the multipart.
   *
   * @param partNumber the part position (starts from 0).
   * @return the underlying MimeBodyPart specified by the partNumber.
   */
  public MimeBodyPart getBodyPart(int partNumber) {
    if (bodyParts.size() < partNumber + 1) {
      return null;
    }
    return ((PartHolder) bodyParts.get(partNumber)).getPart();
  }

  /**
   * Get a part by the contentId.
   * <p>
   * Although it is unlikely that the Content-Id will re-occur across a mime
   * multi-part, this is possible, so use of this method may not return the
   * expected body part.
   * </p>
   *
   * @param id the defining content-id.
   * @return the contents of the body part specified by the contentId or null if
   *         the contentId is not present.
   */
  public byte[] getPart(String id) {
    byte[] result = null;
    PartHolder tmp = new PartHolder(id);
    if (bodyParts.contains(tmp)) {
      result = ((PartHolder) bodyParts.get(bodyParts.indexOf(tmp))).getBytes();
    }
    return result;
  }

  /**
   * Get a BodyPart based on the it's position within the multipart.
   *
   * @param partNumber the part position (starts from 0).
   * @return the underlying bytes specified by the partNumber.
   */
  public byte[] getPart(int partNumber) {
    if (bodyParts.size() < partNumber + 1) {
      return null;
    }
    return ((PartHolder) bodyParts.get(partNumber)).getBytes();
  }

  /** @see Enumeration#hasMoreElements */
  @Override
  public boolean hasMoreElements() {
    return hasNext();
  }

  /**
   * @see Enumeration#nextElement
   * @see #next()
   */
  @Override
  public Object nextElement() {
    return next();
  }

  /**
   * @see Iterator#hasNext()
   */
  @Override
  public boolean hasNext() {
    return bodyPartIterator.hasNext();
  }

  /**
   * @see Iterator#next()
   * @return the next body part in the list, either a byte array or MimeBodyPart
   */
  @Override
  public Object next() {
    Object result = null;
    if (simpleIterator) {
      result = ((PartHolder) bodyPartIterator.next()).getBytes();
    }
    else {
      result = ((PartHolder) bodyPartIterator.next()).getPart();
    }
    return result;
  }

  /**
   * @see Iterator#remove()
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove is not supported");
  }

  /**
   * Return the number of body parts in this mime multipart.
   *
   * @return the number of body parts
   */
  public int size() {
    return bodyParts.size();
  }

  /**
   * Return the underlying data source used to parse this mime multipart.
   *
   * @return the datasource
   * @see javax.activation.DataSource
   * @see InputStreamDataSource
   */
  public DataSource getDataSource() {
    return dataSource;
  }

  /**
   * Convenience Method to get the content-Type
   *
   * @return the contenttype
   * @see javax.activation.DataSource#getContentType()
   */
  public String getContentType() {
    return dataSource.getContentType();
  }

  /**
   * Convenience Method to get the name from the underlying datasource.
   *
   * @return the name
   * @see javax.activation.DataSource#getName()
   */
  public String getName() {
    return dataSource.getName();
  }

  private void initialise() throws MessagingException, IOException {
    multipart = new MimeMultipart(dataSource);
    for (int i = 0; i < multipart.getCount(); i++) {
      MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(i);
      PartHolder ph = new PartHolder(part);
      if (bodyParts.contains(ph)) {
        logR.warn(ph.contentId + " already exists as a part");
      }
      bodyParts.add(ph);
    }
    bodyPartIterator = bodyParts.iterator();
  }

  private class PartHolder {
    private MimeBodyPart bodyPart;
    private String contentId;
    private byte[] bodyBytes;

    PartHolder(String id) {
      contentId = id;
      bodyPart = null;
      bodyBytes = null;
    }

    PartHolder(MimeBodyPart p) throws IOException, MessagingException {
      bodyPart = p;
      contentId = bodyPart.getContentID();
      if (contentId == null) {
        logR.warn("No Content Id Found as part of body part, "
              + "assigning a unique id " + "for referential integrity");
        contentId = idGenerator.create(p);
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      StreamUtil.copyStream(bodyPart.getInputStream(), out);
      bodyBytes = out.toByteArray();
    }

    MimeBodyPart getPart() {
      return bodyPart;
    }

    byte[] getBytes() {
      return bodyBytes;
    }

    /**
     *
     * @see java.lang.Object#equals(java.lang.Object)
     *
     */
    @Override
    public boolean equals(Object o) {
      boolean rc = false;
      if (o instanceof PartHolder) {
        rc = contentId.equals(((PartHolder) o).contentId);
      }
      return rc;
    }

    /**
     *
     * @see java.lang.Object#hashCode()
     *
     */
    @Override
    public int hashCode() {
      return contentId.hashCode();
    }

    /**
     *
     * @see java.lang.Object#toString()
     *
     */
    @Override
    public String toString() {
      return super.toString() + " Content-Id=[" + contentId + "]";
    }
  }
}
