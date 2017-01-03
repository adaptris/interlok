package com.adaptris.core.jdbc;

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public enum PooledConnectionProperties {
  
  acquireIncrement {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setAcquireIncrement(Integer.parseInt(value));
    }
  }, 
  
  acquireRetryAttempts {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setAcquireRetryAttempts(Integer.parseInt(value));
    }
  }, 
  
  acquireRetryDelay {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setAcquireRetryDelay(Integer.parseInt(value));
    }
  }, 
  
  autoCommitOnClose {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setAutoCommitOnClose(Boolean.valueOf(value));
    }
  }, 
  
  automaticTestTable {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setAutomaticTestTable(value);
    }
  }, 
  
  breakAfterAcquireFailure {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setBreakAfterAcquireFailure(Boolean.valueOf(value));
    }
  }, 
  
  checkoutTimeout {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setCheckoutTimeout(Integer.parseInt(value));
    }
  }, 
  
  connectionCustomizerClassName {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setConnectionCustomizerClassName(value);
    }
  }, 
  
  connectionTesterClassName {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setConnectionTesterClassName(value);
    }
  }, 
  
  contextClassLoaderSource {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setContextClassLoaderSource(value);
    }
  }, 
  
  debugUnreturnedConnectionStackTraces {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setDebugUnreturnedConnectionStackTraces(Boolean.valueOf(value));
    }
  }, 
  
  description {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setDescription(value);
    }
  }, 
  
  factoryClassLocation {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setFactoryClassLocation(value);
    }
  }, 
  
  forceIgnoreUnresolvedTransactions {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setForceIgnoreUnresolvedTransactions(Boolean.valueOf(value));
    }
  }, 
  
  forceSynchronousCheckins {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setForceSynchronousCheckins(Boolean.valueOf(value));
    }
  }, 
  
  forceUseNamedDriverClass {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setForceUseNamedDriverClass(Boolean.valueOf(value));
    }
  }, 
  
  idleConnectionTestPeriod {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setIdleConnectionTestPeriod(Integer.parseInt(value));
    }
  }, 
  
  initialPoolSize {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setInitialPoolSize(Integer.parseInt(value));
    }
  }, 
  
  maxAdministrativeTaskTime {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setMaxAdministrativeTaskTime(Integer.parseInt(value));
    }
  }, 
  
  maxConnectionAge {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setMaxConnectionAge(Integer.parseInt(value));
    }
  }, 
  
  maxIdleTime {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setMaxIdleTime(Integer.parseInt(value));
    }
  }, 
  
  maxIdleTimeExcessConnections {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setMaxIdleTimeExcessConnections(Integer.parseInt(value));
    }
  }, 
  
  maxPoolSize {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setMaxPoolSize(Integer.parseInt(value));
    }
  }, 
  
  maxStatements {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setMaxStatements(Integer.parseInt(value));
    }
  }, 
  
  maxStatementsPerConnection {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setMaxStatementsPerConnection(Integer.parseInt(value));
    }
  }, 
  
  minPoolSize {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setMinPoolSize(Integer.parseInt(value));
    }
  }, 
  
  overrideDefaultPassword {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setOverrideDefaultPassword(value);
    }
  }, 
  
  overrideDefaultUser {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setOverrideDefaultUser(value);
    }
  }, 
  
  preferredTestQuery {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setPreferredTestQuery(value);
    }
  }, 
  
  privilegeSpawnedThreads {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setPrivilegeSpawnedThreads(Boolean.valueOf(value));
    }
  }, 
  
  propertyCycle {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setPropertyCycle(Integer.parseInt(value));
    }
  }, 
  
  statementCacheNumDeferredCloseThreads {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setStatementCacheNumDeferredCloseThreads(Integer.parseInt(value));
    }
  }, 
  
  testConnectionOnCheckin {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setTestConnectionOnCheckin(Boolean.valueOf(value));
    }
  }, 
  
  testConnectionOnCheckout {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setTestConnectionOnCheckout(Boolean.valueOf(value));
    }
  }, 
  
  unreturnedConnectionTimeout {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setUnreturnedConnectionTimeout(Integer.parseInt(value));
    }
  }, 
  
  userOverridesAsString {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setUserOverridesAsString(value);
    }
  }, 
  
  usesTraditionalReflectiveProxies {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setUsesTraditionalReflectiveProxies(Boolean.valueOf(value));
    }
  },
  
  loginTimeout {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setLoginTimeout(Integer.parseInt(value));
    }
  }, 
  
  dataSourceName {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setDataSourceName(value);
    }
  },
  
  numHelperThreads {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setNumHelperThreads(Integer.parseInt(value));
    }
  },
  
  identityToken {
    @Override
    void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception {
      dataSource.setIdentityToken(value);
    }
  };
  
  abstract void applyProperty(ComboPooledDataSource dataSource, String value) throws Exception;
  
  private static PooledConnectionProperties searchEnumIgnoreCase(String search) {
    for (PooledConnectionProperties each : PooledConnectionProperties.class.getEnumConstants()) {
      if (each.name().equalsIgnoreCase(search)) {
        return each;
      }
    }
    return null;
  }

  public static void apply(KeyValuePairSet props, ComboPooledDataSource pool) throws Exception {
    if (props == null) return;
    for (KeyValuePair kvp : props.getKeyValuePairs()) {
      PooledConnectionProperties connectionProperty = searchEnumIgnoreCase(kvp.getKey());
      if (connectionProperty != null) {
        connectionProperty.applyProperty(pool, kvp.getValue());
      }
    }
  }
}
