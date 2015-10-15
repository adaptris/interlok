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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.channels.FileLock;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageImp;
import com.adaptris.util.IdGenerator;

/**
 * @author lchan
 * @author $Author: lchan $
 */
class FileBackedMessageImpl extends AdaptrisMessageImp implements FileBackedMessage {
  protected File tempDir;
  protected File outputFile;
  protected File inputFile;
  private int bufferSize;
  private long maxSizeBeforeException;

  private transient Logger logR = LoggerFactory.getLogger(FileBackedMessage.class);

  private transient Set<Closeable> openStreams;
  
  private final int MEGABYTE = 1 << 10;

  FileBackedMessageImpl(IdGenerator guid, FileBackedMessageFactory fac, File tmpDir, int bufsiz, long maxSizeInline) {
    super(guid, fac);
    tempDir = tmpDir;
    try {
      outputFile = null;
      inputFile = null;
      bufferSize = bufsiz;
      maxSizeBeforeException = maxSizeInline;
      openStreams = new HashSet<Closeable>();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected String getPayloadForLogging() {
    return "file-location:" + inputFile;
  }

  /** @see AdaptrisMessage#setPayload(byte[]) */
  @Override
  public void setPayload(byte[] bytes) {
    // If we don't have a file and we're setting the payload to be empty, don't do anything
    if(inputFile == null && (bytes == null || bytes.length == 0)) {
      return;
    }
    
    try {
      OutputStream out = getOutputStream();
      out.write(bytes != null ? bytes : new byte[0]);
      out.flush();
      out.close();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** @see AdaptrisMessage#getPayload() */
  @Override
  public byte[] getPayload() {
    long size = getSize(); // getSize() could be expensive in other implementations, so don't call it twice if we don't have to
    if (size >= maxSizeBeforeException) {
      throw new RuntimeException("Payload is > " + maxSizeBeforeException + " bytes, use getInputStream()");
    }
    try (ByteArrayOutputStream out = new ByteArrayOutputStream((int) size);
        InputStream in = getInputStream()) {
      IOUtils.copyLarge(in, out, new byte[bufferSize]);
      return out.toByteArray();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisMessage#getSize()
   */
  @Override
  public long getSize() {
    return inputFile == null ? 0 : inputFile.length();
  }

  /** @see AdaptrisMessage#setStringPayload(String) */
  @Override
  public void setStringPayload(String s) {
    setStringPayload(s, null);
  }

  /** @see AdaptrisMessage#setStringPayload(String, String) 
   * @deprecated Since 3.0.6 use setContent(String, String). 
   **/
  @Deprecated
  public void setStringPayload(String payloadString, String charEnc) {
    this.setContent(payloadString, charEnc);
  }
  
  @Override
  public void setContent(String content, String charEncoding) {
    // If we don't have a file and we're setting the payload to be empty, don't do anything
    if(inputFile == null && StringUtils.isEmpty(content)) {
      return;
    }

    PrintStream p = null;
    try {
      if (!isEmpty(charEncoding)) {
        p = new PrintStream(getOutputStream(), true, charEncoding);
      }
      else {
        p = new PrintStream(getOutputStream(), true);
      }
      p.print(content != null ? content : "");
      setContentEncoding(charEncoding);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    finally {
      IOUtils.closeQuietly(p);
    }
  }

  /** @see AdaptrisMessage#getStringPayload() 
   * @deprecated Since 3.0.6 use getContent(). 
   **/
  @Deprecated
  @Override
  public String getStringPayload() {
    return this.getContent();
  }
  
  @Override
  public String getContent() {
    long size = getSize(); // getSize() could be expensive in subclasses, so don't call it twice if we don't have to
    if (size >= maxSizeBeforeException) {
      throw new RuntimeException("Payload is > " + maxSizeBeforeException + ", use getInputStream()");
    }
    String result = null;
    
    try(ByteArrayOutputStream out = new ByteArrayOutputStream((int) size);
        InputStream in = getInputStream();) {
      IOUtils.copyLarge(in, out, new byte[bufferSize]);
      result = getContentEncoding() != null ? out.toString(getContentEncoding()) : out.toString();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  /**
   * @see com.adaptris.core.lms.FileBackedMessage#getInputStream()
   */
  @Override
  public InputStream getInputStream() throws IOException {
    // If we have an inputFile, return a stream for it. If we don't, return an empty stream
    if(inputFile == null) {
      return IOUtils.toInputStream("");
    }
    
    FileInputStream in = new FileInputStream(inputFile);
    return new FileFilterInputStream(in);
  }

  /**
   * @see com.adaptris.core.lms.FileBackedMessage#getOutputStream()
   */
  @Override
  public OutputStream getOutputStream() throws IOException {
    if(outputFile == null) {
      outputFile = createTempFile();
    }
    FileOutputStream fileOut = new FileOutputStream(outputFile);
    return new FileFilteredOutputStream(fileOut);
  }

  @Override
  public void initialiseFrom(File sourceFile) throws IOException {
    if (sourceFile.exists() && sourceFile.isFile() && sourceFile.canRead()) {
      inputFile = sourceFile;
    }
    else {
      throw new IOException(sourceFile.getCanonicalPath() + " is not accessible");
    }
  }

  /**
   * @see com.adaptris.core.AdaptrisMessage#equivalentForTracking (com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public boolean equivalentForTracking(AdaptrisMessage other) {
    boolean result = false;

    if (areEqual(getUniqueId(), other.getUniqueId())) {
      if (areEqual(getContentEncoding(), other.getContentEncoding())) {
        if (this.getMetadata().equals(other.getMetadata())) {
          result = true;
        }
      }
    }
    return result;
  }

  /** @see Object#clone() */
  @Override
  public Object clone() throws CloneNotSupportedException {
    FileBackedMessageImpl result = (FileBackedMessageImpl) super.clone();
    try {
      // If we have an input file, copy our contents to the other message. If we don't,
      // the other message will create it's own file when written to and then closed.
      if(inputFile != null) {
        result.inputFile = createTempFile();
        try(InputStream in = new FileInputStream(inputFile); 
            FileOutputStream out = new FileOutputStream(result.inputFile);
            FileLock lock = out.getChannel().lock()) {
          byte[] buffer = new byte[10 * MEGABYTE]; // 10MB buffer
          IOUtils.copyLarge(in, out, buffer);
        }
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  /**
   *
   * @see com.adaptris.core.lms.FileBackedMessage#currentSource()
   */
  @Override
  public File currentSource() {
    if(inputFile == null) {
      try {
        inputFile = createTempFile();
      } catch (IOException e) {
        logR.error("Unable to create temporary file!", e);
      }
    }
    
    return inputFile;
  }

//  protected File currentOutputfile() {
//    return outputFile;
//  }

  private File createTempFile() throws IOException {
    return ((FileBackedMessageFactory) getFactory()).createTempFile(tempDir, this);
  }

  // Yeah I know it's bad form to have a finalizer, but really
  // WE HAVE FILE LOCKS!
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    for (Closeable c : openStreams) {
      try {
        c.close();
      }
      catch (IOException ignored) {
        ;
      }
    }
  }

  private class FileFilteredOutputStream extends FilterOutputStream {
    private boolean alreadyClosed;

    FileFilteredOutputStream(FileOutputStream out) throws IOException {
      super(out);
      openStreams.add(this);
      alreadyClosed = false;
    }

    @Override
    public void close() throws IOException {
      super.close();
      openStreams.remove(this);
      if (!alreadyClosed) {
        // Now that the file has been closed, we need to switch the reference.
        inputFile = outputFile;
        outputFile = null;
        alreadyClosed = true;
      }
    }
  }

  private class FileFilterInputStream extends FilterInputStream {
    FileFilterInputStream(FileInputStream in) throws IOException {
      super(in);
      openStreams.add(this);
    }

    @Override
    public void close() throws IOException {
      super.close();
      openStreams.remove(this);
    }
  }

}
