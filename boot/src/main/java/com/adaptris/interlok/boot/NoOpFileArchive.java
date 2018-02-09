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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Manifest;

import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.ExplodedArchive;

/**
 * Minimal {@link Archive} implementation for a given {@link File}.
 * <p>
 * We could use {@link ExplodedArchive} but that does extra work that we don't need.
 * </p>
 * 
 * @author lchan
 *
 */
public class NoOpFileArchive implements Archive {

  private File root;

  public NoOpFileArchive(File f) {
    root = f;
  }

  @Override
  public Iterator<Entry> iterator() {
    return Collections.EMPTY_LIST.iterator();
  }

  @Override
  public URL getUrl() throws MalformedURLException {
    return this.root.toURI().toURL();
  }

  @Override
  public Manifest getManifest() throws IOException {
    return null;
  }

  @Override
  public List<Archive> getNestedArchives(EntryFilter filter) throws IOException {
    return Collections.EMPTY_LIST;
  }

  @Override
  public String toString() {
    try {
      return root.getCanonicalPath();
    } catch (Exception e) {
      return root.getAbsolutePath();
    }
  }

}
