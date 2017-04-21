package com.adaptris.core.services.jdbc;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Build an SQL Statement for {@link JdbcDataQueryService} from static configuration.
 * 
 * @config jdbc-configured-sql-statement
 * @author gdries
 *
 */
@XStreamAlias("jdbc-configured-sql-statement")
public class ConfiguredSQLStatement implements JdbcStatementCreator {

  @NotBlank
  @InputFieldHint(style = "SQL")
  @InputFieldHint(expression = true)
  private String statement;


  public ConfiguredSQLStatement() {
  }
  
  public ConfiguredSQLStatement(String statement) {
    setStatement(statement);
  }
  
  @Override
  public String createStatement(AdaptrisMessage msg) {
    return msg.resolve(statement);
  }

  public String getStatement() {
    return statement;
  }

  public void setStatement(String statement) {
    this.statement = Args.notBlank(statement, "statement");
  }

}
