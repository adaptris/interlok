package com.adaptris.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.PayloadInputStreamWrapper;
import com.adaptris.core.common.PayloadOutputStreamWrapper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.text.Conversion;

/**
 * @author mwarman
 */
@SuppressWarnings("deprecation")
public class SymmetricKeyCryptographyServiceTest extends SecurityServiceExample {

  private static final String ALGORITHM = "AES";
  private static final String CIPHER = "AES/CBC/PKCS5Padding";

  private static final String PAYLOAD = "Hello World";

  private byte[] key;
  private byte[] iv;
  private byte[] encryptedPayload;

  @Before
  public void setUp() throws Exception {
    key = generateRandomEncodedByteArray(32);
    iv = generateRandomEncodedByteArray(16);
    encryptedPayload = encrypt(PAYLOAD, key, iv);
  }

  @Test
  public void testDoService_BadConfig() throws Exception {
    try {
      LifecycleHelper.initAndStart(new SymmetricKeyCryptographyService());
      fail();
    } catch (CoreException expected) {

    }
    SymmetricKeyCryptographyService service = new SymmetricKeyCryptographyService()
        .withAlgorithm("BLAH").withCipherTransformation("AES/Blah/Blah")
        .withKey(new ConstantDataInputParameter("123"))
        .withInitialVector(new ConstantDataInputParameter("123"));
    AdaptrisMessage message =
        AdaptrisMessageFactory.getDefaultInstance().newMessage(encryptedPayload);
    try {
      ServiceCase.execute(service, message);
      fail();
    } catch (ServiceException expected) {

    }
  }


  @Test
  public void testDoService() throws Exception {
    SymmetricKeyCryptographyService service = new SymmetricKeyCryptographyService();
    service.setAlgorithm(ALGORITHM);
    service.setCipherTransformation(CIPHER);
    service.setKey(new ConstantDataInputParameter(Conversion.byteArrayToBase64String(key)));
    service.setInitialVector(new ConstantDataInputParameter(Conversion.byteArrayToBase64String(iv)));
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(encryptedPayload);
    ServiceCase.execute(service, message);
    assertEquals(PAYLOAD, message.getContent());
  }

  @Test
  public void testDoServiceEncrypt() throws Exception {
    SymmetricKeyCryptographyService service = new SymmetricKeyCryptographyService();
    service.setAlgorithm(ALGORITHM);
    service.setCipherTransformation(CIPHER);
    service.setOperationMode(SymmetricKeyCryptographyService.OpMode.ENCRYPT);
    service.setKey(new ConstantDataInputParameter(Conversion.byteArrayToBase64String(key)));
    service.setInitialVector(new ConstantDataInputParameter(Conversion.byteArrayToBase64String(iv)));
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    ServiceCase.execute(service, message);
    assertTrue(Arrays.equals(encryptedPayload, message.getPayload()));
  }

  @Test
  public void testDoServiceEncrypt_WithSourceTarget() throws Exception {
    SymmetricKeyCryptographyService service = new SymmetricKeyCryptographyService();
    service.setAlgorithm(ALGORITHM);
    service.setCipherTransformation(CIPHER);
    service.setOperationMode(SymmetricKeyCryptographyService.OpMode.ENCRYPT);
    service.setKey(new ConstantDataInputParameter(Conversion.byteArrayToBase64String(key)));
    service.setInitialVector(new ConstantDataInputParameter(Conversion.byteArrayToBase64String(iv)));
    service.setSource(new PayloadInputStreamWrapper());
    service.setTarget(new PayloadOutputStreamWrapper());
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    ServiceCase.execute(service, message);
    assertTrue(Arrays.equals(encryptedPayload, message.getPayload()));
  }


  @Override
  protected SymmetricKeyCryptographyService retrieveObjectForSampleConfig() {
    return new SymmetricKeyCryptographyService()
        .withAlgorithm(ALGORITHM)
        .withCipherTransformation(CIPHER)
        .withKey(new ConstantDataInputParameter(Conversion.byteArrayToBase64String(generateRandomEncodedByteArray(32))))
        .withInitialVector(new ConstantDataInputParameter(
            Conversion.byteArrayToBase64String(generateRandomEncodedByteArray(16))))
        .withSource(new PayloadInputStreamWrapper()).withTarget(new PayloadOutputStreamWrapper());
  }

  private byte[] generateRandomEncodedByteArray(int size) {
    SecureRandom r = new SecureRandom();
    byte[] byteArray = new byte[size];
    r.nextBytes(byteArray);
    return byteArray;
  }

  private byte[] encrypt(String value, byte[] key, byte[] iv) throws Exception {
    Cipher cipher = Cipher.getInstance(CIPHER);
    SecretKeySpec secretKeySpecy = new SecretKeySpec(key, ALGORITHM);
    IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpecy, ivParameterSpec);
    return cipher.doFinal(value.getBytes());
  }
}