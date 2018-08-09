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

package com.adaptris.core;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.NonBlockingQuartzThreadPool;
import com.adaptris.util.FifoMutexLock;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>Poller</code> which provides <i>cron</i> style scheduled polling based on the <a
 * href="http://www.quartz-scheduler.org/">Quartz project</a>.
 * </p>
 * <p>
 * A single Quartz Scheduler per Adapter (or Classloader) is used by all configured instances of QuartzCronPoller.
 * </p>
 * <p>
 * A file <code>quartz.properties</code> is used to configure the Scheduler. This file is included in quartz.jar with default
 * settings which should be suitable for most of our purposes. In an Adapter deployment with more than 10 consumers using
 * {@link QuartzCronPoller} you may wish to increase the size of the <code>Scheduler</code>'s thread pool. To do this place the
 * amended copy of the properties file in the Adapter Framework config directory.
 * </p>
 * <p>
 * For deployments with less 10 consumers using this {@link Poller}, there is little or no benefit in increasing the size of the
 * thread pool; each {@link AdaptrisPollingConsumer} instance is single threaded.
 * </p>
 * <p>
 * Although this class was created to allow 'every weekday at 10am' style scheduling, it can also be used to poll e.g. 'every 10
 * seconds'. Where a poll has not completed before the next scheduled poll is triggered, the subsequent poll will fail quietly,
 * because it cannot obtain the associated lock.
 * </p>
 * <p>
 * The following is copied directly from the Quartz CronExpression javadocs.
 * </p>
 * </p> Cron expressions are comprised of 6 required fields and one optional field separated by white space. The fields respectively
 * are described as follows:
 * 
 * <table cellspacing="8">
 * <tr>
 * <th align="left">Field Name</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Allowed Values</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Allowed Special Characters</th>
 * 
 * </tr>
 * <tr>
 * <td align="left"><code>Seconds</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>0-59</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * /</code></td>
 * 
 * </tr>
 * <tr>
 * <td align="left"><code>Minutes</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>0-59</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * /</code></td>
 * 
 * </tr>
 * <tr>
 * <td align="left"><code>Hours</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>0-23</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * /</code></td>
 * 
 * </tr>
 * <tr>
 * <td align="left"><code>Day-of-month</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>1-31</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * ? / L W C</code></td>
 * 
 * </tr>
 * <tr>
 * <td align="left"><code>Month</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>1-12 or JAN-DEC</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * /</code></td>
 * 
 * </tr>
 * <tr>
 * <td align="left"><code>Day-of-Week</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>1-7 or SUN-SAT</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * ? / L #</code></td>
 * 
 * </tr>
 * <tr>
 * <td align="left"><code>Year (Optional)</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>empty, 1970-2099</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * </table>
 * </p> The '*' character is used to specify all values. For example, "*" in the minute field means "every minute". </p>
 * <p>
 * The '?' character is allowed for the day-of-month and day-of-week fields. It is used to specify 'no specific value'. This is
 * useful when you need to specify something in one of the two fileds, but not the other.
 * </p>
 * <p>
 * The '-' character is used to specify ranges For example "10-12" in the hour field means "the hours 10, 11 and 12".
 * </p>
 * <p>
 * The ',' character is used to specify additional values. For example "MON,WED,FRI" in the day-of-week field means "the days
 * Monday, Wednesday, and Friday".
 * </p>
 * <p>
 * The '/' character is used to specify increments. For example "0/15" in the seconds field means "the seconds 0, 15, 30, and 45".
 * And "5/15" in the seconds field means "the seconds 5, 20, 35, and 50". Specifying '*' before the '/' is equivalent to specifying
 * 0 is the value to start with. Essentially, for each field in the expression, there is a set of numbers that can be turned on or
 * off. For seconds and minutes, the numbers range from 0 to 59. For hours 0 to 23, for days of the month 0 to 31, and for months 1
 * to 12. The "/" character simply helps you turn on every "nth" value in the given set. Thus "7/6" in the month field only turns on
 * month "7", it does NOT mean every 6th month, please note that subtlety.
 * </p>
 * <p>
 * The 'L' character is allowed for the day-of-month and day-of-week fields. This character is short-hand for "last", but it has
 * different meaning in each of the two fields. For example, the value "L" in the day-of-month field means
 * "the last day of the month" - day 31 for January, day 28 for February on non-leap years. If used in the day-of-week field by
 * itself, it simply means "7" or "SAT". But if used in the day-of-week field after another value, it means
 * "the last xxx day of the month" - for example "6L" means "the last friday of the month". When using the 'L' option, it is
 * important not to specify lists, or ranges of values, as you'll get confusing results.
 * </p>
 * <p>
 * The 'W' character is allowed for the day-of-month field. This character is used to specify the weekday (Monday-Friday) nearest
 * the given day. As an example, if you were to specify "15W" as the value for the day-of-month field, the meaning is:
 * "the nearest weekday to the 15th of the month". So if the 15th is a Saturday, the trigger will fire on Friday the 14th. If the
 * 15th is a Sunday, the trigger will fire on Monday the 16th. If the 15th is a Tuesday, then it will fire on Tuesday the 15th.
 * However if you specify "1W" as the value for day-of-month, and the 1st is a Saturday, the trigger will fire on Monday the 3rd, as
 * it will not 'jump' over the boundary of a month's days. The 'W' character can only be specified when the day-of-month is a single
 * day, not a range or list of days.
 * </p>
 * <p>
 * The 'L' and 'W' characters can also be combined for the day-of-month expression to yield 'LW', which translates to
 * "last weekday of the month".
 * </p>
 * <p>
 * The '#' character is allowed for the day-of-week field. This character is used to specify "the nth" day of the month. For
 * example, the value of "6#3" in the day-of-week field means the third Friday of the month (day 6 = Friday and "#3" = the 3rd one
 * in the month). Other examples: "2#1" = the first Monday of the month and "4#5" = the fifth Wednesday of the month. Note that if
 * you specify "#5" and there is not 5 of the given day-of-week in the month, then no firing will occur that month.
 * </p>
 * <p>
 * The legal characters and the names of months and days of the week are not case sensitive.
 * </p>
 * <p>
 * Support for specifying both a day-of-week and a day-of-month value is not complete (you'll need to use the '?' character in on of
 * these fields).
 * </p>
 * 
 * @config quartz-cron-poller
 * 
 * 
 */
