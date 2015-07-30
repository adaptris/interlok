package com.adaptris.core.services.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.lang.StringUtils;

import com.adaptris.security.password.Password;

public class JmxOperationInvoker {
  
  public Object invoke(String serviceUrl, String objectName, String username, String password,
      String methodName, Object[] params, String[] signatures) throws Exception {
    Map<String, String[]> env = new HashMap<>();
    if ((!StringUtils.isEmpty(username)) && (!StringUtils.isEmpty(password))) {
      String[] credentials = {username, Password.decode(password)};
      env.put(JMXConnector.CREDENTIALS, credentials);
    }
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(serviceUrl);
    JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxServiceUrl, env);
    ObjectName objectNameInst = ObjectName.getInstance(objectName);
    MBeanServerConnection mbeanConn = jmxConnector.getMBeanServerConnection();
    try {
      return mbeanConn.invoke(objectNameInst, methodName, params, signatures);
    } finally {
      jmxConnector.close();

    }
  }

}
