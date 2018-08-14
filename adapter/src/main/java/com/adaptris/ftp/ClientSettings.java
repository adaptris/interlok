/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.ftp;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;

public abstract class ClientSettings {
  /**
   * Additional settings on the {@code FTPClient}.
   * 
   */
  public static enum FTP implements Setter<FTPClient> {

    /**
     * Maps to {@code FTPClient#setActiveExternalIPAddress(String)}
     * 
     */
    ActiveExternalIPAddress() {
      @Override
      public void set(FTPClient s, String setting) throws IOException {
        s.setActiveExternalIPAddress(setting);
      }
    },
    /**
     * Maps to {@code FTPClient#setAutodetectUTF8(boolean)}
     * 
     */
    AutodetectUTF8() {
      public void set(FTPClient s, String setting) throws IOException {
        s.setAutodetectUTF8(BooleanUtils.toBoolean(setting));
      }
    },
    /**
     * Maps to {@code FTPClient#setControlEncoding(String)}
     * 
     */
    ControlEncoding() {
      public void set(FTPClient s, String setting) throws IOException {
        s.setControlEncoding(setting);
      }
    },
    /**
     * Maps to {@code FTPClient#setListHiddenFiles(boolean)}
     * 
     */
    ListHiddenFiles() {
      public void set(FTPClient s, String setting) throws IOException {
        s.setListHiddenFiles(BooleanUtils.toBoolean(setting));
      }
    },
    /**
     * Maps to {@code FTPClient#setPassiveNatWorkaround(boolean)}
     * 
     */
    PassiveNatWorkaround() {
      @Override
      @SuppressWarnings("deprecation")
      public void set(FTPClient s, String setting) throws IOException {
        s.setPassiveNatWorkaround(BooleanUtils.toBooleanObject(setting).booleanValue());
      }

    },
    /**
     * Maps to {@code FTPClient#setPassiveLocalIPAddress(String)}.
     * 
     */
    PassiveLocalIPAddress() {

      @Override
      public void set(FTPClient s, String setting) throws IOException {
        s.setPassiveLocalIPAddress(setting);
      }

    },
    /**
     * Maps to {@code FTPClient#setReceieveDataSocketBufferSize(int)}
     * 
     */
    ReceiveDataSocketBufferSize() {
      @Override
      public void set(FTPClient s, String setting) throws IOException {
        s.setReceieveDataSocketBufferSize(Integer.parseInt(setting));
      }
    },
    /**
     * Maps to {@code FTPClient#setRemoteVerificationEnabled(boolean)}.
     * 
     */
    RemoteVerificationEnabled() {
      @Override
      public void set(FTPClient s, String setting) throws IOException {
        s.setRemoteVerificationEnabled(BooleanUtils.toBoolean(setting));
      }

    },
    /**
     * Maps to {@code FTPClient#setReportActiveExternalIPAddress(String)}.
     * 
     */
    ReportActiveExternalIPAddress() {
      @Override
      public void set(FTPClient s, String setting) throws IOException {
        s.setReportActiveExternalIPAddress(setting);
      }

    },
    /**
     * Maps to {@code FTPClient#setSendDataSocketBufferSize(int)}
     * 
     */
    SendDataSocketBufferSize() {
      @Override
      public void set(FTPClient s, String setting) throws IOException {
        s.setSendDataSocketBufferSize(Integer.parseInt(setting));
      }
    },
    /**
     * Maps to {@code FTPClient#setStrictMultilineParsing(boolean)}
     * 
     */
    StrictMultilineParsing() {
      @Override
      public void set(FTPClient s, String setting) throws IOException {
        s.setStrictMultilineParsing(BooleanUtils.toBoolean(setting));
      }
    },
    /**
     * Maps to {@code FTPClient#setStrictReplyParsing(boolean)}
     * 
     */
    StrictReplyParsing() {
      @Override
      public void set(FTPClient s, String setting) throws IOException {
        s.setStrictReplyParsing(BooleanUtils.toBoolean(setting));
      }
    },
    /**
     * Maps to {@code FTPClient#setUseEPSVwithIPv4(boolean)}
     * 
     */
    UseEPSVwithIPv4() {
      @Override
      public void set(FTPClient s, String setting) throws IOException {
        s.setUseEPSVwithIPv4(BooleanUtils.toBoolean(setting));
      }
    };
  }

