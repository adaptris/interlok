package com.adaptris.core.services.findreplace;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;

/**
 * Interface for handling how find and replace operations occur for {@link FindAndReplaceService}.
 */
public interface ReplacementSource {

  /**
   * <p>
   * Obtains a replacement value for {@link FindAndReplaceService} based on the passed configured value.
   * </p>
   * 
   * @param msg the {@link AdaptrisMessage} being processed
   * @return the String to be used as the replacement.
   */
  String obtainValue(AdaptrisMessage msg) throws ServiceException;
}
