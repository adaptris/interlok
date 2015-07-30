package com.adaptris.core.transform;

import static com.adaptris.core.transform.XmlRuleValidatorTest.CHILDREN_OF_HERA;
import static com.adaptris.core.transform.XmlRuleValidatorTest.CHILDREN_OF_RHEA;
import static com.adaptris.core.transform.XmlRuleValidatorTest.THE_TWELVE_TITANS;
import static com.adaptris.core.transform.XmlRuleValidatorTest.XML_FAMILY_TREE;
import static com.adaptris.core.transform.XmlRuleValidatorTest.XML_WITH_NAMESEPACE;
import static com.adaptris.core.transform.XmlRuleValidatorTest.XPATH_CHILDREN_OF_DEMETER;
import static com.adaptris.core.transform.XmlRuleValidatorTest.XPATH_CHILDREN_OF_HERA;
import static com.adaptris.core.transform.XmlRuleValidatorTest.XPATH_CHILDREN_OF_RHEA;
import static com.adaptris.core.transform.XmlRuleValidatorTest.XPATH_CHILD_NAME;
import static com.adaptris.core.transform.XmlRuleValidatorTest.XPATH_ITERATION_CHILDREN_OF_CRONOS;
import static com.adaptris.core.transform.XmlRuleValidatorTest.XPATH_ITERATION_CHILDREN_OF_HADES;
import static com.adaptris.core.transform.XmlRuleValidatorTest.XPATH_ITERATION_CHILDREN_OF_ZEUS;
import static com.adaptris.core.transform.XmlRuleValidatorTest.XPATH_NS_ITERATION_OLYMPIAN_NAMES;
import static com.adaptris.core.transform.XmlRuleValidatorTest.XPATH_NS_ITERATION_TITAN_NAMES;
import static com.adaptris.core.transform.XmlRuleValidatorTest.XPATH_NS_OLYMPIAN_NAME;
import static com.adaptris.core.transform.XmlRuleValidatorTest.XPATH_NS_TITAN_NAME;
import static com.adaptris.core.transform.XmlRuleValidatorTest.createNamespaceHolder;
import static com.adaptris.core.transform.XmlRuleValidatorTest.createSample;

import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.transform.validate.NotNullContentValidation;
import com.adaptris.transform.validate.SimpleListContentValidation;
import com.adaptris.transform.validate.ValidationStage;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

@SuppressWarnings("deprecation")
public class XmlRuleValidationServiceTest extends TransformServiceExample {


  public XmlRuleValidationServiceTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testSetNamespaceContext() {
    XmlRuleValidationService obj = new XmlRuleValidationService();
    assertNull(obj.getNamespaceContext());
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("hello", "world"));
    obj.setNamespaceContext(kvps);
    assertEquals(kvps, obj.getNamespaceContext());
    obj.setNamespaceContext(null);
    assertNull(obj.getNamespaceContext());
  }

  public void testContentValidationNonXmlMessage() throws Exception {
    XmlRuleValidationService service = new XmlRuleValidationService();
    ValidationStage vs = new ValidationStage();
    vs.setIterationXpath(XPATH_ITERATION_CHILDREN_OF_ZEUS);
    vs.setElementXpath(XPATH_CHILDREN_OF_HERA);
    SimpleListContentValidation rule = new SimpleListContentValidation();
    rule.setListEntries(Arrays.asList(CHILDREN_OF_HERA));
    vs.addRule(rule);
    service.setValidationStages(Arrays.asList(new ValidationStage[]
    {
      vs
    }));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("ABCDEFG");
    try {
      execute(service, msg);
      fail("RuleValidationService success when expecting failure");
    }
    catch (ServiceException expected) {
    }
  }

  public void testListContentValidationDataInList() throws Exception {
    XmlRuleValidationService service = new XmlRuleValidationService();
    ValidationStage vs = new ValidationStage();
    vs.setIterationXpath(XPATH_ITERATION_CHILDREN_OF_ZEUS);
    vs.setElementXpath(XPATH_CHILDREN_OF_HERA);
    SimpleListContentValidation rule = new SimpleListContentValidation();
    rule.setListEntries(Arrays.asList(CHILDREN_OF_HERA));
    vs.addRule(rule);
    service.setValidationStages(Arrays.asList(new ValidationStage[]
    {
      vs
    }));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_FAMILY_TREE);
    try {
      execute(service, msg);
    }
    catch (ServiceException e) {
      fail("RuleValidationService failure when expecting success");
    }
  }

  public void testListContentValidationWithNamespace() throws Exception {
    XmlRuleValidationService service = new XmlRuleValidationService();
    service.setNamespaceContext(createNamespaceHolder());
    ValidationStage vs = new ValidationStage();
    vs.setIterationXpath(XPATH_NS_ITERATION_TITAN_NAMES);
    vs.setElementXpath(XPATH_NS_TITAN_NAME);
    SimpleListContentValidation rule = new SimpleListContentValidation();
    rule.setListEntries(Arrays.asList(THE_TWELVE_TITANS));
    vs.addRule(new NotNullContentValidation());
    vs.addRule(rule);
    service.addValidationStage(vs);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_WITH_NAMESEPACE);
    try {
      execute(service, msg);
    }
    catch (ServiceException e) {
      fail("RuleValidationService failure when expecting success");
    }
  }

  public void testListContentValidationDataNotInList() throws Exception {
    XmlRuleValidationService service = new XmlRuleValidationService();
    ValidationStage vs = new ValidationStage();
    vs.setIterationXpath(XPATH_ITERATION_CHILDREN_OF_ZEUS);
    vs.setElementXpath(XPATH_CHILDREN_OF_DEMETER);
    SimpleListContentValidation rule = new SimpleListContentValidation();
    rule.setListEntries(Arrays.asList(CHILDREN_OF_HERA));
    vs.addRule(rule);
    service.addValidationStage(vs);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_FAMILY_TREE);
    try {
      execute(service, msg);
      fail("RuleValidationService success when expecting exception");
    }
    catch (ServiceException e) {
      ; // expected
    }
  }

  public void testListContentValidationWithNamespaceDataNotInList() throws Exception {
    XmlRuleValidationService service = new XmlRuleValidationService();
    service.setNamespaceContext(createNamespaceHolder());
    ValidationStage vs = new ValidationStage();
    vs.setIterationXpath(XPATH_NS_ITERATION_OLYMPIAN_NAMES);
    vs.setElementXpath(XPATH_NS_OLYMPIAN_NAME);
    SimpleListContentValidation rule = new SimpleListContentValidation();
    rule.setListEntries(Arrays.asList(THE_TWELVE_TITANS));
    vs.addRule(new NotNullContentValidation());
    vs.addRule(rule);
    service.addValidationStage(vs);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_WITH_NAMESEPACE);
    try {
      execute(service, msg);
      fail("RuleValidationService success when expecting exception");
    }
    catch (ServiceException e) {
      ; // expected
    }
  }

  public void testNotNullContentValidationDataNotNull() throws Exception {
    XmlRuleValidationService service = new XmlRuleValidationService();
    ValidationStage vs = new ValidationStage();
    vs.setIterationXpath(XPATH_ITERATION_CHILDREN_OF_ZEUS);
    vs.setElementXpath(XPATH_CHILD_NAME);
    vs.addRule(new NotNullContentValidation());
    service.addValidationStage(vs);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_FAMILY_TREE);
    try {
      execute(service, msg);
    }
    catch (ServiceException e) {
      fail("RuleValidationService failure when expecting success");
    }
  }

  public void testNotNullContentValidationDataIsNull() throws Exception {
    XmlRuleValidationService service = new XmlRuleValidationService();
    ValidationStage vs = new ValidationStage();
    vs.setIterationXpath(XPATH_ITERATION_CHILDREN_OF_HADES);
    vs.setElementXpath(XPATH_CHILD_NAME);
    vs.addRule(new NotNullContentValidation());
    service.addValidationStage(vs);

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_FAMILY_TREE);
    try {
      execute(service, msg);
      fail("RuleValidationService success when expecting exception");
    }
    catch (ServiceException e) {
      ; // expected
    }
  }

  public void testMultipleContentValidation() throws Exception {
    XmlRuleValidationService service = new XmlRuleValidationService();

    ValidationStage vs = new ValidationStage();
    vs.setIterationXpath(XPATH_ITERATION_CHILDREN_OF_ZEUS);
    vs.setElementXpath(XPATH_CHILDREN_OF_HERA);
    SimpleListContentValidation rule = new SimpleListContentValidation();
    rule.setListEntries(Arrays.asList(CHILDREN_OF_HERA));
    vs.addRule(new NotNullContentValidation());
    vs.addRule(rule);
    service.addValidationStage(vs);

    vs = new ValidationStage();
    vs.setIterationXpath(XPATH_ITERATION_CHILDREN_OF_CRONOS);
    vs.setElementXpath(XPATH_CHILDREN_OF_RHEA);
    rule = new SimpleListContentValidation();
    rule.setListEntries(Arrays.asList(CHILDREN_OF_RHEA));
    vs.addRule(new NotNullContentValidation());
    vs.addRule(rule);
    service.addValidationStage(vs);

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(XML_FAMILY_TREE);
    try {
      execute(service, msg);
    }
    catch (ServiceException e) {
      fail("RuleValidationService failure when expecting success");
    }
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!-- \n The example document for this validation is\n" + XML_FAMILY_TREE
        + "\n-->\n\n" + "\n<!-- With the following service we are checking that"
        + "\na0) Cronos and Rhea had children and have names"
        + "\na1) Names of Cronos + Rhea's children must be one of Zeus, Demeter, Hades, Hera"
        + "\nb0) Zeus and Hera had children and have names" + "\nb1) Zeus and Hera's Children are one of Hebe, Ares, or Hephaestus"
        + "\nc0) Zeus and Leto had children and have names"
        + "\nc1) Zeus and Leto's children are not called Hebe, Ares, or Hephaestus"
        + "\n-->\n";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    XmlRuleValidationService service = new XmlRuleValidationService();
    service.setValidationStages(createSample().getValidationStages());
    return service;
  }
}
