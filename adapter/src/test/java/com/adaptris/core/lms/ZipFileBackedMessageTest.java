/*
 * $RCSfile: FileBackedMessageTest.java,v $
 * $Revision: 1.6 $
 * $Date: 2009/07/01 13:06:03 $
 * $Author: lchan $
 */
package com.adaptris.core.lms;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.BaseCase;
import com.adaptris.core.lms.ZipFileBackedMessageFactory.CompressionMode;

public class ZipFileBackedMessageTest extends FileBackedMessageTest {

  private ZipFileBackedMessageFactory mf;
  
  @Before
  public void setup() {
    mf = new ZipFileBackedMessageFactory();
    mf.setCompressionMode(CompressionMode.Both);
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageCase#getMessageFactory()
   */
  @Override
  protected ZipFileBackedMessageFactory getMessageFactory() {
    return mf;
  }
  
  /**
   * This tests inputs a compressed file and expects uncompressed data to come out.
   */
  @Test
  public void testInitFromFile() throws Exception {
    ZipFileBackedMessageFactory factory = getMessageFactory();
    factory.setCompressionMode(CompressionMode.Uncompress); // We are feeding a zip file, so the mode must be Uncompress or Both
    FileBackedMessage orig = (FileBackedMessage) factory.newMessage();
    
    File srcFile = new File(BaseCase.PROPERTIES.getProperty("msg.initFromZipFile"));
    orig.initialiseFrom(srcFile);
    
    // Uncompressed size is 185 bytes
    assertEquals("file size ", 185, orig.getSize()); 
    assertEquals("payload size ", 185, orig.getPayload().length);
  }
  
  /**
   * This tests inputs a compressed stream and expects uncompressed data to come out.
   */
  @Test
  public void testUncompressStream() throws Exception {
    ZipFileBackedMessageFactory factory = getMessageFactory();
    factory.setCompressionMode(CompressionMode.Uncompress);
    FileBackedMessage orig = (FileBackedMessage) factory.newMessage();
    
    File srcFile = new File(BaseCase.PROPERTIES.getProperty("msg.initFromZipFile"));
    
    try(InputStream in = new FileInputStream(srcFile);
        OutputStream out = orig.getOutputStream()) {
      IOUtils.copy(in,  out);
    }
    
    // Uncompressed size is 185 bytes
    assertEquals("file size ", 185, orig.getSize()); 
    assertEquals("payload size ", 185, orig.getPayload().length);
  }
  
  /**
   * This tests inputs an uncompressed file and expects an Exception
   */
  @Test(expected=UnsupportedOperationException.class)
  public void testInitFromUncompressedFile() throws Exception {
    ZipFileBackedMessageFactory factory = getMessageFactory();
    factory.setCompressionMode(CompressionMode.Compress); 
    FileBackedMessage orig = (FileBackedMessage) factory.newMessage();
    
    File srcFile = new File(BaseCase.PROPERTIES.getProperty("msg.initFromFile"));
    orig.initialiseFrom(srcFile);

    // Exception expected
  }
  
  /**
   * This tests inputs an uncompressed stream and expects an Exception because we tell the factory that
   * we are going to send it compressed data (compressionMode=Uncompress)
   */
  @Test(expected=ZipException.class)
  public void testInitFromUncompressedStream() throws Exception {
    ZipFileBackedMessageFactory factory = getMessageFactory();
    factory.setCompressionMode(CompressionMode.Uncompress);
    FileBackedMessage orig = (FileBackedMessage) factory.newMessage();
    
    File srcFile = new File(BaseCase.PROPERTIES.getProperty("msg.initFromFile"));
    
    try(InputStream in = new FileInputStream(srcFile);
        OutputStream out = orig.getOutputStream()) {
      IOUtils.copy(in,  out);
    }

    // Now try to get the input stream. This has to throw a ZipException because the stream is not a zip file
    orig.getInputStream();
  }

  /**
   * This tests creates a compressed file
   */
  @Test
  public void testCreateCompressedFile() throws Exception {
    ZipFileBackedMessageFactory factory = getMessageFactory();
    factory.setCompressionMode(CompressionMode.Compress); 
    FileBackedMessage orig = (FileBackedMessage) factory.newMessage();
    
    File srcFile = new File(BaseCase.PROPERTIES.getProperty("msg.initFromFile"));
    try(FileInputStream in = new FileInputStream(srcFile);
        OutputStream out = orig.getOutputStream()) {
      IOUtils.copy(in, out);
    }

    // File should now be compressed. Check if it's a zip file and if the compressed size equals the input file size
    try(ZipFile zipFile = new ZipFile(orig.currentSource())) {
      long size = 0;
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while(entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        size += entry.getSize();
      }
      
      assertEquals("message size", orig.getSize(), size);
      assertEquals("payload size", srcFile.length(), size);
    }
    
    // Check if the InputStream from the message also yields compressed data
    try(ZipInputStream zin = new ZipInputStream(orig.getInputStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      zin.getNextEntry();
      IOUtils.copy(zin,  out);
      assertEquals("payload size", srcFile.length(), out.size());
    }
  }
  
}