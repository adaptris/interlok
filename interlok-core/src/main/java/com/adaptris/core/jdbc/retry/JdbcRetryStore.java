package com.adaptris.core.jdbc.retry;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.validation.constraints.NotBlank;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.MimeEncoder;
import com.adaptris.core.http.jetty.retry.RetryStore;
import com.adaptris.core.jdbc.DatabaseConnection;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.cloud.RemoteBlob;
import com.adaptris.interlok.util.Args;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * JDBC-based implementation of <code>RetryStore</code>.
 * </p>
 * <p>
 * This uses <code>JdbcTemplate</code> from Spring for database operations. If
 * there is no explicit configuration of the sqlProperties then the SQL
 * statements are found in the file <code>retry-store-derby.properties</code> which
 * is suitable for Apache Derby and MySQL. If explicitly configured, the
 * property file is expected to be present on the classpath.
 * </p>
 * <p>
 * The default property file is listed below.
 *
 * <pre><code>
 create.sql = CREATE TABLE retry_store \
 (message_id VARCHAR(256) NOT NULL, \
 acknowledge_id VARCHAR(256) NOT NULL, \
 message BLOB NOT NULL, \
 retry_interval INTEGER NOT NULL, \
 total_retries INTEGER NOT NULL, \
 retries_to_date INTEGER NOT NULL, \
 marshalled_service BLOB NOT NULL, \
 acknowledged CHAR NOT NULL, \
 updated_on TIMESTAMP, \
 inserted_on TIMESTAMP, \
 \
 CONSTRAINT pk_message_id PRIMARY KEY (message_id), \
 CONSTRAINT idx_acknowledge_id UNIQUE (acknowledge_id))

 insert.sql = INSERT INTO retry_store \
 (message_id, acknowledge_id, message, retry_interval, total_retries, \
 retries_to_date, marshalled_service, acknowledged, inserted_on, updated_on) \
 VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

 select.sql = SELECT * FROM retry_store WHERE message_id=?

 acknowledge.sql = UPDATE retry_store SET acknowledged=?, \
 updated_on=? WHERE acknowledge_id=?

 update-retry.sql = UPDATE retry_store SET retries_to_date=(retries_to_date + 1), \
 updated_on=? WHERE message_id=?

 retry.sql = SELECT * FROM retry_store WHERE \
 (acknowledged='F' AND (retries_to_date < total_retries OR total_retries = -1))

 delete.acknowleged.sql = DELETE FROM retry_store WHERE acknowledged='T'

 delete.sql = DELETE FROM retry_store WHERE message_id=?

 select.expired.sql = SELECT * FROM retry_store \
 WHERE (acknowledged='F' AND \
 (retries_to_date >= total_retries AND total_retries != -1))
 </code></pre>
 *
 * </p>
 * <p>
 * The create.sql script is <b>always</b> executed upon initialisation, 
 * any errors are discarded
 * </p>
 */

@XStreamAlias("retry-store-jdbc")
@ComponentProfile(summary = "Store message for retry in a database using jdbc", since = "4.9.0")
@DisplayOrder(order = { "sqlPropertiesFile" })
@Slf4j
public class JdbcRetryStore implements RetryStore {

  private static final String RETRY_STORE_PROPERTIES = "retry-store-derby.properties";
  private static final String CREATE_SQL = "create.sql";
  private static final String INSERT_SQL = "insert.sql";
  private static final String DELETE_SQL = "delete.sql";
  private static final String SELECT_EXPIRED_SQL = "select.expired.sql";
  private static final String RETRY_SQL = "retry.sql";
  private static final String UPDATE_RETRY_SQL = "update-retry.sql";
  private static final String DELETE_ACKNOWLEGED_SQL = "delete.acknowleged.sql";
  private static final String ACKNOWLEDGE_SQL = "acknowledge.sql";

  private transient Properties sqlStatements;
  private transient MimeEncoder encoder;
  private AdaptrisConnection connection;
  private Connection sqlConnection;

  /**
   * Set the sql properties file to use, by default it will look for the value
   * "retry-store-derby.properties"
   */
  @Getter
  @Setter
  @NotBlank
  @InputFieldDefault(value = RETRY_STORE_PROPERTIES)
  private String sqlPropertiesFile;

  /**
   * <p>
   * Creates a new instance. Default properties file is
   * <code>retry-store-derby.properties</code>.
   * </p>
   */
  public JdbcRetryStore() {
    encoder = new MimeEncoder();
    encoder.setRetainUniqueId(true);
    setSqlPropertiesFile(RETRY_STORE_PROPERTIES);
  }

  @Override
  public void prepare() throws CoreException {
    Args.notBlank(getSqlPropertiesFile(), "sqlPropertiesFile");
  }

