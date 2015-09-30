package com.adaptris.core;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.adaptris.util.IdGenerator;

/**
 * {@link AdaptrisMessage} implementation created by {@link DefaultMessageFactory}
 *
 * @author lchan
 * @author $Author: lchan $
 */
public class DefaultAdaptrisMessageImp extends AdaptrisMessageImp {

  private byte[] payload;

  protected DefaultAdaptrisMessageImp(IdGenerator guid, AdaptrisMessageFactory fac) throws RuntimeException {
    super(guid, fac);
    setPayload(new byte[0]);
  }

  /**
   * @see com.adaptris.core.AdaptrisMessage#equivalentForTracking (com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public boolean equivalentForTracking(AdaptrisMessage other) {
    boolean result = false;

    if (areEqual(getUniqueId(), other.getUniqueId())) {
      if (Arrays.equals(getPayload(), other.getPayload())) {
        if (areEqual(getCharEncoding(), other.getCharEncoding())) {
          if (this.getMetadata().equals(other.getMetadata())) {
            result = true;
          }
        }
      }
    }
    return result;
  }

  @Override
  protected String getPayloadForLogging() {
    return getStringPayload();
  }

  /** @see AdaptrisMessage#setPayload(byte[]) */
  @Override
  public void setPayload(byte[] bytes) {
    if (bytes == null) {
      payload = new byte[0];
    }
    else {
      payload = bytes;
    }
  }

  /** @see AdaptrisMessage#getPayload() */
  @Override
  public byte[] getPayload() {
    byte[] result = null;

    if (payload != null) {
      result = new byte[payload.length];
      System.arraycopy(payload, 0, result, 0, payload.length);
    }

    return result;
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisMessage#getSize()
   */
  @Override
  public long getSize() {
    return payload != null ? payload.length : 0;
  }

  /** @see AdaptrisMessage#setStringPayload(String) */
  @Override
  public void setStringPayload(String s) {
    setStringPayload(s, null);
  }

  /** @see AdaptrisMessage#setStringPayload(String, String) */
  @Override
  public void setStringPayload(String payloadString, String charEnc) {
    this.setContent(payloadString, charEnc);
  }
  
  /** @see AdaptrisMessage#setContent(String, String) */
  public void setContent(String payloadString, String charEnc) {
    if (payloadString != null) {
      try {
        if (!isEmpty(charEnc)) {
          payload = payloadString.getBytes(charEnc);
        }
        else {
          payload = payloadString.getBytes();
        }
        setCharEncoding(charEnc);
      }
      catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
    }
    else {
      payload = new byte[0];
      setCharEncoding(charEnc);
    }
  }
  
  

  /** @see AdaptrisMessage#getStringPayload() */
  @Override
  public String getStringPayload() {
    return this.getContent();
  }
  
  /** @see AdaptrisMessage#getContent() */
  @Override
  public String getContent() {
    if (payload != null) {
      if (isEmpty(getCharEncoding())) {
        return new String(payload);
      }
      else {
        try { // want this to be runtime Exc.
          return new String(payload, getCharEncoding());
        }
        catch (UnsupportedEncodingException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return null;
  }

  /** @see Object#clone() */
  @Override
  public Object clone() throws CloneNotSupportedException {
    DefaultAdaptrisMessageImp result = (DefaultAdaptrisMessageImp) super.clone();
    // clone the payload.
    try {
      byte[] newPayload = new byte[payload.length];
      System.arraycopy(payload, 0, newPayload, 0, payload.length);
      result.setPayload(newPayload);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisMessage#getInputStream()
   */
  @Override
  public InputStream getInputStream() throws IOException {
    return payload != null ? new ByteArrayInputStream(payload) : new ByteArrayInputStream(new byte[0]);
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisMessage#getOutputStream()
   */
  @Override
  public OutputStream getOutputStream() throws IOException {
    return new ByteFilterStream(new ByteArrayOutputStream());
  }

  private class ByteFilterStream extends FilterOutputStream {
    ByteFilterStream(OutputStream out) {
      super(out);
    }

    @Override
    public void close() throws IOException {
      super.close();
      payload = ((ByteArrayOutputStream) super.out).toByteArray();
    }
  }

}
