package com.adaptris.core;

import static com.adaptris.core.util.MetadataHelper.convertFromProperties;
import static com.adaptris.core.util.MetadataHelper.convertToProperties;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.Set;

import javax.validation.constraints.Pattern;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.adaptris.util.text.mime.MultiPartInput;
import com.adaptris.util.text.mime.MultiPartOutput;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>AdaptrisMessageEncoder</code> that stores <code>AdaptrisMessage</code> payload and metadata as a
 * mime-encoded multipart message.
 * </p>
 * <p>
 * The metadata is treated as a though it were a set of Properties, therefore using = as part of either the metadata key or data
 * makes the behaviour undefined.
 * </p>
 * <p>
 * By default the data is not encoded, however this behaviour can be overriden through use of the <code>setMetadataEncoding()</code>
 * and <code>setPayloadEncoding()</code> methods.
 * </p>
 * 
 * @config mime-encoder
 */
@XStreamAlias("mime-encoder")
public class MimeEncoder extends AdaptrisMessageEncoderImp {

  private static final String PAYLOAD_CONTENT_ID = "AdaptrisMessage/payload";
  private static final String METADATA_CONTENT_ID = "AdaptrisMessage/metadata";
  private static final String EXCEPTION_CONTENT_ID = "AdaptrisMessage/exception";

  @Pattern(regexp = "base64|quoted-printable|uuencode|x-uuencode|x-uue|binary|7bit|8bit")
  private String metadataEncoding;
  @Pattern(regexp = "base64|quoted-printable|uuencode|x-uuencode|x-uue|binary|7bit|8bit")
  private String payloadEncoding;
  private Boolean retainUniqueId;

  public MimeEncoder() {
    super();
  }

