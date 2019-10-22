package com.adaptris.interlok.cloud;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import javax.validation.constraints.NotBlank;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.interlok.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Wraps a {@link FileFilter} instance and allows you to use that as your filter.
 * <p>
 * The file filter implementation is configured as a classname, and is expected to have a
 * <strong>String constructor</strong> (the filter-expression is used as that parameter); this is
 * because not all FileFilter implementations will have aliases and configuration that are
 * appropriate for marshalling
 * </p>
 * <p>
 * Apache commons-io filters such as {@code SizeBasedFilter} only provide a numeric constructor, so
 * these cannot yet be supported via this wrapper; however, you have other options available in the
 * {@code com.adaptris.core.fs} package.
 * </p>
 * <p>
 * Since a {@link RemoteBlob} does not expose all the possible methods of {@link java.io.File}, it
 * is wrapped as a {@link RemoteFile}. <strong>If your filter uses anything other than the filename
 * / size / lastmodified, then results will be undefined.</strong>
 * </p>
 * 
 * 
 * @config remote-blob-filter-wrapper
 */
@XStreamAlias("remote-blob-filter-wrapper")
@ComponentProfile(summary = "Wraps a FileFilter instance for filtering remote blobs", since = "3.9.2")
public class RemoteBlobFilterWrapper implements RemoteBlobFilter {

  @NotBlank
  private String fileFilterImp;
  @NotBlank
  private String filterExpression;

  private transient FileFilter fileFilter;

  @Override
  public boolean accept(RemoteBlob blob) {
    if (fileFilter == null) {
      fileFilter = createFilter(getFilterExpression(), getFileFilterImp());
    }
    return fileFilter.accept(blob.toFile());
  }

  public String getFileFilterImp() {
    return fileFilterImp;
  }

  /**
   * Specify the file filter classname that will be used.
   * 
   * @param fileFilterImp the classname; may not be null.
   */
  public void setFileFilterImp(String fileFilterImp) {
    this.fileFilterImp = Args.notNull(fileFilterImp, "file-filter-imp");
  }

  public RemoteBlobFilterWrapper withFilterImp(String s) {
    setFileFilterImp(s);
    return this;
  }

  public String getFilterExpression() {
    return filterExpression;
  }


  /**
   * Specify the file filter expression that will be used.
   * 
   * @param filterExpression the expression; may not be null.
   */
  public void setFilterExpression(String filterExpression) {
    this.filterExpression = Args.notNull(filterExpression, "filter-expression");
  }

  public RemoteBlobFilterWrapper withFilterExpression(String expression) {
    setFilterExpression(expression);
    return this;
  }

  // Copied from FsHelper which makes me a little bit sad.
  private static FileFilter createFilter(String filterExpression, String filterImpl) {
    FileFilter result = null;
    try {
      if (isEmpty(filterExpression)) {
        result = (filePath) -> true;
      } else {
        Class[] paramTypes = {String.class};
        Object[] args = {filterExpression};
        Class c = Class.forName(filterImpl);
        Constructor cnst = c.getDeclaredConstructor(paramTypes);
        result = (FileFilter) cnst.newInstance(args);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }
}
