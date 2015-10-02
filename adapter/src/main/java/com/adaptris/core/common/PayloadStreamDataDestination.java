package com.adaptris.core.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataDestination;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This {@link DataDestination} is used when you want to source/target data to/from the {@link AdaptrisMessage}'s payload.
 * </p>
 * <p>
 * An example might be specifying that the XML content required for the {@link XPathService} can be found in
 * the payload of an {@link AdaptrisMessage}.
 * </p>
 * 
 * @author amcgrath
 * @config payload-stream-destination
 * @license BASIC
 */
@XStreamAlias("payload-stream-destination")
public class PayloadStreamDataDestination implements DataDestination<InputStream> {

  public PayloadStreamDataDestination() {
    
  }
  
  @Override
  public InputStream getData(InterlokMessage message) throws InterlokException {
    InputStream result = null;
    try {
      result = message.getInputStream();
    } catch (IOException e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return result;
  }

  @Override
  public void setData(InterlokMessage message, InputStream in) throws InterlokException {
    try (OutputStream bufOut = new BufferedOutputStream(message.getOutputStream()); InputStream bufIn = new BufferedInputStream(in)) {
      IOUtils.copy(bufIn, bufOut);
    } catch (IOException e) {
      ExceptionHelper.rethrowCoreException(e);;
    }
  }

}
