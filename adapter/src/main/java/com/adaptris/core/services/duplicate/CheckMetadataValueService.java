package com.adaptris.core.services.duplicate;

import static org.apache.commons.lang.StringUtils.isEmpty;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Branching <code>Service</code> implementation which checks the value stored against a configured metadata key against a list of
 * previously received values. If the value to check is null or empty, a <code>ServiceException</code> is thrown. If the value has
 * previously been received, the configured <code>nextServiceIdIfDuplicate</code> is set on the message. If the value is not
 * contained in the store of previous values, <code>nextServiceIdIfUnique</code> is set.
 * </p>
 * 
 * @config check-metadata-value-service
 */
@XStreamAlias("check-metadata-value-service")
public class CheckMetadataValueService extends DuplicateMetadataValueService {

  @NotBlank
  private String nextServiceIdIfDuplicate;
  @NotBlank
  private String nextServiceIdIfUnique;
  
  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public CheckMetadataValueService() {
    
  }

  @Override
  public void init() throws CoreException {
    if (nextServiceIdIfDuplicate == null) {
      throw new CoreException("Next Service ID if Duplicate is null");
    }
    if (nextServiceIdIfUnique == null) {
      throw new CoreException("Next Service ID if Unique is null");
    }
    super.init();
  }
  
  /** @see com.adaptris.core.Service
   *   #doService(com.adaptris.core.AdaptrisMessage) */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    loadPreviouslyReceivedValues();
    
    String value = msg.getMetadataValue(getMetadataKey());
    
    if (isEmpty(value)) {
      throw new ServiceException
        ("required metadata [" + getMetadataKey() + "] missing");
    }
    
    if (previousValuesStore.contains(value)) {
      handleDuplicate(msg, value);
    }
    else {
      log.debug("unique value [" + value + "] received");
      msg.setNextServiceId(getNextServiceIdIfUnique());
    }
  }

  private void handleDuplicate(AdaptrisMessage msg, String value) 
    throws ServiceException {
    
    String errorMessage = createErrorMessage(value);
    log.warn(errorMessage);
    
    msg.setNextServiceId(getNextServiceIdIfDuplicate());
  }
  
  /** 
   * <p>
   * This is a branching service, even though its parent(s) aren't.
   * </p>
   * @see com.adaptris.core.ServiceImp#isBranching() 
   */
  @Override
  public boolean isBranching() {
    return true;
  }

  private String createErrorMessage(String value) {
    StringBuffer result = new StringBuffer();
    result.append("value [");
    result.append(value);
    result.append("] stored against key [");
    result.append(getMetadataKey());
    result.append("] exists in list of previously stored values");
    
    return result.toString();
  }


  // properties...

  /**
   * <p>
   * Returns the ID of the next <code>Service</code> to apply if the metadata
   * exists if the store of previous values.
   * </p>
   * @return the ID of the next <code>Service</code> to apply if the metadata
   * exists if the store of previous values
   */
  public String getNextServiceIdIfDuplicate() {
    return nextServiceIdIfDuplicate;
  }

  /**
   * <p>
   * Sets the ID of the next <code>Service</code> to apply if the metadata
   * exists if the store of previous values. May not be null or empty.
   * </p>
   * @param s the ID of the next <code>Service</code> to apply if the metadata
   * exists if the store of previous values
   */
  public void setNextServiceIdIfDuplicate(String s) {
    if (isEmpty(s)) {
      throw new IllegalArgumentException("null or empty param");
    }
    nextServiceIdIfDuplicate = s;
  }

  /**
   * <p>
   * Returns the ID of the next <code>Service</code> to apply if the metadata
   * does not exist if the store of previous values.
   * </p>
   * @return the ID of the next <code>Service</code> to apply if the metadata
   * does not exist if the store of previous values
   */
  public String getNextServiceIdIfUnique() {
    return nextServiceIdIfUnique;
  }

  /**
   * <p>
   * Sets the ID of the next <code>Service</code> to apply if the metadata
   * does not exist if the store of previous values. May not be null or empty.
   * </p>
   * @param s the ID of the next <code>Service</code> to apply if the metadata
   * does not exist if the store of previous values
   */
  public void setNextServiceIdIfUnique(String s) {
    if (isEmpty(s)) {
      throw new IllegalArgumentException("null or empty param");
    }
    nextServiceIdIfUnique = s;
  }  
}
