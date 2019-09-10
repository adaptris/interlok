package com.adaptris.core.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.interlok.types.MessageWrapper;
import com.adaptris.security.password.Password;
import com.adaptris.security.util.SecurityUtil;
import com.adaptris.util.stream.StreamUtil;
import com.adaptris.util.text.Conversion;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @config symmetric-key-cryptography-service
 * @author mwarman
 */
@XStreamAlias("symmetric-key-cryptography-service")
@AdapterComponent
@ComponentProfile(summary = "Encrypts or Decrypts payload using key and initial vector", tag = "service,cryptography")
@DisplayOrder(order = {"algorithm", "cipherTransformation", "operationMode", "key", "initialVector", "source", "target"})
public class SymmetricKeyCryptographyService extends ServiceImp {

  @NotBlank
  @Valid
  @InputFieldHint(expression = true)
  private String algorithm;

  @NotBlank
  @Valid
  @InputFieldHint(expression = true)
  private String cipherTransformation;

  @NotNull
  @Valid
  @AutoPopulated
  @InputFieldDefault(value = "DECRYPT")
  private OpMode operationMode;
  
  @NotNull
  @Valid
  private DataInputParameter<String> key;

  @NotNull
  @Valid
  private DataInputParameter<String> initialVector;

  @Valid
  @InputFieldDefault(value = "the payload")
  private MessageWrapper<InputStream> source;

  @Valid
  @InputFieldDefault(value = "the payload")
  private MessageWrapper<OutputStream> target;

  public enum OpMode {
    DECRYPT {
      @Override
      void execute(Cipher cipher, SecretKeySpec secretKeySpec, IvParameterSpec ivParameterSpec,
          InputStream msgIn, OutputStream msgOut)
          throws InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        try (CipherInputStream in = new CipherInputStream(msgIn, cipher);
            OutputStream out = msgOut) {
          StreamUtil.copyAndClose(in, out);
        }
      }
    },
    ENCRYPT {
      @Override
      void execute(Cipher cipher, SecretKeySpec secretKeySpec, IvParameterSpec ivParameterSpec,
          InputStream msgIn, OutputStream msgOut)
          throws InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        try (InputStream in = msgIn;
            CipherOutputStream out = new CipherOutputStream(msgOut, cipher)) {
          StreamUtil.copyAndClose(in, out);
        }
      }
    };
    abstract void execute(Cipher cipher, SecretKeySpec secretKeySpec,
        IvParameterSpec ivParameterSpec, InputStream msgIn, OutputStream msgOut)
        throws InvalidAlgorithmParameterException, InvalidKeyException, IOException;
  }


  public SymmetricKeyCryptographyService() {
    SecurityUtil.addProvider();
    setOperationMode(OpMode.DECRYPT);
  }


  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      String algToUse = msg.resolve(getAlgorithm());
      String cipherToUse = msg.resolve(getCipherTransformation());
      byte[] keyBytes = Conversion.base64StringToByteArray(
          Password.decode(ExternalResolver.resolve(getKey().extract(msg))));
      byte[] initialVectorBytes =  Conversion.base64StringToByteArray(getInitialVector().extract(msg));
      Cipher cipher = Cipher.getInstance(cipherToUse);
      SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, algToUse);
      IvParameterSpec ivParameterSpec = new IvParameterSpec(initialVectorBytes);
      getOperationMode().execute(cipher, secretKeySpec, ivParameterSpec, source().wrap(msg),
          target().wrap(msg));
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
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
    try {
      Args.notBlank(getAlgorithm(), "algorithm");
      Args.notBlank(getCipherTransformation(), "cipherTransformation");
      Args.notNull(getKey(), "key");
      Args.notNull(getInitialVector(), "initalVector");
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  /**
   * Set the name of the secret-key algorithm.
   * <p>
   * This value is passed in as one of the parameters to
   * {@link SecretKeySpec#SecretKeySpec(byte[], String)}; while it is free text, the correct value
   * will depend on what you have agreed with the remote party and support within your JVM.
   * </p>
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
   * Set the cipher transformation to be applied
   * <p>
   * This value is passed into {@link Cipher#getInstance(String)}; while it is free text, the
   * correct value will depend on what you have agreed with the remote party and support within your
   * JVM.
   * </p>
   *
   * @param cipherTransformation the name of the transformation, e.g. {@code AES/CBC/PKCS5Padding}.
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
   * Set the initial vector for the algorithm
   * 
   * <p>
   * Depending on the algorithm you have chosen, then size of the initial vector will vary. For
   * instance, for AES, it needs to be 16 bytes
   * </p>
   * 
   * @param initialVector the Base64 encoded string of initial vector bytes.
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
   * Set the initial key the service
   * 
   * <p>
   * Depending on the algorithm you have chosen, then size of the key will vary. For instance, for
   * AES, it needs to be 32 bytes
   * </p>
   * 
   * @param key Base64 encoded string of key bytes.
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

  public MessageWrapper<InputStream> getSource() {
    return source;
  }

  /**
   * Set the source for the input of the crypto service.
   * 
   * @param source the source.
   */
  public void setSource(MessageWrapper<InputStream> source) {
    this.source = source;
  }

  public SymmetricKeyCryptographyService withSource(MessageWrapper<InputStream> source) {
    setSource(source);
    return this;
  }

  private MessageWrapper<InputStream> source() {
    return ObjectUtils.defaultIfNull(getSource(), (msg) -> {
      return msg.getInputStream();
    });
  }

  public MessageWrapper<OutputStream> getTarget() {
    return target;
  }

  /**
   * Set the target for the output of the crypto service.
   * 
   * @param target the target.
   */
  public void setTarget(MessageWrapper<OutputStream> target) {
    this.target = target;
  }

  public SymmetricKeyCryptographyService withTarget(MessageWrapper<OutputStream> target) {
    setTarget(target);
    return this;
  }

  private MessageWrapper<OutputStream> target() {
    return ObjectUtils.defaultIfNull(getTarget(), (msg) -> {
      return msg.getOutputStream();
    });
  }

}

