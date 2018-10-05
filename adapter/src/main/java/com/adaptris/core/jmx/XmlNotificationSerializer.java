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

import javax.management.Notification;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.XmlUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Serializes a {@link Notification} into an XML message.
 * <p>
 * {@link Notification#getUserData()} is always added as object metadata against the key
 * {@value com.adaptris.core.jmx.NotificationSerializer#OBJ_METADATA_USERDATA}.
 * </p>
 * 
 * @config xml-jmx-notification-serializer
 * 
 */
@XStreamAlias("xml-jmx-notification-serializer")
public class XmlNotificationSerializer implements NotificationSerializer {
  private static final String XML_ENCODING = "UTF-8";
  private static final String XML_ROOT_ELEMENT = "Notification";

  private static enum NotificationElement {

    Type("/Notification/Type") {
      @Override
      String getValue(Notification n) {
        return n.getType();
      }
    },
    Source("/Notification/Source") {
      @Override
      String getValue(Notification n) {
        return n.getSource().toString();
      }
    },
    Timestamp("/Notification/Timestamp") {
      @Override
      String getValue(Notification n) {
        return String.valueOf(n.getTimeStamp());
      }

    },
    SequenceNumber("/Notification/SequenceNumber") {
      @Override
      String getValue(Notification n) {
        return String.valueOf(n.getSequenceNumber());
      }
    },
    Message("/Notification/Message") {
      @Override
      String getValue(Notification n) {
        return n.getMessage();
      }
    };

    private String xpath;

    NotificationElement(String s) {
      xpath = s;
    }

    public String xpathToNode() {
      return xpath;
    }

    abstract String getValue(Notification n);
  }

  private String outputMessageEncoding = null;


  public XmlNotificationSerializer() {

  }

  @Override
  public AdaptrisMessage serialize(Notification n, AdaptrisMessage msg) throws CoreException, IOException {
    try {
      XmlUtils xmlBuilder = new XmlUtils();
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Element root = doc.createElement(XML_ROOT_ELEMENT);
      doc.appendChild(root);
      xmlBuilder.setSource(doc);
      for (NotificationElement e : NotificationElement.values()) {
        xmlBuilder.setNodeValue(e.xpathToNode(), e.getValue(n));
      }
      try (OutputStream out = msg.getOutputStream()) {
        String encoding = XmlHelper.getXmlEncoding(msg, getOutputMessageEncoding());
        xmlBuilder.writeDocument(out, encoding);
        msg.setContentEncoding(encoding);
      }
      if (n.getUserData() != null) {
        msg.getObjectHeaders().put(OBJ_METADATA_USERDATA, n.getUserData());
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
    return msg;
  }

  public String getOutputMessageEncoding() {
    return outputMessageEncoding;
  }

  /**
   * Set the encoding for the resulting XML document.
   * <p>
   * If not specified the following rules will be applied:
   * </p>
   * <ol>
   * <li>If the {@link AdaptrisMessage#getCharEncoding()} is non-null then that will be used.</li>
   * <li>UTF-8</li>
   * </ol>
   * <p>
   * As a result; the character encoding on the message is always set using {@link AdaptrisMessage#setContentEncoding(String)}.
   * </p>
   * 
   * @param encoding the character
   */
  public void setOutputMessageEncoding(String encoding) {
    outputMessageEncoding = encoding;
  }

}
