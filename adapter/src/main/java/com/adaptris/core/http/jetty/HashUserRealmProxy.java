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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Constraint;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.management.webserver.SecurityHandlerWrapper;
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
 * @deprecated use {@link ConfigurableSecurityHandler} instead; since 3.3.0
 */
@Deprecated
@XStreamAlias("jetty-hash-user-realm-proxy")
public class HashUserRealmProxy implements SecurityHandlerWrapper {
  private static transient boolean warningLogged;

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @NotNull
  @NotBlank
  @AutoPopulated
  private String userRealm;
  @NotNull
  @NotBlank
  private String filename;
  @AdvancedConfig
  private Integer refreshInterval;
  @NotNull
  @XStreamImplicit
  @Valid
  private List<SecurityConstraint> securityConstraints;
  
  public HashUserRealmProxy() {
    if (!warningLogged) {
      log.warn("[{}] is deprecated, use [{}] instead", this.getClass().getSimpleName(),
          ConfigurableSecurityHandler.class.getName());
      warningLogged = true;
    }
    securityConstraints = new ArrayList<>();
    this.setUserRealm("MediationFramework");
  }
  
  
  @Override
  public SecurityHandler createSecurityHandler() throws Exception {
    return configure(new ConstraintSecurityHandler());
  }

  private ConstraintSecurityHandler configure(ConstraintSecurityHandler securityHandler) {
    securityHandler.setAuthenticator(new BasicAuthenticator());
    securityHandler.setLoginService(createLoginService());
    for (SecurityConstraint securityConstraint : this.getSecurityConstraints()) {
      Constraint constraint = new Constraint();
      constraint.setName(securityConstraint.getConstraintName());
      constraint.setRoles(asArray(securityConstraint.getRoles()));
      constraint.setAuthenticate(securityConstraint.isMustAuthenticate());

      for (String path : securityConstraint.getPaths()) {
        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec(path);
        securityHandler.addConstraintMapping(constraintMapping);
      }
    }
    return securityHandler;
  }

  private HashLoginService createLoginService() {
    HashLoginService loginService = new HashLoginService(getUserRealm(), getFilename());
    if (getRefreshInterval() != null) {
      loginService.setHotReload(true);
    }
    return loginService;
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

  /**
   * Set the filename containing the username/password/roles.
   * 
   * @param filename the filename.
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }


  /**
   * @return the refreshInterval
   */
  public Integer getRefreshInterval() {
    return refreshInterval;
  }


  /**
   * Specify the refresh interval (in seconds) for monitoring the password file.
   * 
   * @param i the refreshInterval to set
   */
  public void setRefreshInterval(Integer i) {
    this.refreshInterval = i;
  }

}
