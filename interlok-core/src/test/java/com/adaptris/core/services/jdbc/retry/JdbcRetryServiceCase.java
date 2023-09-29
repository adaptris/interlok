package com.adaptris.core.services.jdbc.retry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.NullConnection;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.jdbc.JdbcService;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.interlok.junit.scaffolding.services.JdbcServiceCase;

public abstract class JdbcRetryServiceCase extends JdbcServiceCase {

  protected static final String JDBC_RETRY_SERVICE_DRIVER = "jdbc.retryservice.driver";
  protected static final String JDBC_RETRY_SERVICE_URL = "jdbc.retryservice.url";
  protected static final String JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE = "jdbc.retryservice.sql.properties.file";


  @Test
  public void testStoreMessageForRetrySyncNoFailure() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test payload");
    msg.addMetadata("retryAckInterval", "2000");
    msg.addMetadata("retryRetries", "2");
    JdbcStoreMessageForRetryService service = new JdbcStoreMessageForRetryService();
    service.setConnection(createConnectionForService());
    service.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    service.setAsynchronousAcknowledgment(false);
    service.setService(createServiceForTestsThatPasses());
    execute(service, msg);
    

    assertFalse(msg.headersContainsKey("retryAckId"));
    assertFalse(msg.headersContainsKey("retryService"));
  }
  
  @Test
  public void testStoreMessageForRetrySyncWithFailure() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test payload");
    msg.addMetadata("retryAckInterval", "2000");
    msg.addMetadata("retryRetries", "2");
    JdbcStoreMessageForRetryService service = new JdbcStoreMessageForRetryService();
    service.setConnection(createConnectionForService());
    service.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    service.setAsynchronousAcknowledgment(false);
    service.setService(createServiceForTestsThatFails());
    execute(service, msg);
    Object[] params = new Object[] { msg.getUniqueId()};
    String result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "acknowledge_id");
   
    assertTrue(msg.headersContainsKey("retryAckId"));
    assertTrue(msg.headersContainsKey("retryService"));
    assertEquals(msg.getMetadataValue("retryAckId"), result);
  }
  
  @Test
  public void testStoreMessageForRetryAsyncAutoRetryOnFail() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test payload");
    msg.addMetadata("retryAckInterval", "2000");
    msg.addMetadata("retryRetries", "2");
    JdbcStoreMessageForRetryService service = new JdbcStoreMessageForRetryService();
    service.setConnection(createConnectionForService());
    service.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    service.setAsynchronousAcknowledgment(true);
    service.setAsyncAutoRetryOnFail(true);
    service.setService(createServiceForTestsThatPasses());
    execute(service, msg);
    Object[] params = new Object[] { msg.getUniqueId()};
    String result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "acknowledge_id");
    

    assertTrue(msg.headersContainsKey("retryAckId"));
    assertTrue(msg.headersContainsKey("retryService"));
    assertEquals(msg.getMetadataValue("retryAckId"), result);
  }
  
  @Test
  public void testStoreMessageForRetryAsyncAutoRetryOnFailWithException() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test payload");
    msg.addMetadata("retryAckInterval", "2000");
    msg.addMetadata("retryRetries", "2");
    JdbcStoreMessageForRetryService service = new JdbcStoreMessageForRetryService();
    service.setConnection(createConnectionForService());
    service.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    service.setAsynchronousAcknowledgment(true);
    service.setAsyncAutoRetryOnFail(true);
    service.setService(createServiceForTestsThatFails());
    execute(service, msg);
    Object[] params = new Object[] { msg.getUniqueId()};
    String result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "acknowledge_id");
    

    assertTrue(msg.headersContainsKey("retryAckId"));
    assertTrue(msg.headersContainsKey("retryService"));
    assertEquals(msg.getMetadataValue("retryAckId"), result);
  }
  
  @Test
  public void testStoreMessageForRetryAsyncNoAutoRetryOnFail() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test payload");
    msg.addMetadata("retryAckInterval", "2000");
    msg.addMetadata("retryRetries", "2");
    JdbcStoreMessageForRetryService service = new JdbcStoreMessageForRetryService();
    service.setConnection(createConnectionForService());
    service.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    service.setAsynchronousAcknowledgment(true);
    service.setAsyncAutoRetryOnFail(false);
    service.setService(createServiceForTestsThatPasses());
    execute(service, msg);
    Object[] params = new Object[] { msg.getUniqueId()};
    String result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "acknowledge_id");

    assertTrue(msg.headersContainsKey("retryAckId"));
    assertTrue(msg.headersContainsKey("retryService"));
    assertEquals(msg.getMetadataValue("retryAckId"), result);
  }
  
  @Test
  public void testStoreMessageForRetryAsyncNoAutoRetryOnFailWithException() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test payload");
    msg.addMetadata("retryAckInterval", "2000");
    msg.addMetadata("retryRetries", "2");
    JdbcStoreMessageForRetryService service = new JdbcStoreMessageForRetryService();
    service.setConnection(createConnectionForService());
    service.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    service.setAsynchronousAcknowledgment(true);
    service.setAsyncAutoRetryOnFail(false);
    service.setService(createServiceForTestsThatFails());
    
    
    assertThrows(ServiceException.class, () -> {
      execute(service, msg);
    }, "Normal exception throwing.");
  }
  
  @Test
  public void testStoreMessageForRetryMissingRequiredMetadataException() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test payload");
    JdbcStoreMessageForRetryService service = new JdbcStoreMessageForRetryService();
    service.setConnection(createConnectionForService());
    service.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    service.setAsynchronousAcknowledgment(true);
    service.setAsyncAutoRetryOnFail(false);
    service.setService(createServiceForTestsThatFails());
    
    
    assertThrows(ServiceException.class, () -> {
      execute(service, msg);
    }, "Required metadata missing");
  }
  
  @Test
  public void testRetryMessageAsyncAutoRetryOnFail() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test payload");
    msg.addMetadata("retryAckInterval", "40");
    msg.addMetadata("retryRetries", "3");
    JdbcStoreMessageForRetryService service = new JdbcStoreMessageForRetryService();
    service.setConnection(createConnectionForService());
    service.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    service.setAsynchronousAcknowledgment(true);
    service.setAsyncAutoRetryOnFail(true);
    service.setService(createServiceForTestsThatFails());
    execute(service, msg);
    Object[] params = new Object[] { msg.getUniqueId()};
    String result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "retries_to_date");

    assertEquals("0", result.toString());
    
    TimeUnit.MILLISECONDS.sleep(50);

    JdbcRetryMessagesService retryService = new JdbcRetryMessagesService();
    retryService.setConnection(createConnectionForService());
    retryService.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    execute(retryService, msg);
    result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "retries_to_date");
    
    assertEquals("1", result.toString());
  }
  
  @Test
  public void testRetryMessageAsyncNoAutoRetryOnFail() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test payload");
    msg.addMetadata("retryAckInterval", "40");
    msg.addMetadata("retryRetries", "3");
    JdbcStoreMessageForRetryService service = new JdbcStoreMessageForRetryService();
    service.setConnection(createConnectionForService());
    service.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    service.setAsynchronousAcknowledgment(true);
    service.setAsyncAutoRetryOnFail(false);
    service.setService(createServiceForTestsThatPasses());
    execute(service, msg);
    Object[] params = new Object[] { msg.getUniqueId()};
    String result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "retries_to_date");

    assertEquals("0", result.toString());
    
    TimeUnit.MILLISECONDS.sleep(50);

    JdbcRetryMessagesService retryService = new JdbcRetryMessagesService();
    retryService.setConnection(createConnectionForService());
    retryService.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    execute(retryService, msg);
    result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "retries_to_date");
    
    assertEquals("1", result.toString());
  }
  
  @Test
  public void testRetryMessageSyncWithException() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test payload");
    msg.addMetadata("retryAckInterval", "40");
    msg.addMetadata("retryRetries", "3");
    JdbcStoreMessageForRetryService service = new JdbcStoreMessageForRetryService();
    service.setConnection(createConnectionForService());
    service.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    service.setAsynchronousAcknowledgment(false);
    service.setService(createServiceForTestsThatFails());
    execute(service, msg);
    Object[] params = new Object[] { msg.getUniqueId()};
    String result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "retries_to_date");

    assertEquals("0", result.toString());
    
    TimeUnit.MILLISECONDS.sleep(50);

    JdbcRetryMessagesService retryService = new JdbcRetryMessagesService();
    retryService.setConnection(createConnectionForService());
    retryService.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    execute(retryService, msg);
    result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "retries_to_date");
    
    assertEquals("1", result.toString());
  }
  
  
  //TO REVIEW
  @Test
  public void testRetryMessageSync() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test payload");
    msg.addMetadata("retryAckInterval", "40");
    msg.addMetadata("retryRetries", "-1");
    JdbcStoreMessageForRetryService service = new JdbcStoreMessageForRetryService();
    service.setConnection(createConnectionForService());
    service.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    service.setAsynchronousAcknowledgment(false);
    service.setService(createServiceForTestsThatFails());
    execute(service, msg);
    Object[] params = new Object[] { msg.getUniqueId()};
    String result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "retries_to_date");

    assertEquals("0", result.toString());
    
    TimeUnit.MILLISECONDS.sleep(50);

    JdbcRetryMessagesService retryService = new JdbcRetryMessagesService();
    retryService.setConnection(createConnectionForService());
    retryService.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    execute(retryService, msg);
    result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "retries_to_date");
    
    assertEquals("1", result.toString());
  }
  
  @Test
  public void testRetryMessagePruneExpired() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test payload");
    msg.addMetadata("retryAckInterval", "40");
    msg.addMetadata("retryRetries", "1");
    JdbcStoreMessageForRetryService service = new JdbcStoreMessageForRetryService();
    service.setConnection(createConnectionForService());
    service.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    service.setAsynchronousAcknowledgment(false);
    service.setService(createServiceForTestsThatFails());
    execute(service, msg);
    Object[] params = new Object[] { msg.getUniqueId()};
    String result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "acknowledge_id");

    TimeUnit.MILLISECONDS.sleep(50);

    JdbcRetryMessagesService retryService = new JdbcRetryMessagesService();
    retryService.setConnection(createConnectionForService());
    retryService.setPruneExpired(true);
    retryService.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    execute(retryService, msg);
    result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "acknowledge_id");
    
    assertEquals(msg.getMetadataValue("retryAckId"), result);
    
    TimeUnit.MILLISECONDS.sleep(50);
    execute(retryService, msg);
    result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "acknowledge_id");

    assertEquals(null, result);
  }
  
  @Test
  public void testAcknowledgeWithoutPruning() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test payload");
    msg.addMetadata("retryAckInterval", "2000");
    msg.addMetadata("retryRetries", "2");
    JdbcStoreMessageForRetryService service = new JdbcStoreMessageForRetryService();
    service.setConnection(createConnectionForService());
    service.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    service.setAsynchronousAcknowledgment(false);
    service.setService(createServiceForTestsThatFails());
    execute(service, msg);
    Object[] params = new Object[] { msg.getUniqueId()};
    String result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "acknowledged");
   
    assertEquals("F", result);
    
    JdbcAcknowledgeService ackService = new JdbcAcknowledgeService();
    ackService.setConnection(createConnectionForService());
    ackService.setPruneAcknowledged(false);
    ackService.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    execute(ackService, msg);
    execute(ackService, msg);
    result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "acknowledged");
    
    assertEquals("T", result);
  }
  
  @Test
  public void testAcknowledgeWithPruning() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test payload");
    msg.addMetadata("retryAckInterval", "2000");
    msg.addMetadata("retryRetries", "2");
    JdbcStoreMessageForRetryService service = new JdbcStoreMessageForRetryService();
    service.setConnection(createConnectionForService());
    service.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    service.setAsynchronousAcknowledgment(false);
    service.setService(createServiceForTestsThatFails());
    execute(service, msg);
    Object[] params = new Object[] { msg.getUniqueId()};
    String result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "acknowledged");
   
    assertEquals("F", result);
    
    JdbcAcknowledgeService ackService = new JdbcAcknowledgeService();
    ackService.setConnection(createConnectionForService());
    ackService.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    execute(ackService, msg);
    execute(ackService, msg);
    result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "acknowledged");
    
    assertEquals(null, result);
  }
  
  public void testAcknowledgeWithNullAckId() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test payload");
    msg.addMetadata("retryAckInterval", "2000");
    msg.addMetadata("retryRetries", "2");
    JdbcStoreMessageForRetryService service = new JdbcStoreMessageForRetryService();
    service.setConnection(createConnectionForService());
    service.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    service.setAsynchronousAcknowledgment(false);
    service.setService(createServiceForTestsThatFails());
    execute(service, msg);
    Object[] params = new Object[] { msg.getUniqueId()};
    String result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "acknowledged");
   
    assertEquals("F", result);
    
    JdbcAcknowledgeService ackService = new JdbcAcknowledgeService();
    ackService.setConnection(createConnectionForService());
    ackService.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    msg.clearMetadata();
    msg.addMetadata("retryAckId", null);
    execute(ackService, msg);
    result = queryDatabaseForTests("SELECT * FROM retry_store WHERE message_id=?", params, "acknowledged");
    
    assertEquals(null, result);
  }
  
  @Test
  public void testNullConnectionException() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test payload");
    msg.addMetadata("retryAckInterval", "2000");
    msg.addMetadata("retryRetries", "2");
    JdbcStoreMessageForRetryService service = new JdbcStoreMessageForRetryService();
    service.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    assertThrows(CoreException.class, () -> {
      execute(service, msg);
    }, "Null connection");
  }
  
  @Test
  public void testNullExpiredMessageProducerException() throws Exception {
    JdbcRetryMessagesService service = new JdbcRetryMessagesService();
    assertThrows(IllegalArgumentException.class, () -> {
      service.setExpiredMessagesProducer(null);
    }, "Null producer");
  }
  

  @Test
  public void testUnableToReadSqlPropertiesFile() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test payload");
    msg.addMetadata("retryAckInterval", "2000");
    msg.addMetadata("retryRetries", "2");
    JdbcStoreMessageForRetryService service = new JdbcStoreMessageForRetryService();
    service.setConnection(createConnectionForService());
    service.setSqlPropertiesFile(PROPERTIES.getProperty("non existent"));
    service.setAsynchronousAcknowledgment(true);
    service.setAsyncAutoRetryOnFail(false);
    service.setService(createServiceForTestsThatFails());
    
    
    assertThrows(CoreException.class, () -> {
      execute(service, msg);
    }, "Unable to read properties file");
  }
  
  @Test
  public void testWrongConnectionType() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test payload");
    msg.addMetadata("retryAckInterval", "2000");
    msg.addMetadata("retryRetries", "2");
    AdaptrisConnection nullConnection = new NullConnection();
    JdbcStoreMessageForRetryService service = new JdbcStoreMessageForRetryService();
    service.setConnection(nullConnection);
    service.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    service.setAsynchronousAcknowledgment(false);
    service.setService(createServiceForTestsThatFails());
    
    JdbcRetryMessagesService retryService = new JdbcRetryMessagesService();
    retryService.setConnection(nullConnection);
    retryService.setPruneExpired(true);
    retryService.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    
    JdbcAcknowledgeService ackService = new JdbcAcknowledgeService();
    ackService.setConnection(nullConnection);
    ackService.setSqlPropertiesFile(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_SQL_PROPERTIES_FILE));
    
    assertThrows(ClassCastException.class, () -> {
      execute(service, msg);
    }, "Wrong connection type");
    assertThrows(ClassCastException.class, () -> {
      execute(retryService, msg);
    }, "Wrong connection type");

  }
  
 
  @Override
  protected Object retrieveObjectForSampleConfig() {
    return createService();
  }
  
  //Helper methods for the unit tests.
  
  protected abstract JdbcService createService();
  
  private AdaptrisConnection createConnectionForService() {
    return new JdbcConnection(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_URL),
        PROPERTIES.getProperty(JDBC_RETRY_SERVICE_DRIVER));
  }
  
  private Service createServiceForTestsThatFails() {
    return new ThrowExceptionService(new ConfiguredException("failed"));
  }
  
  private Service createServiceForTestsThatPasses() {
    return new AddMetadataService(new MetadataElement("key1", "val1"));
  }


  private Connection createConnection() throws Exception {
    Class.forName(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_DRIVER));
    Connection conn = DriverManager.getConnection(PROPERTIES.getProperty(JDBC_RETRY_SERVICE_URL));
    conn.setAutoCommit(true);
    return conn;
  }
 
//we query the same DB table outside of the service in order to check and compare entries were inserted, updated, deleted successfully
//and as expected.
  private String queryDatabaseForTests(String query, Object[] parameters, String resultColumn) throws Exception {
    Connection conn = createConnection();
    ResultSet rs = null;
    PreparedStatement ps = conn.prepareStatement(query);
    try {
      int count = 1;
      for (Object o : parameters) {
        ps.setObject(count, o);
        count++;
      }
      rs = ps.executeQuery();
      while (rs.next()) {
       return rs.getObject(resultColumn).toString();
      }
    }
    finally {
      JdbcUtil.closeQuietly(ps);
      JdbcUtil.closeQuietly(conn);
    }
    return null;   
  }
}
