/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.core.services.splitter;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.interlok.util.CloseableIterable;
import com.adaptris.util.NumberUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <p>
 * Split an AdaptrisMessage object based on bytes. Specify the <code>splitUnit</code> to be "BYTE" if splitting by number of bytes.
 * Specify the <code>splitUnit</code> to be "CHUNK" if splitting into chunks of equal bytes (subject to rounding).
 * <code>splitSize</code> controls the number of bytes/chunks to split by. <code>retainedSplits</code> can be specified to
 * control the maximum number of splits to retain (defaults to 1). Specify -1 to retain all splits.
 * </p>
 * 
 * @config bytes-splitter
 */
@XStreamAlias("bytes-splitter")
@DisplayOrder(order = {"splitSize",  "splitUnit", "retainedSplits", "copyMetadata", "copyObjectMetadata"})
public class BytesSplitter extends MessageSplitterImp {

  private static final int DEFAULT_SPLIT = 10*1024*1024;
  private static final int DEFAULT_RETAINED_SPLITS = 1;
  private static final SplitUnit DEFAULT_SPLIT_UNIT = SplitUnit.BYTE;
  public static final int RETAIN_ALL_SPLITS = -1;

  @InputFieldDefault(value = "1")
  private Integer retainedSplits = DEFAULT_RETAINED_SPLITS;
  @InputFieldDefault(value = "10")
  private Integer splitSize = DEFAULT_SPLIT;
  @InputFieldDefault(value = "BYTE")
  private String splitUnit;

  public enum SplitUnit {
    BYTE,
    CHUNK
  }

  public BytesSplitter() {

  }

  public BytesSplitter(Integer splitSize) {
    this();
    setSplitSize(splitSize);
  }

  @Override
  public CloseableIterable<AdaptrisMessage> splitMessage(final AdaptrisMessage msg) throws CoreException {
    SplitUnit _splitUnit = splitUnit();
    logR.trace("BytesSplitter splits into {} {}(s)", splitSize(), _splitUnit);
    int chunkSize = _splitUnit == SplitUnit.CHUNK ? new BigDecimal(msg.getSize()).divide(new BigDecimal(splitSize), RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP).intValue() : splitSize;
    try (InputStream is = msg.getInputStream()) {
      return new BytesSplitGenerator(is, chunkSize, msg, selectFactory(msg));
    } catch (IOException e) {
      throw new CoreException(e);
    }
  }

  public Integer getSplitSize() {
    return splitSize;
  }

  public void setSplitSize(Integer splitSize) {
    this.splitSize = splitSize;
  }

  public int splitSize() {
    return splitSize != null ? splitSize : DEFAULT_SPLIT;
  }

  public String getSplitUnit() {
    return splitUnit;
  }

  public void setSplitUnit(String splitUnit) {
    this.splitUnit = splitUnit;
  }

  public SplitUnit splitUnit() {
    return !StringUtils.isEmpty(splitUnit) ? SplitUnit.valueOf(splitUnit) : DEFAULT_SPLIT_UNIT;
  }

  public Integer getRetainedSplits() {
    return retainedSplits;
  }

  public void setRetainedSplits(Integer retainedSplits) {
    if (retainedSplits != null && retainedSplits >= RETAIN_ALL_SPLITS) this.retainedSplits = retainedSplits;
    else this.retainedSplits = DEFAULT_RETAINED_SPLITS;
  }

  /**
   * Read the BufferedReader line by line and return each line as an
   * AdaptrisMessage. This implementation is NOT thread safe or reentrant!
   */
  private class BytesSplitGenerator extends SplitMessageIterator {
    private int numberOfMessages;
    private InputStream is;
    private int chunkSize;

    public BytesSplitGenerator(InputStream is, int chunkSize, AdaptrisMessage msg, AdaptrisMessageFactory factory) {
      super(msg, factory);
      this.is = is;
      this.chunkSize = chunkSize;
      logR.trace("Using message factory: {}", factory.getClass());
    }

    @Override
    protected AdaptrisMessage constructAdaptrisMessage() throws IOException {
      if (retainedSplits != RETAIN_ALL_SPLITS && numberOfMessages >= retainedSplits) return null;

      AdaptrisMessage tmpMessage = factory.newMessage();
      try (OutputStream os = tmpMessage.getOutputStream()) {
        logR.trace("Working on split {}", numberOfMessages);
        byte[] chunk = new byte[chunkSize];
        int read = IOUtils.read(is, chunk);
        if (read == 0) return null;
        os.write(chunk, 0, read);
      }

      numberOfMessages++;
      copyMetadata(msg, tmpMessage);
      return tmpMessage;
    }

    @Override
    public void close() throws IOException {
      logR.trace("Split gave {} messages", numberOfMessages);
    }

  };

}
