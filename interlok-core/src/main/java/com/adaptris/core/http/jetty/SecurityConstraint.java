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

package com.adaptris.core.http.jetty;

import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.eclipse.jetty.util.security.Constraint;
import org.hibernate.validator.constraints.NotBlank;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * A security constraint which allows you to place restrictions on a number of paths.
 * </p>
 * <p>
 * Given a single or multiple paths, you can set the roles that allow access.
 * </p>
 * <p>
 * By default we set a single path to "/". The list of paths can easily be overridden.
 * </p>
 * 
 * @config jetty-security-constraint
 */
@XStreamAlias("jetty-security-constraint")
public class SecurityConstraint {
  
  @NotNull
  @NotBlank
  private String roles;
  @NotNull
  @NotBlank
  private boolean mustAuthenticate;
  @NotNull
  @NotBlank
  private String constraintName;
  @NotNull
  @XStreamImplicit(itemFieldName = "url-path")
  private List<String> paths;
  
  public SecurityConstraint() {
    setConstraintName(Constraint.__BASIC_AUTH);
    setMustAuthenticate(true);
    paths = Arrays.asList("/");
  }
  
  public String getRoles() {
    return roles;
  }
  
  public void setRoles(String roles) {
    this.roles = roles;
  }
  
  public boolean isMustAuthenticate() {
    return mustAuthenticate;
  }
  
  public void setMustAuthenticate(boolean mustAuthenticate) {
    this.mustAuthenticate = mustAuthenticate;
  }
  
  public String getConstraintName() {
    return constraintName;
  }
  
  public void setConstraintName(String constraintName) {
    this.constraintName = constraintName;
  }

  public List<String> getPaths() {
    return paths;
  }
  
  public void setPaths(List<String> paths) {
    this.paths = paths;
  }

}