  @Override
  public void init() throws CoreException {
    if (sqlStatements == null) {
      sqlStatements = new Properties();

      InputStream in = null;

      try {
        in = this.getClass().getClassLoader().getResourceAsStream(getSqlPropertiesFile());
        if (in == null) {
          in = new FileInputStream(getSqlPropertiesFile());
        }
        sqlStatements.load(in);
        in.close();
      } catch (Exception e) {
        throw new CoreException("problem loading file [" + getSqlPropertiesFile() + "]");
      }
    }

    if (sqlConnection == null) {
      try {
        sqlConnection = getConnection();
      } catch (Exception e) {
        throw new CoreException("error connecting to the Database");
      }
    }
    try {
      createStoreTable();
      log.debug("store table created");
    } catch (Exception e) {
    }
  }

  @Override
  public void write(AdaptrisMessage msg) throws InterlokException {
    PreparedStatement ps = null;
    validateMessage(msg);
    Object[] params = new Object[] { msg.getUniqueId(), msg.getMetadataValue(Constants.ACKNOWLEDGE_ID_KEY),
        encoder.encode(msg), Integer.parseInt(msg.getMetadataValue(Constants.RETRY_INTERVAL_KEY)),
        Integer.parseInt(msg.getMetadataValue(Constants.RETRIES_KEY)), Integer.valueOf(0),
        msg.getMetadataValue(Constants.MARSHALLED_SERVICE_KEY).getBytes(), "F" };
    try {
      ps = prepareStatementWithParameters(sqlConnection, sqlStatements.getProperty(INSERT_SQL), params);
      log.trace("executing insert statement");
      ps.executeUpdate();
    } catch (SQLException e) {
      throw ExceptionHelper.wrapInterlokException(e);
    } finally {
      JdbcUtil.closeQuietly(ps);
    }
  }

  @Override
  public boolean delete(String msgId) throws InterlokException {
    PreparedStatement ps = null;
    Object[] params = new Object[] { msgId };
    try {
      ps = prepareStatementWithParameters(sqlConnection, sqlStatements.getProperty(DELETE_SQL), params);
      log.trace("executing delete statement");
      ps.executeUpdate();
      return true;
    } catch (SQLException e) {
      throw ExceptionHelper.wrapInterlokException(e);
    } finally {
      JdbcUtil.closeQuietly(ps);
    }
  }

  @Override
  public Iterable<RemoteBlob> report() throws InterlokException {
    return Collections.EMPTY_LIST;
  }

  @Override
  public AdaptrisMessage buildForRetry(String msgId, Map<String, String> metadata, AdaptrisMessageFactory factory)
      throws InterlokException {
    return null;
  }

  @Override
  public Map<String, String> getMetadata(String msgId) throws InterlokException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void acknowledge(String acknowledgeId) throws InterlokException {
    PreparedStatement ps = null;
    Object[] params = { Constants.ACKNOWLEDGED, new Date(), acknowledgeId };
    try {
      ps = prepareStatementWithParameters(sqlConnection, sqlStatements.getProperty(ACKNOWLEDGE_SQL), params);
      log.trace("executing update statement");
      ps.executeUpdate();
    } catch (SQLException e) {
      throw ExceptionHelper.wrapInterlokException(e);
    } finally {
      JdbcUtil.closeQuietly(ps);
    }
  }

  @Override
  public void deleteAcknowledged() throws InterlokException {
    PreparedStatement ps = null;
    try {
      ps = prepareStatementWithoutParameters(sqlConnection, sqlStatements.getProperty(DELETE_ACKNOWLEGED_SQL));
      log.trace("executing delete statement");
      ps.executeUpdate();
    } catch (SQLException e) {
      throw ExceptionHelper.wrapInterlokException(e);
    } finally {
      JdbcUtil.closeQuietly(ps);
    }
  }

  @Override
  public List<AdaptrisMessage> obtainExpiredMessages() throws InterlokException {
    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = prepareStatementWithoutParameters(sqlConnection, sqlStatements.getProperty(SELECT_EXPIRED_SQL));
      log.trace("executing select statement");
      rs = ps.executeQuery();
      if (rs == null) {
        return result;
      }
      while (rs.next()) {
        JdbcRetryStoreEntry resultRow = mapRow(rs);
        result.add(convert(resultRow));
      }
    } catch (SQLException e) {
      throw ExceptionHelper.wrapInterlokException(e);
    } finally {
      JdbcUtil.closeQuietly(rs);
      JdbcUtil.closeQuietly(ps);
    }

