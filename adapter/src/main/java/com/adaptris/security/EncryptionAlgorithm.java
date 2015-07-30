/*
 * $Author: lchan $
 * $RCSfile: EncryptionAlgorithm.java,v $
 * $Revision: 1.8 $
 * $Date: 2005/08/16 21:43:56 $
 */
package com.adaptris.security;

/**
 * Container class with encryption algorithm information.
 * <p>
 * Its only purpose is as a container for information required by the
 * <code>SecurityService</code> object
 *
 * @author $Author: lchan $
 */
public class EncryptionAlgorithm {

  private String algorithm;
  private Integer keyLength;

  /**
   * @see Object#Object()
   *
   */
  public EncryptionAlgorithm() {
  }

  public EncryptionAlgorithm(String alg) {
    this();
    setAlgorithm(alg);
  }

  /**
   * Constructor.
   *
   * @param s the algorithm
   * @param length The length of the key to use.
   * @see #setAlgorithm(String)
   * @see #setKeyLength(Integer)
   */
  public EncryptionAlgorithm(String s, int length) {
    this();
    setKeyLength(length);
    setAlgorithm(s);
  }

  /**
   * Constructor.
   * 
   * @param s the algorithm
   * @param length The length of the key to use.
   * @see #setAlgorithm(String)
   * @see #setKeyLength(Integer)
   */
  public EncryptionAlgorithm(String s, String length) {
    this(s, Integer.parseInt(length));
  }

  /**
   * Set the algorithm.
   *
   * @param s
   *          The encryption algorithm to use, this is provider specific e.g
   *          "DESede/CBC/PKCS5Padding", (3Des, cipher block chain mode, PKCS5
   *          style padding
   */
  public void setAlgorithm(String s) {
    algorithm = s;
  }

  /**
   * Set the keylength.
   *
   * @param i
   *          the keylength.
   */
  public void setKeyLength(Integer i) {
    keyLength = i;
  }

  /**
   * Return the algorithm.
   *
   * @return the encryption algorithm
   */
  public String getAlgorithm() {
    return algorithm;
  }

  /**
   * return the key length.
   *
   * @return the keylength
   */
  public Integer getKeyLength() {
    return keyLength;
  }

  /**
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "[" + getAlgorithm() + "][" + getKeyLength() + "]";
  }

}