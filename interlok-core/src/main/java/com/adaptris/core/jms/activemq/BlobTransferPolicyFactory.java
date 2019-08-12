/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.jms.activemq;

import static org.apache.commons.lang3.StringUtils.isBlank;

import org.apache.activemq.blob.BlobTransferPolicy;
import org.apache.activemq.blob.BlobUploadStrategy;
import org.apache.activemq.blob.DefaultBlobUploadStrategy;

import com.adaptris.annotation.DisplayOrder;
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
 * If fields are not explicitly set, then the corresponding {@link org.apache.activemq.blob.BlobUploadStrategy} method will not be
 * invoked.
 * </p>
 * 
 * @config activemq-blob-transfer-policy
 * @author lchan
 * 
 */
@XStreamAlias("activemq-blob-transfer-policy")
@DisplayOrder(order = {"brokerUploadUrl", "defaultUploadUrl", "uploadUrl", "bufferSize", "uploadStrategy"})
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

  /** @see BlobTransferPolicy#setUploadStrategy(org.apache.activemq.blob.BlobUploadStrategy) */
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
