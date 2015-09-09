package com.adaptris.core.lms;

import static com.adaptris.core.fs.FsMessageProducerTest.BASE_KEY;
import static com.adaptris.core.fs.FsMessageProducerTest.BASE_TEMP_DIR;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.fs.FsProducerExample;

public class LargeFsMessageProducerTest extends FsProducerExample {

  private static final String DEFAULT_DEST = "/tgt";

  public LargeFsMessageProducerTest(java.lang.String testName) {
    super(testName);
  }


  @Override
  protected void tearDown() throws Exception {
    // delete contents of destination...
    FileUtils.deleteQuietly(FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY)
        + DEFAULT_DEST, true)));
    FileUtils.deleteQuietly(FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_KEY), true)));
    FileUtils
        .deleteQuietly(FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_TEMP_DIR), true)));
  }

  public void testProduceWithDefaultMessageFactory() throws Exception {
    LargeFsProducer producer = create();
    start(producer);
    producer.produce(new DefaultMessageFactory().newMessage("dummy"));
    stop(producer);
  }

  public void testProduce() throws Exception {
    LargeFsProducer producer = create();
    start(producer);
    producer.produce(new FileBackedMessageFactory().newMessage("dummy"));
    stop(producer);
  }

  public void testProduceWithRenameTo() throws Exception {
    LargeFsProducer producer = create();
    producer.setUseRenameTo(true);
    start(producer);
    producer.produce(new FileBackedMessageFactory().newMessage("dummy"));
    stop(producer);
  }

  public void testProduceWithOverride() throws Exception {
    LargeFsProducer producer = create();
    start(producer);
    producer.produce(new FileBackedMessageFactory().newMessage("dummy"), new ConfiguredProduceDestination(PROPERTIES
        .getProperty(BASE_KEY)));
    stop(producer);
  }

  public void testProduceWithTempDir() throws Exception {
    String tempDir = PROPERTIES.getProperty(BASE_TEMP_DIR);
    LargeFsProducer producer = create();

    producer.setTempDirectory(tempDir);
    start(producer);
    producer.produce(new FileBackedMessageFactory().newMessage("dummy"));
    stop(producer);
    File f = FsHelper.createFileReference(FsHelper.createUrlFromString(PROPERTIES.getProperty(BASE_TEMP_DIR), true));
    assertTrue(tempDir + " exists", f.exists());
    assertTrue(tempDir + " is a directory", f.isDirectory());
  }

  public void testSetDestination() {
    LargeFsProducer producer = create();
    ProduceDestination dest = new ConfiguredProduceDestination("destination");
    producer.setDestination(dest);
    assertTrue(producer.getDestination().equals(dest));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    LargeFsProducer producer = create();
    StandaloneProducer result = new StandaloneProducer(producer);
    return result;
  }

  private LargeFsProducer create() {
    // create producer
    LargeFsProducer producer = new LargeFsProducer();
    producer.setDestination(new ConfiguredProduceDestination(PROPERTIES.getProperty(BASE_KEY) + DEFAULT_DEST));
    producer.setCreateDirs(true);
    return producer;
  }

}