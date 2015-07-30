package com.adaptris.core.metadata;

import java.util.Set;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Metadata Filter implementation that does no filtering.
 * 
 * @config no-op-metadata-filter
 */
@XStreamAlias("no-op-metadata-filter")
public class NoOpMetadataFilter implements MetadataFilter {


  public NoOpMetadataFilter() {
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
   * Simply returns a shallow clone of the original metadata set.
   *
   */
  @Override
  public MetadataCollection filter(MetadataCollection original) {
    return (MetadataCollection) original.clone();
  }

}
