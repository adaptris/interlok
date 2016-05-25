/*
 * Copyright 2016 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.core.jmx;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.management.Notification;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Serializes a {@link Notification} into an simple text message.
 * 
 * <p>
 * {@link Notification#getUserData()} is always added as object metadata against the key
 * {@value NotificationSerializer#OBJ_METADATA_USERDATA}.
 * </p>
 * 
 * @config simple-jmx-notification-serializer
 */
@XStreamAlias("simple-jmx-notification-serializer")
public class SimpleNotificationSerializer implements NotificationSerializer {

  private static enum NotificationElement {

    Type {
      @Override
      void apply(Notification n, Properties p) {
        p.setProperty(name(), n.getType());
      }

    },
    Source {
      @Override
      void apply(Notification n, Properties p) {
        p.setProperty(name(), n.getSource().toString());
      }
    },
    Timestamp {
      @Override
      void apply(Notification n, Properties p) {
        p.setProperty(name(), String.valueOf(n.getTimeStamp()));
      }

    },
    SequenceNumber {
      @Override
      void apply(Notification n, Properties p) {
        p.setProperty(name(), String.valueOf(n.getSequenceNumber()));
      }
    },
    Message {
      @Override
      void apply(Notification n, Properties p) {
        p.setProperty(name(), n.getMessage());
      }
    };

    abstract void apply(Notification n, Properties p);
  }

  public AdaptrisMessage serialize(Notification n, AdaptrisMessage msg) throws CoreException, IOException {
    Properties p = new Properties();
    for (NotificationElement e : NotificationElement.values()) {
      e.apply(n, p);
    }
    try (OutputStream out = msg.getOutputStream()) {
      p.store(out, "");
    }
    msg.getObjectHeaders().put(OBJ_METADATA_USERDATA, n.getUserData());
    return msg;
  }
}