  /**
   * Additional settings on the {@code FTPSClient}.
   * 
   */
  public static enum FTPS implements Setter<FTPSClient> {
    /**
     * Maps to {@code FTPSClient#setAuthValue(String)}.
     * 
     */
    AuthValue() {
      @Override
      public void set(FTPSClient s, String setting) throws IOException {
        s.setAuthValue(setting);
      }
    },
    /**
     * Maps to {@code FTPSClient#setEnabledCipherSuites(String[])}; use a comma-separated string.
     * 
     */
    EnabledCipherSuites() {
      @Override
      public void set(FTPSClient s, String setting) throws IOException {
        s.setEnabledCipherSuites(setting.split(","));
      }

    },
    /**
     * Maps to {@code FTPSClient#setEnabledProtocols(String[])}; use a comma-separated string.
     * 
     */
    EnabledProtocols() {
      @Override
      public void set(FTPSClient s, String setting) throws IOException {
        s.setEnabledProtocols(setting.split(","));
      }

    },
    /**
     * Maps to {@code FTPSClient#setEnabledSessionCreation(boolean)}.
     * 
     */
    EnabledSessionCreation() {
      @Override
      public void set(FTPSClient s, String setting) throws IOException {
        s.setEnabledSessionCreation(BooleanUtils.toBoolean(setting));
      }
    },
    /**
     * Maps to {@code FTPSClient#setEndpointCheckingEnabled(boolean)}.
     * 
     */
    EndpointCheckingEnabled() {
      @Override
      public void set(FTPSClient s, String setting) throws IOException {
        s.setEndpointCheckingEnabled(BooleanUtils.toBoolean(setting));
      }
    },
    /**
     * Maps to {@code FTPSClient#setNeedClientAuth(boolean)}.
     * 
     */
    NeedClientAuth() {
      @Override
      public void set(FTPSClient s, String setting) throws IOException {
        s.setNeedClientAuth(BooleanUtils.toBoolean(setting));
      }
    },
    /**
     * Maps to {@code FTPSClient#setUseClientMode(boolean)}.
     * 
     */
    UseClientMode() {
      @Override
      public void set(FTPSClient s, String setting) throws IOException {
        s.setUseClientMode(BooleanUtils.toBoolean(setting));
      }
    },
    /**
     * Maps to {@code FTPSClient#setWantClientAuth(boolean)}.
     * 
     */
    WantClientAuth() {
      @Override
      public void set(FTPSClient s, String setting) throws IOException {
        s.setWantClientAuth(BooleanUtils.toBoolean(setting));
      }
    };
  }

  public static <T extends FTPClient> void preConnectSettings(T client, Setter<T>[] setters,
                                                              Map<String, String> additionalSettings) {
    additionalSettings.entrySet().forEach(setting -> {
      Arrays.asList(setters).forEach(s -> {
        if (setting.getKey().equalsIgnoreCase(s.name())) {
          apply(s, client, setting.getValue());
        }
      });
    });
  }

  private static <T extends FTPClient> void apply(Setter setter, T client, String value) {
    try {
      setter.set(client, value);
    } catch (Exception e) {
      throw new InvalidSettingException(e);
    }
  }

  public interface Setter<T extends FTPClient> {
    void set(T s, String setting) throws IOException;

    String name();
  }

  private static class InvalidSettingException extends RuntimeException {
    protected InvalidSettingException(Exception e) {
      super(e);
    }
  }
}
