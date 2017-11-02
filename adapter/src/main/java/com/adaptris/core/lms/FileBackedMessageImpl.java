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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.channels.FileLock;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageImp;
import com.adaptris.util.IdGenerator;

class FileBackedMessageImpl extends AdaptrisMessageImp implements FileBackedMessage {
  protected File tempDir;
  protected File outputFile;
  protected File inputFile;
  private int bufferSize;
  private long maxSizeBeforeException;

  protected transient Logger log = LoggerFactory.getLogger(FileBackedMessage.class);

  private transient Set<Closeable> openStreams;
  
  private final int MEGABYTE = 1 << 10;

  FileBackedMessageImpl(IdGenerator guid, FileBackedMessageFactory fac) {
    super(guid, fac);
    tempDir = fac.tempDirectory();
    try {
      outputFile = null;
      inputFile = null;
      bufferSize = fac.defaultBufferSize();
      maxSizeBeforeException = fac.maxMemorySizeBytes();
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
    try (OutputStream out = getOutputStream()) {
      out.write(bytes != null ? bytes : new byte[0]);
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
    try (PrintStream out = (!isEmpty(charEncoding)) ? new PrintStream(getOutputStream(), true, charEncoding)
        : new PrintStream(getOutputStream(), true)) {
      out.print(content != null ? content : "");
      setContentEncoding(charEncoding);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
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
    return new FileFilterInputStream(inputFile);
  }

  /**
   * @see com.adaptris.core.lms.FileBackedMessage#getOutputStream()
   */
  @Override
  public OutputStream getOutputStream() throws IOException {
    if(outputFile == null) {
      outputFile = createTempFile();
    }
    // FileOutputStream fileOut = new FileOutputStream(outputFile);
    return new FileFilterOutputStream(outputFile);
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
        log.error("Unable to create temporary file!", e);
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

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    for (Closeable c : openStreams) {
      IOUtils.closeQuietly(c);
    }
  }

  // INTERLOK-1926 FilterOutputStream ultimately calls write(int) which is just crazy IO.
  // So we extend FileOutputStream directly
  private class FileFilterOutputStream extends FileOutputStream {
    private boolean alreadyClosed;

    FileFilterOutputStream(File out) throws IOException {
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

  private class FileFilterInputStream extends FileInputStream {
    FileFilterInputStream(File in) throws IOException {
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
