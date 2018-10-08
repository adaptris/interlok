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

package com.adaptris.security.util;

import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.prng.DigestRandomGenerator;
import org.bouncycastle.crypto.prng.SP800SecureRandomBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Security Utilities.
 *
 * @see SecureRandom
 * @author $Author: lchan $
 */
public abstract class SecurityUtil {

  private static boolean initialised = false;

  private static SecureRandom secureRandomInstance;

  /**
   * Add a JCE provider.
   * <p>
   * This also performs the SeedGenerator initialisation, and is only performed
   * once, regardless of how many times this method is invoked
   * </p>
   */
  public static void addProvider() {
    initialise();
  }

  /**
   * Return a SecureRandom implementation.
   * 
   * @return a SecureRandom instance.
   */
  public static SecureRandom getSecureRandom() {
    initialise();
    return secureRandomInstance;
  }

  public static String getAlgorithms() {
    initialise();
    StringBuffer sb = new StringBuffer("Security Algorithms available");
    for (Provider provider : Security.getProviders()) {
      sb.append("\nProvider : " + provider.toString() + "\n");

      Object[] objs = provider.keySet().toArray();
      Arrays.sort(objs);
      for (Object o : objs) {
        sb.append(o.toString() + ", ");
      }
    }
    return sb.toString();
  }

  private static synchronized void initialise() {
    if (initialised) {
      return;
    }
    try {
      Security.addProvider(new BouncyCastleProvider());   
      secureRandomInstance = SecureRandom.getInstanceStrong();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    initialised = true;
  }
}
