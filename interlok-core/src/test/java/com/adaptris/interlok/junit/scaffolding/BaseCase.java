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

package com.adaptris.interlok.junit.scaffolding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultEventHandler;
import com.adaptris.core.EventHandler;
import com.adaptris.core.ProcessingExceptionHandler;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandardProcessingExceptionHandler;
import com.adaptris.core.StateManagedComponent;
import com.adaptris.core.stubs.MessageCounter;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.stubs.ObjectUtils;
import com.adaptris.core.stubs.UpgradedToJunit4;
import com.adaptris.core.util.LifecycleHelper;

/**
 * <p>
 * Base for unit tests which require access to properties in file
 * <code>unit-tests.properties</code>. Also provides utility methods for
 * checking files exist and deleting them.
 * </p>
 */
@SuppressWarnings("deprecation")
public abstract class BaseCase {
  public static final long MAX_WAIT = 65000;
  public static final int DEFAULT_WAIT_INTERVAL = 100;

  public static final Properties PROPERTIES;
  public static final String PROPERTIES_RESOURCE = "unit-tests.properties";
  static {
    PROPERTIES = new Properties();
    try (InputStream in = BaseCase.class.getClassLoader().getResourceAsStream(PROPERTIES_RESOURCE)) {
      PROPERTIES.load(in);
    }
    catch (Exception e) {
      throw new RuntimeException("cannot locate resource [" + PROPERTIES_RESOURCE + "] on classpath", e);
    }
  }

  protected ValidatorFactory vFactory = Validation.buildDefaultValidatorFactory();

  protected transient Log log = LogFactory.getLog(this.getClass().getName());
  protected static transient Logger slf4jLogger = LoggerFactory.getLogger(BaseCase.class);
  @Rule
  public TestName testName = new TestName();

  @Before
  public void beforeTests() {
    if (this instanceof UpgradedToJunit4) {
      if (!((UpgradedToJunit4) this).isAnnotatedForJunit4()) {
        throw new RuntimeException(this.getClass().getCanonicalName()
          + ": isAnnotatedForJunit4() method returned false, please add annotations, and override the method.");
      }
    }
  }

  public String getName() {
    return testName.getMethodName();
  }

  public static void execute(StandaloneConsumer c, StandaloneProducer p, AdaptrisMessage m, MockMessageListener stub)
      throws Exception {
    execute(c, p, m, 1, MAX_WAIT, stub);
  }

  public static void execute(StandaloneConsumer c, StandaloneProducer p, AdaptrisMessage m, int count, long wait,
                             MockMessageListener stub) throws Exception {
    start(c);
    start(p);
    try {
      for (int i = 0; i < count; i++) {
        p.produce((AdaptrisMessage) m.clone());
      }
      if (stub != null) {
        waitForMessages(stub, count);
      }
      else {
        Thread.sleep(wait);
      }
    }
    finally {
      stop(p);
      stop(c);
    }
  }

  public static void start(ComponentLifecycle c) throws CoreException {
    LifecycleHelper.initAndStart(c, false);
  }

  public static void stop(ComponentLifecycle c) {
    LifecycleHelper.stopAndClose(c, false);
  }

  public static void start(ComponentLifecycle... comps) throws CoreException {
    for (ComponentLifecycle c : comps) {
      start(c);
    }
  }

  public static void stop(ComponentLifecycle... comps) {
    for (ComponentLifecycle c : comps) {
      stop(c);
    }
  }

