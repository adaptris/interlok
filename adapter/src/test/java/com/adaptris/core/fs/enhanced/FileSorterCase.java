package com.adaptris.core.fs.enhanced;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.adaptris.core.BaseCase;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.util.SafeGuidGenerator;

public abstract class FileSorterCase extends BaseCase {
  private static final String COLON = ":";
  private static final String HYPHEN = "-";

  private File baseDir;

  public FileSorterCase(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
    File parentDir =
        FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty("FileSorterTest.tempDirUrl"), true));
    baseDir = new File(parentDir, safeName());
    baseDir.mkdirs();
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    FileUtils.deleteQuietly(baseDir);
  }

  protected List<File> createFiles(int count, long pause) throws Exception {
    return createFiles(count, this.getClass().getSimpleName(), ".xml", pause);
  }

  protected List<File> createFiles(int count) throws Exception {
    return createFiles(count, 0);
  }

  protected List<File> createFiles(int count, String prefix, String suffix, long pause) throws Exception {
    List<Integer> nums = new ArrayList<Integer>();
    List<File> result = new ArrayList<File>();
    for (int i = 1; i <= count; i++) {
      nums.add(Integer.valueOf(i));
    }
    Collections.shuffle(nums);
    for (Integer i : nums) {
      File f = new File(baseDir, String.format("%1$s-%2$03d%3$s", prefix, i, suffix));
      // Write it in gradients of 512bytes
      ensureSize(512 * i, f);
      result.add(f);
      if (pause > 0) {
        Thread.sleep(pause);
      }
    }
    log("createFiles", result);
    return result;
  }

  protected void log(String txt, List<File> list) {
    System.out.println(this.getClass().getSimpleName() + "--- Start file List (" + txt + ")");
    for (File f : list) {
      System.out.println(f.getName() + ", lastModified=" + f.lastModified() + ", size="
          + f.length());
    }
    System.out.println("--- End file List");
  }

  protected static String safeName() {
    return new SafeGuidGenerator().create(new Object());
  }

  private void ensureSize(long size, File f) throws IOException {
    RandomAccessFile rf = new RandomAccessFile(f, "rw");
    rf.setLength(size);
    rf.close();
  }

}
