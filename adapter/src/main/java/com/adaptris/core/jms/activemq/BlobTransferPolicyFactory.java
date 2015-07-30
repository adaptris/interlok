package com.adaptris.core.jms.activemq;

import static org.apache.commons.lang.StringUtils.isBlank;

import org.apache.activemq.blob.BlobTransferPolicy;
import org.apache.activemq.blob.BlobUploadStrategy;
import org.apache.activemq.blob.DefaultBlobUploadStrategy;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Proxy class for creating BlobTransferPolicy objects
 * 
 * <p>
 * This class is simply a class that can be marshalled correctly.
 * </p>
 * <p>
 * As {@link DefaultBlobUploadStrategy} does not conform to the marshalling requirements of a no-param constructor it should not be
 * explicitly configured.
 * </p>
 * <p>
 * If fields are not explicitly set, then the corresponding {@link BlobTransferPolicy} method will not be invoked.
 * </p>
 * 
 * @config activemq-blob-transfer-policy
 * @author lchan
 * 
 */
@XStreamAlias("activemq-blob-transfer-policy")
public class BlobTransferPolicyFactory {
  private String brokerUploadUrl = null;
  private String defaultUploadUrl = null;
  private String uploadUrl = null;
  private BlobUploadStrategy uploadStrategy = null;
  private Integer bufferSize = null;

  /**
   * Default constructor.
   * <p>
   * All fields are initialised to be null.
   * </p>
   */
  public BlobTransferPolicyFactory() {
  }

  /**
   * Create a BlobTransferPolicy.
   *
   * @return a BlobTransferPolicy
   */
  public BlobTransferPolicy create() {
    BlobTransferPolicy btp = new BlobTransferPolicy();
    if (!isBlank(getBrokerUploadUrl())) {
      btp.setBrokerUploadUrl(getBrokerUploadUrl());
    }

    if (!isBlank(getDefaultUploadUrl())) {
      btp.setDefaultUploadUrl(getDefaultUploadUrl());
    }
    if (!isBlank(getUploadUrl())) {
      btp.setUploadUrl(getUploadUrl());
    }
    if (getBufferSize() != null) {
      btp.setBufferSize(getBufferSize());
    }
    if (getUploadStrategy() != null) {
      btp.setUploadStrategy(getUploadStrategy());
    }
    return btp;
  }

  /**
   * @see BlobTransferPolicy#getBrokerUploadUrl()
   */
  public String getBrokerUploadUrl() {
    return brokerUploadUrl;
  }

  /**
   * @see BlobTransferPolicy#setBrokerUploadUrl(String)
   */
  public void setBrokerUploadUrl(String url) {
    brokerUploadUrl = url;
  }

  /** @see BlobTransferPolicy#getDefaultUploadUrl() */
  public String getDefaultUploadUrl() {
    return defaultUploadUrl;
  }

  /** @see BlobTransferPolicy#setDefaultUploadUrl(String) */
  public void setDefaultUploadUrl(String url) {
    defaultUploadUrl = url;
  }

  /** @see BlobTransferPolicy#getUploadUrl() */
  public String getUploadUrl() {
    return uploadUrl;
  }

  /** @see BlobTransferPolicy#setUploadUrl(String) */
  public void setUploadUrl(String url) {
    uploadUrl = url;
  }

  /** @see BlobTransferPolicy#getUploadStrategy() */
  public BlobUploadStrategy getUploadStrategy() {
    return uploadStrategy;
  }

  /** @see BlobTransferPolicy#setUploadStrategy(BlobUploadStrategy) */
  public void setUploadStrategy(BlobUploadStrategy strat) {
    uploadStrategy = strat;
  }

  /** @see BlobTransferPolicy#getBufferSize() */
  public Integer getBufferSize() {
    return bufferSize;
  }

  /** @see BlobTransferPolicy#setBufferSize(int) */
  public void setBufferSize(Integer i) {
    bufferSize = i;
  }

}
