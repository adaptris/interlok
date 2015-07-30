package com.adaptris.core.management;

import static com.adaptris.core.management.Constants.DBG;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

/**
 * Initialise the classpath using {@link URLClassLoader}
 *
 * @author lchan
 * 
 */
public class ClasspathInitialiser {

  private static final String CLASSPATH_KEY = "java.class.path";
  private transient Collection<String> currentClasspath;
  private transient static ClasspathInitialiser self = null;
  private static final String DEFAULT_CONFIG_DIR = "config";
  private static final String DEFAULT_LIB_DIR = "lib";

  private ClasspathInitialiser() {
    currentClasspath = getCurrentClassPath();
  }

  /**
   * Loads the resources from the specified directories and it's subdirectories if loadSubdirs is true. *
   * <p>
   * The system property <code>java.class.path</code> is retroactively changed to include the new jars added.
   * </p>
   *
   * @param adapterClasspath a list of directories whose contents should be added to the classpath. If empty or null, then the
   *          default directories are loaded.
   * @param loadSubdirs whether or not sub directory contents are parsed.
   * @return the ClasspathInitialiser for further operations.
   */
  public static ClasspathInitialiser init(Collection<String> adapterClasspath, boolean loadSubdirs) {
    if (self == null) {
      self = new ClasspathInitialiser();
      try {
        if (adapterClasspath == null || adapterClasspath.size() == 0) {
          String userDir = System.getProperty("user.dir");
          if (!userDir.endsWith(File.separator)) {
            userDir = userDir + File.separator;
          }
          self.add(new File(userDir, DEFAULT_CONFIG_DIR));
          self.load(userDir + DEFAULT_LIB_DIR, loadSubdirs);
        }
        else {
          for (String path : adapterClasspath) {
            self.load(path, loadSubdirs);
          }
        }
        self.setSystemClasspath();
      }
      catch (Exception e) {
        System.err.println(e.getMessage());
        System.err.println("Failed to initialise, forced exit()");
        System.exit(1);
      }
    }
    return self;
  }

  private String getLoadedClasspath() {
    StringBuffer sb = new StringBuffer();
    for (String element : currentClasspath) {
      sb.append(element);
      sb.append(File.pathSeparator);
    }
    return sb.toString();
  }

  private void setSystemClasspath() {
    System.setProperty(CLASSPATH_KEY, getLoadedClasspath());
  }

  /**
   * Add all the jars from the specified dir into the current classloader.
   * <p>
   * Using URLClassLoader, this will add all the jars in the specified directory into the ClassLoader that was used to load this
   * class.
   * </p>
   * <p>
   * Any jars that are already present in the directory, but are already referenced in the system property
   * <code>java.class.path</code> are considered to be already loaded, and not added to the classloader. This check is performed on
   * the absolute pathname of the file, rather than the name, so <code>c:\jars\xerces.jar</code> and <code>c:\new\xerces.jar</code>
   * will be considered separate jars.
   * </p>
   *
   * @param dir the directory to load
   * @throws Exception on any failure.
   */
  public void load(String dir) throws Exception {
    add(new File(dir));
    Collection<String> jarsToLoad = getJars(dir);
    if (jarsToLoad != null) {
      for (String jar : jarsToLoad) {
        File file = new File(jar);
        add(file);
      }
    }
  }

  /**
   * Add a directory to the classpath.
   *
   * @param dir the directory to load
   * @param loadSubdirs whether or not to include sub-directory contents.
   * @throws Exception
   * @see #load(String)
   */
  public void load(String dir, boolean loadSubdirs) throws Exception {
    load(dir);
    if (loadSubdirs) {
      File actDir = new File(dir);
      File[] subDirs = actDir.listFiles(new FileFilter() {

        @Override
        public boolean accept(File file) {
          return file.isDirectory();
        }
      });

      for (File subDir : subDirs) {
        load(subDir.getAbsolutePath(), loadSubdirs);
      }
    }
  }

  /**
   * Add the file object to the classpath.
   *
   * @param file the File object to add.
   * @throws Exception on error.
   */
  public void add(File file) throws Exception {
    URLClassLoader urlLoader = getUrlClassLoader();
    Method method = getAddMethod();
    if (urlLoader != null) {
      if (!currentClasspath.contains(file.getCanonicalPath())) {
        if (DBG) {
          System.err.println("(Info) ClasspathInitialiser.load: " + file.getCanonicalPath());
        }
        method.invoke(urlLoader, new Object[]
        {
          file.toURI().toURL()
        });

        currentClasspath.add(file.getCanonicalPath());
      }
    }
  }

  /**
   * Get the method used to add jars to the classloader.
   * <p>
   * We will be using the Method object to add urls to the classloader
   * </p>
   *
   * @see java.lang.reflect.Method
   * @return the method
   * @throws Exception on any error
   */
  private Method getAddMethod() throws Exception {
    Method addUrlMethod = null;
    addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]
    {
      java.net.URL.class
    });

    addUrlMethod.setAccessible(true);
    return addUrlMethod;
  }

  /**
   * Get the <code>URLClassLoader</code> associated with this object.
   *
   * @return the URLClassLoader
   */
  private URLClassLoader getUrlClassLoader() {

    ClassLoader urlLoader = null;
    urlLoader = StandardBootstrap.class.getClassLoader();
    return urlLoader instanceof URLClassLoader ? (URLClassLoader) urlLoader : null;
  }

  /**
   * Get the current classpath from the system property.
   * <p>
   * Any duplicates are removed from the classpath specified by the system property <code>java.class.path</code>
   * </p>
   *
   * @return a collection containing the classpath
   */
  private Collection<String> getCurrentClassPath() {

    ArrayList<String> arraylist = new ArrayList<String>();
    StringTokenizer st = new StringTokenizer(System.getProperty(CLASSPATH_KEY), File.pathSeparator);
    while (st.hasMoreElements()) {
      try {
        File file = new File((String) st.nextElement());
        if (!arraylist.contains(file.getCanonicalPath())) {
          arraylist.add(file.getCanonicalPath());
          if (DBG) {
            System.out.println("(Info) StandardBootstrap.getCurrentClassPath: " + file.getCanonicalPath());
          }
        }
      }
      catch (Exception e) {
        ;
      }
    }

    return arraylist;
  }

  /**
   * Get a list of jars from the specified directory.
   *
   * @return a list of jars in the specified directory
   * @param dir the directory to search
   */
  private Collection<String> getJars(String dir) {

    ArrayList<String> jars = new ArrayList<String>();
    File file = new File(dir);
    if (file.exists()) {
      String[] files = file.list(JarFilter.getDefaultInstance());
      if (files != null) {
        for (int i = 0; i < files.length; i++) {
          String filename = file.getPath() + File.separator + files[i];
          jars.add(filename);
        }
      }
    }
    if (DBG && (jars == null || jars.size() < 1)) {
      System.err.println("(Warning) ClasspathInitialiser.getJars() " + dir + " is empty or does not exist");
    }
    return jars;
  }

}
