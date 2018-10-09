package com.adaptris.core.security;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.util.stream.StreamUtil;
import com.adaptris.util.text.Conversion;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.hibernate.validator.constraints.NotBlank;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

/**
 * @config symmetric-key-cryptography-service
 * @author mwarman
 */
@XStreamAlias("symmetric-key-cryptography-service")
@AdapterComponent
@ComponentProfile(summary = "Encrypts or Decrypts payload using key and initial vector", tag = "service,cryptography")
@DisplayOrder(order = {"algorithm", "cipherTransformation", "operationMode", "key", "initialVector"})
public class SymmetricKeyCryptographyService extends ServiceImp {

  @NotBlank
  @Valid
  private String algorithm;

  @NotBlank
  @Valid
  private String cipherTransformation;

  @NotNull
  @Valid
  private OpMode operationMode = OpMode.DECRYPT;
  
  @NotNull
  @Valid
  private DataInputParameter<String> key;

  @NotNull
  @Valid
  private DataInputParameter<String> initialVector;


  public enum OpMode {
    DECRYPT {
      @Override
      void execute(Cipher cipher, SecretKeySpec secretKeySpec, IvParameterSpec ivParameterSpec, AdaptrisMessage msg) throws InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        StreamUtil.copyAndClose(new CipherInputStream(msg.getInputStream(), cipher), msg.getOutputStream());
      }
    },
    ENCRYPT {
      @Override
      void execute(Cipher cipher, SecretKeySpec secretKeySpec, IvParameterSpec ivParameterSpec, AdaptrisMessage msg) throws InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        StreamUtil.copyAndClose(msg.getInputStream(), new CipherOutputStream(msg.getOutputStream(), cipher));
      }
    };
    abstract void execute(Cipher cipher, SecretKeySpec secretKeySpec, IvParameterSpec ivParameterSpec, AdaptrisMessage msg) throws InvalidAlgorithmParameterException, InvalidKeyException, IOException;
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      byte[] keyBytes =  Conversion.base64StringToByteArray(getKey().extract(msg));
      byte[] initialVectorBytes =  Conversion.base64StringToByteArray(getInitialVector().extract(msg));
      Cipher cipher = Cipher.getInstance(getCipherTransformation());
      SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, getAlgorithm());
      IvParameterSpec ivParameterSpec = new IvParameterSpec(initialVectorBytes);
      getOperationMode().execute(cipher, secretKeySpec, ivParameterSpec, msg);
    } catch (Exception e) {
      ExceptionHelper.rethrowServiceException(e);
    }
  }

  @Override
  protected void initService() throws CoreException {

  }

  @Override
  protected void closeService() {

  }

  @Override
  public void prepare() throws CoreException {

  }

  /**
   *
   * @param algorithm the name of the secret-key algorithm to be associated with the given key.
   */
  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public SymmetricKeyCryptographyService withAlgorithm(String algorithm){
    setAlgorithm(algorithm);
    return this;
  }

  /**
   *
   * @param cipherTransformation the name of the transformation, e.g., AES/CBC/PKCS5Padding.
   */
  public void setCipherTransformation(String cipherTransformation) {
    this.cipherTransformation = cipherTransformation;
  }

  public String getCipherTransformation() {
    return cipherTransformation;
  }

  public SymmetricKeyCryptographyService withCipherTransformation(String cipherTransformation){
    setCipherTransformation(cipherTransformation);
    return this;
  }

  /**
   *
   * @param initialVector Base64 encoded string of initial vector bytes.
   */
  public void setInitialVector(DataInputParameter<String> initialVector) {
    this.initialVector = initialVector;
  }

  public DataInputParameter<String> getInitialVector() {
    return initialVector;
  }

  public SymmetricKeyCryptographyService withInitialVector(DataInputParameter<String> initialVector){
    setInitialVector(initialVector);
    return this;
  }

  /**
   *
   * @param key  Base64 encoded string of key bytes.
   */
  public void setKey(DataInputParameter<String> key) {
    this.key = key;
  }

  public DataInputParameter<String> getKey() {
    return key;
  }

  public SymmetricKeyCryptographyService withKey(DataInputParameter<String> key){
    setKey(key);
    return this;
  }

  /**
   *
   * @param mode the operation mode of the cipher: ENCRYPT or DECRYPT (default: DECRYPT)
   */
  public void setOperationMode(OpMode mode) {
    this.operationMode = mode;
  }

  public OpMode getOperationMode() {
    return operationMode;
  }
}

