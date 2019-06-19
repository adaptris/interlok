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

package com.adaptris.core.stubs;

import java.util.EnumSet;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;

/**
 * <p>
 * The stub factory which returns implementations of
 * <code>AdaptrisMessage</code>.
 * </p>
 * <p>
 * Functionally the same as DefaultMessageFactory but we want to check that the
 * message implementations that are created can be different.
 * </p>
 */
public final class DefectiveMessageFactory extends DefaultMessageFactory {

  public static enum WhenToBreak {
    /** Break when getInputStream() is called */
    INPUT,
    /** Break when getOutputStream() is called */
    OUTPUT,
    /**
     * Break when either getInputStream() or getOutputStream() is called
     * 
     */
    BOTH,
    /**
     * Never break when getInputStream() or getOutputStream() is called
     * 
     */
    NEVER, 
    /**
     * Break when getting metadata
     * 
     */
    METADATA_GET, 
    /**
     * Break when setting metadata
     * 
     */
    METADATA_SET
  };

  private transient EnumSet<WhenToBreak> whenToBreak;

  public DefectiveMessageFactory() {
    this(EnumSet.of(WhenToBreak.INPUT, WhenToBreak.OUTPUT));
  }

  public DefectiveMessageFactory(EnumSet<WhenToBreak> set) {
    whenToBreak = set;
  }

  public DefectiveMessageFactory(WhenToBreak wtb) {
    this(asEnumSet(wtb));
  }

  @Override
  public AdaptrisMessage newMessage() {
    AdaptrisMessage result = new DefectiveAdaptrisMessage(uniqueIdGenerator(), this);
    return result;
  }

  boolean brokenInput() {
    return BooleanUtils.or(new boolean[] {whenToBreak.contains(WhenToBreak.INPUT)});
  }

  boolean brokenOutput() {
    return BooleanUtils.or(new boolean[] {whenToBreak.contains(WhenToBreak.OUTPUT)});
  }

  boolean brokenMetadataGet() {
    return BooleanUtils.or(new boolean[] {whenToBreak.contains(WhenToBreak.METADATA_GET)});
  }

  boolean brokenMetadataSet() {
    return BooleanUtils.or(new boolean[] {whenToBreak.contains(WhenToBreak.METADATA_SET)});
  }

  private static EnumSet<WhenToBreak> asEnumSet(WhenToBreak wtb) {
    if (wtb == WhenToBreak.NEVER) {
      return EnumSet.noneOf(WhenToBreak.class);
    }
    if (wtb == WhenToBreak.BOTH) {
      return EnumSet.of(WhenToBreak.INPUT, WhenToBreak.OUTPUT);
    }
    return EnumSet.of(wtb);
  }
}
