package com.adaptris.tester.runtime.services.preprocessor;


import com.adaptris.core.CoreException;
import com.adaptris.core.varsub.Constants;
import com.adaptris.core.varsub.VariableSubstitutionPreProcessor;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("variable-substitution-preprocessor")
public class VarSubPreprocessor implements Preprocessor {

  @XStreamImplicit
  private List<String> propertyFile;

  public VarSubPreprocessor(){
    setPropertyFile(new ArrayList<String>());
  }

  @Override
  public String execute(String input) throws PreprocessorException {
    try {
      VariableSubstitutionPreProcessor processor = new VariableSubstitutionPreProcessor(createPropertyFileSet());
      return processor.process(input);
    } catch (CoreException e) {
      throw new PreprocessorException("Failed to substitute variables", e);
    }
  }

  public void setPropertyFile(List<String> propertyFile) {
    this.propertyFile = propertyFile;
  }

  public List<String> getPropertyFile() {
    return propertyFile;
  }

  public void addPropertyFile(String propertyFile){
    this.propertyFile.add(propertyFile);
  }

  private KeyValuePairSet createPropertyFileSet() throws PreprocessorException{
    if (propertyFile.size() == 0){
      throw new PreprocessorException("At least one properties file must be set");
    }
    KeyValuePairSet kvp = new KeyValuePairSet();
    for (int i = 0; i < propertyFile.size(); i++){
      kvp.addKeyValuePair(new KeyValuePair(Constants.VARSUB_PROPERTIES_URL_KEY + "." + i, propertyFile.get(i)));
    }
    return kvp;
  }
}
