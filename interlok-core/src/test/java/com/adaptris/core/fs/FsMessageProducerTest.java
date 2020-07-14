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

package com.adaptris.core.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.EmptyFileNameCreator;
import com.adaptris.core.FileNameCreator;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.MetadataFileNameCreator;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.fs.AppendingFsWorker;
import com.adaptris.fs.FsWorker;
import com.adaptris.fs.NioWorker;
import com.adaptris.fs.OverwriteIfExistsWorker;
import com.adaptris.fs.StandardWorker;
import com.adaptris.util.GuidGenerator;

@SuppressWarnings("deprecation")
public class FsMessageProducerTest extends FsProducerExample {

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  public static final String BASE_KEY = "FsMessageProducerTest.baseUrl";
  public static final String BASE_TEMP_DIR = "FsMessageProducerTest.tempDirUrl";
  private static final String TEXT = "The quick brown fox";

  private enum FsFilenameConfig {
    Formatted() {

      @Override
      public FileNameCreator create() {
        return new FormattedFilenameCreator();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\nThe FormattedFilenameCreator implementation allows you to generate an arbitrary filename based"
            + "\non the message-id and current timestamp\n\n-->\n";
      }

      @Override
      public boolean matches(FileNameCreator impl) {
        return FormattedFilenameCreator.class.equals(impl.getClass());
      }

    },
    Metadata() {
      @Override
      public FileNameCreator create() {
        return new MetadataFileNameCreator("MetadataKey_Containing_The_Filename");
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\nThe MetadataFileNameCreator implementation allows you to generate a filename from metadata" + "\n\n-->\n";
      }

      @Override
      public boolean matches(FileNameCreator impl) {
        return MetadataFileNameCreator.class.equals(impl.getClass());
      }
    };

    public abstract FileNameCreator create();

    public abstract String getXmlHeader();

    public abstract boolean matches(FileNameCreator impl);
  }
  private enum FsWorkerConfig {

    Standard() {

      @Override
      public StandardWorker getImplementation() {
        return new StandardWorker();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\nThe StandardWorker implementation uses standard java.io methods \n\n-->\n";
      }

      @Override
      public boolean matches(FsWorker impl) {
        return StandardWorker.class.equals(impl.getClass());
      }

    },

    Overwrite() {

      @Override
      public OverwriteIfExistsWorker getImplementation() {
        return new OverwriteIfExistsWorker();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\nThe OverwriteIfExistsWorker overwrites the target file for any write operations,"
            + "\n other than that it is functionally equivalent to StandardWorker\n\n-->\n";
      }

      @Override
      public boolean matches(FsWorker impl) {
        return OverwriteIfExistsWorker.class.equals(impl.getClass());
      }

    },
    Appending() {

      @Override
      public AppendingFsWorker getImplementation() {
        return new AppendingFsWorker();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\nThe AppendingFsWorker appends to the target file for any write operations,"
            + "\n other than that it is functionally equivalent to StandardWorker\n\n-->\n";
      }

      @Override
      public boolean matches(FsWorker impl) {
        return AppendingFsWorker.class.equals(impl.getClass());
      }

    },
    NIO() {
      @Override
      public NioWorker getImplementation() {
        return new NioWorker();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\nThe NioWorker implementation is the default and uses java.nio methods for performing"
            + "\n read and write operations, other than that it is functionally equivalent to StandardWorker\n\n-->\n";
      }

      @Override
      public boolean matches(FsWorker impl) {
        return NioWorker.class.equals(impl.getClass());
      }
    };

    public abstract FsWorker getImplementation();

    public abstract String getXmlHeader();

    public abstract boolean matches(FsWorker impl);
  }

  @Test
  public void testCreateName() throws Exception {
    String subdir = new GuidGenerator().safeUUID();
    FsProducer producer = createProducer(subdir);
    assertEquals(producer.getClass().getCanonicalName(), producer.createName());
  }

