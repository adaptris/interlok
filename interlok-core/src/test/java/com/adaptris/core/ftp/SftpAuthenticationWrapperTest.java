package com.adaptris.core.ftp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.sftp.SftpClient;

public class SftpAuthenticationWrapperTest {

  @Test
  public void testConnect() throws Exception {
    SftpAuthenticationProvider mockProvider = Mockito.mock(SftpAuthenticationProvider.class);
    SftpClient mockClient = Mockito.mock(SftpClient.class);
    when(mockProvider.connect(any(), any())).thenReturn(mockClient);
    SftpAuthenticationWrapper wrapper = new SftpAuthenticationWrapper(mockProvider);
    assertEquals(mockClient, wrapper.connect(mockClient, null));
  }

  @Test
  public void testConnect_AllFailed_NoLogging() throws Exception {
    Assertions.assertThrows(FileTransferException.class, () -> {
      SftpAuthenticationProvider mockProvider = Mockito.mock(SftpAuthenticationProvider.class);
      SftpClient mockClient = Mockito.mock(SftpClient.class);
      when(mockProvider.connect(any(), any())).thenThrow(new IOException());
      SftpAuthenticationWrapper wrapper = new SftpAuthenticationWrapper(mockProvider);
      wrapper.connect(mockClient, null);
    });
  }

  @Test
  public void testConnect_AllFailed_Logging() throws Exception {
    Assertions.assertThrows(FileTransferException.class, () -> {
      SftpAuthenticationProvider mockProvider = Mockito.mock(SftpAuthenticationProvider.class);
      SftpClient mockClient = Mockito.mock(SftpClient.class);
      when(mockProvider.connect(any(), any())).thenThrow(new IOException());
      SftpAuthenticationWrapper wrapper = new SftpAuthenticationWrapper(mockProvider);
      wrapper.setLogExceptions(true);
      wrapper.connect(mockClient, null);
    });
  }

}