@XStreamAlias("quartz-cron-poller")
@DisplayOrder(order = {"cronExpression", "useCustomThreadPool", "quartzId", "schedulerGroup"})
public class QuartzCronPoller extends PollerImp {

  private transient SchedulerFactory factory;
  private transient Scheduler scheduler;
  private transient JobDetail jobDetail;
  private transient String registeredQuartzId; // used for Job, Trigger and Listener
  private transient String registeredSchedulerGroup;

  // marshalled
  private String cronExpression;
  @AdvancedConfig
  private String quartzId;
  @AdvancedConfig
  private String schedulerGroup;
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean useCustomThreadPool;

  private static final transient FifoMutexLock lock = new FifoMutexLock();

  /**
   * <p>
   * Creates a new instance. The default cron expression is <code>0 0,10,20,30,40,50 * * * ?</code> (every ten minutes every day,
   * from on the hour).
   * </p>
   */
  public QuartzCronPoller() {
    // factory = new StdSchedulerFactory();
    setCronExpression("0 0,10,20,30,40,50 * * * ?");
  }

  public QuartzCronPoller(String exp) {
    this();
    setCronExpression(exp);
  }

  public QuartzCronPoller(String exp, String quartzId) {
    this();
    setCronExpression(exp);
    setQuartzId(quartzId);
  }

  public QuartzCronPoller(String exp, Boolean useCustomThreadPool) {
    this();
    setCronExpression(exp);
    setUseCustomThreadPool(useCustomThreadPool);
  }