  /**
   * <p>
   * Check if the file component of the passed file URL exists.
   * </p>
   */
  protected boolean checkFileExists(String fileUrl) {
    try {
      URL url = new URL(fileUrl);
      File file = new File(url.getFile());

      return file.exists();
    }
    catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * <p>
   * Delete the file component of the passed file URL.
   * </p>
   */
  protected void removeFile(String fileUrl) {
    try {
      URL url = new URL(fileUrl);
      File file = new File(url.getFile());

      file.delete();
    }
    catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public static void assertRoundtripEquality(Object input, Object output,
      List<Class> classesToIgnore) throws Exception {
    if (input == null && output == null) {
      return;
    }
    slf4jLogger.trace("Input = " + input);
    slf4jLogger.trace("Output = " + output);
    lenientClassAssertion(input, output);
    try {
      String[] toCall = ObjectUtils.filterGetterWithNoSetter(input.getClass(), ObjectUtils.getPrimitiveGetters(input.getClass()));
      for (int i = 0; i < toCall.length; i++) {
        slf4jLogger.trace("Verifying " + input.getClass().getName() + "." + toCall[i] + "()");
        Object a = ObjectUtils.invokeGetter(input, toCall[i]);
        Object b = ObjectUtils.invokeGetter(output, toCall[i]);

        assertEquals(input.getClass().getName() + "." + toCall[i] + "()", a, b);
      }
      // Right. This test will presumably depend on non-self-referential
      // getters and setters (back-refs should be called retrieve now).
      // If we get a OutOfMemoryError or StackOverflowError this is
      // probably the culprit
      toCall = ObjectUtils.filterGetterWithNoSetter(input.getClass(), ObjectUtils.getObjectGetters(input.getClass()));
      for (int i = 0; i < toCall.length; i++) {
        slf4jLogger.trace("Recursive Call after " + input.getClass() + "." + toCall[i] + "()");
        Object a = ObjectUtils.invokeGetter(input, toCall[i]);
        Object b = ObjectUtils.invokeGetter(output, toCall[i]);
        // If this class is in our ignore list, then just carry on.
        //
        if (a != null && classesToIgnore.contains(a.getClass())) {
          slf4jLogger.trace("Explicitly ignoring recursion on " + a.getClass());
          continue;
        }
        assertRoundtripEquality(a, b);
      }
    }
    catch (Exception e) {
      slf4jLogger.error(e.getMessage(), e);
      throw e;
    }
  }

  // Make sure that lists are in fact lists (but we might be doing ArrayList vs LinkedList, or
  // because of INTERLOK-3342 HashSet vs LinkedHashSet
  public static void lenientClassAssertion(Object input, Object output) {
    // This is a bit of a hack, but we don't always have object equality
    // in our config, simply checking the size is a test of the
    // castor marshall/unmarshall however.
    if (input instanceof Set) {
      assertTrue(output instanceof Set);
      assertEquals(((Collection) input).size(), ((Collection) output).size());
      return;
    }
    if (input instanceof List) {
      assertTrue(output instanceof List);
      assertEquals(((Collection) input).size(), ((Collection) output).size());
      return;
    }
    if (input instanceof Collection) {
      assertTrue(output instanceof Collection);
      assertEquals(((Collection) input).size(), ((Collection) output).size());
      return;
    }
    assertEquals(input.getClass(), output.getClass());
  }

  public static void assertRoundtripEquality(Object input, Object output) throws Exception {
    assertRoundtripEquality(input, output, new ArrayList<Class>());
  }


  protected void doJavaxValidation(Object... objs) {
    Validator validator = vFactory.getValidator();
    for (Object o : objs) {
      Set<ConstraintViolation<Object>> violations = validate(validator, o);
      logViolations(violations);
      assertEquals("Expected 0 Constraint Violations, got " + violations.size(), 0, violations.size());
    }
  }

  protected void logViolations(Collection<ConstraintViolation<Object>> violations) {
    for (ConstraintViolation<Object> v : violations) {
      String logString = String.format("Constraint Violation: [%1$s]=[%2$s]", v.getPropertyPath(), v.getMessage());
      log.warn(logString);
      System.err.println(logString);
    }
  }

  protected Set<ConstraintViolation<Object>> validate(Validator v, Object o) {
    if (o == null) {
      return Collections.EMPTY_SET;
    }
    Validator validator = ObjectUtils.defaultIfNull(v, vFactory.getValidator());
    return validator.validate(o);
  }

  @Deprecated
  protected static List<Method> getObjectGetters(Class c) {
    return ObjectUtils.getObjectGetters(c);
  }

  @Deprecated
  protected static List<Method> getPrimitiveGetters(Class c) {
    return ObjectUtils.getPrimitiveGetters(c);
  }

  @Deprecated
  protected static List<Method> getGetters(Class c) {
    return ObjectUtils.getGetters(c);
  }

  @Deprecated
  protected static String[] filterGetterWithNoSetter(Class c, List<Method> getters) throws Exception {
    return ObjectUtils.filterGetterWithNoSetter(c, getters);
  }

  @Deprecated
  protected static Object invokeGetter(Object obj, String methodName) throws Exception {
    return ObjectUtils.invokeGetter(obj, methodName);
  }

  public static EventHandler createandStartDummyEventHandler() throws CoreException {
    DefaultEventHandler eh = new DefaultEventHandler();
    LifecycleHelper.init(eh);
    LifecycleHelper.start(eh);
    return eh;
  }

  public static ProcessingExceptionHandler createandStartDummyMessageErrorHandler()
      throws CoreException {
    StandardProcessingExceptionHandler eh = new StandardProcessingExceptionHandler();
    LifecycleHelper.init(eh);
    LifecycleHelper.start(eh);
    return eh;
  }

  public static void waitForMessages(MessageCounter listener, int count) throws Exception {
    waitForMessages(listener, count, MAX_WAIT);
  }

  public static void waitForMessages(MessageCounter listener, int count, long maxWaitWs) throws Exception {
    long totalWaitTime = 0;
    while (listener.messageCount() < count && totalWaitTime < maxWaitWs) {
      Thread.sleep(DEFAULT_WAIT_INTERVAL);
      totalWaitTime += DEFAULT_WAIT_INTERVAL;
    }
  }

  public static void waitFor(StateManagedComponent component, ComponentState state) throws Exception {
    waitFor(component, state, MAX_WAIT);
  }

  public static void waitFor(StateManagedComponent component, ComponentState state, long maxWaitMs) throws Exception {
    long waitTime = 0;
    while (waitTime < maxWaitMs && !state.equals(component.retrieveComponentState())) {
      waitTime += DEFAULT_WAIT_INTERVAL;
      Thread.sleep(DEFAULT_WAIT_INTERVAL);
    }
  }

  protected String renameThread(String newName) {
    String name = Thread.currentThread().getName();
    Thread.currentThread().setName(newName);
    return name;
  }

  public static String getConfiguration(String key) {
    return PROPERTIES.getProperty(key);
  }
}