  @Test
  public void testSetFilenameCreator() throws Exception {
    String subdir = new GuidGenerator().safeUUID();
    FsProducer producer = createProducer(subdir);
    try {
      producer.setFilenameCreator(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    producer.setFilenameCreator(new EmptyFileNameCreator());
    assertEquals(EmptyFileNameCreator.class, producer.getFilenameCreator().getClass());
  }

  @Test
  public void testSetFsWorker() throws Exception {
    String subdir = new GuidGenerator().safeUUID();
    FsProducer producer = createProducer(subdir);
    assertEquals(NioWorker.class, producer.getFsWorker().getClass());
    OverwriteIfExistsWorker worker = new OverwriteIfExistsWorker();
    producer.setFsWorker(worker);
    assertEquals(OverwriteIfExistsWorker.class, producer.getFsWorker().getClass());
    assertEquals(worker, producer.getFsWorker());
    try {
      producer.setFsWorker(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(OverwriteIfExistsWorker.class, producer.getFsWorker().getClass());
    assertEquals(worker, producer.getFsWorker());

  }

  @Test
  public void testSetCreateDirs() throws Exception {
    FsProducer producer = new FsProducer();
    assertNull(producer.getCreateDirs());
    assertFalse(producer.shouldCreateDirs());
    producer.setCreateDirs(Boolean.TRUE);
    assertNotNull(producer.getCreateDirs());
    assertEquals(Boolean.TRUE, producer.getCreateDirs());
    assertTrue(producer.shouldCreateDirs());
    producer.setCreateDirs(null);
    assertNull(producer.getCreateDirs());
    assertFalse(producer.shouldCreateDirs());
  }

  @Test
  public void testProduce() throws Exception {
    String subdir = new GuidGenerator().safeUUID();
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File dir = new File(parentDir, subdir);
      StandaloneProducer sp = new StandaloneProducer(createProducer(subdir));
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
      ServiceCase.execute(sp, msg);
      assertEquals(1, dir.listFiles().length);
      assertTrue(msg.containsKey(CoreConstants.FS_PRODUCE_DIRECTORY));
      assertTrue(msg.containsKey(CoreConstants.PRODUCED_NAME_KEY));
      assertEquals(dir.getCanonicalPath(), msg.getMetadataValue(CoreConstants.FS_PRODUCE_DIRECTORY));
      assertEquals(msg.getUniqueId(), msg.getMetadataValue(CoreConstants.PRODUCED_NAME_KEY));
    }
    finally {
      FileUtils.deleteQuietly(new File(parentDir, subdir));
    }
  }

  @Test
  public void testProduceWithNoCreateDir() throws Exception {
    String subdir = new GuidGenerator().safeUUID();
    FsProducer fs = createProducer(subdir);
    fs.setCreateDirs(false);
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      start(fs);
      fs.produce(new DefaultMessageFactory().newMessage(TEXT));
      fail();
    }
    catch (ProduceException expected) {
    }
    finally {
      FileUtils.deleteQuietly(new File(parentDir, subdir));
      stop(fs);
    }
  }

  @Test
  public void testProduceWithMetadataFilenameCreator() throws Exception {
    String subdir = new GuidGenerator().safeUUID();
    FsProducer fs = createProducer(subdir);
    fs.setCreateDirs(true);
    fs.setFilenameCreator(new MetadataFileNameCreator("targetFilename"));
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File dir = new File(parentDir, subdir);
      start(fs);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
      msg.addMetadata("targetFilename", new GuidGenerator().safeUUID());
      fs.produce(msg);
      assertEquals(1, dir.listFiles().length);
    }
    finally {
      FileUtils.deleteQuietly(new File(parentDir, subdir));
      stop(fs);
    }
  }

  @Test
  public void testProduceFileAlreadyExists_OverwriteIfExists() throws Exception {
    String subdir = new GuidGenerator().safeUUID();
    FsProducer fs = createProducer(subdir);
    fs.setCreateDirs(true);
    fs.setFsWorker(new OverwriteIfExistsWorker());
    fs.setFilenameCreator(new MetadataFileNameCreator("targetFilename"));
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File dir = new File(parentDir, subdir);
      start(fs);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
      msg.addMetadata("targetFilename", new GuidGenerator().safeUUID());
      dir.mkdirs();
      File targetFile = new File(dir, msg.getMetadataValue("targetFilename"));
      targetFile.createNewFile();
      fs.produce(msg);
      assertEquals(1, dir.listFiles().length);
      assertEquals(OverwriteIfExistsWorker.class, fs.getFsWorker().getClass());
    }
    finally {
      FileUtils.deleteQuietly(new File(parentDir, subdir));
      stop(fs);
    }
  }

