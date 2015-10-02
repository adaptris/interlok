package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Changes the character encoding associated with a message.
 * <p>
 * this service does nothing with the data, but simply changes the character encoding associated with the message using
 * {@link AdaptrisMessage#setCharEncoding(String)}. If this service is used, and there is no configured character encoding then the
 * character encoding associated with the message is set to null (which forces the platform default encoding).
 * </p>
 * 
 * @config change-char-encoding-service
 * 
 * @license BASIC
 */
@XStreamAlias("change-char-encoding-service")
public class ChangeCharEncodingService extends ServiceImp {

  private String charEncoding;

  public ChangeCharEncodingService() {
    super();
    setCharEncoding(null);
  }

  public ChangeCharEncodingService(String cs) {
    this();
    setCharEncoding(cs);
  }


  /**
   * @see com.adaptris.core.Service #doService(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    msg.setContentEncoding(getCharEncoding());
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public void init() throws CoreException {
    // na
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
    // na
  }

  public String getCharEncoding() {
    return charEncoding;
  }

  /**
   * Set the character encoding
   *
   * @param s the character encoding
   */
  public void setCharEncoding(String s) {
    charEncoding = s;
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }
}
