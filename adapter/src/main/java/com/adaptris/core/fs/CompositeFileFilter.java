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

package com.adaptris.core.fs;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.oro.io.AwkFilenameFilter;
import org.apache.oro.io.GlobFilenameFilter;
import org.apache.oro.io.Perl5FilenameFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link FileFilter} that contains other filters.
 * 
 * <p>
 * It is designed for use with either the FsConsumer or FtpConsumer, as a result of the limitations of
 * {@link com.adaptris.core.ConsumeDestination}, this filter creates its underlying filters from a single <code>__@@__</code>
 * separated string (this notation was chosen so that it is unlikely to be used as a pattern matching sequence).
 * </p>
 * <p>
 * The format of the string required is {@code 'FILTER_TYPE'='filter-expression'} where the following types of filter are understood
 * (case matters).
 * </p>
 * <ul>
 * <li>NewerThan is equivalent to using {@link NewerThan}</li>
 * <li>OlderThan is equivalent to using {@link OlderThan}</li>
 * <li>SizeGT is equivalent to using {@link SizeGreaterThan} SizeGreaterThan</li>
 * <li>SizeGTE is equivalent to using {@link SizeGreaterThanOrEqual}</li>
 * <li>SizeLT is equivalent to using {@link SizeLessThan}</li>
 * <li>SizeLTE is equivalent to using {@link SizeLessThanOrEqual}</li>
 * <li>Perl is equivalent to using org.apache.oro.io.Perl5FilenameFilter</li>
 * <li>Awk is equivalent to using org.apache.oro.io.AwkFilenameFilter</li>
 * <li>Glob is equivalent to using org.apache.oro.io.GlobFilenameFilter</li>
 * </ul>
 * <p>
 * In the event of a unknown filter type being used, it will be assumed to be a fully qualified classname which will be constructed
 * using reflection; the filter-expression will be passed in as the sole constructor argument.
 * </p>
 * <p>
 * The filter-expression value is something that is understood by the filter in question. e.g. the size in bytes for Size based
 * filters, any errors encountered attempting to create filter results in the filter being ignored.
 * </p>
 * <p>
 * The filter only accepts the file if all the underlying filters accept the file, thus this represents an implicit AND condition.
 * So for example we would use <code>SizeGT=4096__@@__Perl=.*\.xml</code> to match all files ending with <code>.xml</code> whose
 * size is greater than 4096 bytes
 * </p>
 * 
 * 
 * @author lchan
 */
public class CompositeFileFilter implements FileFilter {
  private transient Logger logR = LoggerFactory.getLogger(this.getClass());
  private static final String FILTER_ITEM_SEPARATOR = "__@@__";
  private static final String FILTER_KEY_SEPARATOR = "=";

  private enum FilterImplementation {
    NewerThan {
      @Override
      FileFilter create(String expr) {
        return new NewerThan(expr);
      }
    },
    OlderThan {
      @Override
      FileFilter create(String expr) {
        return new OlderThan(expr);
      }
    },
    SizeGT {
      @Override
      FileFilter create(String expr) {
        return new SizeGreaterThan(expr);
      }
    },
    SizeGTE {
      @Override
      FileFilter create(String expr) {
        return new SizeGreaterThanOrEqual(expr);
      }
    },
    SizeLT {
      @Override
      FileFilter create(String expr) {
        return new SizeLessThan(expr);
      }
    },
    SizeLTE {
      @Override
      FileFilter create(String expr) {
        return new SizeLessThanOrEqual(expr);
      }
    },
    Perl {
      @Override
      FileFilter create(String expr) {
        return new Perl5FilenameFilter(expr);
      }
    },
    Awk {
      @Override
      FileFilter create(String expr) {
        return new AwkFilenameFilter(expr);
      }
    },
    Glob {
      @Override
      FileFilter create(String expr) {
        return new GlobFilenameFilter(expr);
      }
    };
    abstract FileFilter create(String expr);
  };

  private transient List<FileFilter> filters;
  private transient boolean quietMode = false;

  /**
   * Default constructor
   */
  private CompositeFileFilter() {
    filters = new ArrayList<FileFilter>();
  }

  /**
   * Create the filefilter using the specified filterExpression.
   *
   * @param filterExpression the filter expression.
   */
  public CompositeFileFilter(String filterExpression) {
    this();
    initFrom(filterExpression);
  }

  public CompositeFileFilter(String filterExpression, boolean quiet) {
    this(filterExpression);
    quietMode = quiet;
  }

  private void initFrom(String filterExpression) {
    String[] filterItems = filterExpression.split(FILTER_ITEM_SEPARATOR);
    for (String filterString : filterItems) {
      int pos = filterString.indexOf(FILTER_KEY_SEPARATOR);
      if (pos == -1) {
        logR.warn("Ignoring invalid filter [" + filterString + "]");
      }
      else {
        String key = filterString.substring(0, pos);
        String value = filterString.substring(pos + 1);
        FileFilter ff = create(key, value);
        if (ff != null) {
          filters.add(ff);
        }
      }
    }
  }

  private FileFilter create(String impl, String expr) {
    FileFilter filter = null;
    try {
      FilterImplementation f = FilterImplementation.valueOf(impl);
      filter = f.create(expr);
    }
    catch (IllegalArgumentException e) {
      filter = newInstance(impl, expr);
    }
    return filter;
  }

  private FileFilter newInstance(String clazzname, String expr) {
    FileFilter result = null;
    try {
      logR.trace("Trying to use : " + clazzname);
      Constructor cnst = Class.forName(clazzname).getDeclaredConstructor(new Class[]
      {
        String.class
      });
      result = (FileFilter) cnst.newInstance((Object[]) new String[]
      {
        expr
      });
    }
    catch (Exception e) {
      logR.trace("Error encounted attempting to use " + clazzname + ", ignoring", e);
    }
    return result;
  }

  /**
   * @see java.io.FileFilter#accept(java.io.File)
   */
  @Override
  public boolean accept(File pathname) {
    int result = 0;
    for (FileFilter f : filters) {
      if (!quietMode) {
        logR.trace("Checking {} is acceptable for {}", pathname.getAbsolutePath(), f.getClass().getSimpleName());
      }
      result += f.accept(pathname) ? 1 : 0;
    }
    return result == filters.size();
  }
}