    return result;
  }

  @Override
  public List<AdaptrisMessage> obtainMessagesToRetry() throws InterlokException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    try {
      ps = prepareStatementWithoutParameters(sqlConnection, sqlStatements.getProperty(RETRY_SQL));
      log.trace("executing select statement");
      rs = ps.executeQuery();
      if (rs == null) {
        return result;
      }
      while (rs.next()) {
        JdbcRetryStoreEntry resultRow = mapRow(rs);
        long now = System.currentTimeMillis();
        long lastRetry = resultRow.getUpdatedOn().getTime();
        int interval = resultRow.getRetryInterval();
        if (lastRetry + interval < now) { // retry required
          result.add(convert(resultRow));
        }
      }
    } catch (InterlokException | SQLException e) {
      throw ExceptionHelper.wrapInterlokException(e);
    } finally {
      JdbcUtil.closeQuietly(rs);
      JdbcUtil.closeQuietly(ps);
    }

    return result;
  }

  @Override
  public void updateRetryCount(String messageId) throws InterlokException {
    PreparedStatement ps = null;
    Object[] params = { new Date(), messageId };
    try {
      ps = prepareStatementWithParameters(sqlConnection, sqlStatements.getProperty(UPDATE_RETRY_SQL), params);
      log.trace("executing update statement");
      ps.executeUpdate();
    } catch (SQLException e) {
      throw ExceptionHelper.wrapInterlokException(e);
    } finally {
      JdbcUtil.closeQuietly(ps);
    }
  }

  private void validateMessage(AdaptrisMessage msg) throws InterlokException {
    if (msg == null) {
      throw new InterlokException("null param");
    }
    if (msg.getMetadataValue(Constants.ACKNOWLEDGE_ID_KEY) == null
        || msg.getMetadataValue(Constants.ACKNOWLEDGE_ID_KEY).isEmpty()) {
      throw new InterlokException("null or empty acknowledge ID");
    }
    if (msg.getMetadataValue(Constants.RETRY_INTERVAL_KEY) == null
        || msg.getMetadataValue(Constants.RETRY_INTERVAL_KEY).isEmpty()) {
      throw new InterlokException("null or empty retry interval");
    }
    if (msg.getMetadataValue(Constants.RETRIES_KEY) == null || msg.getMetadataValue(Constants.RETRIES_KEY).isEmpty()) {
      throw new InterlokException("null or empty retries");
    }
    if (msg.getMetadataValue(Constants.MARSHALLED_SERVICE_KEY) == null
        || msg.getMetadataValue(Constants.MARSHALLED_SERVICE_KEY).isEmpty()) {
      throw new InterlokException("null or empty marshalled service");
    }
    if (msg.getMetadataValue(Constants.MARSHALLED_CLASS_NAME_KEY) == null
        || msg.getMetadataValue(Constants.MARSHALLED_CLASS_NAME_KEY).isEmpty()) {
      throw new InterlokException("null or empty marshalled class name");
    }
    if (msg.getMetadataValue(Constants.ASYNCHRONOUS_KEY) == null
        || msg.getMetadataValue(Constants.ASYNCHRONOUS_KEY).isEmpty()) {
      throw new InterlokException("null or empty asynchronous ack");
    }
  }

  private AdaptrisMessage convert(JdbcRetryStoreEntry retryStoreEntry) throws InterlokException {

    AdaptrisMessage result = encoder.decode(retryStoreEntry.getEncodedMessage());

    // MimeEncoder 'workaround'...
    result.setUniqueId(retryStoreEntry.getMessageId()); // bodge

    // for size reasons
    retryStoreEntry.setEncodedMessage(null);
    retryStoreEntry.setMarshalledService(null);

    return result;
  }

  private void createStoreTable() throws Exception {
    PreparedStatement ps = null;
    try {
      ps = prepareStatementWithoutParameters(sqlConnection, sqlStatements.getProperty(CREATE_SQL));
      log.trace("Executing create statement");
      ps.executeUpdate();
    } catch (SQLException e) {
      throw ExceptionHelper.wrapInterlokException(e);
    } finally {
      JdbcUtil.closeQuietly(ps);
    }
  }

  private static PreparedStatement prepareStatementWithParameters(Connection c, String query, Object[] parameters)
      throws SQLException {
    PreparedStatement preparedStatement = c.prepareStatement(query);
    int count = 1;
    for (Object o : parameters) {
      preparedStatement.setObject(count, o);
      count++;
    }
    return preparedStatement;
  }

  private static PreparedStatement prepareStatementWithoutParameters(Connection c, String query) throws SQLException {
    return c.prepareStatement(query);
  }

  private JdbcRetryStoreEntry mapRow(ResultSet rs) throws SQLException {
    JdbcRetryStoreEntry result = new JdbcRetryStoreEntry();

    result.setMessageId(rs.getString("message_id"));
    result.setAcknowledgeId(rs.getString("acknowledge_id"));
    result.setEncodedMessage(rs.getBytes("message"));
    result.setRetryInterval(rs.getInt("retry_interval"));
    result.setTotalRetries(rs.getInt("total_retries"));
    result.setRetriesToDate(rs.getInt("retries_to_date"));
    result.setMarshalledService(new String(rs.getBytes("marshalled_service")));
    result.setInsertedOn(rs.getTimestamp("inserted_on"));
    result.setUpdatedOn(rs.getTimestamp("updated_on"));

    if (Constants.NOT_ACKNOWLEDGED.equals(rs.getString("acknowledged"))) {
      result.setAcknowledged(false);
    }
    if (Constants.ACKNOWLEDGED.equals(rs.getString("acknowledged"))) {
      result.setAcknowledged(true);
    }

    return result;
  }

  public void setConnection(AdaptrisConnection connection) {
    this.connection = connection;
  }

  public Connection getConnection() throws SQLException {
    return this.connection.retrieveConnection(DatabaseConnection.class).connect();
  }

  @Override
  public void makeConnection(AdaptrisConnection connection) {
    setConnection(connection);
  }
}
