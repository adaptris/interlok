/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.interlok.boot;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.loader.Launcher;
import org.springframework.boot.loader.PropertiesLauncher;
import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.JarFileArchive;

/**
 * Customised {@link Launcher} instance that mimics the existing behaviour for interlok.
 * <p>
 * It is based on {@link PropertiesLauncher} from the {@code org.springframework.boot:spring-boot-loader} project, with changes to
 * support the custom properties already supported by interlok. {@link PropertiesLauncher} does a lot more than we need; you can
 * switch to using {@link PropertiesLauncher} by simply specifying it as the main class, the bundled {@code loader.properties} is
 * sufficient to start a standard instance.
 * </p>
 *
 * @author the original authors for {@link PropertiesLauncher}.
 */
public class InterlokLauncher extends Launcher {

  private static final String ZIP_EXT = ".zip";
  private static final String JAR_EXT = ".jar";
  private static final String[] JAR_EXTS =
  {
      JAR_EXT, ZIP_EXT
  };

  private static final String DEFAULT_CONFIG_DIR = "config";

  private static final List<String> SPECIAL_DIRS = Collections.unmodifiableList(Arrays.asList(".git", ".svn", "CVS", ".hg"));
  private static final String DEBUG_PREFIX = "(" + InterlokLauncher.class.getSimpleName() + ") ";

  private static final FileFilter JAR_FILTER = new JarFilter();

  private static final boolean DEBUG = Boolean.getBoolean("adp.bootstrap.debug") || Boolean.getBoolean("interlok.bootstrap.debug");

  private static final String INTERLOK_MAIN_CLASS = "com.adaptris.core.management.SimpleBootstrap";
  private static final String[] ARG_ADAPTER_CLASSPATH = new String[]
  {
      "-adapterClasspath", "--adapterClasspath"
  };
  private static final String[] ARG_IGNORE_SUBDIRS = new String[]
  {
      "-ignoreSubDirs", "--ignoreSubDirs"
  };

  private final String DEFAULT_CLASSPATH = "./config,./lib";

  private List<String> paths = new ArrayList<>();
  private CommandLineArgs commandLine;
  private boolean recursive;
  private boolean defaultClasspath = true;

  public InterlokLauncher(String[] argv) {
    try {
      commandLine = CommandLineArgs.parse(argv);
      recursive = !commandLine.hasArgument(ARG_IGNORE_SUBDIRS);
      paths = initializePaths();
    }
    catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  public static void main(String[] args) throws Exception {
    InterlokLauncher launcher = new InterlokLauncher(args);
    launcher.launch(launcher.rebuildArgs());
  }

  private List<String> initializePaths() {
    String pathsToParse = DEFAULT_CLASSPATH;
    if (commandLine.hasArgument(ARG_ADAPTER_CLASSPATH)) {
      pathsToParse = commandLine.getArgument(ARG_ADAPTER_CLASSPATH);
      defaultClasspath = false;
    }
    List<String> result = new ArrayList<>();
    for (String path : pathsToParse.split("[,|" + File.pathSeparator + "]")) {
      result.add(cleanupPath(path));
    }
    debug("Nested archive paths: ", result);
    return result;
  }

  @Override
  protected String getMainClass() throws Exception {
    return INTERLOK_MAIN_CLASS;
  }

  protected String[] rebuildArgs() throws Exception {
    return commandLine.remove(ARG_ADAPTER_CLASSPATH).remove(ARG_IGNORE_SUBDIRS).render();
  }

  @Override
  protected List<Archive> getClassPathArchives() throws Exception {
    List<Archive> lib = new ArrayList<>();
    for (String path : this.paths) {
      lib.addAll(createArchives(path));
    }
    return lib;
  }

  private String cleanupPath(String path) {
    path = path.trim();
    // No need for current dir path
    if (path.startsWith("./")) {
      path = path.substring(2);
    }
    if (JAR_FILTER.accept(new File(path))) {
      return path;
    }
    if (path.endsWith("/*")) {
      path = path.substring(0, path.length() - 1);
    }
    else {
      // It's a directory
      if (!path.endsWith("/") && !path.equals(".")) {
        path = path + "/";
      }
    }
    return path;
  }

  private static void debug(String message, Object... objects) {
    if (DEBUG) {
      StringBuilder sb = new StringBuilder(DEBUG_PREFIX).append(message);
      for (Object o : objects) {
        sb.append(o.toString());
      }
      System.err.println(sb.toString());
    }
  }

  private List<Archive> createArchives(String path) throws Exception {
    List<Archive> lib = new ArrayList<>();
    File file = new File(path);
    if (file.isDirectory()) {
      debug("Added ", file);
      lib.add(new NoOpFileArchive(file));
      if (defaultClasspath && !file.getName().equals(DEFAULT_CONFIG_DIR)) {
        lib.addAll(createArchives(file, recursive));
      }
    }
    else {
      if (JAR_FILTER.accept(file)) {
        addArchive(file, lib);
      }
    }
    return lib;
  }

  private static List<Archive> createArchives(File dir, boolean loadSubdirs) throws IOException {
    ArrayList<Archive> jars = new ArrayList<>();
    if (SPECIAL_DIRS.contains(dir.getName())) {
      debug("Ignoring special directory ", dir.getName());
      return jars;
    }
    debug("Adding jars from " + dir.getCanonicalPath());
    File[] files = dir.listFiles(JAR_FILTER);
    for (File jar : files) {
      debug("Adding ", jar.getName());
      addArchive(jar, jars);
    }
    if (loadSubdirs) {
      File[] subDirs = dir.listFiles(new FileFilter() {

        @Override
        public boolean accept(File file) {
          return file.isDirectory();
        }
      });
      for (File subDir : subDirs) {
        jars.addAll(createArchives(subDir, loadSubdirs));
      }
    }
    return jars;
  }

  private static void addArchive(File f, List<Archive> list) throws IOException {
    if (f.exists()) {
      list.add(new JarFileArchive(f));
    }
  }

  private static class JarFilter implements FileFilter {
    @Override
    public boolean accept(File file) {
      for (String s : JAR_EXTS) {
        if (file.getName().toLowerCase().endsWith(s)) {
          return true;
        }
      }
      return false;
    }
  }
}
