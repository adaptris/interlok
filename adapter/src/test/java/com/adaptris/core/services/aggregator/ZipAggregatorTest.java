package com.adaptris.core.services.aggregator;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.services.metadata.AddFormattedMetadataService;
import com.adaptris.core.services.splitter.SplitByMetadata;
import com.adaptris.core.services.splitter.SplitJoinService;
import com.adaptris.core.services.splitter.SplitJoinServiceTest;
import com.adaptris.core.stubs.DefectiveMessageFactory;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.adaptris.core.services.aggregator.ZipAggregator.DEFAULT_FILENAME_METADATA;

public class ZipAggregatorTest extends AggregatingServiceExample {


  public ZipAggregatorTest(String name) {
    super(name);
  }

  public void testJoinMessage() throws Exception {

    ZipAggregator aggr = new ZipAggregator();
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>hello</document>");
    splitMsg1.addMetadata(DEFAULT_FILENAME_METADATA, "file1.xml");
    AdaptrisMessage splitMsg2 = new DefectiveMessageFactory().newMessage("<document>world</document>");
    splitMsg2.addMetadata(DEFAULT_FILENAME_METADATA, "file2.xml");
    AdaptrisMessage willBeIgnoredMsg = new DefectiveMessageFactory().newMessage("<document>world</document>");
    aggr.joinMessage(original, Arrays.asList(splitMsg1, splitMsg2, willBeIgnoredMsg));

    boolean isZipped = new ZipInputStream(new ByteArrayInputStream(original.getPayload())).getNextEntry() != null;

    assertTrue(isZipped);

    Map<String, String> results = zipBytesToResultsMap(original.getPayload());

    assertEquals(2, results.size());
    assertTrue(results.containsKey("file1.xml"));
    assertTrue(results.containsKey("file2.xml"));
    assertEquals(results.get("file1.xml"), "<document>hello</document>");
    assertEquals(results.get("file2.xml"), "<document>world</document>");

  }

  public void testGetFilenameMetadata() throws Exception {
    ZipAggregator z = new ZipAggregator();
    assertEquals(DEFAULT_FILENAME_METADATA, z.filenameMetadata());
    z.setFilenameMetadata("filename-via-value");
    assertEquals("filename-via-value", z.filenameMetadata());
    z = new ZipAggregator("filename-via-constructor");
    assertEquals("filename-via-constructor", z.filenameMetadata());
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    SplitJoinService service = new SplitJoinService();
    AddFormattedMetadataService addFormattedMetadataService = new AddFormattedMetadataService();
    addFormattedMetadataService.setFormatString("%1$s.xml");
    addFormattedMetadataService.setMetadataKey("filename");
    addFormattedMetadataService.setArgumentMetadataKeys(Collections.singletonList("value"));
    service.setService(SplitJoinServiceTest.wrap(addFormattedMetadataService));
    service.setSplitter(new SplitByMetadata("comma-separated-list", "value"));
    service.setAggregator(new ZipAggregator());
    return service;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-ZipAggregator";
  }

  /**
   * Returns a Map<String, String> where filename is key and value is file contents.
   * @param bytes zip bytes
   * @return Map where filename is key and value is file contents
   * @throws Exception
   */

  private Map<String, String> zipBytesToResultsMap(byte[] bytes) throws Exception{
    byte[] buffer = new byte[1024];

    Map<String, String> results  = new HashMap<>();
    ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes));
    try {
      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null) {
        String fileName = ze.getName();
        String contents = "";
        int len;
        while ((len = zis.read(buffer)) > 0) {
          contents += new String(buffer, 0, len);
        }
        results.put(fileName, contents);
      }
    } finally {
      zis.closeEntry();
      zis.close();
    }
    return results;
  }
}