package com.adaptris.core.management.classloader;

import java.net.URLClassLoader;

public interface ClassLoaderFactory {
  URLClassLoader create(ClassLoader parent);
}
