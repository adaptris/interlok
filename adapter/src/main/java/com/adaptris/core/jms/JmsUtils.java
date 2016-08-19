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

package com.adaptris.core.jms;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;

public abstract class JmsUtils {

  public static JMSException wrapJMSException(Throwable e) {
    return wrapJMSException(e.getMessage(), e);
  }

  public static JMSException wrapJMSException(String msg, Throwable e) {
    if (e instanceof JMSException) {
      return (JMSException) e;
    }
    JMSException exc = new JMSException(msg);
    exc.initCause(e);
    return exc;
  }

  public static void rethrowJMSException(Throwable e) throws JMSException {
    rethrowJMSException(e.getMessage(), e);
  }

  public static void rethrowJMSException(String msg, Throwable e) throws JMSException {
    if (e instanceof JMSException) {
      throw (JMSException) e;
    }
    JMSException exc = new JMSException(msg);
    exc.initCause(e);
    throw exc;
  }

  /**
   * Delete a {@link TemporaryQueue} without logging any errors.
   * 
   * @param q the queue.
   */
  public static void deleteQuietly(TemporaryQueue q) {
    if (q == null) return;
    try {
      q.delete();
    }
    catch (Exception e) {

    }
  }

  /**
   * Delete a {@link TemporaryTopic} without logging any errors.
   * 
   * @param t the topic
   */
  public static void deleteQuietly(TemporaryTopic t) {
    if (t == null) return;
    try {
      t.delete();
    }
    catch (Exception e) {

    }
  }

  /**
   * Delete a temporary destnation without logging any errors.
   * 
   * @param d the topic
   */
  public static void deleteTemporaryDestination(Destination d) {
    if (d == null) return;
    if (d instanceof TemporaryQueue) {
      deleteQuietly((TemporaryQueue) d);
    }
    if (d instanceof TemporaryTopic) {
      deleteQuietly((TemporaryTopic) d);
    }
  }

  /**
   * Close a {@link Connection} without logging any errors or stopping the connection first.
   * 
   * @param con the queue.
   * @see #closeQuietly(Connection, boolean)
   */
  public static void closeQuietly(Connection con) {
    closeQuietly(con, false);
  }

  /**
   * Close a {@link Connection} without logging any errors.
   * 
   * @param con the queue.
   * @param stopFirst whether or not to stop the connection first.
   */
  public static void closeQuietly(Connection con, boolean stopFirst) {
    if (con == null) return;
    try {
      if (stopFirst) {
        stopQuietly(con);
      }
      con.close();
    }
    catch (Exception ex) {
    }
  }

  public static void stopQuietly(Connection con) {
    if (con == null) return;
    try {
      con.stop();
    }
    catch (Exception e) {
    }
  }

  /**
   * Close a {@link Session} without logging any errors.
   * 
   * @param session the session.
   */
  public static void closeQuietly(Session session) {
    if (session == null) return;
    try {
      session.close();
    }
    catch (Exception ex) {
    }
  }

  /**
   * Close a {@link MessageProducer} without logging any errors.
   * 
   * @param producer the producer.
   */
  public static void closeQuietly(MessageProducer producer) {
    if (producer == null) return;
    try {
      producer.close();
    }
    catch (Exception ex) {
    }
  }

  /**
   * Close a {@link MessageConsumer} without logging any errors.
   * 
   * @param consumer the consumer.
   */
  public static void closeQuietly(MessageConsumer consumer) {
    if (consumer == null) return;
    boolean wasInterrupted = Thread.interrupted();
    try {
      consumer.close();
    }
    catch (Exception ex) {
    }
    finally {
      if (wasInterrupted) {
        // Reset the interrupted flag as it was before.
        Thread.currentThread().interrupt();
      }
    }
  }
}
