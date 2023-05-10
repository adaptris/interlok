package com.adaptris.core.management.webserver;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

@SuppressWarnings("deprecation")
public class JettyServerManagerTest {
  
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
  public void testAddServer_WithKey(TestInfo info) throws Exception {
    JettyServerManager manager = new JettyServerManager();
    Server server = JettyServerBuilder.build();
    try {
      JettyServerBuilder.start(server);

      manager.addServer(info.getDisplayName(), server);
      assertTrue(manager.isStarted());
      manager.removeServer(info.getDisplayName());
      assertFalse(manager.isStarted());
    } finally {
      JettyServerBuilder.destroy(server);
    }
  }

  @Test
  public void testAddServlet(TestInfo info) throws Exception {
    JettyServerManager manager = new JettyServerManager();
    Server server = JettyServerBuilder.build();
    try {
      JettyServerBuilder.start(server);

      manager.addServer(info.getDisplayName(), server);
      DummyServlet jettyServlet = new DummyServlet();
      String uri = "/" + info.getDisplayName();
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
  public void testAddServlet_WithKey(TestInfo info) throws Exception {
    JettyServerManager manager = new JettyServerManager();
    Server server = JettyServerBuilder.build();
    try {
      JettyServerBuilder.start(server);

      String uri = "/" + info.getDisplayName();
      String key = info.getDisplayName();
      
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
  public void testRemoveDeployment_NotYetAdded(TestInfo info) throws Exception {
    JettyServerManager manager = new JettyServerManager();
    Server server = JettyServerBuilder.build();
    try {
      JettyServerBuilder.start(server);
      String uri = "/" + info.getDisplayName();
      String key = info.getDisplayName();
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
  public void testDeploymentOperations_NotYetStarted(TestInfo info) throws Exception {
    JettyServerManager manager = new JettyServerManager();
    Server server = JettyServerBuilder.build();
    try {
      String uri = "/" + info.getDisplayName();
      String key = info.getDisplayName();
      
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
  public void testAddServletHolder(TestInfo info) throws Exception {
    JettyServerManager manager = new JettyServerManager();
    Server server = JettyServerBuilder.build();
    try {
      JettyServerBuilder.start(server);

      manager.addServer(info.getDisplayName(), server);
      ServletHolder jettyServlet = new ServletHolder( new DummyServlet());
      String uri = "/" + info.getDisplayName();
      
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
  public void testAddServletHolder_WithKey(TestInfo info) throws Exception {
    JettyServerManager manager = new JettyServerManager();
    Server server = JettyServerBuilder.build();
    try {
      JettyServerBuilder.start(server);

      String key = info.getDisplayName();
      String uri = "/" + info.getDisplayName();

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
