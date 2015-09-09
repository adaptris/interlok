package com.adaptris.security.certificate;

import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.util.Constants;

/**
 * Convenience wrapper for verifiying Certificate chains.
 * 
 * @author lchan
 */
public class CertificatePathVerifier {

  private static final String COLLECTION = "Collection";
  private static final String PKIX = "PKIX";

  private static transient Logger logR = LoggerFactory.getLogger(CertificatePathVerifier.class);

  /**
   * Verifies the specified certificate chain against the trusted anchors. The
   * trusted anchors contains all public certificate that is trusted. This
   * method will make use of JDK1.4's utilities to verify the certificate chain.
   * 
   * @param certs the certificate chain being verified
   * @param trusted the keystore storing the trusted anchors.
   * @return true if verification is succeeded; false otherwise
   */
  public static boolean verify(Certificate[] certs, KeyStore trusted) {
    if (trusted == null) {
      logR.warn("trusted keystore is null, cert chain verification fails.");
      return false;
    }
    if (certs == null || certs.length == 0) {
      logR.debug("Verifying a zero length certificate chain as [true]");
      return true;
    }
    try {
      CertPathBuilder certPathBuilder = CertPathBuilder.getInstance(PKIX);

      X509CertSelector targetConstraints = new X509CertSelector();

      for (int i = 0; i < certs.length; i++) {
        targetConstraints.setSubject(((X509Certificate) certs[i])
            .getSubjectX500Principal().getEncoded());
      }
      PKIXBuilderParameters params = new PKIXBuilderParameters(trusted,
          targetConstraints);

      List<Certificate> certsList = new ArrayList<Certificate>();
      for (int i = 0; i < certs.length; i++) {
        certsList.add(certs[i]);
      }
      CollectionCertStoreParameters ccsp = new CollectionCertStoreParameters();
      CertStore store = CertStore.getInstance(COLLECTION, ccsp);
      params.addCertStore(store);

      CertPath certPath = certPathBuilder.build(params).getCertPath();
      if (certPath == null) {
        throw new AdaptrisSecurityException("Failed to get certificate path");
      }
    }
    catch (Exception e) {
      if (Constants.DEBUG) {
        logR.debug("Failed to verify certificate chain due "
            + "to underlying exception", e);
      }
      return false;
    }
    return true;
  }
}
