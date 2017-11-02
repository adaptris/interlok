/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.lms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

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
    orig.initialiseFrom(srcFile);
    try (InputStream in = orig.getInputStream();
        OutputStream out = orig.getOutputStream()) {
      IOUtils.copy(in,  out);
    }
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
  
  @Test
  public void testUncompressedFile_FailFast_False() throws Exception {
    ZipFileBackedMessageFactory factory = getMessageFactory();
    factory.setStrict(false);
    factory.setCompressionMode(CompressionMode.Uncompress);
    FileBackedMessage orig = (FileBackedMessage) factory.newMessage();
    File srcFile = new File(BaseCase.PROPERTIES.getProperty("msg.initFromFile"));
    orig.initialiseFrom(srcFile);
    assertEquals(srcFile.length(), orig.getSize());
    assertNotNull(orig.getInputStream());
  }

  /**
   * This tests inputs an uncompressed stream and expects an Exception because we tell the factory that we are going to send it
   * compressed data (compressionMode=Uncompress)
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

  @Test
  @Override
  public void testCurrentSource() throws Exception {
    ZipFileBackedMessageFactory factory = getMessageFactory();
    FileBackedMessage orig = (FileBackedMessage) getMessageFactory().newMessage();
    assertNotNull(orig.currentSource());
    File srcFile = new File(BaseCase.PROPERTIES.getProperty("msg.initFromZipFile"));
    orig.initialiseFrom(srcFile);
    assertNotSame(srcFile.length(), orig.getSize());
    assertNotSame(srcFile.length(), orig.getPayload().length);
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
