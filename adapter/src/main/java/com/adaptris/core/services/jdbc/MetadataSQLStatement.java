package com.adaptris.core.services.jdbc;

import javax.validation.constraints.NotNull;

import com.adaptris.core.AdaptrisMessage;

public class MetadataSQLStatement implements JdbcStatementCreator {

  public MetadataSQLStatement() {
  }
  
  @NotNull
  private String metadataKey;
  
  @Override
  public String createStatement(AdaptrisMessage msg) {
    return msg.getMetadataValue(getMetadataKey());
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String metadataKey) {
    this.metadataKey = metadataKey;
  }

}
