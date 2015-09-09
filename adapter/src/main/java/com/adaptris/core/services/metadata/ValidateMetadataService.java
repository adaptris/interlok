package com.adaptris.core.services.metadata;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Verify that a message has all the required metadata keys set.
 * <p>
 * If any of the required keys does not have a values stored against it, a <code>ServiceException</code> is thrown.
 * </p>
 * 
 * @config validate-metadata-service
 * @license BASIC
 */
@XStreamAlias("validate-metadata-service")
public class ValidateMetadataService extends ServiceImp {

  @XStreamImplicit(itemFieldName = "required-key")
  @NotNull
  @AutoPopulated
  private List<String> requiredKeys;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public ValidateMetadataService() {
    requiredKeys = new ArrayList<String>();
  }

  public ValidateMetadataService(List<String> list) {
    this();
    setRequiredKeys(list);
  }

  public void doService(AdaptrisMessage msg) throws ServiceException {

    for (String requiredKey : requiredKeys) {
      if (msg.getMetadataValue(requiredKey) == null || "".equals(msg.getMetadataValue(requiredKey))) {
        throw new ServiceException("required metadata key [" + requiredKey + "] null, empty or not present");
      }
    }
  }

  /**
   * <p>
   * Returns the <code>List</code> of keys which must be present and have non
   * empty values.
   * </p>
   *
   * @return the <code>List</code> of keys which must be present and have non
   *         empty values
   */
  public List<String> getRequiredKeys() {
    return requiredKeys;
  }

  /**
   * <p>
   * Sets the <code>List</code> of keys which must be present and have non empty
   * values.
   * </p>
   *
   * @param l the <code>List</code> of keys which must be present and have non
   *          empty values
   */
  public void setRequiredKeys(List<String> l) {
    requiredKeys = l;
  }

  /**
   * <p>
   * Add a key to the <code>List</code>.
   * </p>
   *
   * @param key the key to add
   */
  public void addRequiredKey(String key) {
    if (key == null || "".equals(key)) {
      throw new IllegalArgumentException();
    }
    requiredKeys.add(key);
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  public void init() throws CoreException {
    // na
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  public void close() {
    // na
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }
}
