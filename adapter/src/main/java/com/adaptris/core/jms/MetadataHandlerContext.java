package com.adaptris.core.jms;

import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;

/**
 * Interface that abstracts the handling of AdaptrisMessage metadata and JMS
 * Headers away from the MessageTypeTranslator.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public interface MetadataHandlerContext {
  /**
   * <p>
   * Returns true if JMS Headers should be copied as metadata and vice-versa
   * </p>
   *
   * @return true if JMS Headers (as well as JMS Properties) should be copied, otherwise false
   */
  boolean moveJmsHeaders();

  /**
   * @return the reportAllErrors
   */
  boolean reportAllErrors();

  /**
   * <p>
   * Returns whether to move metadata to JMS properties and vice versa.
   * </p>
   * 
   * @return whether to move metadata to JMS properties and vice versa
   * @deprecated since 3.0.2 use {@link RemoveAllMetadataFilter} to stop any metadata from being transferred.
   */
  @Deprecated
  boolean moveMetadata();

  /**
   * Get the metadata filter implementation to be used when converting between AdaptrisMessage and JMS Message objects.
   *
   * @return the metadata filter to use.
   */
  MetadataFilter metadataFilter();

}
