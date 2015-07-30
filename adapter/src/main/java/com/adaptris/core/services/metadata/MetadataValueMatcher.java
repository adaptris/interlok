/*
 * $RCSfile: $
 * $Revision: $
 * $Date: $
 * $Author: $
 */
package com.adaptris.core.services.metadata;

import com.adaptris.util.KeyValuePairSet;

/**
 * Interface for use in {@link MetadataValueBranchingService}.
 ** <p>
 * In the adapter configuration file this class is aliased as <b>metadata-value-matcher</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 
 * @author lchan
 * @author $Author: $
 */
public interface MetadataValueMatcher {

  /**
   * Get the next service id from the mapping.
   * 
   * @param serviceKey the service key generated from metadata
   * @param mappings the list of mappings from
   *          {@link MetadataValueBranchingService#getMetadataToServiceIdMappings()}
   * @return the service key that was found, null otherwise.
   */
  String getNextServiceId(String serviceKey, KeyValuePairSet mappings);
}
