/*
 * Copyright 2017 Adaptris Ltd.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.lang.reflect.Constructor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.jdbc.JdbcParameterException;
import com.adaptris.core.management.vcs.VcsConflictException;
import com.adaptris.core.management.vcs.VcsException;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.fs.FsException;
import com.adaptris.fs.FsFileNotFoundException;
import com.adaptris.fs.FsFilenameExistsException;
import com.adaptris.ftp.FtpException;
import com.adaptris.naming.adapter.AdapterNamingException;
import com.adaptris.sftp.SftpException;
import com.adaptris.util.datastore.DataStoreException;

public class CoreExceptionTest {
  private static String[] EXCEPTION_NAMES =
  {
      CoreException.class.getName(), AdapterNamingException.class.getName(), JdbcParameterException.class.getName(),
      OutOfStateException.class.getName(), ProduceException.class.getName(), ServiceException.class.getName(),
      VcsException.class.getName(), VcsConflictException.class.getName(), UnresolvedMetadataException.class.getName(),

      FileTransferException.class.getName(), FsException.class.getName(), FsFilenameExistsException.class.getName(),
          FsFileNotFoundException.class.getName(), FtpException.class.getName(),
      SftpException.class.getName(), DataStoreException.class.getName()
  };

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testConstructor() throws Exception {
    for (String s : EXCEPTION_NAMES) {
      try {
        Exception e = (Exception) Class.forName(s).getDeclaredConstructor().newInstance();
        assertNull(e.getCause());
        assertNull(e.getMessage());
      }
      catch (InstantiationException | NoSuchMethodException sometimes) {

      }
    }
  }

  @Test
  public void testConstructorThrowable() throws Exception {
    Exception cause = new Exception();
    Class<?>[] paramTypes =
    {
        Throwable.class
    };
    Object[] args =
    {
        cause
    };
    for (String s : EXCEPTION_NAMES) {
      try {
        Constructor<?> cnst = Class.forName(s).getDeclaredConstructor(paramTypes);
        Exception e = (Exception) cnst.newInstance(args);
        assertEquals(cause, e.getCause());
        assertEquals(Exception.class.getName(), e.getMessage());
      }
      catch (NoSuchMethodException sometimes) {

      }
    }
  }

  @Test
  public void testConstructorException() throws Exception {
    Exception cause = new Exception();
    Class<?>[] paramTypes =
    {
        Exception.class
    };
    Object[] args =
    {
        cause
    };
    for (String s : EXCEPTION_NAMES) {
      try {
        Constructor<?> cnst = Class.forName(s).getDeclaredConstructor(paramTypes);
        Exception e = (Exception) cnst.newInstance(args);
        assertEquals(cause, e.getCause());
        assertEquals(Exception.class.getName(), e.getMessage());
      }
      catch (NoSuchMethodException sometimes) {

      }
    }
  }

  @Test
  public void testConstructorString() throws Exception {
    Class<?>[] paramTypes =
    {
        String.class
    };
    Object[] args =
    {
        "hello"
    };
    for (String s : EXCEPTION_NAMES) {
      try {
        Constructor<?> cnst = Class.forName(s).getDeclaredConstructor(paramTypes);
        Exception e = (Exception) cnst.newInstance(args);
        assertNull(e.getCause());
        assertEquals("hello", e.getMessage());
      }
      catch (NoSuchMethodException sometimes) {

      }
    }
  }

  @Test
  public void testConstructorStringThrowable() throws Exception {
    Exception cause = new Exception();
    Class<?>[] paramTypes =
    {
        String.class, Throwable.class
    };
    Object[] args =
    {
        "hello", cause
    };
    for (String s : EXCEPTION_NAMES) {
      try {
        Constructor<?> cnst = Class.forName(s).getDeclaredConstructor(paramTypes);
        Exception e = (Exception) cnst.newInstance(args);
        assertEquals(cause, e.getCause());
        assertEquals("hello", e.getMessage());
      }
      catch (NoSuchMethodException sometimes) {

      }
    }
  }

  @Test
  public void testConstructorStringException() throws Exception {
    Exception cause = new Exception();
    Class<?>[] paramTypes =
    {
        String.class, Exception.class
    };
    Object[] args =
    {
        "hello", cause
    };
    for (String s : EXCEPTION_NAMES) {
      try {
        Constructor<?> cnst = Class.forName(s).getDeclaredConstructor(paramTypes);
        Exception e = (Exception) cnst.newInstance(args);
        assertEquals(cause, e.getCause());
        assertEquals("hello", e.getMessage());
      }
      catch (NoSuchMethodException sometimes) {

      }
    }
  }

}
