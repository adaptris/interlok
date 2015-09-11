package com.adaptris.core.services.jdbc;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Character Stream Statement Parameter.
 * 
 * <p>
 * Note that this class pays no heed to the {@link StatementParameter#setQueryClass(String)} setting. Additionally,
 * {@link StatementParameter#setConvertNull(Boolean)} has no meaning; null/empty/whitespace values are implicitly false.
 * </p>
 * 
 * @config jdbc-character-stream-statement-parameter
 * @author amcgrath
 * 
 */
@XStreamAlias("jdbc-character-stream-statement-parameter")
public class CharacterStreamStatementParameter extends StatementParameter {
  
  public CharacterStreamStatementParameter() {
    super();
    setQueryType(QueryType.payload);
  }

  public CharacterStreamStatementParameter(String query, Class<?> clazz, QueryType type) {
    this(query, clazz, type, null);
  }

  public CharacterStreamStatementParameter(String query, String classname, QueryType type) {
    this(query, classname, type, null);
  }

  public CharacterStreamStatementParameter(String query, String classname, QueryType type, Boolean nullConvert) {
    this(query, classname, type, nullConvert, null);
  }

  public CharacterStreamStatementParameter(String query, String classname, QueryType type, Boolean nullConvert, String paramName) {
    super(query, classname, type, nullConvert, paramName);
  }
  
  public CharacterStreamStatementParameter(String query, Class<?> clazz, QueryType type, Boolean nullConvert) {
    this(query, clazz.getName(), type, nullConvert);
  }

  @Override
  public void apply(int parameterIndex, PreparedStatement statement, AdaptrisMessage msg) throws SQLException, ServiceException {
    try {
      statement.setCharacterStream(parameterIndex, msg.getReader());
    } catch (IOException ex) {
      throw new ServiceException(ex);
    }
  }
  
  @Override
  public Object convertToQueryClass(Object value) throws ServiceException {
    return null;
  }
  
  public Object getQueryValue(AdaptrisMessage msg) {
    return null;
  }
  
}