  @Test
  public void testProduceFileAlreadyExists_StandardWorker() throws Exception {
    String subdir = new GuidGenerator().safeUUID();
    FsProducer fs = createProducer(subdir);
    fs.setCreateDirs(true);
    fs.setFsWorker(new StandardWorker());
    fs.setFilenameCreator(new MetadataFileNameCreator("targetFilename"));
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File dir = new File(parentDir, subdir);
      start(fs);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
      msg.addMetadata("targetFilename", new GuidGenerator().safeUUID());
      dir.mkdirs();
      File targetFile = new File(dir, msg.getMetadataValue("targetFilename"));
      targetFile.createNewFile();
      fs.produce(msg);
      fail();
    }
    catch (ProduceException expected) {

    }
    finally {
      FileUtils.deleteQuietly(new File(parentDir, subdir));
      stop(fs);
    }
  }

  @Test
  public void testProduceFileAlreadyExists_NioWorker() throws Exception {
    String subdir = new GuidGenerator().safeUUID();
    FsProducer fs = createProducer(subdir);
    fs.setCreateDirs(true);
    fs.setFsWorker(new NioWorker());
    fs.setFilenameCreator(new MetadataFileNameCreator("targetFilename"));
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File dir = new File(parentDir, subdir);
      start(fs);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
      msg.addMetadata("targetFilename", new GuidGenerator().safeUUID());
      dir.mkdirs();
      File targetFile = new File(dir, msg.getMetadataValue("targetFilename"));
      targetFile.createNewFile();
      fs.produce(msg);
      fail();
    }
    catch (ProduceException expected) {

    }
    finally {
      FileUtils.deleteQuietly(new File(parentDir, subdir));
      stop(fs);
    }
  }

  @Test
  public void testProduceFileAlreadyExists_AppendingFsWorker() throws Exception {
    String subdir = new GuidGenerator().safeUUID();
    FsProducer fs = createProducer(subdir);
    fs.setCreateDirs(true);
    fs.setFsWorker(new AppendingFsWorker());
    fs.setFilenameCreator(new MetadataFileNameCreator("targetFilename"));
    FsWorker fsWorker = new StandardWorker();
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File dir = new File(parentDir, subdir);
      start(fs);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
      msg.addMetadata("targetFilename", new GuidGenerator().safeUUID());
      dir.mkdirs();
      File targetFile = new File(dir, msg.getMetadataValue("targetFilename"));
      fsWorker.put(TEXT.getBytes(), targetFile);
      fs.produce(msg);
      assertEquals(1, dir.listFiles().length);
      assertEquals(AppendingFsWorker.class, fs.getFsWorker().getClass());
      assertEquals(TEXT + TEXT, new String(fsWorker.get(targetFile)));
    }
    finally {
      FileUtils.deleteQuietly(new File(parentDir, subdir));
      stop(fs);
    }
  }

  @Test
  public void testProduceWithOverride() throws Exception {

    String subdir = new GuidGenerator().safeUUID();
    String override = new GuidGenerator().safeUUID();
    FsProducer producer = createProducer(subdir);
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File dir = new File(parentDir, subdir);
      start(producer);
      producer.produce(new DefaultMessageFactory().newMessage(TEXT),
          new ConfiguredProduceDestination(PROPERTIES.getProperty(BASE_KEY) + "/" + override));
      assertNull(new File(parentDir, subdir).listFiles());
      assertEquals(1, new File(parentDir, override).listFiles().length);
    }
    finally {
      stop(producer);
      FileUtils.deleteQuietly(new File(parentDir, subdir));
      FileUtils.deleteQuietly(new File(parentDir, override));

    }
  }

  @Test
  public void testProduceWithNullOverride() throws Exception {
    String subdir = new GuidGenerator().safeUUID();
    FsProducer producer = createProducer(subdir);
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      File dir = new File(parentDir, subdir);
      start(producer);
      producer.produce(new DefaultMessageFactory().newMessage(TEXT), null);
      fail();
    }
    catch (ProduceException expected) {
      ;
    }
    finally {
      stop(producer);
      FileUtils.deleteQuietly(new File(parentDir, subdir));
    }
  }

  @Test
  public void testProduceWithTempDir() throws Exception {
    String subdir = new GuidGenerator().safeUUID();
    String tmpDir = new GuidGenerator().safeUUID();
    String tempDir = PROPERTIES.getProperty(BASE_TEMP_DIR) + "/" + tmpDir;
    FsProducer producer = createProducer(subdir);
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    File tmpParentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_TEMP_DIR), true));
    try {
      producer.setTempDirectory(tempDir);
      StandaloneProducer sp = new StandaloneProducer(producer);
      ServiceCase.execute(sp, new DefaultMessageFactory().newMessage(TEXT));
      File f = new File(tmpParentDir, tmpDir);
      assertTrue(f.exists());
      assertTrue(f.isDirectory());
      assertEquals(1, new File(parentDir, subdir).listFiles().length);
      assertEquals(0, new File(tmpParentDir, tmpDir).listFiles().length);
    }
    finally {
      FileUtils.deleteQuietly(new File(parentDir, subdir));
      FileUtils.deleteQuietly(new File(tmpParentDir, tmpDir));

    }
  }

  @Test
  public void testProduceWithNoCreateDirAndTempDir() throws Exception {
    String subdir = new GuidGenerator().safeUUID();
    String tmpDir = new GuidGenerator().safeUUID();
    String tempDir = PROPERTIES.getProperty(BASE_TEMP_DIR) + "/" + tmpDir;
    FsProducer fs = createProducer(subdir);
    fs.setCreateDirs(false);
    fs.setTempDirectory(tempDir);
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    File tmpParentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_TEMP_DIR), true));
    try {
      start(fs);
      fs.produce(new DefaultMessageFactory().newMessage(TEXT));
      fail();
    }
    catch (ProduceException expected) {
    }
    finally {
      stop(fs);
      FileUtils.deleteQuietly(new File(parentDir, subdir));
      FileUtils.deleteQuietly(new File(tmpParentDir, tmpDir));
    }
  }

  @Test
  public void testBug1675ProduceWithSpacesInDir() throws Exception {
    String subdir = "A%20Directory%20With%20Spaces";
    File parentDir = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true));
    try {
      FsProducer producer = createProducer(subdir);
      StandaloneProducer sp = new StandaloneProducer(producer);
      ServiceCase.execute(sp, new DefaultMessageFactory().newMessage(TEXT));
      assertEquals(1, new File(parentDir, "A Directory With Spaces").listFiles().length);
    }
    finally {
      FileUtils.deleteQuietly(new File(parentDir, "A Directory With Spaces"));

    }
  }

  @Test
  public void testSetDestination() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    FsProducer producer = createProducer(subDir);
    ProduceDestination dest = new ConfiguredProduceDestination("destination");
    producer.setDestination(dest);
    assertTrue(producer.getDestination().equals(dest));
  }

  private FsProducer createProducer(String subDir) {
    String baseString = PROPERTIES.getProperty(BASE_KEY);
    // create producer
    FsProducer producer = new FsProducer().withBaseDirectoryUrl(baseString + "/" + subDir);
    producer.setCreateDirs(true);
    return producer;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected String createBaseFileName(Object object) {
    FsProducer p = (FsProducer) ((StandaloneProducer) object).getProducer();
    return super.createBaseFileName(object) + "-" + p.getFsWorker().getClass().getSimpleName() + "-"
        + p.getFilenameCreator().getClass().getSimpleName();
  }

  @Override
  protected List<StandaloneProducer> retrieveObjectsForSampleConfig() {
    List<StandaloneProducer> result = new ArrayList<StandaloneProducer>();
    for (FsWorkerConfig config : FsWorkerConfig.values()) {
      for (FsFilenameConfig fname : FsFilenameConfig.values()) {
        FsProducer producer = createProducer("subdir");
        producer.setFsWorker(config.getImplementation());
        producer.setFilenameCreator(fname.create());
        result.add(new StandaloneProducer(producer));
      }
    }
    return result;
  }

  @Override
  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object)
        + getFsWorker((FsProducer) ((StandaloneProducer) object).getProducer()).getXmlHeader()
        + getFilenameConfig((FsProducer) ((StandaloneProducer) object).getProducer()).getXmlHeader();
  }

  private FsWorkerConfig getFsWorker(FsProducer p) {
    FsWorkerConfig result = null;
    for (FsWorkerConfig cfg : FsWorkerConfig.values()) {
      if (cfg.matches(p.getFsWorker())) {
        result = cfg;
        break;
      }
    }
    return result;
  }

  private FsFilenameConfig getFilenameConfig(FsProducer p) {
    FsFilenameConfig result = null;
    for (FsFilenameConfig cfg : FsFilenameConfig.values()) {
      if (cfg.matches(p.getFilenameCreator())) {
        result = cfg;
        break;
      }
    }
    return result;
  }

}
