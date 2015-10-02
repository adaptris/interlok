package com.adaptris.core;

import static com.adaptris.core.CoreConstants.OBJ_METADATA_EXCEPTION;

import java.io.IOException;

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairCollection;

public class ExceptionDestinationTest extends ExampleProduceDestinationCase {

  private static final String DEFAULT_DEST = "DEFAULT_DEST";
  public ExceptionDestinationTest(String name) {
    super(name);
  }

  @Override
  public void setUp() {
  }

  @Override
  public void tearDown() {

  }

  public void testSetDefaultDestination() {
    ExceptionDestination d = new ExceptionDestination();
    d.setDefaultDestination(DEFAULT_DEST);
    assertEquals(DEFAULT_DEST, d.getDefaultDestination());
    try {
      d.setDefaultDestination("");
      fail("'' destination");
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      d.setDefaultDestination(null);
      fail("null destination");
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(DEFAULT_DEST, d.getDefaultDestination());

  }

  public void testSetExceptionMapping() {
    ExceptionDestination d = new ExceptionDestination();
    KeyValuePairCollection col = createExceptionMappings();
    d.setExceptionMapping(col);
    assertTrue(col == d.getExceptionMapping());
    assertEquals(col, d.getExceptionMapping());
    try {
      d.setExceptionMapping(null);
      fail("null Exception mapping");
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(col, d.getExceptionMapping());
  }

  public void testFirstMatchedException() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    Exception e = new CoreException(new ProduceException());
    msg.addObjectHeader(OBJ_METADATA_EXCEPTION, e);
    ExceptionDestination ed = new ExceptionDestination(DEFAULT_DEST, createExceptionMappings());
    String dest = ed.getDestination(msg);
    assertEquals(ed.toString(), CoreException.class.getName(), dest);
  }

  public void testNestedException() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    Exception e = new Exception(new CoreException());
    msg.addObjectHeader(OBJ_METADATA_EXCEPTION, e);
    ExceptionDestination ed = new ExceptionDestination(DEFAULT_DEST, createExceptionMappings());
    String dest = ed.getDestination(msg);
    assertEquals(ed.toString(), CoreException.class.getName(), dest);
  }

  public void testExceptionNotFound() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    Exception e = new Exception(new NullPointerException());
    msg.addObjectHeader(OBJ_METADATA_EXCEPTION, e);
    ExceptionDestination ed = new ExceptionDestination(DEFAULT_DEST, createExceptionMappings());
    String dest = ed.getDestination(msg);
    assertEquals(ed.toString(), DEFAULT_DEST, dest);
  }

  public void testNoException() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ExceptionDestination ed = new ExceptionDestination(DEFAULT_DEST, createExceptionMappings());
    String dest = ed.getDestination(msg);
    assertEquals(ed.toString(), DEFAULT_DEST, dest);
  }

  private KeyValuePairCollection createExceptionMappings() {
    KeyValuePairCollection kvps = new KeyValuePairCollection();
    kvps.add(new KeyValuePair(CoreException.class.getName(), CoreException.class.getName()));
    kvps.add(new KeyValuePair(ProduceException.class.getName(), ProduceException.class.getName()));
    return kvps;

  }

  @Override
  protected ProduceDestination createDestinationForExamples() {
    ExceptionDestination dest = new ExceptionDestination();
    KeyValuePairCollection kvps = new KeyValuePairCollection();
    kvps.add(new KeyValuePair(CoreException.class.getName(), "The Destination associated with CoreException"));
    kvps.add(new KeyValuePair(ServiceException.class.getName(), "The Destination associated with ServiceException"));
    kvps.add(new KeyValuePair(ProduceException.class.getName(), "The Destination associated with ProduceException"));
    kvps.add(new KeyValuePair(IOException.class.getName(), "The Destination associted with an IOException"));
    dest.setExceptionMapping(kvps);
    dest.setDefaultDestination("The Default Destination if no match");
    return dest;
  }

  @Override
  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object)
        + "<!--\n\nThis ProduceDestination implementation derives its destination from the exception in object metatdata."
        + "\nThe exception and nested causes are matched based on class name. If the exception matches"
        + "\nthen the string associated with this class name is used as the destination."
        + "\nIf no match is found then the default destination will be used."
        + "\n\nThis destination is only valid when used as part of a ProcessingExceptionHandler chain"
        + "\nas during normal operations you won't have any exceptions" + "\n\n-->\n";
  }

}
