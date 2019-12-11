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

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.oro.io.Perl5FilenameFilter;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Channel;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.ConsumerCase;
import com.adaptris.core.CoreException;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.Poller;
import com.adaptris.core.QuartzCronPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.fs.enhanced.AlphabeticAscending;
import com.adaptris.core.fs.enhanced.AlphabeticDescending;
import com.adaptris.core.fs.enhanced.FileSorter;
import com.adaptris.core.fs.enhanced.LastModifiedAscending;
import com.adaptris.core.fs.enhanced.LastModifiedDescending;
import com.adaptris.core.fs.enhanced.NoSorting;
import com.adaptris.core.fs.enhanced.SizeAscending;
import com.adaptris.core.fs.enhanced.SizeDescending;
import com.adaptris.core.runtime.AdapterManager;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.TimeInterval;

public abstract class FsConsumerCase extends ConsumerCase {
  protected static final String COLON = ":";
  protected static final String HYPHEN = "-";
  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "FsConsumerCase.baseDir";

  public static final String BASE_KEY = "FsMessageConsumerTest.destinationName";

  private static final Poller[] POLLERS =
    {
        new FixedIntervalPoller(new TimeInterval(60L, TimeUnit.SECONDS)), new QuartzCronPoller("0 */5 * * * ?"),
        new FsImmediateEventPoller()
    };

  private static final List<Poller> POLLER_LIST = Arrays.asList(POLLERS);

  private enum FileSortImplementation {

    AlphabeticAscending() {

      @Override
      public FileSorter getImplementation() {
        return new AlphabeticAscending();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\nThe configured <file-sorter> allows you to sort filenames alphabetically ascending"
            + "\nbefore processing the files found in a directory. \n\n-->\n";
      }

      @Override
      public boolean matches(FileSorter impl) {
        return AlphabeticAscending.class.equals(impl.getClass());
      }

    },
    AlphabeticDescending() {
      @Override
      public FileSorter getImplementation() {
        return new AlphabeticDescending();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\nThe configured <file-sorter> allows you to sort filenames alphabetically descending"
            + "\nbefore processing the files found in a directory. \n\n-->\n";
      }

      @Override
      public boolean matches(FileSorter impl) {
        return AlphabeticDescending.class.equals(impl.getClass());
      }
    },
    NoSort() {
      @Override
      public FileSorter getImplementation() {
        return new NoSorting();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\nThe configured <file-sorter> does no sorting (and is the default) of the files"
            + "\nbefore processing the files found in a directory. \n\n-->\n";
      }

      @Override
      public boolean matches(FileSorter impl) {
        return NoSorting.class.equals(impl.getClass());
      }
    },
    LastModifiedAscending() {
      @Override
      public FileSorter getImplementation() {
        return new LastModifiedAscending();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\nThe configured <file-sorter> sorts the filenames, by lastmodified, ascending"
            + "\nbefore processing the files found in a directory. \n\n-->\n";
      }

      @Override
      public boolean matches(FileSorter impl) {
        return LastModifiedAscending.class.equals(impl.getClass());
      }
    },
    LastModifiedDescending() {
      @Override
      public FileSorter getImplementation() {
        return new LastModifiedDescending();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\nThe configured <file-sorter> sorts the filenames, by lastmodified, descending"
            + "\nbefore processing the files found in a directory. \n\n-->\n";
      }

      @Override
      public boolean matches(FileSorter impl) {
        return LastModifiedDescending.class.equals(impl.getClass());
      }
    },
    SizeAscending() {
      @Override
      public FileSorter getImplementation() {
        return new SizeAscending();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\nThe configured <file-sorter> sorts the filenames, by actual file size, ascending"
            + "\nbefore processing the files found in a directory. \n\n-->\n";
      }

      @Override
      public boolean matches(FileSorter impl) {
        return SizeAscending.class.equals(impl.getClass());
      }
    },
    SizeDescending() {
      @Override
      public FileSorter getImplementation() {
        return new SizeDescending();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\nThe configured <file-sorter> sorts the filenames, by actual file size, descending"
            + "\nbefore processing the files found in a directory. \n\n-->\n";
      }

      @Override
      public boolean matches(FileSorter impl) {
        return SizeDescending.class.equals(impl.getClass());
      }
    };

    public abstract FileSorter getImplementation();

    public abstract String getXmlHeader();

    public abstract boolean matches(FileSorter impl);
  }

