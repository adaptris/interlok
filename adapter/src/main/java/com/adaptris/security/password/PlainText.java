package com.adaptris.security.password;

class PlainText extends PasswordImpl {

  public PlainText() {

  }

  public String decode(String encrypted, String charset) {
    return encrypted;
  }

  public String encode(String plainText, String charset) {
    return plainText;
  }
}
