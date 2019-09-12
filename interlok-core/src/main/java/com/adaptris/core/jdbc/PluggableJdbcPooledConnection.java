package com.adaptris.core.jdbc;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Concrete {@link JdbcPooledConnectionImpl} that allows you to plug in connection pool
 * implementations.
 * <p>
 * Generally, we find that c3p0 is good enough; however, in some use-cases you might want to switch
 * to a different connection pool implementation. This variant allows you to plug in different
 * builder implementations for the underlying connection pool.
 * </p>
 */
@XStreamAlias("pluggable-jdbc-pooled-connection")
@AdapterComponent
@ComponentProfile(summary = "Connect to a database using a JDBC driver; connection pooling is pluggable",
    tag = "connections,jdbc", since = "3.9.2")
@DisplayOrder(
    order = {"username", "password", "driverImp", "connectUrl", "builder", "poolProperties", "connectionProperties"})
public class PluggableJdbcPooledConnection extends JdbcPooledConnectionImpl {

  @Valid
  @InputFieldDefault(value = "default-jdbc-pool-factory")
  @AutoPopulated
  @NotNull
  private ConnectionPoolBuilder builder;
  @Valid
  private KeyValuePairSet poolProperties;

  public PluggableJdbcPooledConnection() {

  }


  @Override
  protected PooledDataSource createPool() throws Exception {
    return builder().build(this);
  }


  @Override
  public boolean equals(Object other) {
    if (other == null)
      return false;

    if (other == this)
      return true;

    if (other instanceof PluggableJdbcPooledConnection) {
      PluggableJdbcPooledConnection conn = (PluggableJdbcPooledConnection) other;

      return new EqualsBuilder().append(conn.getConnectUrl(), this.getConnectUrl()).append(conn.getDriverImp(), this.getDriverImp())
          .append(conn.getAlwaysValidateConnection(), this.getAlwaysValidateConnection())
          .append(conn.getDebugMode(), this.getDebugMode()).append(conn.getTestStatement(), this.getTestStatement())
          .append(conn.getAutoCommit(), this.getAutoCommit()).append(conn.getConnectionProperties(), this.getConnectionProperties())
          .append(conn.getBuilder(), this.getBuilder()).append(conn.getPoolProperties(), this.getPoolProperties()).isEquals();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(19, 37).append(this.getConnectUrl()).append(getDriverImp())
        .append(this.getAlwaysValidateConnection()).append(getDebugMode()).append(getTestStatement()).append(getAutoCommit())
        .append(this.getConnectionProperties()).append(getBuilder()).append(getPoolProperties()).toHashCode();
  }


  public ConnectionPoolBuilder getBuilder() {
    return builder;
  }

  private ConnectionPoolBuilder builder() {
    return ObjectUtils.defaultIfNull(getBuilder(), new DefaultPoolFactory());
  }

  /**
   * Set the builder to use when creating the connection pool.
   * 
   * @param builder
   */
  public void setBuilder(ConnectionPoolBuilder builder) {
    this.builder = Args.notNull(builder, "builder");
  }

  public PluggableJdbcPooledConnection withBuilder(ConnectionPoolBuilder builder) {
    setBuilder(builder);
    return this;
  }

  public KeyValuePairSet getPoolProperties() {
    return poolProperties;
  }

  /**
   * Set any additional connection pool properties over and above the defaults.
   * <p>
   * By its very nature, since we don't know what connection pool implementation is going to be used,
   * the specific keys and values required here will be dependent on the pool implementation. Please
   * consult their documentation to figure out the correct values.
   * </p>
   * 
   * @param kvps the connection pool properties.
   */
  public void setPoolProperties(KeyValuePairSet kvps) {
    this.poolProperties = kvps;
  }

  public KeyValuePairSet poolProperties() {
    return ObjectUtils.defaultIfNull(getPoolProperties(), new KeyValuePairSet());
  }

  public PluggableJdbcPooledConnection withPoolProperties(KeyValuePairSet values) {
    setPoolProperties(values);
    return this;
  }

}
