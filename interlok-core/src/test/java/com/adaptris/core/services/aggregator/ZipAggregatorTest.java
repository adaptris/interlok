package com.adaptris.core.services.aggregator;

import static com.adaptris.core.services.aggregator.ZipAggregator.DEFAULT_FILENAME_METADATA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.services.conditional.conditions.ConditionImpl;
import com.adaptris.core.services.metadata.AddFormattedMetadataService;
import com.adaptris.core.services.splitter.SplitByMetadata;
import com.adaptris.core.stubs.DefectiveMessageFactory;

public class ZipAggregatorTest extends AggregatingServiceExample {


  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
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

  @Test
  public void testJoinMessageWithFilter() throws Exception {
    ZipAggregator aggr = new ZipAggregator();
    aggr.setFilterCondition(new MetadataFilenameCondition());
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>hello</document>");
    splitMsg1.addMetadata(DEFAULT_FILENAME_METADATA, "xfile1.xml");
    AdaptrisMessage splitMsg2 = new DefectiveMessageFactory().newMessage("<document>world2</document>");
    splitMsg2.addMetadata(DEFAULT_FILENAME_METADATA, "file2.xml");
    AdaptrisMessage splitMsg3 = new DefectiveMessageFactory().newMessage("<document>world3</document>");
    splitMsg3.addMetadata(DEFAULT_FILENAME_METADATA, "xfile3.xml");
    AdaptrisMessage splitMsg4 = new DefectiveMessageFactory().newMessage("<document>world4</document>");
    splitMsg4.addMetadata(DEFAULT_FILENAME_METADATA, "file4.xml");
    AdaptrisMessage willBeIgnoredMsg = new DefectiveMessageFactory().newMessage("<document>world4</document>");
    aggr.joinMessage(original, Arrays.asList(splitMsg1, splitMsg2, splitMsg3, splitMsg4, willBeIgnoredMsg));

    boolean isZipped = new ZipInputStream(new ByteArrayInputStream(original.getPayload())).getNextEntry() != null;

    assertTrue(isZipped);

    Map<String, String> results = zipBytesToResultsMap(original.getPayload());

    assertEquals(2, results.size());
    assertTrue(results.containsKey("file2.xml"));
    assertTrue(results.containsKey("file4.xml"));
    assertEquals(results.get("file2.xml"), "<document>world2</document>");
    assertEquals(results.get("file4.xml"), "<document>world4</document>");

  }

  @Test
  public void testGetFilenameMetadata() throws Exception {
    ZipAggregator z = new ZipAggregator();
    assertEquals(DEFAULT_FILENAME_METADATA, z.filenameMetadata());
    z.setFilenameMetadata("filename-via-value");
    assertEquals("filename-via-value", z.filenameMetadata());
    z = new ZipAggregator("filename-via-constructor");
    assertEquals("filename-via-constructor", z.filenameMetadata());
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!--"
        + "\nThis aggregator takes all the files specified by 'filename'; creates a zipfile, and sets that as the payload."
        + "\n-->\n";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected List<Service> retrieveObjectsForSampleConfig() {
    return createExamples(new SplitByMetadata("comma-separated-list", "value"), new ZipAggregator(),
        new AddFormattedMetadataService().withFormatString("%1$s.xml").withMetadataKey("filename")
            .withArgumentMetadataKeys(Collections.singletonList("value")));
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

  // Filter out metadata filename values that start with 'x'
  private class MetadataFilenameCondition extends ConditionImpl {
    @Override
    public boolean evaluate(AdaptrisMessage message) throws CoreException {
      final String metadataValue = message.getMetadataValue(DEFAULT_FILENAME_METADATA);
      if (metadataValue != null && metadataValue.startsWith("x"))
        return false;
      return true;
    }

    @Override
    public void close() {
      throw new RuntimeException();
    }
  }
}