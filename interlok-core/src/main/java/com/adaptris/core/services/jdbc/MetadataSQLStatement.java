package com.adaptris.core.services.jdbc;

import javax.validation.constraints.NotBlank;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Build an SQL Statement for {@link JdbcDataQueryService} from metadata.
 * 
 * @config jdbc-metadata-sql-statement
 * @author gdries
 *
 */
@JacksonXmlRootElement(localName = "jdbc-metadata-sql-statement")
@XStreamAlias("jdbc-metadata-sql-statement")
public class MetadataSQLStatement implements JdbcStatementCreator {


  @NotBlank
  private String metadataKey;

  public MetadataSQLStatement() {
  }
  
  @Override
  public String createStatement(AdaptrisMessage msg) {
    return msg.getMetadataValue(getMetadataKey());
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String metadataKey) {
    this.metadataKey = Args.notBlank(metadataKey, "metadataKey");
  }

}
