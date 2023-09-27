package com.adaptris.core.jdbc.retry;

import java.util.Date;

/**
 * <p>
 * Represents an entry in <code>retry_store</code>, the sole table in the 
 * 'retry store database'.
 * </p>
 */
class JdbcRetryStoreEntry {

  private String messageId;
  private String acknowledgeId;
  private byte[] encodedMessage;
  private int retryInterval;
  private int totalRetries;
  private int retriesToDate;
  private String marshalledService;
  private boolean acknowledged;
  private Date insertedOn;
  private Date updatedOn;

  Date getInsertedOn() {
    return insertedOn;
  }
  
  void setInsertedOn(Date d) {
    this.insertedOn = d;
  }
  
  Date getUpdatedOn() {
    return updatedOn;
  }
  
  void setUpdatedOn(Date d) {
    this.updatedOn = d;
  }
  
  String getAcknowledgeId() {
    return acknowledgeId;
  }
  
  void setAcknowledgeId(String s) {
    this.acknowledgeId = s;
  }
  
  byte[] getEncodedMessage() {
    return encodedMessage;
  }
  
  void setEncodedMessage(byte[] b) {
    this.encodedMessage = b;
  }
  
  String getMessageId() {
    return messageId;
  }
  
  void setMessageId(String s) {
    this.messageId = s;
  }
  
  int getTotalRetries() {
    return totalRetries;
  }
  
  void setTotalRetries(int i) {
    this.totalRetries = i;
  }
  
  int getRetriesToDate() {
    return retriesToDate;
  }
  
  void setRetriesToDate(int i) {
    this.retriesToDate = i;
  }
  
  int getRetryInterval() {
    return retryInterval;
  }
  
  void setRetryInterval(int i) {
    this.retryInterval = i;
  }
  
  String getMarshalledService() {
    return marshalledService;
  }
  
  void setMarshalledService(String s) {
    this.marshalledService = s;
  }
  
  boolean getAcknowledged() {
    return acknowledged;
  }
  
  void setAcknowledged(boolean b) {
    this.acknowledged = b;
  }
}
