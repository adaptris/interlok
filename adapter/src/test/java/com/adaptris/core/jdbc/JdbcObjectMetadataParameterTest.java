package com.adaptris.core.jdbc;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;

public class JdbcObjectMetadataParameterTest extends NullableParameterCase {
  
  private static final Integer METADATA_VALUE = new Integer(999);
  private static final String METADATA_KEY = "PARAM_METADATA_KEY";
  private AdaptrisMessage message;
  
  public void setUp() throws Exception {    
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
  }
  
  @Override
  protected JdbcObjectMetadataParameter createParameter() {
    return new JdbcObjectMetadataParameter();
  }

  public void testNoMetadataKeyForInputParam() throws Exception {    
    JdbcObjectMetadataParameter param = new JdbcObjectMetadataParameter();
    try {
      param.applyInputParam(message);
    } catch(JdbcParameterException ex) {
      // expected, pass
    }
  }

  public void testNoMetadataKeyForOutputParam() throws Exception {    
    JdbcObjectMetadataParameter param = new JdbcObjectMetadataParameter();
    try {
      param.applyOutputParam(null, message);
    } catch(JdbcParameterException ex) {
      // expected, pass
    }
  }
  
  public void testMetadataDoesNotExistInputParam() throws Exception {
    JdbcObjectMetadataParameter param = new JdbcObjectMetadataParameter();
    param.setMetadataKey("DOES_NOT_EXIST_KEY");
    try {
      param.applyInputParam(message);
    } catch(JdbcParameterException ex) {
      // expected, pass
    }
  }
  
  public void testMetadataDoesNotExistOutputParam() throws Exception {
    JdbcObjectMetadataParameter param = new JdbcObjectMetadataParameter();
    param.setMetadataKey("DOES_NOT_EXIST_KEY");
    param.applyOutputParam(null, message);
    // expected, pass
  }
  
  public void testMetadataAlreadyExistsOutputParam() throws Exception {
    message.addObjectMetadata(METADATA_KEY, METADATA_VALUE);
    assertEquals(message.getObjectMetadata().get(METADATA_KEY), METADATA_VALUE);
    
    JdbcObjectMetadataParameter param = new JdbcObjectMetadataParameter();
    param.setMetadataKey(METADATA_KEY);
    
    param.applyOutputParam("NEW_PARAM_METADATA_VALUE", message);
    
    assertEquals(message.getObjectMetadata().get(METADATA_KEY), "NEW_PARAM_METADATA_VALUE");
  }
  
  public void testMetadataAppliedInputParam() throws Exception {
    message.addObjectMetadata(METADATA_KEY, METADATA_VALUE);
    
    JdbcObjectMetadataParameter param = new JdbcObjectMetadataParameter();
    param.setMetadataKey(METADATA_KEY);
    Object appliedInputParam = param.applyInputParam(message);
    
    assertEquals(appliedInputParam, METADATA_VALUE);
  }
  
  public void testMetadataAppliedOutputParam() throws Exception {    
    JdbcObjectMetadataParameter param = new JdbcObjectMetadataParameter();
    param.setMetadataKey(METADATA_KEY);
    
    param.applyOutputParam(METADATA_VALUE, message);
    
    assertTrue(message.getObjectMetadata().containsKey(METADATA_KEY));
    assertEquals(message.getObjectMetadata().get(METADATA_KEY), METADATA_VALUE);
  }

}
