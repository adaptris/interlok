package com.adaptris.core.metadata;

import java.util.Arrays;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MetadataFilter} that just uses the configured {@link KeyValuePairSet} as the metadata.
 * <p>
 * This class will have the effect of replacing all metadata with a fixed set of metadata.
 * </p>
 * 
 * @config fixed-values-metadata-filter
 * @since 3.9.0
 */
@XStreamAlias("fixed-values-metadata-filter")
@ComponentProfile(summary = "Replaces all metadata with a fixed set of metadata", since = "3.9.0")
public class FixedValuesMetadataFilter extends MetadataFilterImpl {
  
  @Valid
  @NotNull
  @AutoPopulated
  private KeyValuePairSet metadata;
  
  public FixedValuesMetadataFilter() {
    setMetadata(new KeyValuePairSet());
  }

  @Override
  public MetadataCollection filter(MetadataCollection original) {
    MetadataCollection result = new MetadataCollection();
    getMetadata().forEach((e) -> {
      result.add(new MetadataElement(e.getKey(), e.getValue()));
    });
    return result;
  }

  public KeyValuePairSet getMetadata() {
    return metadata;
  }

  public void setMetadata(KeyValuePairSet metadata) {
    this.metadata = Args.notNull(metadata, "metadata");
  }

  public FixedValuesMetadataFilter withMetadata(KeyValuePairSet kvps) {
    setMetadata(kvps);
    return this;
  }
  
  public FixedValuesMetadataFilter withMetadata(KeyValuePair...keyValuePairs) {
    return withMetadata(new KeyValuePairSet(Arrays.asList(keyValuePairs)));
  }
}
