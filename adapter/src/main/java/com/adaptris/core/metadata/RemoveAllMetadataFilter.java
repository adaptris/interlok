package com.adaptris.core.metadata;

import java.util.Set;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Metadata Filter implementation that removes all metadata.
 * 
 * @config remove-all-metadata-filter
 * @since 3.0.2
 */
@XStreamAlias("remove-all-metadata-filter")
public class RemoveAllMetadataFilter implements MetadataFilter {


  public RemoveAllMetadataFilter() {
  }

  @Override
  public MetadataCollection filter(AdaptrisMessage message) {
    return filter(message.getMetadata());
  }

  @Override
  public MetadataCollection filter(Set<MetadataElement> original) {
    return filter(new MetadataCollection(original));
  }

  /**
   * Returns a new empty {@link MetadataCollection}.
   * 
   */
  @Override
  public MetadataCollection filter(MetadataCollection original) {
    return new MetadataCollection();
  }

}
