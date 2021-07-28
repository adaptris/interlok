package com.adaptris.core.management;

import static com.adaptris.core.runtime.AdapterComponentMBean.ID_PREFIX;
import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_ADAPTER_TYPE;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import javax.management.ObjectName;
import org.junit.Test;
import com.adaptris.core.Adapter;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.runtime.AdapterComponentMBean;
import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.util.GuidGenerator;

@SuppressWarnings("deprecation")
public class ShutdownHandlerTest {

  private static GuidGenerator nameGenerator = new GuidGenerator();

  @Test
  public void testRun() throws Exception {
    String adapterName = nameGenerator.safeUUID();
    Adapter adapter = new Adapter();
    adapter.setUniqueId(adapterName);
    BootstrapProperties boot = bootstrapWithAdapter(adapter);
    AdapterManagerMBean mgmtBean = boot.getConfigManager().createAdapter();
    try {
      mgmtBean.requestStart();
      ShutdownHandler shutdown = new ShutdownHandler(boot);
      shutdown.run();
    } finally {
      unregisterQuietly(mgmtBean);
    }
  }

  @Test
  public void testShutdown() throws Exception {
    String adapterName = nameGenerator.safeUUID();
    Adapter adapter = new Adapter();
    adapter.setUniqueId(adapterName);
    BootstrapProperties boot = bootstrapWithAdapter(adapter);
    AdapterManagerMBean mgmtBean = boot.getConfigManager().createAdapter();
    try {
      mgmtBean.requestStart();
      HashSet<ObjectName> set = new HashSet(Arrays.asList(mgmtBean.createObjectName(), ObjectName
          .getInstance(JMX_ADAPTER_TYPE + ID_PREFIX + nameGenerator.safeUUID())));
      ShutdownHandler shutdown = new ShutdownHandler(boot);
      shutdown.shutdown(set);
    } finally {
      unregisterQuietly(mgmtBean);
    }
  }

  @Test
  public void testForceShutdown() throws Exception {
    String adapterName = nameGenerator.safeUUID();
    Adapter adapter = new Adapter();
    adapter.setUniqueId(adapterName);
    BootstrapProperties boot = bootstrapWithAdapter(adapter);
    AdapterManagerMBean mgmtBean = boot.getConfigManager().createAdapter();
    try {
      mgmtBean.requestStart();
      HashSet<ObjectName> set = new HashSet(Arrays.asList(mgmtBean.createObjectName(), ObjectName
          .getInstance(JMX_ADAPTER_TYPE + ID_PREFIX + nameGenerator.safeUUID())));
      ShutdownHandler shutdown = new ShutdownHandler(boot);
      shutdown.forceShutdown(set);
    } finally {
      unregisterQuietly(mgmtBean);
    }
  }

  private BootstrapProperties bootstrapWithAdapter(Adapter adapter) throws Exception {
    File filename = TempFileUtils.createTrackedFile(adapter.getUniqueId(), null, adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    BootstrapProperties boot = new BootstrapProperties(new Properties());
    boot.put("adapterConfigUrl.1", filename.toURI().toURL().toString());
    return boot;
  }

  private void unregisterQuietly(AdapterComponentMBean mgmtBean) {
    try {
      mgmtBean.unregisterMBean();
    } catch (Exception e) {

    }
  }
}