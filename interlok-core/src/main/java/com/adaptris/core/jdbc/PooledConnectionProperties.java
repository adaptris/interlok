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

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.SimpleBeanUtil;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Used with {@link AdvancedJdbcPooledConnection} to configure the underlying c3po datasource.
 * 
 * @author amcgrath
 *
 */
public enum PooledConnectionProperties {
  
  acquireIncrement {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setAcquireIncrement(Integer.parseInt(value));
    }

    @Override
    public Class propertyType() {
      return Integer.class;
    }
  }, 
  
  acquireRetryAttempts {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setAcquireRetryAttempts(Integer.parseInt(value));
    }

    @Override
    public Class propertyType() {
      return Integer.class;
    }
  }, 
  
  acquireRetryDelay {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setAcquireRetryDelay(Integer.parseInt(value));
    }

    @Override
    public Class propertyType() {
      return Integer.class;
    }
  }, 
  
  autoCommitOnClose {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setAutoCommitOnClose(Boolean.valueOf(value));
    }

    @Override
    public Class propertyType() {
      return Boolean.class;
    }
  }, 
  
  automaticTestTable {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setAutomaticTestTable(value);
    }

    @Override
    public Class propertyType() {
      return String.class;
    }
  }, 
  
  breakAfterAcquireFailure {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setBreakAfterAcquireFailure(Boolean.valueOf(value));
    }

    @Override
    public Class propertyType() {
      return Boolean.class;
    }
  }, 
  
  checkoutTimeout {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setCheckoutTimeout(Integer.parseInt(value));
    }

    @Override
    public Class propertyType() {
      return Integer.class;
    }
  }, 

  connectionCustomizerClassName {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setConnectionCustomizerClassName(value);
    }

    @Override
    public Class propertyType() {
      return String.class;
    }
  },

  connectionTesterClassName {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setConnectionTesterClassName(value);
    }

    @Override
    public Class propertyType() {
      return String.class;
    }
  },

  contextClassLoaderSource {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setContextClassLoaderSource(value);
    }

    @Override
    public Class propertyType() {
      return String.class;
    }
  },

  debugUnreturnedConnectionStackTraces {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setDebugUnreturnedConnectionStackTraces(Boolean.valueOf(value));
    }

    @Override
    public Class propertyType() {
      return Boolean.class;
    }
  }, 
  
  description {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setDescription(value);
    }

    @Override
    public Class propertyType() {
      return String.class;
    }
  }, 
  
  factoryClassLocation {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setFactoryClassLocation(value);
    }

    @Override
    public Class propertyType() {
      return String.class;
    }
  }, 
  
  forceIgnoreUnresolvedTransactions {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setForceIgnoreUnresolvedTransactions(Boolean.valueOf(value));
    }

    @Override
    public Class propertyType() {
      return Boolean.class;
    }
  }, 
  
  forceSynchronousCheckins {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setForceSynchronousCheckins(Boolean.valueOf(value));
    }

    @Override
    public Class propertyType() {
      return Boolean.class;
    }
  }, 
  
  forceUseNamedDriverClass {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setForceUseNamedDriverClass(Boolean.valueOf(value));
    }

    @Override
    public Class propertyType() {
      return Boolean.class;
    }
  }, 
  
  idleConnectionTestPeriod {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setIdleConnectionTestPeriod(Integer.parseInt(value));
    }

    @Override
    public Class propertyType() {
      return Integer.class;
    }
  }, 
  
  initialPoolSize {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setInitialPoolSize(Integer.parseInt(value));
    }

    @Override
    public Class propertyType() {
      return Integer.class;
    }
  }, 
  
  maxAdministrativeTaskTime {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setMaxAdministrativeTaskTime(Integer.parseInt(value));
    }


    @Override
    public Class propertyType() {
      return Integer.class;
    }
  }, 
  
  maxConnectionAge {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setMaxConnectionAge(Integer.parseInt(value));
    }

    @Override
    public Class propertyType() {
      return Integer.class;
    }
  }, 
  
  maxIdleTime {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setMaxIdleTime(Integer.parseInt(value));
    }

    @Override
    public Class propertyType() {
      return Integer.class;
    }
  }, 
  
  maxIdleTimeExcessConnections {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setMaxIdleTimeExcessConnections(Integer.parseInt(value));
    }

    @Override
    public Class propertyType() {
      return Integer.class;
    }
  }, 
  
  maxPoolSize {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setMaxPoolSize(Integer.parseInt(value));
    }

    @Override
    public Class propertyType() {
      return Integer.class;
    }
  }, 
  
  maxStatements {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setMaxStatements(Integer.parseInt(value));
    }

    @Override
    public Class propertyType() {
      return Integer.class;
    }
  }, 
  
  maxStatementsPerConnection {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setMaxStatementsPerConnection(Integer.parseInt(value));
    }

    @Override
    public Class propertyType() {
      return Integer.class;
    }
  }, 
  
  minPoolSize {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setMinPoolSize(Integer.parseInt(value));
    }

    @Override
    public Class propertyType() {
      return Integer.class;
    }
  }, 
  
  overrideDefaultPassword {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setOverrideDefaultPassword(value);
    }

    @Override
    public Class propertyType() {
      return String.class;
    }
  }, 
  
  overrideDefaultUser {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setOverrideDefaultUser(value);
    }

    @Override
    public Class propertyType() {
      return String.class;
    }
  }, 
  
  preferredTestQuery {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setPreferredTestQuery(value);
    }

    @Override
    public Class propertyType() {
      return String.class;
    }
  }, 
  
  privilegeSpawnedThreads {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setPrivilegeSpawnedThreads(Boolean.valueOf(value));
    }

    @Override
    public Class propertyType() {
      return Boolean.class;
    }
  }, 
  
  propertyCycle {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setPropertyCycle(Integer.parseInt(value));
    }

    @Override
    public Class propertyType() {
      return Integer.class;
    }
  }, 
  
  statementCacheNumDeferredCloseThreads {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setStatementCacheNumDeferredCloseThreads(Integer.parseInt(value));
    }

    @Override
    public Class propertyType() {
      return Integer.class;
    }
  }, 
  
  testConnectionOnCheckin {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setTestConnectionOnCheckin(Boolean.valueOf(value));
    }

    @Override
    public Class propertyType() {
      return Boolean.class;
    }
  }, 
  
  testConnectionOnCheckout {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setTestConnectionOnCheckout(Boolean.valueOf(value));
    }

    @Override
    public Class propertyType() {
      return Boolean.class;
    }
  }, 
  
  unreturnedConnectionTimeout {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setUnreturnedConnectionTimeout(Integer.parseInt(value));
    }

    @Override
    public Class propertyType() {
      return Integer.class;
    }
  }, 
  
  userOverridesAsString {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setUserOverridesAsString(value);
    }

    @Override
    public Class propertyType() {
      return String.class;
    }
  },

  usesTraditionalReflectiveProxies {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setUsesTraditionalReflectiveProxies(Boolean.valueOf(value));
    }

    @Override
    public Class propertyType() {
      return Boolean.class;
    }
  },
  
  loginTimeout {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setLoginTimeout(Integer.parseInt(value));
    }

    @Override
    public Class propertyType() {
      return Integer.class;
    }
  }, 
  
  dataSourceName {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setDataSourceName(value);
    }

    @Override
    public Class propertyType() {
      return String.class;
    }
  },
  
  numHelperThreads {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setNumHelperThreads(Integer.parseInt(value));
    }

    @Override
    public Class propertyType() {
      return Integer.class;
    }
  },
  
  identityToken {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setIdentityToken(value);
    }

    @Override
    public Class propertyType() {
      return String.class;
    }
  };
  
  abstract void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception;
  
  public abstract Class propertyType();

  private static PooledConnectionProperties searchEnumIgnoreCase(String search) {
    for (PooledConnectionProperties each : PooledConnectionProperties.class.getEnumConstants()) {
      if (each.name().equalsIgnoreCase(search)) {
        return each;
      }
    }
    return null;
  }

  public static void apply(KeyValuePairSet props, ComboPooledDataSource pool) {
    if (props == null) return;
    for (KeyValuePair kvp : props.getKeyValuePairs()) {
      PooledConnectionProperties connectionProperty = searchEnumIgnoreCase(kvp.getKey());
      try {
        if (connectionProperty != null) {
          connectionProperty.applyProperty(pool, kvp.getValue());
        } else {
          SimpleBeanUtil.callSetter(pool, "set" + kvp.getKey(), kvp.getValue());
        }
      } catch (Exception ignoredIntentionally) {

      }
    }
  }
}
