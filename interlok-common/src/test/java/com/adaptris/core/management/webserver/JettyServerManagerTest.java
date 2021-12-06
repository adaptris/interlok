package com.adaptris.core.management.webserver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import javax.servlet.http.HttpServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

@SuppressWarnings("deprecation")
public class JettyServerManagerTest {
  @Rule
  public TestName testName = new TestName();
  
  @Test
  @SuppressWarnings("removal")
  public void testWebServerManagementUtil() {
    ServerManager mgr = WebServerManagementUtil.getServerManager();
    assertNotNull(mgr);
    JettyServerManager jmgr = JettyServerManager.getInstance();
    assertSame(mgr, jmgr);
  }
  
  @Test
  public void testIsStarted() throws Exception {
    JettyServerManager manager = new JettyServerManager();
    Server s1 = JettyServerBuilder.build();
    Server s2 = JettyServerBuilder.build();
    try {
      JettyServerBuilder.start(s1);
      manager.addServer(s1);
      assertTrue(manager.isStarted());
      manager.addServer(s2);
      assertFalse(manager.isStarted());
      JettyServerBuilder.start(s2);
      assertTrue(manager.isStarted());
      manager.removeServer(s1);
      manager.removeServer(s2);
      assertFalse(manager.isStarted());
    } finally {
      JettyServerBuilder.destroy(s1);
      JettyServerBuilder.destroy(s2);
    }    
  }

  @Test
  public void testRemoveServer() throws Exception {
    JettyServerManager manager = new JettyServerManager();
    Server s1 = JettyServerBuilder.build();
    Server s2 = JettyServerBuilder.build();
    try {
      JettyServerBuilder.start(s1);
      manager.addServer(s1);
      // Servers are started hopefully
      assertTrue(manager.isStarted());
      manager.removeServer(s2);
      manager.removeServer(s1);
      // No Servers left;
      assertFalse(manager.isStarted());
    } finally {
      JettyServerBuilder.destroy(s1);
    }
  }
  
  @Test
  public void testAddServer() throws Exception {
    JettyServerManager manager = new JettyServerManager();
    Server server = JettyServerBuilder.build();
    try {
      JettyServerBuilder.start(server);
      manager.addServer(server);
      // Servers are started hopefully
      assertTrue(manager.isStarted());
      manager.removeServer(server);
      // No Servers left;
      assertFalse(manager.isStarted());
    } finally {
      JettyServerBuilder.destroy(server);
    }
  }

  @Test
  public void testAddServer_WithKey() throws Exception {
    JettyServerManager manager = new JettyServerManager();
    Server server = JettyServerBuilder.build();
    try {
      JettyServerBuilder.start(server);

      manager.addServer(testName.getMethodName(), server);
      assertTrue(manager.isStarted());
      manager.removeServer(testName.getMethodName());
      assertFalse(manager.isStarted());
    } finally {
      JettyServerBuilder.destroy(server);
    }
  }

  @Test
  public void testAddServlet() throws Exception {
    JettyServerManager manager = new JettyServerManager();
    Server server = JettyServerBuilder.build();
    try {
      JettyServerBuilder.start(server);

      manager.addServer(testName.getMethodName(), server);
      DummyServlet jettyServlet = new DummyServlet();
      String uri = "/" + testName.getMethodName();
      HashMap<String, Object> properties = createProperties(uri, () -> JettyServerManager.defaultSecurityStub()); 
      manager.addServlet(jettyServlet, properties);
      manager.startDeployment(uri);
      manager.stopDeployment(uri);
      manager.removeDeployment(uri);
    } finally {
      JettyServerBuilder.destroy(server);
    }
  }

