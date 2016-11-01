package com.adaptris.tester.runners;

import org.apache.tools.ant.BuildFileRule;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class TestExecutorAntTaskTest {

  @Rule
  public final BuildFileRule buildRule = new BuildFileRule();

  //TODO look into creating stub implementation of TestClientapplyService
  @Ignore
  @Test
  public void execute() throws Exception {
    String testFile = "sample.xml";
    File parentDir = new File(this.getClass().getClassLoader().getResource(testFile).getFile()).getParentFile();
    Project project = new Project();
    TestExecutorAntTask  task = new TestExecutorAntTask();
    task.setProject(project);
    FileSet fs = new FileSet();
    fs.setDir(parentDir);
    FilenameSelector fns = new FilenameSelector();
    fns.setName(testFile);
    fs.addFilename(fns);
    task.addFileSet(fs);
    //TODO probably shouldn't be the resources folder.
    task.setResultsDirectory(parentDir);
    task.execute();
    File outputFile = new File(parentDir, "result_" + testFile);
  }

}