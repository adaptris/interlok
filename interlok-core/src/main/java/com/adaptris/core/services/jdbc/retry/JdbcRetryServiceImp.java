package com.adaptris.core.services.jdbc.retry;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.MimeEncoder;
import com.adaptris.core.jdbc.DatabaseConnection;
import com.adaptris.core.jdbc.JdbcService;
import com.adaptris.core.jdbc.retry.Constants;
import com.adaptris.core.jdbc.retry.JdbcRetryRowEntry;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.interlok.InterlokException;

import lombok.Getter;
import lombok.Setter;


/**
 * </p>
 * <p>
 * If there is no explicit configuration of the sqlProperties then the SQL
 * statements are found in the file <code>retry-store-derby.properties</code> which
 * is suitable for Apache Derby and MySQL. If explicitly configured, the
 * property file is expected to be present on the classpath.
 * </p>
 * <p>
 * The default property file statements are listed below.
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
 (acknowledged='F' AND (retries_to_date &lt; total_retries OR total_retries = -1))

 delete.acknowleged.sql = DELETE FROM retry_store WHERE acknowledged='T'

 delete.sql = DELETE FROM retry_store WHERE message_id=?

 select.expired.sql = SELECT * FROM retry_store \
 WHERE (acknowledged='F' AND \
 (retries_to_date &gt;= total_retries AND total_retries != -1))
 </code></pre>
 *
 * </p>
 * <p>
 * The create.sql script is <b>always</b> executed upon initialisation, 
 * any errors are discarded
 * </p>
 * <p>
 * Partial implementation of behaviour common to retry services.
 * </p>
 */

public abstract class JdbcRetryServiceImp extends JdbcService {
  
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


  protected static AdaptrisMarshaller marshaller;

  static { // only create marshaller once
    marshaller = DefaultMarshaller.getDefaultMarshaller();
  }
  
  /**
   * Set the sql properties file to use, by default it will look for the value
   * "retry-store-derby.properties"
   */
  
  @Getter
  @Setter
  @NotBlank
  @InputFieldDefault(value = RETRY_STORE_PROPERTIES)
  private String sqlPropertiesFile;
 
  @InputFieldDefault(value = "true")
  private boolean pruneAcknowledged;
  