  @Test
  public void testAddServlet_WithKey() throws Exception {
    JettyServerManager manager = new JettyServerManager();
    Server server = JettyServerBuilder.build();
    try {
      JettyServerBuilder.start(server);

      String uri = "/" + testName.getMethodName();
      String key = testName.getMethodName();
      
      manager.addServer(key, server);
      DummyServlet jettyServlet = new DummyServlet();
      
      HashMap<String, Object> properties = createProperties(key, null); 
      manager.addServlet(key, jettyServlet, properties);
      manager.startDeployment(key, uri);
      manager.stopDeployment(key,uri);
      manager.removeDeployment(key, uri);
    } finally {
      JettyServerBuilder.destroy(server);
    }
  }
  
  @Test
  public void testRemoveDeployment_NotYetAdded() throws Exception {
    JettyServerManager manager = new JettyServerManager();
    Server server = JettyServerBuilder.build();
    try {
      JettyServerBuilder.start(server);
      String uri = "/" + testName.getMethodName();
      String key = testName.getMethodName();
      ServletHolder jettyServlet = new ServletHolder( new DummyServlet());
      
      manager.addServer(key, server);
      // This is coverage for trying to find the WebappContext
      manager.stopDeployment(uri);
      manager.removeDeployment(uri);
      manager.removeDeployment(jettyServlet, uri);
      
      manager.stopDeployment(key, uri);
      manager.removeDeployment(key, uri);
      manager.removeDeployment(key, jettyServlet, uri);
      
    } finally {
      JettyServerBuilder.destroy(server);
    }
  }
  
  @Test
  public void testDeploymentOperations_NotYetStarted() throws Exception {
    JettyServerManager manager = new JettyServerManager();
    Server server = JettyServerBuilder.build();
    try {
      String uri = "/" + testName.getMethodName();
      String key = testName.getMethodName();
      
      manager.addServer(key, server);
      
      manager.startDeployment(uri);
      manager.stopDeployment(uri);
      manager.removeDeployment(uri);
            
      manager.startDeployment(key, uri);
      manager.stopDeployment(key, uri);
      manager.removeDeployment(key, uri);
      
    } finally {
      JettyServerBuilder.destroy(server);
    }
  }
  
  @Test
  public void testAddServletHolder() throws Exception {
    JettyServerManager manager = new JettyServerManager();
    Server server = JettyServerBuilder.build();
    try {
      JettyServerBuilder.start(server);

      manager.addServer(testName.getMethodName(), server);
      ServletHolder jettyServlet = new ServletHolder( new DummyServlet());
      String uri = "/" + testName.getMethodName();
      
      HashMap<String, Object> properties = createProperties(uri, () -> JettyServerManager.defaultSecurityStub()); 
      manager.addServlet(jettyServlet, properties);
      manager.startDeployment(uri);
      manager.stopDeployment(uri);
      manager.removeDeployment(jettyServlet, uri);
    } finally {
      JettyServerBuilder.destroy(server);
    }
  }
  
  @Test
  public void testAddServletHolder_WithKey() throws Exception {
    JettyServerManager manager = new JettyServerManager();
    Server server = JettyServerBuilder.build();
    try {
      JettyServerBuilder.start(server);

      String key = testName.getMethodName();
      String uri = "/" + testName.getMethodName();

      manager.addServer(key, server);
      ServletHolder jettyServlet = new ServletHolder( new DummyServlet());
      
      HashMap<String, Object> properties = createProperties(uri, () -> JettyServerManager.defaultSecurityStub()); 
      manager.addServlet(key, jettyServlet, properties);
      manager.startDeployment(key, uri);
      manager.stopDeployment(key, uri);
      manager.removeDeployment(key, jettyServlet, uri);
    } finally {
      JettyServerBuilder.destroy(server);
    }
  }
  
  private HashMap<String, Object> createProperties(String path, SecurityHandlerWrapper security) {
    HashMap<String, Object> result = new HashMap<>();
    result.put(JettyServerManager.CONTEXT_PATH, path);
    result.put(JettyServerManager.SECURITY_CONSTRAINTS, security);
    return result;
  }


  private class DummyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
  }
}