  private enum FilterImplementation {
    Regex {
      @Override
      public String getExpression() {
        return ".*\\.xml";
      }

      @Override
      public String getImpl() {
        return RegexFileFilter.class.getCanonicalName();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\nThe configured <file-filter-imp> you to filter filenames based on an java.util.regex regular expression"
            + "\n<filter-expression> contains the regular expression"
            + "\nIt is the default if not explicitly configured."
            + "\n\n-->\n";
      }
    },
    LargerThan {
      @Override
      public String getExpression() {
        return "102480";
      }

      @Override
      public String getImpl() {
        return SizeGreaterThan.class.getCanonicalName();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\n  "
            + "The configured <file-filter-imp>  allows you to filter files based on the size of each file, it must be"
            + "\n greater than the size specified (in bytes) in <filter-expression> \n\n-->\n";
      }
    },
    LargerThanOrEqual {

      @Override
      public String getExpression() {
        return "102480";
      }

      @Override
      public String getImpl() {
        return SizeGreaterThanOrEqual.class.getCanonicalName();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\n  "
            + "The configured <file-filter-imp>  allows you to filter files based on the size of each file, it must be"
            + "\n greater than or equal to the size specified (in bytes) in <filter-expression> \n\n-->\n";
      }
    },
    LessThan {
      @Override
      public String getExpression() {
        return "4096";
      }

      @Override
      public String getImpl() {
        return SizeLessThan.class.getCanonicalName();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\n  "
            + "The configured <file-filter-imp>  allows you to filter files based on the size of each file, it must be"
            + "\n less than the size specified (in bytes) in <filter-expression> \n\n-->\n";
      }
    },
    LessThanOrEqual {
      @Override
      public String getExpression() {
        return "4096";
      }

      @Override
      public String getImpl() {
        return SizeLessThanOrEqual.class.getCanonicalName();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\n  "
            + "The configured <file-filter-imp>  allows you to filter files based on the size of each file, it must be"
            + "\n less than or equal to the size specified (in bytes) in <filter-expression> \n\n-->\n";
      }
    },
    OlderThan {
      @Override
      public String getExpression() {
        return "-P1D";
      }

      @Override
      public String getImpl() {
        return OlderThan.class.getCanonicalName();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\n  "
            + "The configured <file-filter-imp>  allows you to filter files based on the last modified of each file, it must be"
            + "\n in this instance older than 1 day based on the <filter-expression> \n\n-->\n";
      }
    },
    Composite {

      @Override
      public String getExpression() {
        return "SizeGT=4096__@@__Regex=.*\\.xml";
      }

      @Override
      public String getImpl() {
        return CompositeFileFilter.class.getCanonicalName();
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\n  "
            + "The configured <file-filter-imp>  allows you to filter files a based on a chain of filters, <filter-expression>"
            + "\ncontains directives about the type of filter and expression to be used for each filter.\n\n-->\n";
      }
    };

    public ConsumeDestination createDestination() {
      return new ConfiguredConsumeDestination("file:////path/to/consume-directory", getExpression());
    }

    public abstract String getExpression();

    public abstract String getImpl();

    public abstract String getXmlHeader();
  }

  public FsConsumerCase(java.lang.String testName) {
    super(testName);
    configureExampleConfigBaseDir();
  }

  /*
   * The reason for this is due to the possible number of combinations that are created as part of retrieveObjectsForSampleConfig...
   * At the current time it is 112 example-xml files for each consumer (a combination of poller-impl/file-filter/file-sort)... which
   * is just going to be insane for a normal example-xml directory.
   */
  protected abstract void configureExampleConfigBaseDir();

  protected abstract FsConsumerImpl createConsumer(String subDir);

  protected abstract FsConsumerImpl createConsumer();

  protected abstract void assertMessages(List<AdaptrisMessage> list, int count, File[] remaining);

  public void testBasicInit() throws Exception {
    String subDir = new GuidGenerator().safeUUID();
    FsConsumerImpl consumer = createConsumer(subDir);
    try {
      FsConsumerImpl consumer2 = createConsumer();
      try {
        LifecycleHelper.init(consumer2);
        fail("no destination  - should throw Exception");
      }
      catch (CoreException e) {
        // expected
      }
      // test creation of file filter imp...
      consumer.getDestination().setFilterExpression(".*\\.xml");
      consumer.setFileFilterImp(Perl5FilenameFilter.class.getName());
      try {
        LifecycleHelper.init(consumer);
      }
      catch (CoreException e) {
        fail("Exception calling init with valid FileFilter " + e);
      }
      LifecycleHelper.close(consumer);
      // test creation of invalid FF Imp
      consumer.setFileFilterImp("com.class.does.not.exist.FileFilter");
      try {
        LifecycleHelper.init(consumer);
        fail("Calling init with invalid FileFilter ");
      }
      catch (CoreException e) {
      }
    }
    finally {
      FileUtils.deleteQuietly(new File(PROPERTIES.getProperty(BASE_KEY), subDir));

    }
  }

