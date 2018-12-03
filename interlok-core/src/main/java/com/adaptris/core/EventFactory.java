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
public abstract class EventFactory {

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

  public static Event createEvent(AdaptrisMessage msg, AdaptrisMarshaller marshaller) throws CoreException {
    return (Event) marshaller.unmarshal(msg.getContent());
  }

  public static Event createEvent(AdaptrisMessage msg) throws CoreException {
    return createEvent(msg, DefaultMarshaller.getDefaultMarshaller());
  }
}
