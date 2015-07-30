package com.adaptris.core.services.exception;

import static org.apache.commons.lang.StringUtils.isBlank;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ExceptionGenerator} implementation that generates the exception from metadata.
 * 
 * <p>
 * Use this class with {@link ThrowExceptionService} to throw an exception as part of a workflow. The exception message (i.e.
 * {@link Exception#getMessage()}) is derived from the configured metadata key. If the metadata key does not exist (or returns no
 * value) then no exception will be thrown. This behaviour differs from {@link ExceptionFromMetadata}
 * 
 * @config possible-exception-from-metadata
 * @author lchan
 * 
 */
@XStreamAlias("possible-exception-from-metadata")
public class PossibleExceptionFromMetadata extends ExceptionFromMetadataImpl {

  public PossibleExceptionFromMetadata() {
  }

  public PossibleExceptionFromMetadata(String metadataKey) {
    this();
    setExceptionMessageMetadataKey(metadataKey);
  }

  public ServiceException create(AdaptrisMessage msg) {
    if (getExceptionMessageMetadataKey() != null && msg.containsKey(getExceptionMessageMetadataKey())) {
      String message = msg.getMetadataValue(getExceptionMessageMetadataKey());
      if (!isBlank(message)) {
        return new ServiceException(message);
      }
    }
    return null;
  }
}
