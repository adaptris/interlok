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

package com.adaptris.core.jdbc;

import static org.junit.Assert.assertTrue;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.util.TimeInterval;

public class JdbcConnectionTest
    extends com.adaptris.interlok.junit.scaffolding.DatabaseConnectionCase<JdbcConnection> {

  public JdbcConnectionTest() {
  }

  @Override
  protected JdbcConnection createConnection() {
    return new JdbcConnection();
  }

  @Override
  protected JdbcConnection configure(JdbcConnection conn1) throws Exception {
    String url = initialiseDatabase();
    conn1.setConnectUrl(url);
    conn1.setConnectionAttempts(1);
    conn1.setConnectionRetryInterval(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    return conn1;
  }


  @Test
  public void testLoadDriverClass() throws Exception {
    DatabaseConnection.loadDriverClass(DRIVER_IMP);
    try {
      DatabaseConnection.loadDriverClass("hello.world.does.not.exist");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("No available driver implementation"));
    }
  }
}
