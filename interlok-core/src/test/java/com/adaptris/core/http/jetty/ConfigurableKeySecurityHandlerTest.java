package com.adaptris.core.http.jetty;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.junit.jupiter.api.Test;

class ConfigurableKeySecurityHandlerTest {

  @Test
  void createSecurityHandler() throws Exception {
    ConfigurableKeySecurityHandler securityHandlerWrapper = new ConfigurableKeySecurityHandler();
    securityHandlerWrapper.setApiKeyHeader("apiKey");
    securityHandlerWrapper.setApiKey("1234");
    securityHandlerWrapper.setPaths(Collections.singletonList("/api/*"));
    SecurityHandler result = securityHandlerWrapper.createSecurityHandler();

    assertTrue(result instanceof ConstraintSecurityHandler);
    ConstraintSecurityHandler securityHandler = (ConstraintSecurityHandler) result;

    assertTrue(securityHandler.getAuthenticator() instanceof KeyAuthenticator);
    KeyAuthenticator keyAuthenticator = (KeyAuthenticator) securityHandler.getAuthenticator();
    assertEquals(ConfigurableKeySecurityHandler.USERNAME, keyAuthenticator.getUsername());
    assertEquals("apiKey", keyAuthenticator.getHeaderName());

    assertTrue(securityHandler.getLoginService() instanceof KeyLoginService);
    KeyLoginService keyLoginService = (KeyLoginService) securityHandler.getLoginService();
    assertEquals(ConfigurableKeySecurityHandler.USERNAME, keyLoginService.getUserPrincipal().getName());
    assertEquals(ConfigurableKeySecurityHandler.ROLE_NAME, keyLoginService.getRoles().get(0).getName());

    List<ConstraintMapping> constraintMappings = securityHandler.getConstraintMappings();
    assertEquals(1, constraintMappings.size());

    ConstraintMapping constraintMapping = constraintMappings.get(0);
    assertEquals("/api/*", constraintMapping.getPathSpec());

    Constraint constraint = constraintMapping.getConstraint();

    assertEquals(KeyAuthenticator.AUTH_METHOD, constraint.getName());
    assertEquals(1, constraint.getRoles().length);
    assertEquals(ConfigurableKeySecurityHandler.ROLE_NAME, constraint.getRoles()[0]);
    assertTrue(constraint.getAuthenticate());
  }

}
