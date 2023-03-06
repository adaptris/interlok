package com.adaptris.core.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Metadata Filter implementation that returns keys in order defined.
 *
 * @config ordered-item-metadata-filter
 */
@JacksonXmlRootElement(localName = "ordered-item-metadata-filter")
@XStreamAlias("ordered-item-metadata-filter")
public class OrderedItemMetadataFilter extends MetadataFilterImpl {

  @NotNull
  @Valid
  @XStreamImplicit(itemFieldName = "metadata-key")
  private List<String> metadataKeys = new ArrayList<>();

  @AdvancedConfig
  private Boolean ignoreCase;

  @Override
  public MetadataCollection filter(MetadataCollection originalCollection) {
    MetadataCollection results = new MetadataCollection();
    Map<String, String> original;
    if(ignoreCase()) {
      original = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      original.putAll(MetadataCollection.asMap(originalCollection));
    } else {
      original = MetadataCollection.asMap(originalCollection);
    }
    for (String key : getMetadataKeys()) {
      String value = original.get(key);
      if (value != null) {
        results.add(new MetadataElement(key, value));
      } else {
        results.add(new MetadataElement(key, ""));
      }
    }
    return results;
  }

  public void setMetadataKeys(List<String> metadataKeys) {
    this.metadataKeys = metadataKeys;
  }

  public List<String> getMetadataKeys() {
    return metadataKeys;
  }

  public void setIgnoreCase(Boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
  }

  public Boolean getIgnoreCase() {
    return ignoreCase;
  }

  private boolean ignoreCase(){
    return getIgnoreCase() != null ? getIgnoreCase() : false;
  }
}
