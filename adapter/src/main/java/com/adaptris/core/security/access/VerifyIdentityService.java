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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
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
    "builder", "verifier"
})
public class VerifyIdentityService extends ServiceImp {

  @NotNull
  @Valid
  @AutoPopulated
  private IdentityBuilder builder;
  @NotNull
  @Valid
  @AutoPopulated
  private IdentityVerifier verifier;

  public VerifyIdentityService() {
    setBuilder(new EmptyIdentityBuilder());
    setVerifier(new AlwaysFailVerifier());
  }

  public VerifyIdentityService(IdentityBuilder b, IdentityVerifier v) {
    this();
    setBuilder(b);
    setVerifier(v);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      if (!getVerifier().validate(getBuilder().build(msg))) {
        throw new ServiceException(getVerifier().getClass().getSimpleName() + " failed");
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
    LifecycleHelper.prepare(getBuilder());
    LifecycleHelper.prepare(getVerifier());
  }

  @Override
  protected void initService() throws CoreException {
    LifecycleHelper.init(getBuilder());
    LifecycleHelper.init(getVerifier());

  }

  public void start() throws CoreException {
    super.start();
    LifecycleHelper.start(getBuilder());
    LifecycleHelper.start(getVerifier());

  }

  public void stop() {
    super.stop();
    LifecycleHelper.stop(getBuilder());
    LifecycleHelper.stop(getVerifier());
  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(getBuilder());
    LifecycleHelper.close(getVerifier());
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
  public IdentityVerifier getVerifier() {
    return verifier;
  }

  /**
   * @param verifier the verifier to set
   */
  public void setVerifier(IdentityVerifier verifier) {
    this.verifier = Args.notNull(verifier, "verifier");
  }
}
