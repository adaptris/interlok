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

package com.adaptris.core.services.jdbc;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.jdbc.DatabaseConnection;
import com.adaptris.core.jdbc.FailoverJdbcConnection;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.jdbc.JdbcService;
import com.adaptris.core.jdbc.JdbcServiceCase;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.TimeInterval;

public abstract class JdbcServiceExample extends JdbcServiceCase {

  public static final String BASE_DIR_KEY = "JdbcServiceExamples.baseDir";
  private static final String HYPHEN = "-";
  private static final String DB_STRING = "jdbc:mysql://localhost:3306/mydatabase";

  private enum ConnectionBuilder {
    FailoverJdbcConnectionBuilder {
      DatabaseConnection build() {
        FailoverJdbcConnection connection = new FailoverJdbcConnection();
        connection.addConnectUrl(DB_STRING);
        connection.addConnectUrl("jdbc:mysql://anotherHost:3306/anotherDatabase");
        return applyDefaultConfig(connection);
      }
    },
    // Pointless in a service
    // PooledJdbcConnectionBuilder {
    // DatabaseConnection build() {
    // JdbcPooledConnection connection = new JdbcPooledConnection();
    // connection.setConnectUrl(DB_STRING);
    // return applyDefaultConfig(connection);
    // }
    // },
    // AdvancedPooledJdbcConnectionBuilder {
    // DatabaseConnection build() {
    // AdvancedJdbcPooledConnection connection = new AdvancedJdbcPooledConnection();
    // connection.setConnectUrl(DB_STRING);
    // KeyValuePairSet poolProps = new KeyValuePairSet();
    // poolProps.add(new KeyValuePair(PooledConnectionProperties.maxPoolSize.name(), "20"));
    // poolProps.add(new KeyValuePair(PooledConnectionProperties.minPoolSize.name(), "5"));
    // connection.setConnectionPoolProperties(poolProps);
    // return applyDefaultConfig(connection);
    // }
    // },
    JdbcConnectionBuilder {
      DatabaseConnection build() {
        JdbcConnection connection = new JdbcConnection();
        connection.setConnectUrl(DB_STRING);
        return applyDefaultConfig(connection);
      }
    };

    abstract DatabaseConnection build();
  }

  public JdbcServiceExample(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected final List retrieveObjectsForSampleConfig() {
    ArrayList result = new ArrayList();
    List<JdbcService> services = buildExamples();
    if (services.size() == 0) {
      for (ConnectionBuilder connectionBuilder : ConnectionBuilder.values()) {
        JdbcService s = (JdbcService) retrieveObjectForSampleConfig();
        s.setConnection(connectionBuilder.build());
        result.add(s);
      }
    }
    else {
      for (JdbcService s : services) {
        for (ConnectionBuilder connectionBuilder : ConnectionBuilder.values()) {
          JdbcService copy = roundTrip(s);
          copy.setConnection(connectionBuilder.build());
          result.add(copy);
        }
      }
    }
    return result;
  }

  // Override as necessary.
  protected List<JdbcService> buildExamples() {
    return new ArrayList<>();
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + HYPHEN + ((JdbcService) object).getConnection().getClass().getSimpleName();
  }

  public static DatabaseConnection applyDefaultConfig(DatabaseConnection connection) {
    connection.setConnectionAttempts(2);
    connection.setUsername("my_db_username");
    connection.setPassword("plain or encoded password");
    connection.setConnectionRetryInterval(new TimeInterval(3L, "SECONDS"));
    KeyValuePairSet connectionProps = new KeyValuePairSet();
    connectionProps.add(new KeyValuePair("dontTrackOpenResources", "true"));
    connectionProps.add(new KeyValuePair("autoReconnect", "true"));
    connection.setConnectionProperties(connectionProps);
    return connection;
  }

  private JdbcService roundTrip(JdbcService s) throws RuntimeException {
    try {
      AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
      return (JdbcService) m.unmarshal(m.marshal(s));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
