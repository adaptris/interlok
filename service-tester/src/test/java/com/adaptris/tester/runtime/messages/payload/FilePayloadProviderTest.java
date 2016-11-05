package com.adaptris.tester.runtime.messages.payload;

import com.adaptris.tester.runtime.messages.MessageException;
import com.adaptris.tester.runtime.messages.MessagesCase;
import org.junit.Test;

import java.io.File;

public class FilePayloadProviderTest extends MessagesCase {

  public FilePayloadProviderTest(String name) {
    super(name);
  }

  public void testGetFile() throws Exception {
    FilePayloadProvider f = new FilePayloadProvider("file:////home/user/payload.xml");
    assertEquals("file:////home/user/payload.xml", f.getFile());
  }

  public void testGetPayload() throws Exception {
    String file = "http_stubs/__files/hello.json";
    File testFile = new File(this.getClass().getClassLoader().getResource(file).getFile());
    String filePath = "file:///" + testFile.getAbsolutePath();
    checkFileExists(filePath);
    FilePayloadProvider f = new FilePayloadProvider(filePath);
    f.init();
    assertEquals("{\"hello\": \"world\"}",f.getPayload());
  }

  @Test
  public void testGetSourceNoFiles() throws Exception {
    try {
      final String testFile = "service.xml";
      File parentDir = new File(this.getClass().getClassLoader().getResource(testFile).getFile()).getParentFile();
      FilePayloadProvider f = new FilePayloadProvider("file:///" + parentDir.getAbsolutePath() + "/doesnotexist.xml");
      f.init();
      f.getPayload();
      fail();
    } catch (MessageException e){
      assertTrue(e.getMessage().contains("Failed to read file"));
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new FilePayloadProvider("file:////home/user/payload.xml");
  }
}