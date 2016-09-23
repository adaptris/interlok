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
package com.adaptris.core.security.access;

import static org.apache.commons.lang.StringUtils.isBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A simple service that allows you to verify that the message contents and/or metadata passes muster.
 * 
 * @author lchan
 *
 */
@XStreamAlias("verify-identity-service")
@AdapterComponent
@ComponentProfile(summary = "Verify and control access based on the message contents", tag = "service,security")
@DisplayOrder(order =
{
    "successId", "failureId"
})
public class VerifyIdentityService extends ServiceImp {
  @AdvancedConfig
  private String successId = null;
  @AdvancedConfig
  private String failureId = null;

  @NotNull
  @Valid
  @AutoPopulated
  private IdentityBuilder builder;
  @NotNull
  @Valid
  @AutoPopulated
  private VerifyIdentity verifier;

  private transient boolean branchingEnabled = false;

  public VerifyIdentityService() {

  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      handleResult(getVerifier().validate(getBuilder().build(msg)), msg);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  private void handleResult(boolean result, AdaptrisMessage msg) throws ServiceException {
    if (isBranching()) {
      if (result) {
        msg.setNextServiceId(getSuccessId());
      }
      else {
        msg.setNextServiceId(getFailureId());
      }
    }
    else {
      if (!result) {
        throw new ServiceException("Validation failed");
      }
    }
  }

  @Override
  public void prepare() throws CoreException {
    if (!isBlank(getSuccessId()) && !isBlank(getFailureId())) {
      branchingEnabled = true;
    }
  }

  @Override
  public boolean isBranching() {
    return branchingEnabled;
  }

  @Override
  protected void initService() throws CoreException {
  }

  public void start() throws CoreException {
    super.start();
  }

  public void stop() {
    super.stop();
  }

  @Override
  protected void closeService() {
  }


  public String getFailureId() {
    return failureId;
  }


  public void setFailureId(String s) {
    failureId = s;
  }


  public String getSuccessId() {
    return successId;
  }

  public void setSuccessId(String s) {
    successId = s;
  }

  /**
   * @return the builder
   */
  public IdentityBuilder getBuilder() {
    return builder;
  }

  /**
   * @param builder the builder to set
   */
  public void setBuilder(IdentityBuilder builder) {
    this.builder = Args.notNull(builder, "builder");
  }

  /**
   * @return the verifier
   */
  public VerifyIdentity getVerifier() {
    return verifier;
  }

  /**
   * @param verifier the verifier to set
   */
  public void setVerifier(VerifyIdentity verifier) {
    this.verifier = Args.notNull(verifier, "verifier");
  }
}
