package com.adaptris.core.services.metadata.compare;

import com.adaptris.core.MetadataElement;

/** Compare two items of metadata returning the result of the comparison.
 * 
 * @author lchan
 *
 */
public interface MetadataComparator {

  MetadataElement compare(MetadataElement firstItem, MetadataElement secondItem);
}
