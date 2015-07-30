package com.adaptris.core.services;

import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Gzip the given payload.
 * <p>
 * This simply uses {@link GZIPOutputStream} in order to create the compressed bytes.
 * </p>
 * 
 * @config gzip-service
 * 
 * @license BASIC
 */
@XStreamAlias("gzip-service")
public class GzipService extends ServiceImp {

  /**
   *  @see com.adaptris.core.Service#doService(AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    InputStream in = null;
    GZIPOutputStream out = null;
    try {
      in = msg.getInputStream();
      out = new GZIPOutputStream(msg.getOutputStream());
      IOUtils.copy(in, out);
    }
    catch (Exception e) {
      throw new ServiceException(e);
    } finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }

  /** @see com.adaptris.core.AdaptrisComponent#close()
   */
  @Override
  public void close() {
  }

  /** @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() {
  }
}
