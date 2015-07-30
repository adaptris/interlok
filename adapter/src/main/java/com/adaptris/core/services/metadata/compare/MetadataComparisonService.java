package com.adaptris.core.services.metadata.compare;

import static org.apache.commons.lang.StringUtils.isBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link Service} that compares two items of metadata.
 * 
 * <p>
 * Sometimes you just want to compare two metadata values and store the result against a 3rd metadata key. Well this does that.
 * </p>
 * 
 * @config metadata-comparison-service
 * 
 * @license BASIC
 */
@XStreamAlias("metadata-comparison-service")
public class MetadataComparisonService extends ServiceImp {

  @NotBlank
  private String firstKey;
  @NotBlank
  private String secondKey;
  @NotNull
  @Valid
  private MetadataComparator comparator;

  public MetadataComparisonService() {
    super();
  }

  public MetadataComparisonService(String first, String second, MetadataComparator mc) {
    this();
    setFirstKey(first);
    setSecondKey(second);
    setComparator(mc);
  }

  public void doService(AdaptrisMessage msg) throws ServiceException {
    msg.addMetadata(getComparator().compare(msg.getMetadata(getFirstKey()), msg.getMetadata(getSecondKey())));
  }

  public void init() throws CoreException {
    if (isBlank(getFirstKey())) {
      throw new CoreException("1st Metadata Key is blank");
    }
    if (isBlank(getSecondKey())) {
      throw new CoreException("2nd Metadata Key is blank");
    }
    if (getComparator() == null) {
      throw new CoreException("Comparator is null");
    }
  }

  public void close() {
    // na
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }

  public String getFirstKey() {
    return firstKey;
  }

  public void setFirstKey(String key) {
    this.firstKey = key;
  }

  public String getSecondKey() {
    return secondKey;
  }

  public void setSecondKey(String key) {
    this.secondKey = key;
  }

  public MetadataComparator getComparator() {
    return comparator;
  }

  public void setComparator(MetadataComparator mc) {
    this.comparator = mc;
  }

}
