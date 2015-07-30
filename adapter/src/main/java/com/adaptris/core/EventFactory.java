package com.adaptris.core;

import com.adaptris.util.GuidGenerator;
import com.adaptris.util.IdGenerator;

/**
 * <p>
 * Static factory which creates <code>Event</code>s.
 * </p><p>
 * The main purpose of this class is to allocate unique ids to new
 * <code>Events</code>, rather to control all construction of new instances.
 * All implementations of <code>Event</code> have public constructors for
 * unmarshalling. A higher-level class controlling unique id allocation is
 * necessary in order that unmarshalled events retain their original unique id.
 * </p>
 */
public final class EventFactory {

  private static IdGenerator uidGenerator;

  static {
    uidGenerator = new GuidGenerator();
  }

  /**
   * <p>
   * Create an <code>Event</code>.
   * </p>
   * @param name the fully qualified class name of the required
   * <code>Event</code>.
   * @return an instance of this <code>Event</code>
   * @throws CoreException wrapping any underlying Exceptions
   */
  public static Event create(String name) throws CoreException {
    try {
      Event result = (Event) Class.forName(name).newInstance();
      result.setUniqueId(uidGenerator.create(result));
      result.setCreationTime(System.currentTimeMillis());
      return result;
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  /**
   * @see #create(String)
   */
  public static <T> T create(Class<T> name) throws CoreException {
    T result;
    try {
      result = (T) Class.forName(name.getName()).newInstance();
      ((Event)result).setUniqueId(uidGenerator.create(result));
      ((Event)result).setCreationTime(System.currentTimeMillis());
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
    return result;
  }

  private EventFactory() {
    // no instances
  }
}
