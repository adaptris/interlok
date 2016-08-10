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

package com.adaptris.core.runtime;

import static com.adaptris.core.CoreConstants.FS_PRODUCE_DIRECTORY;
import static com.adaptris.core.CoreConstants.OBJ_METADATA_EXCEPTION;
import static com.adaptris.core.CoreConstants.PRODUCED_NAME_KEY;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MessageLifecycleEvent;

public class MessageDigestErrorEntry extends MessageDigestEntry {

  private static final long serialVersionUID = 201211231143L;

  private String stackTrace;
  private String fileSystemPath;
  private MessageLifecycleEvent lifecycleEvent;

  public MessageDigestErrorEntry() {
    super();
  }

  public MessageDigestErrorEntry(String uniqueId, String workflowId) {
    this(uniqueId, workflowId, new Date());
  }

  public MessageDigestErrorEntry(String uniqueId, String workflowId, Date date) {
    super(uniqueId, workflowId, date);
  }


  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("date", getDate()).append("unique-id", getUniqueId())
        .append("workflow-id", getWorkflowId()).append("fsLocation", getFileSystemPath()).append("stacktrace", getStackTrace())
        .toString();
  }

  private void buildStackTrace(Exception e) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw, true);
    try {
      if (e != null) {
        e.printStackTrace(pw);
      }
    }
    finally {
      IOUtils.closeQuietly(pw);
      IOUtils.closeQuietly(sw);
    }
    // if e == null, then the stackTrace will be the empty string.
    stackTrace = sw.toString();
  }

  public void setStackTrace(String st) {
    stackTrace = st;
  }

  public void setStackTrace(Exception e) {
    buildStackTrace(e);
  }

  public String getStackTrace() {
    return stackTrace;
  }

  public String getFileSystemPath() {
    return fileSystemPath;
  }


  public void setFileSystemPath(String s) {
    fileSystemPath = s;
  }

  public void setFileSystemFile(File f) throws IOException {
    if (f!=null) {
      fileSystemPath = f.getCanonicalPath();
    }
  }

  public MessageLifecycleEvent getLifecycleEvent() {
    return lifecycleEvent;
  }

  public void setLifecycleEvent(MessageLifecycleEvent event) {
    lifecycleEvent = event;
  }

  public void extractInfo(AdaptrisMessage msg) {
    addException(msg);
    addLifecycleEvent(msg);
    addFileSystemLocation(msg);
  }

  private void addException(AdaptrisMessage msg) {
    Map<Object, Object> objectMetadata = msg.getObjectHeaders();
    if (objectMetadata.containsKey(OBJ_METADATA_EXCEPTION)) {
      Exception e = (Exception) objectMetadata.get(OBJ_METADATA_EXCEPTION);
      setStackTrace(e);
    }
  }

  private void addLifecycleEvent(AdaptrisMessage msg) {
    try {
      setLifecycleEvent(msg.getMessageLifecycleEvent().clone());
    }
    catch (CloneNotSupportedException unlikely) {
      setLifecycleEvent(null);
    }
  }

  private void addFileSystemLocation(AdaptrisMessage msg) {
    try {
      if (msg.containsKey(PRODUCED_NAME_KEY)) {
        if (msg.containsKey(FS_PRODUCE_DIRECTORY)) {
          setFileSystemFile(new File(msg.getMetadataValue(FS_PRODUCE_DIRECTORY), msg.getMetadataValue(PRODUCED_NAME_KEY)));
        }
        else {
          setFileSystemPath(msg.getMetadataValue(PRODUCED_NAME_KEY));
        }
      }
    }
    catch (IOException e) {
      setFileSystemPath(null);
    }
  }

}