  public MimeEncoder(Boolean retainUniqueId, String metadataEncoding, String payloadEncoding) {
    this();
    setRetainUniqueId(retainUniqueId);
    setMetadataEncoding(metadataEncoding);
    setPayloadEncoding(payloadEncoding);
  }
  /**
   * Encode the <code>AdaptrisMessage</code> object.
   * <p>
   * The target object is assumed to be of the type <code>OutputStream</code>
   * </p>
   *
   * @see AdaptrisMessageEncoder#writeMessage(AdaptrisMessage, Object)
   */
  public void writeMessage(AdaptrisMessage msg, Object target)
      throws CoreException {

    try {
      if (!(target instanceof OutputStream)) {
        throw new IllegalArgumentException(
            "MimeEncoder can only encode to an OutputStream");
      }
      OutputStream encodedOutput = (OutputStream) target;
      // Use the message unique id as the message id.
      MultiPartOutput output = new MultiPartOutput(msg.getUniqueId());
      output.addPart(msg.getPayload(), payloadEncoding, PAYLOAD_CONTENT_ID);
      output.addPart(MimeEncoder.getMetadata(msg), metadataEncoding,
          METADATA_CONTENT_ID);
      if (msg.getObjectHeaders().containsKey(
          CoreConstants.OBJ_METADATA_EXCEPTION)) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream(out);
        Exception e = (Exception) msg.getObjectHeaders().get(
            CoreConstants.OBJ_METADATA_EXCEPTION);
        e.printStackTrace(pout);
        pout.flush();
        output.addPart(out.toByteArray(), EXCEPTION_CONTENT_ID);
      }
      encodedOutput.write(output.getBytes());
      encodedOutput.flush();
    }
    catch (Exception e) {
      throw new CoreException("Could not encode the AdaptrisMessage object", e);
    }
  }

  /**
   * Decode into an <code>AdaptrisMessage</code> object.
   * <p>
   * The source object is assumed to be of the type <code>InputStream</code>
   * </p>
   *
   * @see com.adaptris.core.AdaptrisMessageEncoder#readMessage(java.lang.Object)
   */
  public AdaptrisMessage readMessage(Object source) throws CoreException {
    AdaptrisMessage msg = null;

    try {
      msg = currentMessageFactory().newMessage();
      if (!(source instanceof InputStream)) {
        throw new IllegalArgumentException(
            "MimeEncoder can only decode from an OutputStream");
      }
      InputStream encodedInput = (InputStream) source;
      MultiPartInput input = new MultiPartInput(encodedInput);
      byte[] readBytes = input.getPart(PAYLOAD_CONTENT_ID);
      if (readBytes == null) {
        throw new IOException("No Payload found");
      }
      msg.setPayload(readBytes);
      readBytes = input.getPart(METADATA_CONTENT_ID);
      if (readBytes == null) {
        throw new IOException("No Metadata Found");
      }
      msg.setMetadata(getMetadataSet(new ByteArrayInputStream(readBytes)));

      if (retainUniqueId()) {
        msg.setUniqueId(input.getName());
      }
    }
    catch (Exception e) {
      throw new CoreException(
          "Could not parse supplied bytes into an AdaptrisMessage object", e);
    }
    return msg;
  }

  /**
   * Convenience method that is available so that existing underlying
   * implementations are not broken due to the AdaptrisMessageEncoder interface
   * change.
   *
   * @param msg the message to encode as a byte array.
   */
  public byte[] encode(AdaptrisMessage msg) throws CoreException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    writeMessage(msg, out);
    return out.toByteArray();
  }

  /**
   * Convenience method that is available so that existing underlying
   * implementations are not broken due to the AdaptrisMessageEncoder interface
   * change.
   *
   * @param bytes the bytes to decode.
   * @return the AdaptrisMessage.
   * @throws CoreException wrapping any underyling exception.
   */
  public AdaptrisMessage decode(byte[] bytes) throws CoreException {
    AdaptrisMessage msg = null;
    ByteArrayInputStream in = null;
    try {
      in = new ByteArrayInputStream(bytes);
      msg = readMessage(in);
      in.close();
    } catch (IOException e) {
      throw new CoreException(e);
    }
    return msg;
  }

  /**
   * <p>
   * Returns the payload MIME encoding.
   * </p>
   *
   * @return the payload MIME encoding
   */
  public String getPayloadEncoding() {
    return payloadEncoding;
  }

  /**
   * <p>
   * Returns the metadata MIME encoding.
   * </p>
   *
   * @return the metadata MIME encoding
   */
  public String getMetadataEncoding() {
    return metadataEncoding;
  }

  /**
   * <p>
   * Sets the payload MIME encoding.
   * </p>
   *
   * @param encoding the payload MIME encoding
   */
  public void setPayloadEncoding(String encoding) {
    payloadEncoding = encoding;
  }

  /**
   * <p>
   * Sets the metadata MIME encoding.
   * </p>
   *
   * @param encoding the metadata MIME encoding
   */
  public void setMetadataEncoding(String encoding) {
    metadataEncoding = encoding;
  }

  /**
   * <p>
   * Returns true if the original ID of a decoded message should be retained for the new message.
   * </p>
   * 
   * @return true if the original ID of a decoded message should be retained for the new message
   */
  public Boolean getRetainUniqueId() {
    return retainUniqueId;
  }

  /**
   * <p>
   * Sets whether the original ID of a decoded message should be retained for the new message.
   * </p>
   * 
   * @param b true if the original ID should be retained
   */
  public void setRetainUniqueId(Boolean b) {
    retainUniqueId = b;
  }

  public boolean retainUniqueId() {
    return getRetainUniqueId() != null ? getRetainUniqueId().booleanValue() : false;
  }

  /**
   * Get metadata out of an AdaptrisMessage
   *
   * @return a byte array representing the adaptris message
   */
  private static byte[] getMetadata(AdaptrisMessage msg) throws IOException {

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Properties metadata = convertToProperties(msg.getMetadata());
    metadata.store(out, "");
    out.close();
    return out.toByteArray();
  }

  private static Set<MetadataElement> getMetadataSet(InputStream in) throws IOException {
    Properties p = new Properties();
    p.load(in);
    return convertFromProperties(p);
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("PayloadEncoding", getPayloadEncoding())
        .append("MetadataEncoding", getMetadataEncoding()).append("retainUniqueId", retainUniqueId()).toString();
  }
}