  @Override
  public void prepare() throws CoreException {
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public void init() throws CoreException {
    try {
      factory = new StdSchedulerFactory(createQuartzProperties());
      scheduler = createAndStartScheduler(factory);
      registeredQuartzId = generateQuartzId();
      log.trace("Using QuartzId [" + registeredQuartzId + "]");
      registeredSchedulerGroup = generateSchedulerGroup();

      // Create the job
      jobDetail = newJob(MyProcessJob.class).withIdentity(getJobKey()).build();

      JobDataMap jobDataMap = jobDetail.getJobDataMap();
      jobDataMap.put("consumer", this);

      // Create the cron trigger
      Trigger cronTrigger = newTrigger().withIdentity(registeredQuartzId, registeredSchedulerGroup)
          .withSchedule(cronSchedule(getCronExpression()).withMisfireHandlingInstructionDoNothing()).build();

      // Update the scheduler
      scheduler.scheduleJob(jobDetail, cronTrigger);
      scheduler.pauseJob(getJobKey());

      log.trace("[" + registeredQuartzId + "] scheduled in group [" + registeredSchedulerGroup + "]");
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  private String generateQuartzId() {
    String result;

    if (!isEmpty(getQuartzId())) {
      result = getQuartzId();
    }
    else {
      if (retrieveConsumer().getDestination() != null) {
        result = retrieveConsumer().getDestination().getUniqueId() + "@" + Integer.toHexString(hashCode());
      }
      else {
        result = "NoDestination@" + Integer.toHexString(hashCode());
      }
    }
    return result;
  }

  String generateSchedulerGroup() {
    String result = Scheduler.DEFAULT_GROUP;
    if (!isEmpty(getSchedulerGroup())) {
      result = getSchedulerGroup();
    }
    return result;
  }

  private static Scheduler createAndStartScheduler(SchedulerFactory fac) throws SchedulerException,
      InterruptedException {
    Scheduler s = null;
    try {
      lock.acquire();
      s = fac.getScheduler();
      s.start();
    }
    finally {
      lock.release();
    }
    return s;
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  @Override
  public void start() throws CoreException {
    try {
      scheduler.resumeJob(getJobKey());
      log.trace("[" + registeredQuartzId + "] resumed in group [" + registeredSchedulerGroup + "]");
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  @Override
  public void stop() {
    pauseJob();
  }

  @Override
  public void close() {
    pauseJob();
    deleteJob();
  }

  void pauseJob() {
    try {
      if (scheduler != null) {
        scheduler.pauseJob(getJobKey());
        log.trace("[{}] paused in group [{}]", registeredQuartzId, registeredSchedulerGroup);
      }
    }
    catch (Exception e) {
      // log.trace("Failed to stop component cleanly, logging exception for informational purposes only", e);
    }
  }

  void deleteJob() {
    try {
      if (scheduler != null) {
        scheduler.deleteJob(getJobKey());
        log.trace("[{}] removed from group [{}]", registeredQuartzId, registeredSchedulerGroup);
      }
    }
    catch (SchedulerException e) {
      // log.trace("Failed to shutdown component cleanly, logging exception for informational purposes only", e);
    }
  }

  /**
   * <p>
   * Return the <i>cron</i> expression to use.
   * </p>
   * 
   * @return the <i>cron</i> expression to use
   */
  public String getCronExpression() {
    return cronExpression;
  }

  /**
   * <p>
   * Sets the <i>cron</i> expression to use.
   * </p>
   * 
   * @param s the <i>cron</i> expression to use
   */
  public void setCronExpression(String s) {
    cronExpression = s;
  }

  public String getName() {
    return registeredQuartzId;
  }

  public String getQuartzId() {
    return quartzId;
  }

  /**
   * Set the quartz id that will be registered.
   * <p>
   * The quartz id acts as the name of the job, the name of the trigger, and the name of the triggerlistener. You should not need to
   * configure this unless you are customising quartz.properties heavily; it is included for completeness.
   * </p>
   * 
   * @param s the quartz id; if null or empty, then one will be generated for you.
   */
  public void setQuartzId(String s) {
    quartzId = s;
  }

  public String getSchedulerGroup() {
    return schedulerGroup;
  }

  /**
   * Set the scheduler group.
   * <p>
   * The 'group' feature may be useful for creating logical groupings or categorizations of Jobs. You should not need to configure
   * this but is included for completeness.
   * </p>
   * 
   * @param s the scheduler group; if null or empty, then {@link Scheduler#DEFAULT_GROUP} is used ('DEFAULT')
   */
  public void setSchedulerGroup(String s) {
    schedulerGroup = s;
  }

  /**
   * Creates a job key that encapsulates the job name and group name. This is used for the various Scheduler methods
   * 
   * @return new JobKey Instance
   */
  protected JobKey getJobKey() {
    return jobKey(registeredQuartzId, registeredSchedulerGroup);
  }

  // Job that executes when cron is triggered
  public static class MyProcessJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
      JobDataMap map = context.getJobDetail().getJobDataMap();
      QuartzCronPoller poller = (QuartzCronPoller) map.get("consumer");
      poller.processMessages();
    }
  }

  private Properties createQuartzProperties() throws CoreException {
    String requestedFile = System.getProperty(StdSchedulerFactory.PROPERTIES_FILE);
    String propFileName = requestedFile != null ? requestedFile : "quartz.properties";
    File propFile = new File(propFileName);
    Properties result = new Properties();
    InputStream in = null;
    try {
      if (propFile.exists()) {
        in = new FileInputStream(propFile);
        result.load(in);
      }
      else if (requestedFile != null) {
        in = Thread.currentThread().getContextClassLoader().getResourceAsStream(requestedFile);
        if (in == null) {
          throw new CoreException("Couldn't find " + requestedFile);
        }
        result.load(in);
      }
      else {
        in = find("quartz.properties", "/quartz.properties", "org/quartz/quartz.properties");
        result.load(in);
      }
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
    result.put(StdSchedulerFactory.PROP_SCHED_MAKE_SCHEDULER_THREAD_DAEMON, Boolean.TRUE.toString());
    result.putAll(System.getProperties());    
    if (useCustomThreadPool()) {
      result.put(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, NonBlockingQuartzThreadPool.class.getCanonicalName());
    }
    // result.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, schedulerName);
    // result.put("org.quartz.plugin.shutdownhook.class",
    // org.quartz.plugins.management.ShutdownHookPlugin.class.getCanonicalName());
    // result.put("org.quartz.plugin.shutdownhook.cleanShutdown", Boolean.FALSE.toString());
    return result;
  }

  private InputStream find(String... resources) throws IOException, CoreException {
    ClassLoader cl = getClass().getClassLoader();
    InputStream in = null;
    for (String res : resources) {
      in = cl.getResourceAsStream(res);
      if (in != null) {
        break;
      }
    }
    if (in == null) {
      throw new CoreException("Default quartz.properties not found in class path");
    }
    return in;
  }

  public Boolean getUseCustomThreadPool() {
    return useCustomThreadPool;
  }

  /**
   * If set to true, then we use {@link NonBlockingQuartzThreadPool} as the threadpool implementation for quartz.
   * <p>
   * If set to false, then the default {@code quartz.properties} are not modified when initialising the {@link StdSchedulerFactory};
   * however, mixing and matching thread pools between different instances is discouraged and may lead to undefined behaviour.
   * </p>
   * 
   * @param b false to disable our own thread pool, default true.
   */
  public void setUseCustomThreadPool(Boolean b) {
    this.useCustomThreadPool = b;
  }

  protected boolean useCustomThreadPool() {
    return BooleanUtils.toBooleanDefaultIfNull(getUseCustomThreadPool(), true);
  }
}
