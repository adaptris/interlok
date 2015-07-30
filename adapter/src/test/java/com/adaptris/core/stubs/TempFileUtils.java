package com.adaptris.core.stubs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileDeleteStrategy;

import com.adaptris.util.GuidGenerator;

public class TempFileUtils {

  private static FileCleaningTracker cleaner = new FileCleaningTracker();
  private static GuidGenerator guid = new GuidGenerator();

  public static File createTrackedFile(Object tracker) throws IOException {
    return createTrackedFile(guid.safeUUID(), null, null, tracker);
  }

  public static File createTrackedFile(String prefix, String suffix, File baseDir, Object tracker) throws IOException {
    File f = File.createTempFile(prefix, suffix, baseDir);
    f.delete();
    f.deleteOnExit();
    cleaner.track(f, tracker, FileDeleteStrategy.FORCE);
    return f;
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
    f.deleteOnExit();
    cleaner.track(f, tracker, FileDeleteStrategy.FORCE);
    return f;
  }

}
