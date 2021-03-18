/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.core.jdbc;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class FailoverJdbcConnectionTest
    extends com.adaptris.interlok.junit.scaffolding.DatabaseConnectionCase<FailoverJdbcConnection> {


  public FailoverJdbcConnectionTest() {

  }


  @Test
  public void testInitialUrlConnectFailure() throws Exception {
    FailoverJdbcConnection conn = configure(createConnection());
    List<String> urls = conn.getConnectUrls();
    conn.setConnectUrls(Arrays.asList(new String[] {PROPERTIES.getProperty("jdbc.url") + nameGen.create(this), urls.get(0)}));
    LifecycleHelper.init(conn);
    conn.connect();
  }

  @Test
  public void testAlwaysValidateConnectionFalse() throws Exception {
    FailoverJdbcConnection conn = configure(createConnection());
    conn.setAlwaysValidateConnection(false);
    List<String> urls = conn.getConnectUrls();
    conn.setConnectUrls(Arrays.asList(new String[] {PROPERTIES.getProperty("jdbc.url") + nameGen.create(this), urls.get(0)}));

    LifecycleHelper.init(conn);
    conn.connect();
  }

  @Override
  protected FailoverJdbcConnection createConnection() {
    return new FailoverJdbcConnection();
  }

  @Override
  protected FailoverJdbcConnection configure(FailoverJdbcConnection conn1) throws Exception {
    String url = initialiseDatabase();
    conn1.addConnectUrl(url);
    conn1.setDriverImp(DRIVER_IMP);
    conn1.setDebugMode(true);
    conn1.setConnectionAttempts(1);
    conn1.setConnectionRetryInterval(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    conn1.setAlwaysValidateConnection(true);
    return conn1;
  }

}
