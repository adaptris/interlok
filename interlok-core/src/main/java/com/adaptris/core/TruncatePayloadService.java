package com.adaptris.core;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.services.splitter.BytesSplitter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Truncate the message payload based on byte size
 */
@XStreamAlias("truncate-payload-service")
@AdapterComponent
@ComponentProfile(summary = "Truncate the payload based on byte size", tag = "payload,truncate")
public class TruncatePayloadService extends ServiceImp {
  private transient Logger log = LoggerFactory.getLogger(TruncatePayloadService.class.getName());

  private Integer bytes;

  public Integer getBytes() {
    return bytes;
  }

  public void setBytes(Integer bytes) {
    this.bytes = bytes;
  }

  /**
   * @param msg
   *          The message whose payload to truncate.
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    int bytesTruncatedTo;
    try (InputStream is = msg.getInputStream(); OutputStream os = msg.getOutputStream()) {
      bytesTruncatedTo = BytesSplitter.truncate(is, getBytes(), os);
    } catch (IOException ex) {
      throw new ServiceException(ex);
    }
    log.debug("Truncated message payload to {} bytes", bytesTruncatedTo);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  protected void initService() {
    /* unused */
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  protected void closeService() {
    /* unused */
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public void prepare() {
    /* unused */
  }
}
