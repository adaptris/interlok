package com.adaptris.interlok.cloud;

import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.junit.Test;

public class RemoteBlobFilterWrapperTest {

  @Test
  public void testAccept() throws Exception {
    RemoteBlobFilterWrapper wrapper = new RemoteBlobFilterWrapper()
        .withFilterImp(RegexFileFilter.class.getCanonicalName()).withFilterExpression(".*");
    RemoteBlob blob = new RemoteBlob.Builder().setBucket("bucket").setName("blob1")
        .setLastModified(new Date().getTime()).setSize(0).build();
    RemoteBlob blob2 = new RemoteBlob.Builder().setBucket("bucket").setName("blob2")
        .setLastModified(new Date().getTime()).setSize(0).build();
    assertTrue(wrapper.accept(blob));
    assertTrue(wrapper.accept(blob2));
  }

  @Test
  public void testAccept_NoFilterExpression() throws Exception {
    RemoteBlobFilterWrapper wrapper = new RemoteBlobFilterWrapper();
    RemoteBlob blob = new RemoteBlob.Builder().setBucket("bucket").setName("blob1")
        .setLastModified(new Date().getTime()).setSize(0).build();
    RemoteBlob blob2 = new RemoteBlob.Builder().setBucket("bucket").setName("blob2")
        .setLastModified(new Date().getTime()).setSize(0).build();
    assertTrue(wrapper.accept(blob));
    assertTrue(wrapper.accept(blob2));
  }

}
