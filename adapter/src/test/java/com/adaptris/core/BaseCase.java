/*
 * $RCSfile: BaseCase.java,v $ $Revision: 1.9 $ $Date: 2009/03/20 10:43:41 $
 * $Author: lchan $
 */
package com.adaptris.core;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.stubs.MessageCounter;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.util.LifecycleHelper;

/**
 * <p>
 * Base for unit tests which require access to properties in file
 * <code>unit-tests.properties</code>. Also provides utility methods for
 * checking files exist and deleting them.
 * </p>
 */
public abstract class BaseCase extends TestCase {
  protected static final long MAX_WAIT = 65000;
  protected static final int DEFAULT_WAIT_INTERVAL = 100;

  private static final Class[] PRIMITIVE_ARRAY =
  { // Oh this is all sorts of weakness, but it's allowed.
      int.class, boolean.class, String.class, float.class, double.class, long.class
  };

  private static final List PRIMITIVES = Arrays.asList(PRIMITIVE_ARRAY);

  public static final Properties PROPERTIES;
  private static final String PROPERTIES_RESOURCE = "unit-tests.properties";
  static {
    PROPERTIES = new Properties();

    InputStream in = BaseCase.class.getClassLoader().getResourceAsStream(PROPERTIES_RESOURCE);

    if (in == null) {
      throw new RuntimeException("cannot locate resource [" + PROPERTIES_RESOURCE + "] on classpath");
    }

    try {
      PROPERTIES.load(in);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected transient Log log = LogFactory.getLog(this.getClass().getName());

  /**
   * <p>
   * Creates a new instance.
   * </p>
   *
   * @param name the name of the test
   */
  public BaseCase(String name) {
    super(name);
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

  protected static void start(ComponentLifecycle c) throws CoreException {
    LifecycleHelper.init(c);
    LifecycleHelper.start(c);
  }

  protected static void stop(ComponentLifecycle c) {
    LifecycleHelper.stop(c);
    LifecycleHelper.close(c);
  }

  protected static void start(ComponentLifecycle... comps) throws CoreException {
    for (ComponentLifecycle c : comps) {
      start(c);
    }
  }

  protected static void stop(ComponentLifecycle... comps) {
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

  protected void assertRoundtripEquality(Object input, Object output, List<Class> classesToIgnore) throws Exception {
    if (input == null && output == null) {
      return;
    }
    log.trace("Input = " + input);
    log.trace("Output = " + output);
    assertEquals(input.getClass(), output.getClass());
    try {
      String[] toCall = filterGetterWithNoSetter(input.getClass(), getPrimitiveGetters(input.getClass()));
      for (int i = 0; i < toCall.length; i++) {
        log.trace("Verifying " + input.getClass().getName() + "." + toCall[i] + "()");
        Object a = invokeGetter(input, toCall[i]);
        Object b = invokeGetter(output, toCall[i]);

        assertEquals(input.getClass().getName() + "." + toCall[i] + "()", a, b);
      }
      // Right. This test will presumably depend on non-self-referential
      // getters and setters (back-refs should be called retrieve now).
      // If we get a OutOfMemoryError or StackOverflowError this is
      // probably the culprit
      toCall = filterGetterWithNoSetter(input.getClass(), getObjectGetters(input.getClass()));
      for (int i = 0; i < toCall.length; i++) {
        log.trace("Recursive Call after " + input.getClass() + "." + toCall[i] + "()");
        Object a = invokeGetter(input, toCall[i]);
        Object b = invokeGetter(output, toCall[i]);
        // If this class is in our ignore list, then just carry on.
        //
        if (a != null && classesToIgnore.contains(a.getClass())) {
          log.trace("Explicitly ignoring recursion on " + a.getClass());
          continue;
        }
        // This is a bit of a hack, but we don't always have object equality
        // in our config, simply checking the size is a test of the
        // castor marshall/unmarshall however.
        if (a instanceof List) {
          assertEquals(input.getClass() + "." + toCall[i] + "() sizes", ((List) a).size(), ((List) b).size());
        }
        else {
          assertRoundtripEquality(a, b);
        }
      }
    }
    catch (Exception e) {
      log.error(e.getMessage(), e);
      throw e;
    }
  }
  protected void assertRoundtripEquality(Object input, Object output) throws Exception {
    assertRoundtripEquality(input, output, new ArrayList<Class>());
  }

  private static List<Method> getObjectGetters(Class c) {
    List list = new ArrayList();
    List matches = getGetters(c);
    for (Iterator i = matches.iterator(); i.hasNext();) {
      Method m = (Method) i.next();
      if (PRIMITIVES.contains(m.getReturnType())) {
        continue;
      }
      list.add(m);
    }
    return list;
  }

  private static List<Method> getPrimitiveGetters(Class c) {
    List<Method> list = new ArrayList<Method>();
    List<Method> matches = getGetters(c);
    for (Iterator i = matches.iterator(); i.hasNext();) {
      Method m = (Method) i.next();
      if (!PRIMITIVES.contains(m.getReturnType())) {
        continue;
      }
      list.add(m);
    }
    return list;
  }

  private static List<Method> getGetters(Class c) {
    List<Method> list = new ArrayList<Method>();
    Method[] methods = c.getMethods();
    for (int i = 0; i < methods.length; i++) {
      String name = methods[i].getName();
      if (!name.startsWith("get") || name.equals("getClass") || name.equals("getInstance")
          || methods[i].getParameterTypes().length != 0) {
        continue;
      }
      list.add(methods[i]);
    }
    return list;
  }

  // Ensure that the list of getter has a corresponding setter.
  private static String[] filterGetterWithNoSetter(Class c, List<Method> getters) throws Exception {
    List result = new ArrayList();
    for (Method m : getters) {
      String methodName = m.getName();
      String fieldName = methodName.substring(3);
      String setter = "set" + fieldName;
      try {
        c.getMethod(setter, new Class[]
        {
          m.getReturnType()
        });
        result.add(m.getName());

      }
      catch (NoSuchMethodException e) {
        ;
      }
    }
    return (String[]) result.toArray(new String[0]);
  }

  private static Object invokeGetter(Object obj, String methodName) throws Exception {
    Method m = obj.getClass().getMethod(methodName, (Class[]) null);
    if (m != null) {
      if (m.getExceptionTypes().length != 0) {
        // It throws an exception. It's not a simple getter and setter
        return null;
      }
      else {
        return m.invoke(obj, (Object[]) null);
      }
    }
    return null;
  }

  protected static EventHandler createandStartDummyEventHandler() throws CoreException {
    DefaultEventHandler eh = new DefaultEventHandler();
    LifecycleHelper.init(eh);
    LifecycleHelper.start(eh);
    return eh;
  }

  protected static ProcessingExceptionHandler createandStartDummyMessageErrorHandler() throws CoreException {
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

}
