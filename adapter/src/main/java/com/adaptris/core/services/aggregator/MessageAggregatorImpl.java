package com.adaptris.core.services.aggregator;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;

/**
 * Abstract implementation of {@link MessageAggregator}.
 * 
 * @author lchan
 * 
 */
public abstract class MessageAggregatorImpl implements MessageAggregator {

  private Boolean overwriteMetadata;

  /**
   * @return the overwriteMetadata
   */
  public Boolean getOverwriteMetadata() {
    return overwriteMetadata;
  }

  /**
   * Whether or not to overwrite original metadata with metadata from the split messages.
   * 
   * @param b the overwriteMetadata to set, default is null (false)
   */
  public void setOverwriteMetadata(Boolean b) {
    this.overwriteMetadata = b;
  }

  protected boolean overwriteMetadata() {
    return getOverwriteMetadata() != null ? getOverwriteMetadata().booleanValue() : false;
  }

  protected void overwriteMetadata(AdaptrisMessage src, AdaptrisMessage target) {
    if (overwriteMetadata()) {
      target.setMetadata(src.getMetadata());
    }
  }

  @Deprecated
  protected static void rethrowCoreException(Exception e) throws CoreException {
    ExceptionHelper.rethrowCoreException(e);
  }
}
