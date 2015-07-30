package com.adaptris.core.services;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BranchingServiceImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Branching <code>Service</code> which sets the unique ID of the next <code>Service</code> to apply based on the size of the
 * <code>AdaptrisMessage</code>.
 * </p>
 * <p>
 * If the size of the message is exactly equal to the specified criteria then the smaller service id is selected.
 * </p>
 * 
 * @config size-based-branching-service
 * @license STANDARD
 */
@XStreamAlias("size-based-branching-service")
public class SizeBasedBranchingService extends BranchingServiceImp {
  @NotBlank
  private String greaterThanServiceId;
  @NotBlank
  private String smallerThanServiceId;
  private long sizeCriteriaBytes;

  /**
   * Creates a new instance.
   * <p>
   * size-criteria-bytes = 1024 * 1024 * 10 (10Mb)
   * </p>
   */
  public SizeBasedBranchingService() {
    setSizeCriteriaBytes(1024 * 1024 * 10);
  }

  /**
   * 
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
    if (getGreaterThanServiceId() == null || getSmallerThanServiceId() == null) {
      throw new CoreException("Service id's may not be null");
    }
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#close()
   */
  @Override
  public void close() {
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    if (msg.getSize() > getSizeCriteriaBytes()) {
      msg.setNextServiceId(getGreaterThanServiceId());
    } else {
      msg.setNextServiceId(getSmallerThanServiceId());
    }
  }

  /**
   * @return the greaterThanServiceId
   */
  public String getGreaterThanServiceId() {
    return greaterThanServiceId;
  }

  /**
   * @param serviceId the greaterThanServiceId to set
   */
  public void setGreaterThanServiceId(String serviceId) {
    greaterThanServiceId = serviceId;
  }

  /**
   * @return the smallerThanServiceId
   */
  public String getSmallerThanServiceId() {
    return smallerThanServiceId;
  }

  /**
   * @param serviceId the smallerThanServiceId to set
   */
  public void setSmallerThanServiceId(String serviceId) {
    smallerThanServiceId = serviceId;
  }

  /**
   * @return the sizeCriteriaBytes
   */
  public long getSizeCriteriaBytes() {
    return sizeCriteriaBytes;
  }

  /**
   * @param l the sizeCriteriaBytes to set
   */
  public void setSizeCriteriaBytes(long l) {
    sizeCriteriaBytes = l;
  }

}
