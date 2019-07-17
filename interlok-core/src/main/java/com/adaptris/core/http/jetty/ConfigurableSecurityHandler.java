package com.adaptris.core.http.jetty;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.management.webserver.SecurityHandlerWrapper;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Jetty Security Handler Wrapper which allows the configuration
 * of different Login Service and Authenticators.
 * @author ellidges
 */
@XStreamAlias("jetty-configurable-security-handler")
@DisplayOrder(order = {"securityConstraints", "loginService", "authenticator"})
public class ConfigurableSecurityHandler implements SecurityHandlerWrapper {
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  
  @NotNull
  @Valid
  private JettyLoginServiceFactory loginService;
  
  @Valid
  @InputFieldDefault(value = "basic-authenticator-factory")
  @AdvancedConfig
  private JettyAuthenticatorFactory authenticator;
  
  @NotNull
  @XStreamImplicit
  private List<SecurityConstraint> securityConstraints;
  
  public ConfigurableSecurityHandler() {
    securityConstraints = new ArrayList<>();
  }
  
  /**
   * @see com.adaptris.core.management.webserver.SecurityHandlerWrapper#createSecurityHandler()
   */
  @Override
  public SecurityHandler createSecurityHandler() throws Exception {
    ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
    Authenticator authenticator = authenticator().retrieveAuthenticator();
    securityHandler.setAuthenticator(authenticator);
    LoginService loginService = getLoginService().retrieveLoginService();
    securityHandler.setLoginService(loginService);
    
    log.debug("Created configurable security handler with [{}][{}]", authenticator, loginService);
    
    for(SecurityConstraint securityConstraint : this.getSecurityConstraints()) {
      Constraint constraint = new Constraint();
      constraint.setName(securityConstraint.constraintName());
      constraint.setRoles(asArray(securityConstraint.getRoles()));
      constraint.setAuthenticate(securityConstraint.isMustAuthenticate());
      
      for(String path : securityConstraint.getPaths()) {
        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec(path);
        
        log.debug("Adding path [{}] with constraint [{}] to security handler", path, constraint);
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

  public JettyLoginServiceFactory getLoginService() {
    return loginService;
  }

  /**
   * Sets the factory which will create the underlying LoginService
   * 
   * @param loginService the login service to use.
   */
  public void setLoginService(JettyLoginServiceFactory loginService) {
    this.loginService = Args.notNull(loginService, "loginService");
  }

  public JettyAuthenticatorFactory getAuthenticator() {
    return authenticator;
  }

  /**
   * Sets the factory which will create the underlying Authenticator
   */
  public void setAuthenticator(JettyAuthenticatorFactory authenticator) {
    this.authenticator = authenticator;
  }

  private JettyAuthenticatorFactory authenticator() {
    return ObjectUtils.defaultIfNull(getAuthenticator(), new BasicAuthenticatorFactory());
  }

  public List<SecurityConstraint> getSecurityConstraints() {
    return securityConstraints;
  }

  public void setSecurityConstraints(List<SecurityConstraint> securityConstraints) {
    this.securityConstraints = Args.notNull(securityConstraints, "securityConstraints");
  }

}
