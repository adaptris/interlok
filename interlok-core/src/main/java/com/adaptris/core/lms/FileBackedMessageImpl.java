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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageImp;
import com.adaptris.util.IdGenerator;

class FileBackedMessageImpl extends AdaptrisMessageImp implements FileBackedMessage {
  protected File outputFile;
  protected File inputFile;
  private int bufferSize;
  private long maxSizeBeforeException;
  private transient StreamWrapper streamWrapper;
  protected transient Logger log = LoggerFactory.getLogger(FileBackedMessage.class);
  
  private final int MEGABYTE = 1 << 10;

  FileBackedMessageImpl(IdGenerator guid, FileBackedMessageFactory fac) {
    super(guid, fac);
    wrappedTry(() -> {
      outputFile = null;
      inputFile = null;
      bufferSize = fac.defaultBufferSize();
      maxSizeBeforeException = fac.maxMemorySizeBytes();
      streamWrapper = fac.newStreamWrapper();
    });
  }

  @Override
  protected String getPayloadForLogging() {
    return "file-location:" + inputFile;
  }

  /** @see AdaptrisMessage#setPayload(byte[]) */
  @Override
  public void setPayload(final byte[] bytes) {
    wrappedTry(() -> {
      try (OutputStream out = getOutputStream()) {
        out.write(bytes != null ? bytes : new byte[0]);
      }
    });
  }

  /** @see AdaptrisMessage#getPayload() */
  @Override
  public byte[] getPayload() {
    long size = getSize(); // getSize() could be expensive in other implementations, so don't call it twice if we don't have to
    if (size >= maxSizeBeforeException) {
      throw new RuntimeException("Payload is > " + maxSizeBeforeException + " bytes, use getInputStream()");
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream((int) size);
    wrappedTry(() -> {
      try (OutputStream closeable = out; InputStream in = getInputStream()) {
        IOUtils.copyLarge(in, closeable, new byte[bufferSize]);
      }
    });
    return out.toByteArray();
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
    wrappedTry(() -> {
      try (PrintStream out = (!isEmpty(charEncoding)) ? new PrintStream(getOutputStream(), true, charEncoding)
          : new PrintStream(getOutputStream(), true)) {
        out.print(content != null ? content : "");
        setContentEncoding(charEncoding);
      }
    });
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
    StringBuilder result = new StringBuilder();
    wrappedTry(() -> {
      try(ByteArrayOutputStream closeable =  new ByteArrayOutputStream((int) size);InputStream in = getInputStream();) {
        IOUtils.copyLarge(in, closeable, new byte[bufferSize]);
        result.append(getContentEncoding() != null ? closeable.toString(getContentEncoding()) : closeable.toString());
      }      
    });
    return result.toString();
  }

  /**
   * @see com.adaptris.core.lms.FileBackedMessage#getInputStream()
   */
  @Override
  public InputStream getInputStream() throws IOException {
    // If we have an inputFile, return a stream for it. If we don't, return an empty stream
    if(inputFile == null) {
      return IOUtils.toInputStream("", Charset.defaultCharset());
    }
    return streamWrapper.openInputStream(inputFile, () -> {
    });

  }

  /**
   * @see com.adaptris.core.lms.FileBackedMessage#getOutputStream()
   */
  @Override
  public OutputStream getOutputStream() throws IOException {
    if(outputFile == null) {
      outputFile = createTempFile();
    }
    return streamWrapper.openOutputStream(outputFile, () -> {
      inputFile = outputFile;
      outputFile = null;
    });
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

    if (StringUtils.equals(getUniqueId(), other.getUniqueId())) {
      if (StringUtils.equals(getContentEncoding(), other.getContentEncoding())) {
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
    wrappedTry(() -> {
      // If we have an input file, copy our contents to the other message. If we don't,
      // the other message will create it's own file when written to and then closed.
      if (inputFile != null) {
        // Should we make this use StreamWrapper? -> but what about the FileLock?
        // probalby not a big deal because it only happens on CloneMessageServiceList...
        result.inputFile = createTempFile();
        try (InputStream in = new FileInputStream(inputFile);
            FileOutputStream out = new FileOutputStream(result.inputFile);
            FileLock lock = out.getChannel().lock()) {
          byte[] buffer = new byte[10 * MEGABYTE]; // 10MB buffer
          IOUtils.copyLarge(in, out, buffer);
        }
      }

    });
    return result;
  }

  /**
   *
   * @see com.adaptris.core.lms.FileBackedMessage#currentSource()
   */
  @Override
  public File currentSource() {
    if(inputFile == null) {
      wrappedTry(() -> {
        inputFile = createTempFile();
      });
    }
    return inputFile;
  }

//  protected File currentOutputfile() {
//    return outputFile;
//  }

  private File createTempFile() throws IOException {
    return ((FileBackedMessageFactory) getFactory()).createTempFile(this);
  }

  protected static void wrappedTry(Operation o) {
    try {
      o.apply();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected interface Operation {
    void apply() throws Exception;
  }

}
