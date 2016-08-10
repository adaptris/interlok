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

package com.adaptris.core.transform;

import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BaseCase;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.transform.validate.IsNullContentValidation;
import com.adaptris.transform.validate.NotInListContentValidation;
import com.adaptris.transform.validate.NotNullContentValidation;
import com.adaptris.transform.validate.RegexpContentValidation;
import com.adaptris.transform.validate.SimpleListContentValidation;
import com.adaptris.transform.validate.ValidationStage;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class XmlRuleValidatorTest extends BaseCase {

  static final String XML_FAMILY_TREE = "<document>\n" + "  <parent id=\"Cronus\">\n"
      + "    <child mother=\"Rhea\"><name>Zeus</name></child>\n" + "    <child mother=\"Rhea\"><name>Demeter</name></child>\n"
      + "    <child mother=\"Rhea\"><name>Hades</name></child>\n" + "    <child mother=\"Rhea\"><name>Hera</name></child>\n"
      + "  </parent>\n" + "  <parent id=\"Coeus\">\n" + "    <child mother=\"Phoebe\"><name>Asteria</name></child>\n"
      + "    <child mother=\"Phoebe\"><name>Leto</name></child>\n" + "  </parent>\n" + "  <parent id=\"Hyperion\">\n"
      + "    <child mother=\"Theia\"><name>Helios</name></child>\n" + "    <child mother=\"Theia\"><name>Selene</name></child>\n"
      + "    <child mother=\"Theia\"><name>Eos</name></child>\n" + "  </parent>\n" + "  <parent id=\"Zeus\">\n"
      + "    <child mother=\"Leto\"><name>Apollo</name></child>\n" + "    <child mother=\"Leto\"><name>Artemis</name></child>\n"
      + "    <child mother=\"Hera\"><name>Hebe</name></child>\n" + "    <child mother=\"Hera\"><name>Ares</name></child>\n"
      + "    <child mother=\"Hera\"><name>Hephaestus</name></child>\n"
      + "    <child mother=\"Dione\"><name>Aphrodite</name></child>\n"
      + "    <child mother=\"Demeter\"><name>Persephone</name></child>\n" + "  </parent>\n" + "  <parent id=\"Hades\">\n"
      + "  </parent>\n" + "</document>\n";

  static final String XML_WITH_NAMESEPACE = "<document xmlns:titan=\"http://www.adaptris.com/titans\" "
      + "xmlns:olympian=\"http://www.adaptris.com/olympians\">\n" + " <titan:names>\n" + "  <titan:name>Hyperion</titan:name>\n"
      + "  <titan:name>Iapetus</titan:name>\n" + "  <titan:name>Coeus</titan:name>\n" + "  <titan:name>Krios</titan:name>\n"
      + "  <titan:name>Cronus</titan:name>\n" + "  <titan:name>Mnemosyne</titan:name>\n" + "  <titan:name>Oceanus</titan:name>\n"
      + "  <titan:name>Phoebe</titan:name>\n" + "  <titan:name>Rhea</titan:name>\n" + "  <titan:name>Tethys</titan:name>\n"
      + "  <titan:name>Theia</titan:name>\n" + "  <titan:name>Themis</titan:name>\n" + " </titan:names>\n" + " <olympian:names>\n"
      + "  <olympian:name>Aphrodite</olympian:name>\n" + "  <olympian:name>Apollo</olympian:name>\n"
      + "  <olympian:name>Ares</olympian:name>\n" + "  <olympian:name>Artemis</olympian:name>\n"
      + "  <olympian:name>Athena</olympian:name>\n" + "  <olympian:name>Demeter</olympian:name>\n"
      + "  <olympian:name>Dionysos</olympian:name>\n" + "  <olympian:name>Hades</olympian:name>\n"
      + "  <olympian:name>Hephaestus</olympian:name>\n" + "  <olympian:name>Hera</olympian:name>\n"
      + "  <olympian:name>Hermes</olympian:name>\n" + "  <olympian:name>Hestia</olympian:name>\n"
      + "  <olympian:name>Poseidon</olympian:name>\n" + "  <olympian:name>Zeus</olympian:name>\n" + " </olympian:names>\n"
      + "</document>\n";

  static final String[] THE_TWELVE_TITANS =
  {
      "Hyperion", "Iapetus", "Coeus", "Krios", "Cronus", "Mnemosyne", "Oceanus", "Phoebe", "Rhea", "Tethys", "Theia", "Themis"
  };

  static final String[] CHILDREN_OF_HERA =
  {
      "Hebe", "Ares", "Hephaestus"
  };

  static final String[] CHILDREN_OF_RHEA = new String[]
  {
      "Zeus", "Demeter", "Hades", "Hera"
  };

  static final String[] CHILDREN_OF_LETO = new String[]
  {
      "Apollo", "Artemis"
  };

  static final String XPATH_CHILDREN_OF_LETO = "child[@mother='Leto']/name";
  static final String XPATH_CHILDREN_OF_RHEA = "child[@mother='Rhea']/name";
  static final String XPATH_ITERATION_CHILDREN_OF_CRONOS = "/document/parent[@id='Cronos']";
  static final String XPATH_ITERATION_CHILDREN_OF_HADES = "/document/parent[@id='Hades']";
  static final String XPATH_CHILDREN_OF_DEMETER = "child[@mother='Demeter']/name";
  static final String XPATH_CHILD_NAME = "child/name";
  static final String XPATH_CHILDREN_OF_HERA = "child[@mother='Hera']/name";
  static final String XPATH_ITERATION_CHILDREN_OF_ZEUS = "/document/parent[@id='Zeus']";

  static final String XPATH_NS_OLYMPIAN_NAME = "olympian:name";
  static final String XPATH_NS_ITERATION_OLYMPIAN_NAMES = "/document/olympian:names";
  static final String XPATH_NS_TITAN_NAME = "titan:name";
  static final String XPATH_NS_ITERATION_TITAN_NAMES = "/document/titan:names";

  public XmlRuleValidatorTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testSetNamespaceContext() {
    XmlRuleValidator obj = new XmlRuleValidator();
    assertNull(obj.getNamespaceContext());
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("hello", "world"));
    obj.setNamespaceContext(kvps);
    assertEquals(kvps, obj.getNamespaceContext());
    obj.setNamespaceContext(null);
    assertNull(obj.getNamespaceContext());
  }

  public void testContentValidationNonXmlMessage() throws Exception {
    XmlRuleValidator validator = new XmlRuleValidator();
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("ABCDEFG");
    XmlValidationService service = new XmlValidationService(validator);
    try {
      ServiceCase.execute(service, msg);
      fail("RuleValidationService success when expecting failure");
    }
    catch (ServiceException expected) {
    }
  }

  public void testContentValidation_XmlMessage_BadXpath() throws Exception {
    XmlRuleValidator validator = new XmlRuleValidator();
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_FAMILY_TREE);
    ValidationStage vs = new ValidationStage("/path/to/something/that/does/not/exist", XPATH_CHILDREN_OF_HERA,
        new NotNullContentValidation());
    vs.setFailOnIterateFailure(true);
    validator.addValidationStage(vs);
    XmlValidationService service = new XmlValidationService(validator);
    try {
      ServiceCase.execute(service, msg);
      fail("RuleValidationService success when expecting failure");
    }
    catch (ServiceException expected) {
    }
  }

  public void testRegexpContentValidation_DataDoesNotMatch() throws Exception {
    XmlRuleValidator validator = new XmlRuleValidator();
    validator.addValidationStage(new ValidationStage(XPATH_ITERATION_CHILDREN_OF_ZEUS, XPATH_CHILDREN_OF_HERA,
        new RegexpContentValidation("[0-9]+")));

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_FAMILY_TREE);
    XmlValidationService service = new XmlValidationService(validator);
    try {
      ServiceCase.execute(service, msg);
      fail("RuleValidationService success when expecting failure");
    }
    catch (ServiceException expected) {
    }
    finally {
      ServiceCase.stop(service);
    }
  }

  public void testRegexpContentValidation_DataMatches() throws Exception {
    XmlRuleValidator validator = new XmlRuleValidator();
    validator.addValidationStage(new ValidationStage(XPATH_ITERATION_CHILDREN_OF_ZEUS, XPATH_CHILDREN_OF_HERA,
        new RegexpContentValidation("[a-zA-Z]+")));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_FAMILY_TREE);
    XmlValidationService service = new XmlValidationService(validator);
    try {
      ServiceCase.start(service);
      service.doService(new DefaultMessageFactory().newMessage(XML_FAMILY_TREE));
      service.doService(new DefaultMessageFactory().newMessage(XML_FAMILY_TREE));
    }
    catch (ServiceException e) {
      fail("RuleValidationService failure when expecting success");
    }
    finally {
      ServiceCase.stop(service);
    }
  }

  public void testNotInListContentValidation_DataInList() throws Exception {
    XmlRuleValidator validator = new XmlRuleValidator();
    validator.addValidationStage(new ValidationStage(XPATH_ITERATION_CHILDREN_OF_ZEUS, XPATH_CHILDREN_OF_HERA,
        new NotInListContentValidation(CHILDREN_OF_HERA)));

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_FAMILY_TREE);
    XmlValidationService service = new XmlValidationService(validator);
    try {
      ServiceCase.execute(service, msg);
      fail("RuleValidationService success when expecting failure");
    }
    catch (ServiceException expected) {
    }
  }

  public void testNotInListContentValidation_DataNotInList() throws Exception {
    XmlRuleValidator validator = new XmlRuleValidator();
    validator.addValidationStage(new ValidationStage(XPATH_ITERATION_CHILDREN_OF_ZEUS, XPATH_CHILDREN_OF_HERA,
        new NotInListContentValidation(CHILDREN_OF_RHEA)));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_FAMILY_TREE);
    XmlValidationService service = new XmlValidationService(validator);
    try {
      ServiceCase.execute(service, msg);
    }
    catch (ServiceException expected) {
      fail("RuleValidationService failure when expecting success");
    }
  }

  public void testListContentValidation_DataInList() throws Exception {
    XmlRuleValidator validator = new XmlRuleValidator();
    validator.setXmlDocumentFactoryConfig(DocumentBuilderFactoryBuilder.newInstance());
    validator.setValidationStages(Arrays.asList(new ValidationStage[]
    {
      new ValidationStage(XPATH_ITERATION_CHILDREN_OF_ZEUS, XPATH_CHILDREN_OF_HERA, new SimpleListContentValidation(
          CHILDREN_OF_HERA))
    }));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_FAMILY_TREE);
    XmlValidationService service = new XmlValidationService(validator);
    try {
      ServiceCase.execute(service, msg);
    }
    catch (ServiceException e) {
      fail("RuleValidationService failure when expecting success");
    }
  }

  public void testListContentValidation_WithNamespace() throws Exception {
    XmlRuleValidator validator = new XmlRuleValidator();
    validator.setNamespaceContext(createNamespaceHolder());
    ValidationStage vs = new ValidationStage(XPATH_NS_ITERATION_TITAN_NAMES, XPATH_NS_TITAN_NAME, new SimpleListContentValidation(
        THE_TWELVE_TITANS), new NotNullContentValidation());
    validator.addValidationStage(vs);

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_WITH_NAMESEPACE);
    XmlValidationService service = new XmlValidationService(validator);
    try {
      ServiceCase.execute(service, msg);
    }
    catch (ServiceException e) {
      fail("RuleValidationService failure when expecting success");
    }
  }

  public void testListContentValidation_DataNotInList() throws Exception {
    XmlRuleValidator validator = new XmlRuleValidator();
    validator.addValidationStage(new ValidationStage(XPATH_ITERATION_CHILDREN_OF_ZEUS, XPATH_CHILDREN_OF_DEMETER,
        new SimpleListContentValidation(CHILDREN_OF_HERA)));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_FAMILY_TREE);
    XmlValidationService service = new XmlValidationService(validator);
    try {
      ServiceCase.execute(service, msg);
      fail("RuleValidationService success when expecting exception");
    }
    catch (ServiceException e) {
      ; // expected
    }
  }

  public void testListContentValidation_WithNamespaceDataNotInList() throws Exception {
    XmlRuleValidator validator = new XmlRuleValidator();
    validator.setNamespaceContext(createNamespaceHolder());
    ValidationStage vs = new ValidationStage(XPATH_NS_ITERATION_OLYMPIAN_NAMES, XPATH_NS_OLYMPIAN_NAME,
        new SimpleListContentValidation(THE_TWELVE_TITANS), new NotNullContentValidation());
    validator.addValidationStage(vs);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_WITH_NAMESEPACE);
    XmlValidationService service = new XmlValidationService(validator);
    try {
      ServiceCase.execute(service, msg);
      fail("RuleValidationService success when expecting exception");
    }
    catch (ServiceException e) {
      ; // expected
    }
  }

  public void testIsNullContentValidation_DataNotNull() throws Exception {
    XmlRuleValidator validator = new XmlRuleValidator();
    validator.addValidationStage(new ValidationStage(XPATH_ITERATION_CHILDREN_OF_ZEUS, XPATH_CHILD_NAME,
        new IsNullContentValidation()));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_FAMILY_TREE);
    XmlValidationService service = new XmlValidationService(validator);
    try {
      ServiceCase.execute(service, msg);
      fail("RuleValidationService sucess when expecting failure");
    }
    catch (ServiceException e) {
    }
  }

  public void testIsNullContentValidation_DataIsNull() throws Exception {
    XmlRuleValidator validator = new XmlRuleValidator();
    // Hades had no children; poor god.
    ValidationStage vs = new ValidationStage(XPATH_ITERATION_CHILDREN_OF_HADES, XPATH_CHILD_NAME, new IsNullContentValidation());
    validator.addValidationStage(vs);

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_FAMILY_TREE);
    XmlValidationService service = new XmlValidationService(validator);
    try {
      ServiceCase.execute(service, msg);
    }
    catch (ServiceException e) {
      fail("RuleValidationService failure when expecting success");
    }
  }

  public void testNotNullContentValidation_DataNotNull() throws Exception {
    XmlRuleValidator validator = new XmlRuleValidator();
    validator.addValidationStage(new ValidationStage(XPATH_ITERATION_CHILDREN_OF_ZEUS, XPATH_CHILD_NAME,
        new NotNullContentValidation()));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_FAMILY_TREE);
    XmlValidationService service = new XmlValidationService(validator);
    try {
      ServiceCase.execute(service, msg);
    }
    catch (ServiceException e) {
      fail("RuleValidationService failure when expecting success");
    }
  }

  public void testNotNullContentValidation_DataIsNull() throws Exception {
    XmlRuleValidator validator = new XmlRuleValidator();
    // Hades had no children; poor god.
    ValidationStage vs = new ValidationStage(XPATH_ITERATION_CHILDREN_OF_HADES, XPATH_CHILD_NAME, new NotNullContentValidation());
    validator.addValidationStage(vs);

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_FAMILY_TREE);
    XmlValidationService service = new XmlValidationService(validator);
    try {
      ServiceCase.execute(service, msg);
      fail("RuleValidationService success when expecting exception");
    }
    catch (ServiceException e) {
      ; // expected
    }
  }

  public void testMultipleContentValidation() throws Exception {
    XmlRuleValidator validator = new XmlRuleValidator();

    ValidationStage vs = new ValidationStage(XPATH_ITERATION_CHILDREN_OF_ZEUS, XPATH_CHILDREN_OF_HERA,
        new SimpleListContentValidation(CHILDREN_OF_HERA), new NotNullContentValidation());
    validator.addValidationStage(vs);

    ValidationStage vs2 = new ValidationStage(XPATH_ITERATION_CHILDREN_OF_CRONOS, XPATH_CHILDREN_OF_RHEA,
        new SimpleListContentValidation(
        CHILDREN_OF_RHEA), new NotNullContentValidation());
    validator.addValidationStage(vs2);

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_FAMILY_TREE);
    XmlValidationService service = new XmlValidationService(validator);
    try {
      ServiceCase.execute(service, msg);
    }
    catch (ServiceException e) {
      fail("RuleValidationService failure when expecting success");
    }
  }

  static XmlRuleValidator createSample() {
    XmlRuleValidator validator = new XmlRuleValidator();
    ValidationStage stage1 = new ValidationStage(XPATH_ITERATION_CHILDREN_OF_CRONOS, XPATH_CHILDREN_OF_RHEA,
        new NotNullContentValidation(), new SimpleListContentValidation(CHILDREN_OF_RHEA));
    validator.addValidationStage(stage1);

    ValidationStage stage2 = new ValidationStage(XPATH_ITERATION_CHILDREN_OF_ZEUS, XPATH_CHILDREN_OF_HERA,
        new NotNullContentValidation(), new SimpleListContentValidation(CHILDREN_OF_HERA));
    validator.addValidationStage(stage2);

    ValidationStage stage3 = new ValidationStage(XPATH_ITERATION_CHILDREN_OF_ZEUS, XPATH_CHILDREN_OF_LETO,
        new NotNullContentValidation(), new NotInListContentValidation(CHILDREN_OF_HERA));

    validator.addValidationStage(stage3);


    return validator;
  }

  static KeyValuePairSet createNamespaceHolder() {
    KeyValuePairSet result = new KeyValuePairSet();
    result.add(new KeyValuePair("titan", "http://www.adaptris.com/titans"));
    result.add(new KeyValuePair("olympian", "http://www.adaptris.com/olympians"));
    return result;
  }
}
