package com.adaptris.core.services.codec;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageEncoder;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Decodes the message.
 *
 * @config decoding-processMessage
 *
 *
 */
@XStreamAlias("decoding-service")
@AdapterComponent
@ComponentProfile(summary = "Decodes the message", tag = "service")
public class DecodingService extends CodecService {

  public DecodingService(){
  }

  public DecodingService(AdaptrisMessageEncoder encoder) {
    super(encoder);
  }

  private boolean overrideHeaders;

  @Override
  public void processMessage(AdaptrisMessage msg) throws ServiceException {
    InputStream msgIn = null;
    OutputStream msgOut = null;
    ByteArrayInputStream decodedPayload = null;
    try {
      msgIn = msg.getInputStream();
      AdaptrisMessage decodedMsg = getEncoder().readMessage(msgIn);
      for (MetadataElement me : decodedMsg.getMetadata()) {
        if (!isOverrideHeaders() && msg.headersContainsKey(me.getKey())){
          continue;
        }
        msg.addMetadata(me);
      }
      decodedPayload = new ByteArrayInputStream(decodedMsg.getPayload());
      msgOut = msg.getOutputStream();
      IOUtils.copy(decodedPayload, msgOut);
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
    finally {
      IOUtils.closeQuietly(msgIn);
      IOUtils.closeQuietly(msgOut);
      IOUtils.closeQuietly(decodedPayload);
    }
  }

  public boolean isOverrideHeaders(){
    return getOverrideHeaders() == null ? false : getOverrideHeaders();
  }

  public Boolean getOverrideHeaders(){
    return this.overrideHeaders;
  }

  public void setOverrideHeaders(boolean overrideHeaders) {
    this.overrideHeaders = overrideHeaders;
  }

}
