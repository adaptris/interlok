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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.validation.constraints.NotNull;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Constraint;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * A Proxy for the Jetty HashUserRealm security handler.
 * 
 * <p>
 * This class simply wraps the Jetty HashUserRealm functionality and exposes as a SecurityHandler to JettyConnection
 * </p>
 * <p>
 * You should check the Jetty documentation for more information.
 * </p>
 * 
 * @config jetty-hash-user-realm-proxy
 * @see JettyConnection
 */

@XStreamAlias("jetty-hash-user-realm-proxy")
public class HashUserRealmProxy implements SecurityHandlerWrapper {
  
  @NotNull
  @NotBlank
  @AutoPopulated
  private String userRealm;
  @NotNull
  @NotBlank
  private String filename;
  @NotNull
  @XStreamImplicit
  private List<SecurityConstraint> securityConstraints;
  
  public HashUserRealmProxy() {
    securityConstraints = new ArrayList<>();
    this.setUserRealm("MediationFramework");
  }
  
  
  @Override
  public SecurityHandler createSecurityHandler() throws Exception {
    ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
    securityHandler.setAuthenticator(new BasicAuthenticator());
    securityHandler.setLoginService(new HashLoginService(getUserRealm(), getFilename()));
    
    for(SecurityConstraint securityConstraint : this.getSecurityConstraints()) {
      Constraint constraint = new Constraint();
      constraint.setName(securityConstraint.getConstraintName());
      constraint.setRoles(asArray(securityConstraint.getRoles()));
      constraint.setAuthenticate(securityConstraint.isMustAuthenticate());
      
      for(String path : securityConstraint.getPaths()) {
        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec(path);
        
        securityHandler.addConstraintMapping(constraintMapping);
      }
    }
    return securityHandler;
  }

  private static String[] asArray(String s) {
    if (s == null) {
      return new String[0];
    }
    StringTokenizer st = new StringTokenizer(s, ",");
    List<String> l = new ArrayList<String>();
    while (st.hasMoreTokens()) {
      l.add(st.nextToken());
    }
    return l.toArray(new String[0]);
  }
  
  public List<SecurityConstraint> getSecurityConstraints() {
    return securityConstraints;
  }

  public void setSecurityConstraints(List<SecurityConstraint> securityConstraints) {
    this.securityConstraints = securityConstraints;
  }

  public String getUserRealm() {
    return userRealm;
  }

  public void setUserRealm(String userRealm) {
    this.userRealm = userRealm;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

}
