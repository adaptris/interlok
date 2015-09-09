package com.adaptris.util;

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.util.text.mime.ByteArrayDataSource;

public class ByteArrayDataSourceTest extends TestCase {

  private static final String EXAMPLE_XML = "<document>" + "<root>"
      + "<data>abcdefg</data>" + "</root>" + "</document>";

  private transient Log logR;

  public ByteArrayDataSourceTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    logR = LogFactory.getLog(this.getClass());

  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testInputStream() throws Exception {
    ByteArrayDataSource bads = new ByteArrayDataSource(EXAMPLE_XML.getBytes(),
        "text/xml", "name");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(bads.getBytes());
    out.flush();
    assertEquals("XML", out.toString(), EXAMPLE_XML);
  }

  public void testContentType() throws Exception {
    ByteArrayDataSource bads = new ByteArrayDataSource(EXAMPLE_XML.getBytes(),
        "text/xml", "name");
    assertEquals("content type", "text/xml", bads.getContentType());
  }

  public void testName() throws Exception {
    ByteArrayDataSource bads = new ByteArrayDataSource(EXAMPLE_XML.getBytes(),
        "text/xml", "name");
    assertEquals("name", "name", bads.getName());
  }

  public void testOutputStream() throws Exception {
    ByteArrayDataSource bads = new ByteArrayDataSource(EXAMPLE_XML.getBytes(),
        "text/xml", "name");
    try {
      bads.getOutputStream();
      fail("getOutputStream should not be supported");
    }
    catch (Exception e) {
      ;
    }
  }

}
