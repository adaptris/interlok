package com.adaptris.core.jdbc.retry;

import java.util.Date;

/**
 * <p>
 * Represents an entry in <code>retry table</code>, the sole table in the 
 * 'retry database'.
 * </p>
 */
public class JdbcRetryRowEntry {

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

  public Date getInsertedOn() {
    return insertedOn;
  }
  
  public void setInsertedOn(Date d) {
    this.insertedOn = d;
  }
  
  public Date getUpdatedOn() {
    return updatedOn;
  }
  
  public void setUpdatedOn(Date d) {
    this.updatedOn = d;
  }
  
  public String getAcknowledgeId() {
    return acknowledgeId;
  }
  
  public void setAcknowledgeId(String s) {
    this.acknowledgeId = s;
  }
  
  public byte[] getEncodedMessage() {
    return encodedMessage;
  }
  
  public void setEncodedMessage(byte[] b) {
    this.encodedMessage = b;
  }
  
  public String getMessageId() {
    return messageId;
  }
  
  public void setMessageId(String s) {
    this.messageId = s;
  }
  
  public int getTotalRetries() {
    return totalRetries;
  }
  
  public void setTotalRetries(int i) {
    this.totalRetries = i;
  }
  
  public int getRetriesToDate() {
    return retriesToDate;
  }
  
  public void setRetriesToDate(int i) {
    this.retriesToDate = i;
  }
  
  public int getRetryInterval() {
    return retryInterval;
  }
  
  public void setRetryInterval(int i) {
    this.retryInterval = i;
  }
  
  public String getMarshalledService() {
    return marshalledService;
  }
  
  public void setMarshalledService(String s) {
    this.marshalledService = s;
  }
  
  public boolean getAcknowledged() {
    return acknowledged;
  }
  
  public void setAcknowledged(boolean b) {
    this.acknowledged = b;
  }
}
