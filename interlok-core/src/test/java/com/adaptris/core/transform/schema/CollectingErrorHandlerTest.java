package com.adaptris.core.transform.schema;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.*;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Test;
import org.xml.sax.SAXParseException;

public class CollectingErrorHandlerTest {

 
  @Test
  public void testHasErrors() throws Exception {
    CollectingSaxErrorHandler handler = new CollectingSaxErrorHandler();
    assertFalse(handler.hasErrors());
    handler.error(createException());
    assertTrue(handler.hasErrors());
    assertEquals(1, handler.errors().spliterator().getExactSizeIfKnown());
    handler.fatalError(createException());
    assertTrue(handler.hasErrors());
    assertEquals(1, handler.fatalErrors().spliterator().getExactSizeIfKnown());
    
    CollectingSaxErrorHandler h2 = new CollectingSaxErrorHandler();
    h2.warning(createException());
    h2.fatalError(createException());
    assertTrue(handler.hasErrors());
    
  }
    
  public static SAXParseException createException() {
    return createException("Dummy Exception");
  }
  
  
  public static SAXParseException createException(String msg) {
    Random r = ThreadLocalRandom.current();
    return new SAXParseException(msg, "", "", r.nextInt(10), r.nextInt(100));
  }
}
