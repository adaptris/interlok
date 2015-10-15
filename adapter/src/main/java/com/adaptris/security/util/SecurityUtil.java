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

import org.apache.commons.logging.Log;
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
public final class SecurityUtil {

  private static boolean initialised = false;

  private static SecureRandom secureRandomInstance;

  /** Default constructor */
  private SecurityUtil() {
  }

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

  /**
   * Print out all the algorithms provided by each provider to the specified
   * logger.
   *
   * @param logR the Log to be used for this output.
   */
  public static void printAlgorithms(Log logR) {
    if (logR.isDebugEnabled() && Constants.DEBUG) {
      logR.debug(getAlgorithms());
    }
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

    Security.addProvider(new BouncyCastleProvider());
    
    initSecureRandom();
    initialised = true;
    return;
  }

  private static void initSecureRandom() {
    SP800SecureRandomBuilder rngb = new SP800SecureRandomBuilder();
    SHA384Digest sha384 = new SHA384Digest();
    byte[] bytes = new byte[sha384.getDigestSize()];
    new DigestRandomGenerator(sha384).nextBytes(bytes);
    secureRandomInstance = rngb.buildHash(sha384, bytes, true);
  }
}
