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
package com.adaptris.interlok.boot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.loader.archive.Archive;

public class InterlokLauncherTest {

  @BeforeEach
  public void setUp() throws Exception {
  }

  @AfterEach
  public void tearDown() throws Exception {
  }

  @Test
  public void testMainClass() throws Exception {
    InterlokLauncher launcher = new InterlokLauncher(null);
    assertEquals(InterlokLauncher.INTERLOK_MAIN_CLASS, launcher.getMainClass());
  }

  @Test
  public void testMainClassFailover() throws Exception {
    InterlokLauncher launcher = new InterlokLauncher(new String[]{
        "-failover", "input.xml"
    });
    assertEquals(InterlokLauncher.INTERLOK_FAILOVER_MAIN_CLASS, launcher.getMainClass());
    launcher = new InterlokLauncher(new String[]{
        "--failover", "input.xml"
    });
    assertEquals(InterlokLauncher.INTERLOK_FAILOVER_MAIN_CLASS, launcher.getMainClass());
  }

  @Test
  public void testMainClassContainer() throws Exception {
    InterlokLauncher launcher = new InterlokLauncher(new String[]
    {
        "-container", "input.properties"
    });
    assertEquals(InterlokLauncher.INTERLOK_CONTAINER_MAIN_CLASS, launcher.getMainClass());
    launcher = new InterlokLauncher(new String[]
    {
        "--container", "input.properties"
    });
    assertEquals(InterlokLauncher.INTERLOK_CONTAINER_MAIN_CLASS, launcher.getMainClass());
  }

  @Test
  public void testMainClassServiceTest() throws Exception {
    InterlokLauncher launcher = new InterlokLauncher(new String[]{
        "-serviceTest", "input.xml"
    });
    assertEquals(InterlokLauncher.SERVICE_TEST_MAIN_CLASS, launcher.getMainClass());
    launcher = new InterlokLauncher(new String[]{
        "--serviceTest", "input.xml"
    });
    assertEquals(InterlokLauncher.SERVICE_TEST_MAIN_CLASS, launcher.getMainClass());
  }

  @Test
  public void testRebuild() throws Exception {
    InterlokLauncher launcher = new InterlokLauncher(new String[]
    {
        "-ignoreSubDirs", "--adapterClasspath", "./lib", "--configcheck", "bootstrap.properties"
    });
    String[] rebuild = launcher.rebuildArgs();
    assertEquals(2, rebuild.length);
    assertEquals("--configcheck", rebuild[0]);
    assertEquals("bootstrap.properties", rebuild[1]);
  }

  @Test
  public void testRebuild_NoArgs() throws Exception {
    InterlokLauncher launcher = new InterlokLauncher(new String[]
    {
        "my.properties"
    });
    String[] rebuild = launcher.rebuildArgs();
    assertEquals(1, rebuild.length);
    assertEquals("my.properties", rebuild[0]);
  }

  @Test
  public void testClasspathArchives_Default() throws Exception {
    InterlokLauncher launcher = new InterlokLauncher(new String[0]);
    assertNotNull(launcher.getClassPathArchives());
    // No ./config;./lib
    assertEquals(0, launcher.getClassPathArchives().size());
  }

  @Test
  public void testClasspathArchives_NonExistent() throws Exception {
    InterlokLauncher launcher = new InterlokLauncher(new String[]
    {
        "-ignoreSubDirs", "--adapterClasspath", "./config,./lib,./some.jar,./someDir/*,anotherDir/,."
    });
    assertNotNull(launcher.getClassPathArchives());
    // . gets a classpath entry, cos it exists.
    assertEquals(1, launcher.getClassPathArchives().size());

  }

  @Test
  public void testClasspathArchives_NoRecurse() throws Exception {
    String javaHome = System.getProperty("java.home");
    InterlokLauncher launcher = new InterlokLauncher(new String[]
    {
        "-ignoreSubDirs", "--adapterClasspath", javaHome
    });
    assertNotNull(launcher.getClassPathArchives());
    assertNotSame(0, launcher.getClassPathArchives().size());
    assertContains(launcher.getClassPathArchives(), new NoOpFileArchive(new File(javaHome)));
  }

  @Test
  public void testClasspathArchives() throws Exception {
    String javaHome = System.getProperty("java.home");
    // must be jars in javahome right?
    InterlokLauncher launcher = new InterlokLauncher(new String[]
    {
        "--adapterClasspath", javaHome
    });
    assertNotNull(launcher.getClassPathArchives());
    assertNotSame(0, launcher.getClassPathArchives().size());
    // the java_home directory will be available.
    assertContains(launcher.getClassPathArchives(), new NoOpFileArchive(new File(javaHome)));
    assertTrue(launcher.getClassPathArchives().size() > 1);
  }

  @Test
  public void testRebuild_Failover() throws Exception {
    InterlokLauncher launcher = new InterlokLauncher(new String[]
        {
            "--failover", "my.properties"
        });
    String[] rebuild = launcher.rebuildArgs();
    assertEquals(1, rebuild.length);
    assertEquals("my.properties", rebuild[0]);
  }

  @Test
  public void testRebuild_FailoverWithNoProperties() throws Exception {
    InterlokLauncher launcher = new InterlokLauncher(new String[]
        {
            "--failover"
        });
    String[] rebuild = launcher.rebuildArgs();
    assertEquals(0, rebuild.length);
  }

  @Test
  public void testRebuild_Container() throws Exception {
    InterlokLauncher launcher = new InterlokLauncher(new String[]
    {
        "--container", "my.properties"
    });
    String[] rebuild = launcher.rebuildArgs();
    assertEquals(1, rebuild.length);
    assertEquals("my.properties", rebuild[0]);
  }

  @Test
  public void testRebuild_ContainerWithNoProperties() throws Exception {
    InterlokLauncher launcher = new InterlokLauncher(new String[]
    {
        "--container"
    });
    String[] rebuild = launcher.rebuildArgs();
    assertEquals(0, rebuild.length);
  }

  @Test
  public void testMainClassPassword() throws Exception {
    InterlokLauncher launcher = new InterlokLauncher(new String[]
    {
        "-password", "PW:", "Hello"
    });
    assertEquals(InterlokLauncher.PASSWORD_GEN_MAIN_CLASS, launcher.getMainClass());
    launcher = new InterlokLauncher(new String[]
    {
        "--password", "PW:", "Hello"
    });
    assertEquals(InterlokLauncher.PASSWORD_GEN_MAIN_CLASS, launcher.getMainClass());
  }


  @Test
  public void testRebuild_Password() throws Exception {
    InterlokLauncher launcher = new InterlokLauncher(new String[]
    {
        "-password", "PW:", "Hello"
    });
    String[] rebuild = launcher.rebuildArgs();
    assertEquals(2, rebuild.length);
    assertEquals("PW:", rebuild[0]);
    assertEquals("Hello", rebuild[1]);
  }

  private void assertContains(List<Archive> list, NoOpFileArchive archive) {
    boolean result = false;
    for (Archive arch : list) {
      if (arch instanceof NoOpFileArchive && ((NoOpFileArchive) arch).toString().equals(archive.toString())) {
        result = true;
        break;
      }
    }
    assertTrue(result);
  }
}