  public void testFileSorter() throws Exception {
    String subdir = new GuidGenerator().safeUUID();
    FsConsumerImpl consumer = createConsumer(subdir);
    assertEquals(NoSorting.class, consumer.getFileSorter().getClass());
    consumer.setFileSorter(new LastModifiedAscending());
    assertEquals(LastModifiedAscending.class, consumer.getFileSorter().getClass());
    try {
      consumer.setFileSorter(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

  public void testSetFileFilterImp() throws Exception {

    String subdir = new GuidGenerator().safeUUID();

    FsConsumerImpl consumer = createConsumer(subdir);
    ((ConfiguredConsumeDestination) consumer.getDestination()).setFilterExpression(".*");
    assertNull(consumer.getFileFilterImp());
    assertEquals(org.apache.commons.io.filefilter.RegexFileFilter.class.getCanonicalName(), consumer.fileFilterImp());
    try {
      try {
        LifecycleHelper.init(consumer);
      }
      catch (CoreException e) {
        fail("Exception calling init with valid FileFilter " + e);
      }
      // test creation of file filter imp...
      consumer.setFileFilterImp(Perl5FilenameFilter.class.getName());
      assertEquals(Perl5FilenameFilter.class.getName(), consumer.getFileFilterImp());
      assertEquals(Perl5FilenameFilter.class.getName(), consumer.fileFilterImp());
      try {
        LifecycleHelper.init(consumer);
      }
      catch (CoreException e) {
        fail("Exception calling init with valid FileFilter " + e);
      }
      finally {
        LifecycleHelper.close(consumer);
      }
      // test creation of invalid FF Imp
      consumer.setFileFilterImp("com.class.does.not.exist.FileFilter");
      assertEquals("com.class.does.not.exist.FileFilter", consumer.getFileFilterImp());
      assertEquals("com.class.does.not.exist.FileFilter", consumer.fileFilterImp());
      try {
        LifecycleHelper.init(consumer);
        fail("Calling init with invalid FileFilter ");
      }
      catch (CoreException e) {

      }
      finally {
        LifecycleHelper.close(consumer);
      }
    }
    finally {
      FileUtils.deleteQuietly(new File(PROPERTIES.getProperty(BASE_KEY), subdir));
    }
  }

  public void testSetDestination() {
    FsConsumerImpl consumer = createConsumer();

    // 1 - valid ConfiguredConsumeDestination
    ConfiguredConsumeDestination dest = new ConfiguredConsumeDestination("dest");
    consumer.setDestination(dest);
    assertTrue(consumer.getDestination().equals(dest));
    try {
      consumer.setDestination(null);
      fail("no Exc. when dest is null");
    }
    catch (IllegalArgumentException e) {
      // ok
    }
  }

  public void testInitWithMkdirs() throws Exception {
    String subdir = new GuidGenerator().safeUUID();
    FsConsumerImpl fs = createConsumer(subdir);
    try {
      fs.setCreateDirs(true);
      LifecycleHelper.init(fs);
    }
    finally {
      LifecycleHelper.close(fs);
      FileUtils.deleteQuietly(new File(PROPERTIES.getProperty(BASE_KEY), subdir));
    }
  }

  public void testSetCreateDirs() throws Exception {
    String subdir = new GuidGenerator().safeUUID();
    try {
      FsConsumerImpl fs = createConsumer(subdir);
      fs.setCreateDirs(null);
      assertNull(fs.getCreateDirs());
      assertFalse(fs.shouldCreateDirs());
      fs.setCreateDirs(Boolean.TRUE);
      assertNotNull(fs.getCreateDirs());
      assertEquals(Boolean.TRUE, fs.getCreateDirs());
      assertTrue(fs.shouldCreateDirs());
    }
    finally {
      FileUtils.deleteQuietly(new File(PROPERTIES.getProperty(BASE_KEY), subdir));
    }
  }

  public void testSetLogAllExceptions() throws Exception {
    String subdir = new GuidGenerator().safeUUID();
    try {
      FsConsumerImpl fs = createConsumer(subdir);
      fs.setLogAllExceptions(null);
      assertNull(fs.getLogAllExceptions());
      assertTrue(fs.logAllExceptions());
      fs.setLogAllExceptions(Boolean.FALSE);
      assertNotNull(fs.getLogAllExceptions());
      assertEquals(Boolean.FALSE, fs.getLogAllExceptions());
      assertFalse(fs.logAllExceptions());
    }
    finally {
      FileUtils.deleteQuietly(new File(PROPERTIES.getProperty(BASE_KEY), subdir));
    }
  }

  public void testInitWithoutMkdirs() throws Exception {
    String subdir = new GuidGenerator().safeUUID();
    FsConsumerImpl fs = createConsumer(subdir);
    fs.setCreateDirs(false);
    try {
      LifecycleHelper.init(fs);
      fail("Should not have been able to init with [" + fs.getDestination().getDestination() + "] w/o creating the directory");
    }
    catch (CoreException e) {
      ;// Expected
    }
    finally {
      LifecycleHelper.close(fs);
      FileUtils.deleteQuietly(new File(PROPERTIES.getProperty(BASE_KEY), subdir));
    }
  }

  public void testSetQuietPeriod() throws Exception {
    FsConsumerImpl fs = createConsumer();
    TimeInterval defaultInterval = new TimeInterval(0L, TimeUnit.SECONDS);
    assertNull(fs.getQuietInterval());
    assertEquals(defaultInterval.toMilliseconds(), fs.olderThanMs());

    TimeInterval interval = new TimeInterval(20L, TimeUnit.SECONDS);

    fs.setQuietInterval(interval);
    assertEquals(interval, fs.getQuietInterval());
    assertEquals(interval.toMilliseconds(), fs.olderThanMs());

    fs.setQuietInterval(null);
    assertNull(fs.getQuietInterval());
    assertEquals(defaultInterval.toMilliseconds(), fs.olderThanMs());
  }

  public void testFsMonitor() throws Exception {
    String subdir = new GuidGenerator().safeUUID();

    FsConsumerImpl fs = createConsumer(subdir);
    fs.setUniqueId(getName());
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    Channel channel = new Channel();
    channel.setUniqueId(getName());
    StandardWorkflow wf = new StandardWorkflow();
    wf.setUniqueId(getName());
    wf.setConsumer(fs);
    channel.getWorkflowList().add(wf);
    adapter.getChannelList().add(channel);

    AdapterManager am = new AdapterManager(adapter);
    try {
      am.registerMBean();
      am.requestInit();
      String objectNameString = String.format("com.adaptris:type=ConsumerMonitor,adapter=%s,channel=%s,workflow=%s,id=%s", getName(),
          getName(), getName(), getName());
      MBeanServer mBeanServer = JmxHelper.findMBeanServer();
      FsConsumerMonitorMBean mbean = JMX.newMBeanProxy(mBeanServer, ObjectName.getInstance(objectNameString),
          FsConsumerMonitorMBean.class);
      assertEquals(0, mbean.messagesRemaining());
    }
    finally {
      am.requestClose();
      am.unregisterMBean();
    }
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    ArrayList result = new ArrayList();
    for (FilterImplementation filter : FilterImplementation.values()) {
      for (Poller poller : POLLER_LIST) {
        for (FileSortImplementation sort : FileSortImplementation.values()) {
          StandaloneConsumer sc = new StandaloneConsumer(createConsumer(null));
          ((FsConsumerImpl) sc.getConsumer()).setPoller(poller);
          ((FsConsumerImpl) sc.getConsumer()).setDestination(filter.createDestination());
          ((FsConsumerImpl) sc.getConsumer()).setFileFilterImp(filter.getImpl());
          ((FsConsumerImpl) sc.getConsumer()).setFileSorter(sort.getImplementation());

          result.add(sc);
        }
      }
    }
    return result;
  }

  /**
   * @see com.adaptris.core.ExampleConfigCase#retrieveObjectForSampleConfig()
   */
  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object)
        + getFilterImplementation((FsConsumerImpl) ((StandaloneConsumer) object).getConsumer()).getXmlHeader()
        + getFileSortImplementation((FsConsumerImpl) ((StandaloneConsumer) object).getConsumer()).getXmlHeader();
  }

  @Override
  protected String createBaseFileName(Object object) {
    FsConsumerImpl p = (FsConsumerImpl) ((StandaloneConsumer) object).getConsumer();
    String filterImpl;
    String fileSortImpl;
    try {
      filterImpl = Class.forName(p.getFileFilterImp()).getSimpleName();
      fileSortImpl = p.getFileSorter().getClass().getSimpleName();
    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return super.createBaseFileName(object) + HYPHEN + p.getPoller().getClass().getSimpleName() + HYPHEN + filterImpl + HYPHEN
        + fileSortImpl;
  }

  private FilterImplementation getFilterImplementation(FsConsumerImpl consumer) {
    if (isBlank(consumer.getFileFilterImp())) {
      throw new RuntimeException();
    }
    FilterImplementation result = null;
    for (FilterImplementation filter : FilterImplementation.values()) {
      if (filter.getImpl().equals(consumer.getFileFilterImp())) {
        result = filter;
        break;
      }
    }
    return result;
  }

  private FileSortImplementation getFileSortImplementation(FsConsumerImpl consumer) {
    FileSortImplementation result = null;
    for (FileSortImplementation sort : FileSortImplementation.values()) {
      if (sort.matches(consumer.getFileSorter())) {
        result = sort;
        break;
      }
    }
    return result;
  }

}
