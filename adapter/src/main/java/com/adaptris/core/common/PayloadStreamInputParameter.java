package com.adaptris.core.common;

import java.io.IOException;
import java.io.InputStream;

import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This {@link DataInputParameter} is used when you want to source/target data to/from the {@link AdaptrisMessage} payload.
 * </p>
 * 
 * 
 * @author amcgrath
 * @config stream-payload-input-parameter
 * @license BASIC
 */
@XStreamAlias("stream-payload-input-parameter")
public class PayloadStreamInputParameter implements DataInputParameter<InputStream> {

  public PayloadStreamInputParameter() {
    
  }
  
  @Override
  public InputStream extract(InterlokMessage message) throws InterlokException {
    InputStream result = null;
    try {
      result = message.getInputStream();
    } catch (IOException e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return result;
  }

}
