package com.adaptris.tester.runtime.services.preprocessor;

import com.adaptris.tester.utils.SimpleStringSubstitution;
import com.adaptris.tester.utils.UtilityException;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairBag;
import com.adaptris.util.KeyValuePairSet;

import java.util.Map;
import java.util.Properties;

import static com.adaptris.core.varsub.Constants.DEFAULT_VARIABLE_POSTFIX;
import static com.adaptris.core.varsub.Constants.DEFAULT_VARIABLE_PREFIX;

public class VarSubPropsPreprocessor implements Preprocessor {

  private KeyValuePairSet properties;

  public VarSubPropsPreprocessor(){
    properties = new KeyValuePairSet();
  }

  public VarSubPropsPreprocessor(Map<String, String> properties){
    this.properties = new KeyValuePairSet(properties);
  }

  @Override
  public String execute(String input) throws PreprocessorException {
    try {
      SimpleStringSubstitution substitution = new SimpleStringSubstitution();
      return substitution.doSubstitution(input, getKvpAsProperties(), DEFAULT_VARIABLE_PREFIX, DEFAULT_VARIABLE_POSTFIX);
    } catch (UtilityException e){
      throw new PreprocessorException(e);
    }
  }

  public void setProperties(KeyValuePairSet properties) {
    this.properties = properties;
  }

  public KeyValuePairSet getProperties() {
    return properties;
  }

  public Properties getKvpAsProperties() {
    return toProperties(properties);
  }

  private Properties toProperties(KeyValuePairBag bag) {
    Properties result = new Properties();
    for (KeyValuePair kvp : bag) {
      result.put(kvp.getKey(), kvp.getValue());
    }
    return result;
  }
}
