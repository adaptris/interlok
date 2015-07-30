package com.adaptris.core.services;

import java.io.InputStream;
import java.io.OutputStream;

import javax.mail.internet.MimeUtility;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.adaptris.util.text.mime.MimeConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Base 64 Encode the message.
 * 
 * @config base64-encode-service
 * @license BASIC
 */
@XStreamAlias("base64-encode-service")
public class Base64EncodeService extends ServiceImp {

  /**
   * @see com.adaptris.core.Service#doService(com.adaptris.core.AdaptrisMessage)
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {

    OutputStream out = null;
    InputStream in = null;
    try {
      in = msg.getInputStream();
      out = MimeUtility.encode(msg.getOutputStream(), MimeConstants.ENCODING_BASE64);
      IOUtils.copy(in, out);
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
    finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#close()
   */
  public void close() {
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  public void init() throws CoreException {
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }
}
