package com.adaptris.core.stubs;

import java.io.IOException;

import com.adaptris.core.CoreException;
import com.adaptris.core.LogHandlerImp;
import com.adaptris.util.license.License;

public abstract class MockLogHandler extends LogHandlerImp {

  public static final String LOG_EXTRACT = "TRACE [main] [DownloadPatchRequestEvent.handle()] "
      + "created non-existent directory cfc135ee0000005d0029428e0fa278ab" + System.getProperty("line.separator")
      + "TRACE [main] [DownloadPatchRequestEvent.downloadPatch()] Downloading patch cfc135ee0000005d0029428e0fa278ab "
      + "from [http://development.adaptris.com/internal/core-latest/2010-05-24/HEADER.html]" + System.getProperty("line.separator")
      + "TRACE [main] [DownloadPatchRequestEvent.downloadPatch()] cfc135ee0000005d0029428e0fa278ab written to "
      + "[cfc135ee0000005d0029428e0fa278ab/HEADER.html]" + System.getProperty("line.separator")
      + "TRACE [main] [DownloadPatchRequestEvent.handle()] created non-existent directory cfc137080000005d0029428e4812f3e9"
      + System.getProperty("line.separator");

  public void clean() throws IOException {
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

}
