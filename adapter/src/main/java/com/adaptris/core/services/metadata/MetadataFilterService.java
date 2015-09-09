package com.adaptris.core.services.metadata;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of {@link Service} that filters metadata keys based on a {@link MetadataFilter}.
 * </p>
 * 
 * @config metadata-filter-service
 * 
 * @license BASIC
 * @see java.util.regex.Pattern
 */
@XStreamAlias("metadata-filter-service")
public class MetadataFilterService extends ServiceImp {

  @NotNull
  @AutoPopulated
  @Valid
  private MetadataFilter filter;

  public MetadataFilterService() {
    setFilter(new NoOpMetadataFilter());
  }

  @Override
  public void doService(AdaptrisMessage msg) {
    log.trace("Filtering metadata using [" + filter.getClass().getCanonicalName() + "]");
    MetadataCollection filtered = filter.filter(msg);
    msg.clearMetadata();
    StringBuffer filteredKeys = new StringBuffer("Metadata keys preserved:");
    for (MetadataElement e : filtered) {
      filteredKeys.append(" ");
      filteredKeys.append(e.getKey());
      msg.addMetadata(e);
    }
    log.trace(filteredKeys.toString());
  }

  @Override
  public void init() throws CoreException {
    // na
  }

  @Override
  public void close() {
    // na
  }

  public MetadataFilter getFilter() {
    return filter;
  }

  public void setFilter(MetadataFilter mf) {
    if (mf == null) {
      throw new IllegalArgumentException("Filter may not be null");
    }
    filter = mf;
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }
}
