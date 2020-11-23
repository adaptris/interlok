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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import com.adaptris.interlok.util.Args;
import com.adaptris.util.GuidGenerator;

public class TempFileUtils {

  private static FileCleaningTracker cleaner = new FileCleaningTracker();
  private static GuidGenerator guid = new GuidGenerator();

  public static File createTrackedFile(Object tracker) throws IOException {
    return createTrackedFile(guid.safeUUID(), null, null, tracker);
  }

  public static File createTrackedFile(Object tracker, Supplier<String> content)
      throws IOException {
    return createTrackedFile(guid.safeUUID(), null, null, tracker, content);
  }

  public static File createTrackedFile(String prefix, String suffix, File baseDir, Object tracker) throws IOException {
    return createTrackedFile(prefix, suffix, baseDir, tracker, null);
  }

  public static File trackFile(File f, Object tracker) {
    cleaner.track(f, tracker, FileDeleteStrategy.FORCE);
    return f;
  }

  public static File createTrackedFile(String prefix, String suffix, File baseDir, Object tracker,
      Supplier<String> content)
      throws IOException {
    Args.notNull(tracker, "tracker");
    File f = File.createTempFile(prefix, suffix, baseDir);
    f.delete();
    Optional.ofNullable(content).ifPresent((c) -> {
      try {
        FileUtils.write(f, c.get(), Charset.defaultCharset());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
    return trackFile(f, tracker);
  }


  public static File createTrackedFile(String prefix, String suffix, Object tracker) throws IOException {
    return createTrackedFile(prefix, suffix, null, tracker);
  }

  public static File createTrackedDir(Object tracker) throws IOException {
    return createTrackedDir(guid.safeUUID(), null, null, tracker);
  }

  public static File createTrackedDir(String prefix, String suffix, File baseDir, Object tracker) throws IOException {
    File f = File.createTempFile(prefix, suffix, baseDir);
    f.delete();
    f.mkdirs();
    cleaner.track(f, tracker, FileDeleteStrategy.FORCE);
    return trackFile(f, tracker);
  }
}