  public JdbcRetryServiceImp() {
    setPruneAcknowledged(true);
    setSqlPropertiesFile(RETRY_STORE_PROPERTIES);
  }
  
  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  protected void initJdbcService() throws CoreException {
    encoder = new MimeEncoder();
    encoder.setRetainUniqueId(true);
    if (getConnection() == null) {
      throw new CoreException("DatabaseConnection is null in service");
    }
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
      try {
        createStoreTable();
        log.debug("store table created");
      } catch (Exception e) {
      }
    }
  }
   
  /** @see com.adaptris.core.AdaptrisComponent#prepare() */
  @Override
  protected void prepareService() throws CoreException {
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  protected void closeJdbcService() {}

  void pruneAcknowledged() {
    try {
      if (isPruneAcknowledged()) {
        log.debug("Pruning Previously Acknowledged Messages");
        deleteAcknowledged();
      }
    }
    catch (Exception e) {
      log.warn("Ignoring exception while pruning acknowledged messages["
          + e.getMessage() + "]");
    }
  }

  /**
   * @return the pruneAcknowledged
   */
  public boolean getPruneAcknowledged() {
    return pruneAcknowledged;
  }

  /**
   * Specify whether to delete messages from the underlying store if they have
   * already been acknowledged.
   *
   * @param b the pruneAcknowledged to set
   */
  public void setPruneAcknowledged(boolean b) {
    this.pruneAcknowledged = b;
  }
  
  private boolean isPruneAcknowledged() {
    return BooleanUtils.toBooleanDefaultIfNull(getPruneAcknowledged(), true);
  }

   void write(AdaptrisMessage msg) throws InterlokException {
    PreparedStatement ps = null;
    validateMessage(msg);
    Object[] params = new Object[] { msg.getUniqueId(), msg.getMetadataValue(Constants.ACKNOWLEDGE_ID_KEY),
        encoder.encode(msg), Integer.parseInt(msg.getMetadataValue(Constants.RETRY_INTERVAL_KEY)),
        Integer.parseInt(msg.getMetadataValue(Constants.RETRIES_KEY)), Integer.valueOf(0),
        msg.getMetadataValue(Constants.MARSHALLED_SERVICE_KEY).getBytes(), "F" };
    try {
      ps = prepareStatementWithParameters(sqlStatements.getProperty(INSERT_SQL), params);
      log.trace("executing insert statement");
      ps.executeUpdate();
    } catch (SQLException e) {
      throw ExceptionHelper.wrapInterlokException(e);
    } finally {
      JdbcUtil.closeQuietly(ps);
    }
  }

   boolean delete(String msgId) throws InterlokException {
    PreparedStatement ps = null;
    Object[] params = new Object[] { msgId };
    try {
      ps = prepareStatementWithParameters(sqlStatements.getProperty(DELETE_SQL), params);
      log.trace("executing delete statement");
      ps.executeUpdate();
      return true;
    } catch (SQLException e) {
      throw ExceptionHelper.wrapInterlokException(e);
    } finally {
      JdbcUtil.closeQuietly(ps);
    }
  }

   void acknowledge(String acknowledgeId) throws InterlokException {
    PreparedStatement ps = null;
    Object[] params = { Constants.ACKNOWLEDGED, new Date(), acknowledgeId };
    try {
      ps = prepareStatementWithParameters(sqlStatements.getProperty(ACKNOWLEDGE_SQL), params);
      log.trace("executing update statement");
      ps.executeUpdate();
    } catch (SQLException e) {
      throw ExceptionHelper.wrapInterlokException(e);
    } finally {
      JdbcUtil.closeQuietly(ps);
    }
  }

   void deleteAcknowledged() throws InterlokException {
    PreparedStatement ps = null;
    try {
      ps = prepareStatementWithoutParameters(sqlStatements.getProperty(DELETE_ACKNOWLEGED_SQL));
      log.trace("executing delete statement");
      ps.executeUpdate();
    } catch (SQLException e) {
      throw ExceptionHelper.wrapInterlokException(e);
    } finally {
      JdbcUtil.closeQuietly(ps);
    }
  }

   List<AdaptrisMessage> obtainExpiredMessages() throws InterlokException {
    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = prepareStatementWithoutParameters(sqlStatements.getProperty(SELECT_EXPIRED_SQL));
      log.trace("executing select statement");
      rs = ps.executeQuery();
      if (rs == null) {
        return result;
      }
      while (rs.next()) {
        JdbcRetryRowEntry resultRow = mapRow(rs);
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

   List<AdaptrisMessage> obtainMessagesToRetry() throws InterlokException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    try {
      ps = prepareStatementWithoutParameters(sqlStatements.getProperty(RETRY_SQL));
      log.trace("executing select statement");
      rs = ps.executeQuery();
      if (rs == null) {
        return result;
      }
      while (rs.next()) {
        JdbcRetryRowEntry resultRow = mapRow(rs);
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

   void updateRetryCount(String messageId) throws InterlokException {
    PreparedStatement ps = null;
    Object[] params = { new Date(), messageId };
    try {
      ps = prepareStatementWithParameters(sqlStatements.getProperty(UPDATE_RETRY_SQL), params);
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

  private AdaptrisMessage convert(JdbcRetryRowEntry retryStoreEntry) throws InterlokException {

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
      ps = prepareStatementWithoutParameters(sqlStatements.getProperty(CREATE_SQL));
      log.trace("Executing create statement");
      ps.executeUpdate();
    } catch (SQLException e) {
      throw ExceptionHelper.wrapInterlokException(e);
    } finally {
      JdbcUtil.closeQuietly(ps);
    }
  }

  private PreparedStatement prepareStatementWithParameters(String query, Object[] parameters) throws SQLException {
    Connection conn;
    conn = makeConnection();
    PreparedStatement ps = conn.prepareStatement(query);
    int count = 1;
    for (Object o : parameters) {
      ps.setObject(count, o);
      count++;
    }
    return ps;
  }

  private PreparedStatement prepareStatementWithoutParameters(String query) throws SQLException {
    Connection conn;
    conn = makeConnection();
    return conn.prepareStatement(query);
  }

  private JdbcRetryRowEntry mapRow(ResultSet rs) throws SQLException {
    JdbcRetryRowEntry result = new JdbcRetryRowEntry();

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

  private Connection makeConnection() throws SQLException {
   return getConnection().retrieveConnection(DatabaseConnection.class).connect();
  }

}
