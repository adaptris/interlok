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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.util.NumberUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Split an AdaptrisMessage object by line. Counts the number of lines, and creates a new message based on some configured number.
 * This Splitter can handle arbitrarily large (File Backed) messages.
 * </p>
 * 
 * @config line-count-splitter
 */
@XStreamAlias("line-count-splitter")
@DisplayOrder(order = {"splitOnLine",  "keepHeaderLines", "ignoreBlankLines", "copyMetadata", "copyObjectMetadata","bufferSize"})
public class LineCountSplitter extends MessageSplitterImp {

  private static final int DEFAULT_BUFFER_SIZE = 8192;
  private static final int DEFAULT_SPLIT = 10;
  private static final int DEFAULT_KEEPHEADER_LINES = 0;

  @InputFieldDefault(value = "0")
  private Integer keepHeaderLines;
  @InputFieldDefault(value = "10")
  private Integer splitOnLine;
  @InputFieldDefault(value = "false")
  private Boolean ignoreBlankLines;
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "8192")
  private Integer bufferSize;

  public LineCountSplitter() {

  }

  public LineCountSplitter(Integer splitOnLine) {
    this();
    setSplitOnLine(splitOnLine);
  }

  @Override
  public com.adaptris.core.util.CloseableIterable<AdaptrisMessage> splitMessage(final AdaptrisMessage msg) throws CoreException {
    logR.trace("LineCountSplitter splits every {} lines", splitOnLine());

    try {
      BufferedReader buf = new BufferedReader(msg.getReader(), bufferSize());
      return new LineCountSplitGenerator(buf, msg, selectFactory(msg), readHeader(buf));
    } catch (IOException e) {
      throw new CoreException(e);
    }
  }

  private String readHeader(BufferedReader buf) throws IOException {
    StringWriter sw = new StringWriter();
    PrintWriter writer = new PrintWriter(sw);
    for (int i = 0; i < keepHeaderLines(); i++) {
      writer.println(buf.readLine());
    }
    return sw.toString();
  }

  /**
   * Set the boundary marker so that the file is split every <code>i</code> lines.
   *
   * @param i the number of lines to split on
   */
  public void setSplitOnLine(Integer i) {
    splitOnLine = i;
  }

  /**
   * Get the number of lines that we are splitting on.
   *
   * @return the number of lines.
   */
  public Integer getSplitOnLine() {
    return splitOnLine;
  }

  int splitOnLine() {
    return NumberUtils.toIntDefaultIfNull(getSplitOnLine(), DEFAULT_SPLIT);
  }

  /**
   * Specify whether to ignore blank lines or not.
   * <p>
   * If blank lines are ignored, then they will not count towards the <code>splitOnLine</code> total, or be output into the
   * resulting split message.
   * </p>
   *
   * @param b true to ignore blank lines, default false
   */
  public void setIgnoreBlankLines(Boolean b) {
    ignoreBlankLines = b;
  }

  /**
   * Whether to ignore blank lines or not.
   * 
   * @see #setIgnoreBlankLines(Boolean)
   * @return true or false;
   */
  public Boolean getIgnoreBlankLines() {
    return ignoreBlankLines;
  }

  boolean ignoreBlankLines() {
    return BooleanUtils.toBooleanDefaultIfNull(getIgnoreBlankLines(), false);
  }

  public Integer getBufferSize() {
    return bufferSize;
  }

  /**
   * Set the internal buffer size.
   * <p>
   * The default buffer size matches the default buffer size in {@link BufferedReader} and {@link BufferedWriter}, changes to the
   * buffersize will impact performance and memory usage depending on the underlying operating system/disk.
   * </p>
   * 
   * @param b the buffer size (default is 8192).
   */
  public void setBufferSize(Integer b) {
    this.bufferSize = b;
  }

  int bufferSize() {
    return NumberUtils.toIntDefaultIfNull(getBufferSize(), DEFAULT_BUFFER_SIZE);
  }

  public Integer getKeepHeaderLines() {
    return keepHeaderLines;
  }

  public void setKeepHeaderLines(Integer keepHeaderLines) {
    this.keepHeaderLines = keepHeaderLines;
  }

  int keepHeaderLines() {
    return NumberUtils.toIntDefaultIfNull(getKeepHeaderLines(), DEFAULT_KEEPHEADER_LINES);
  }

  /**
   * Read the BufferedReader line by line and return each line as an
   * AdaptrisMessage. This implementation is NOT thread safe or reentrant!
   */
  private class LineCountSplitGenerator extends SplitMessageIterator {
    private final BufferedReader buf;
    private final String header;
    private int numberOfMessages;

    public LineCountSplitGenerator(BufferedReader buf, AdaptrisMessage msg, AdaptrisMessageFactory factory, String header) {
      super(msg, factory);
      this.buf = buf;
      this.header = header;
      logR.trace("Using message factory: {}", factory.getClass());
    }

    @Override
    protected AdaptrisMessage constructAdaptrisMessage() throws IOException {
      AdaptrisMessage tmpMessage = factory.newMessage();
      int i = 0;

      try (PrintWriter print = new PrintWriter(new BufferedWriter(tmpMessage.getWriter(), bufferSize()), false)) {
        logR.trace("Working on split {}", numberOfMessages);
        print.print(header);
        while (i < splitOnLine()) {
          String line = buf.readLine();
          if (line == null) {
            break;
          }
          if (!"".equals(line) || !ignoreBlankLines()) {
            print.println(line);
            i++;
          }
        }
      }

      if (i > 0) {
        numberOfMessages++;
        copyMetadata(msg, tmpMessage);
        return tmpMessage;
      }

      return null;
    }

    @Override
    public void close() throws IOException {
      logR.trace("Split gave {} messages", numberOfMessages);
      buf.close();
    }

  };

}
