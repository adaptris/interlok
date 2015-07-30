package com.adaptris.core.services.metadata;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;

/**
 * <p>
 * Implementation of <code>Service</code> that reformats matching metadata.
 * </p>
 * <p>
 * Each matching metadata key from {@link ReformatMetadata#getMetadataKeyRegexp()} will be taken and passed to concrete subclasses
 * to modify.
 * </p>
 * 
 * @license BASIC
 * @see ReformatDateService
 * @see TrimMetadataService
 * @see ReplaceMetadataValue
 */
public abstract class ReformatMetadata extends ServiceImp {

  @NotBlank
  private String metadataKeyRegexp;

  public ReformatMetadata() {
  }

  public ReformatMetadata(String regexp) {
    this();
    setMetadataKeyRegexp(regexp);
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void close() {
  }

  /**
   * <p>
   * Adds the configured metadata to the message.
   * </p>
   *
   * @param msg the message to process
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    if (isBlank(getMetadataKeyRegexp())) {
      return;
    }
    try {
      Set<MetadataElement> metadata = msg.getMetadata();
      Set<MetadataElement> modifiedMetadata = new HashSet<MetadataElement>();
      for (MetadataElement e : metadata) {
        if (e.getKey().matches(metadataKeyRegexp)) {
          e.setValue(reformat(e.getValue(), msg.getCharEncoding()));
          modifiedMetadata.add(e);
        }
      }
      log.trace("Modified metadata : " + modifiedMetadata);
      msg.setMetadata(modifiedMetadata);
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
  }

  protected abstract String reformat(String s, String msgCharset) throws Exception;

  /**
   * @return the metadataKeyRegexp
   */
  public String getMetadataKeyRegexp() {
    return metadataKeyRegexp;
  }

  /**
   * Set the regular expression to match against.
   *
   * @param s the metadataKeyRegexp to set
   */
  public void setMetadataKeyRegexp(String s) {
    metadataKeyRegexp = s;
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }

}
