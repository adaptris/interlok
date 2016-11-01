package com.adaptris.tester.runners;


import com.adaptris.tester.runtime.ServiceTestException;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Vector;

public class TestExecutorAntTask extends Task {

  File resultsDirectory;
  Vector<FileSet> filesets = new Vector<FileSet>();

  public void setResultsDirectory(File resultsDirectory) {
    this.resultsDirectory = resultsDirectory;
  }

  public void addFileSet(FileSet fileset) {
    if (!filesets.contains(fileset)) {
      filesets.add(fileset);
    }
  }

  public void execute() throws BuildException {
    DirectoryScanner ds;
    for (FileSet fileset : filesets) {
      ds = fileset.getDirectoryScanner(getProject());
      File dir = ds.getBasedir();
      String[] filesInSet = ds.getIncludedFiles();
      for (String filename : filesInSet) {
        File file = new File(dir,filename);
        try {
          TestExecutor.execute(file, resultsDirectory);
        } catch (ServiceTestException e) {
          throw new BuildException(e);
        }
      }
    }

  }
}
