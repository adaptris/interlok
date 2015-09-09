package com.adaptris.security.certificate;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.pkcs.CertificationRequest;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;

import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.exc.CertException;

/**
 * Creating Certificate Signing Requests.
 * 
 * @author $Author: lchan $
 */
public final class CertRequestHandler {
  private CertRequestHandler() {
  }

  /**
   * Create a certificate request based on the provided certificate.
   * <p>
   * The certificate request is returned as a printable string.
   * <p>
   * The default implementation returns a PEM string which is bounded by <code>-----BEGIN NEW CERTIFICATE REQUEST-----</code> and
   * <code>-----END NEW CERTIFICATE REQUEST-----</code> with each line terminated by a \n, e.g.
   * <p>
   * 
   * <pre>
   * {@code 
   * -----BEGIN NEW CERTIFICATE REQUEST-----
   * MIIC1zCCAb8CAQAwgZMxGTAXBgNVBAMTEEdoaWxhaW5lIFd5bnlhcmQxFTATBgNV
   * BAsTDElyaXMgU3VwcG9ydDENMAsGA1UEChMESXJpczEQMA4GA1UEBxMHRGF0Y2hl
   * dDESMBAGA1UECBMJQmVya3NoaXJlMQswCQYDVQQGEwJVSzEdMBsGCSqGSIb3DQEJ
   * ARYOZ2x3QGlyaXMuY28udWswggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIB
   * AQCRQMSX5HCu25aQ+dtozxoGP1QtYC6JXuyGQkQ2MX2JsF1y+WM4NWUvhZvCKjmg
   * jsQL2c/rBzXifZCDRzMuHymOcmWXBaFJJr3b2XZQykzhKZKvTx6X1oU7PPcf+ws0
   * fNRQ3c1ZekPRYTUmSKyswJwIJDmpx4folU348taHwqcnK/LizekgjwrcZwSmFkk6
   * zhGTji2Ris4NMGvm/gDPGGmstxXTQIT62zOP1c61IhkKqxTtMZqmuB2HgQ8MRAza
   * xAxm7uoGbSCgUrWOjb92BHYxvQw8LKKkqKmKWRmDPPQKlBP4iR7vPUqXlb4G/keN
   * jR2EdByCJbkCUCwuVU124lJbAgMBAAEwDQYJKoZIhvcNAQEEBQADggEBADHpHomV
   * ZItNOeXjBDQGWNX9SpA9QV5IvEzm4u5mRI+XFsgYXvybeLMYC6Vrpxl9INVI6hpx
   * Nblq09Cq8lHQusyJNVEW3ibc73T5OZrCSnPTD7DUoKpwLwkDOwze7NHu+7NceUm1
   * pHCdiVe9Q3AC3+qlIdOhXGB3L5/Tn+8rYFNMAV8TQl7yRAz0g4lm+CdXQWaozTLY
   * /1MifSidgYoOq2lCE6l6JsVUv7mBTgaA52GuL0XvfopMOJrEuVUfFy7xVZMqD92L
   * ThcZUaIq5/Z3PUGLi8txXXb0Ga81SkLAHpBljgED0pV06EsrDz/N+12aH75zDcE+
   * Odt/GdYzfpeYvBg=
   * -----END NEW CERTIFICATE REQUEST-----
   * }
   *  </pre>
   * 
   * @param c the certificate
   * @param key the private key
   * @return the PEM encoded string
   * @throws AdaptrisSecurityException if any error occurs
   * @see AdaptrisSecurityException
   */
  public static String createRequest(Certificate c, PrivateKey key)
      throws AdaptrisSecurityException {

    String pemRequest = null;
    ByteArrayOutputStream out = null;
    try {

      CertificationRequest req = createCertRequest(c, key);
      out = new ByteArrayOutputStream();
      out.write("-----BEGIN NEW CERTIFICATE REQUEST-----".getBytes());
      out.write(req.getEncoded());
      out.write("-----END NEW CERTIFICATE REQUEST-----".getBytes());
      out.close();
      pemRequest = out.toString();
    }
    catch (Exception e) {
      throw new CertException(e);
    }
    return pemRequest;
  }

  /**
   * Create a CertificateRequest based on the provided certificate and write it
   * to the supplied outputStream.
   * <p>
   * The default implementation writes out the request as a DER encoded ASN.1
   * data structure
   * 
   * @param c the certificate
   * @param out the OutputStream to write to
   * @param key the Private key
   * @throws AdaptrisSecurityException if any error occurs
   * @see AdaptrisSecurityException
   */
  public static void createRequest(Certificate c, PrivateKey key,
                                   OutputStream out)
      throws AdaptrisSecurityException {
    try {

      CertificationRequest req = createCertRequest(c, key);
      out.write(req.getEncoded());
    }
    catch (Exception e) {
      throw new CertException(e);
    }
    return;
  }

  /**
   * Create a certificate Request.
   */
  private static CertificationRequest createCertRequest(Certificate c,
                                                      PrivateKey key)
      throws Exception {

    X509Certificate x509 = (X509Certificate)c;
    x509.getSigAlgName();

        X500Name entityName = new X500Name(x509.getSubjectDN().getName());
        KeyPair entityPair = KeyPairGenerator.getInstance("RSA").genKeyPair();
        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(x509.getPublicKey().getEncoded());
        // Generate the certificate signing request
        PKCS10CertificationRequestBuilder csrBuilder = new PKCS10CertificationRequestBuilder(entityName, publicKeyInfo);
//        // SCEP servers usually require a challenge password
//        csrBuilder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_challengePassword, new DERPrintableString(new String(
//                "password".toCharArray())));
        JcaContentSignerBuilder builder = new JcaContentSignerBuilder(x509.getSigAlgName());
        PKCS10CertificationRequest csr = csrBuilder.build(builder.build(entityPair.getPrivate()));


    
//    CertificateRequest certRequest = new CertificateRequest(
//        x509.getPublicKey(), (Name) x509.getSubjectDN());
//
//    certRequest.sign(x509.getSignatureAlgorithm(), key);
    return csr.toASN1Structure();
  }
}