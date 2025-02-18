package com.adaptris.core.http.jetty;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;

import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.management.webserver.SecurityHandlerWrapper;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import lombok.Getter;
import lombok.Setter;

/**
 * Jetty Security Handler Wrapper which allows works with API keys.
 */
@XStreamAlias("jetty-configurable-key-security-handler")
@AdapterComponent
@ComponentProfile(summary = "Jetty Security Handler Wrapper which allows works with API keys.", tag = "jetty,security", since = "5.0.4")
@DisplayOrder(order = { "apiKeyHeader", "apiKey", "paths" })
public class ConfigurableKeySecurityHandler implements SecurityHandlerWrapper {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  static final String ROLE_NAME = "KEY_BASED";
  static final String USERNAME = "KEY";

  /**
   * Header key used to retrieve the API key
   */
  @Getter
  @Setter
  @NotBlank
  private String apiKeyHeader;

  /**
   * API key to check against. This can be an environment variable e.g. <code>%env{API_KEY}</code> or a system property e.g.
   * <code>%sysprop{api.key}</code>
   */
  @Getter
  @Setter
  @NotBlank
  @InputFieldHint(style = "PASSWORD", external = true)
  private String apiKey;

  /**
   * List of url paths to protect using the provided API key
   */
  @Getter
  @Setter
  @XStreamImplicit(itemFieldName = "url-path")
  private List<String> paths;

  public ConfigurableKeySecurityHandler() {
    paths = new ArrayList<>();
  }

  @Override
  public SecurityHandler createSecurityHandler() throws Exception {
    ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
    Authenticator authenticator = new KeyAuthenticator(USERNAME, getApiKeyHeader());
    securityHandler.setAuthenticator(authenticator);
    LoginService loginService = new KeyLoginService(USERNAME, ExternalResolver.resolve(getApiKey()), ROLE_NAME);
    securityHandler.setLoginService(loginService);

    log.debug("Created configurable key security handler");

    Constraint constraint = new Constraint();
    constraint.setName(authenticator.getAuthMethod());
    constraint.setRoles(new String[] { ROLE_NAME });
    constraint.setAuthenticate(true);

    for (String path : getPaths()) {
      ConstraintMapping constraintMapping = new ConstraintMapping();
      constraintMapping.setConstraint(constraint);
      constraintMapping.setPathSpec(path);

      log.debug("Adding path [{}] with constraint [{}] to security handler", path, constraint);
      securityHandler.addConstraintMapping(constraintMapping);
    }
    return securityHandler;
  }

}
