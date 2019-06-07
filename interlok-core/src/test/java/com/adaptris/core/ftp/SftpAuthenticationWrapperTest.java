package com.adaptris.core.ftp;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;
import java.io.IOException;
import org.junit.Test;
import org.mockito.Mockito;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.sftp.SftpClient;

public class SftpAuthenticationWrapperTest {

  @Test
  public void testConnect() throws Exception {
    SftpAuthenticationProvider mockProvider = Mockito.mock(SftpAuthenticationProvider.class);
    SftpClient mockClient = Mockito.mock(SftpClient.class);
    when(mockProvider.connect(anyObject(), anyObject())).thenReturn(mockClient);
    SftpAuthenticationWrapper wrapper = new SftpAuthenticationWrapper(mockProvider);
    assertEquals(mockClient, wrapper.connect(mockClient, null));
  }

  @Test(expected = FileTransferException.class)
  public void testConnect_AllFailed_NoLogging() throws Exception {
    SftpAuthenticationProvider mockProvider = Mockito.mock(SftpAuthenticationProvider.class);
    SftpClient mockClient = Mockito.mock(SftpClient.class);
    when(mockProvider.connect(anyObject(), anyObject())).thenThrow(new IOException());
    SftpAuthenticationWrapper wrapper = new SftpAuthenticationWrapper(mockProvider);
    wrapper.connect(mockClient, null);
  }


  @Test(expected = FileTransferException.class)
  public void testConnect_AllFailed_Logging() throws Exception {
    SftpAuthenticationProvider mockProvider = Mockito.mock(SftpAuthenticationProvider.class);
    SftpClient mockClient = Mockito.mock(SftpClient.class);
    when(mockProvider.connect(anyObject(), anyObject())).thenThrow(new IOException());
    SftpAuthenticationWrapper wrapper = new SftpAuthenticationWrapper(mockProvider);
    wrapper.setLogExceptions(true);
    wrapper.connect(mockClient, null);
  }


}
