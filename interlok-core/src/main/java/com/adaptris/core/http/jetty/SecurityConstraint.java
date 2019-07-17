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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.jetty.util.security.Constraint;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;
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
@DisplayOrder(order = {"paths", "roles", "mustAuthenticate", "constraintName"})
public class SecurityConstraint {
  
  @NotNull
  @NotBlank
  private String roles;
  @InputFieldDefault(value = "true")
  @AdvancedConfig
  private Boolean mustAuthenticate;
  @InputFieldDefault(value = Constraint.__BASIC_AUTH)
  @AdvancedConfig
  private String constraintName;
  @NotNull
  @XStreamImplicit(itemFieldName = "url-path")
  private List<String> paths;
  
  public SecurityConstraint() {
    paths = Arrays.asList("/");
  }
  
  public String getRoles() {
    return roles;
  }
  
  /**
   * A comma separated list of roles that the user must have to satisfy the constraint.
   * 
   * @param roles a comma separated list of roles
   */
  public void setRoles(String roles) {
    this.roles = Args.notBlank(roles, "roles");
  }
  
  public Boolean getMustAuthenticate() {
    return mustAuthenticate;
  }

  public boolean isMustAuthenticate() {
    return BooleanUtils.toBooleanDefaultIfNull(getMustAuthenticate(), true);
  }
  
  /**
   * Whether or not we must authenticate.
   * 
   * @param b true or false, the default is true if not explicitly specified.
   */
  public void setMustAuthenticate(Boolean b) {
    this.mustAuthenticate = b;
  }
  
  public String getConstraintName() {
    return constraintName;
  }
  
  /**
   * Set the name of the constraint
   * 
   * @param the name of the constraint; if not specified then defaults to 'BASIC' {@link Constraint#__BASIC_AUTH}.
   * @see Constraint
   */
  public void setConstraintName(String constraintName) {
    this.constraintName = constraintName;
  }

  public String constraintName() {
    return StringUtils.defaultIfBlank(getConstraintName(), Constraint.__BASIC_AUTH);
  }


  public List<String> getPaths() {
    return paths;
  }
  
  public void setPaths(List<String> paths) {
    this.paths = Args.notNull(paths, "paths");
  }

}
