package com.adaptris.core.services.splitter;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.services.aggregator.IgnoreOriginalXmlDocumentAggregator;
import com.adaptris.core.services.conditional.Condition;
import com.adaptris.core.services.conditional.Operator;
import com.adaptris.core.services.conditional.conditions.*;
import com.adaptris.core.services.conditional.operator.Equals;
import com.adaptris.core.services.conditional.operator.IsEmpty;
import com.adaptris.core.services.metadata.XpathMetadataService;
import com.adaptris.core.services.metadata.xpath.ConfiguredXpathQuery;
import com.adaptris.core.services.metadata.xpath.XpathQuery;
import com.adaptris.core.stubs.MessageHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.InsertNode;
import com.adaptris.util.text.xml.XPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;


public class SplitJoinServiceFilterTest extends SplitterServiceExample {
  private static Log logR = LogFactory.getLog(SplitJoinServiceFilterTest.class);

  public static final String ENCODING_UTF8 = "UTF-8";
  public static final String SPLIT_XPATH2 = "//record_item";
  public static final String DELETED_FLAG_XPATH = "//Deleted";
  public static final String XPATH_OUTPUT = "/filtered-output";
  public static final String INPUT_FILE_KEY = "XpathSplitter.filter-test.input";

  @Rule
  public TestName testName = new TestName();

  public SplitJoinServiceFilterTest(String testName) {
    super(testName);
  }


  @Before
  protected void setUp() throws Exception {
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-XpathSplitter";
  }

  private Condition createFilterCondition() {
    // Payload not empty
    ConditionNot payloadNotEmptyCondition = new ConditionNot();
    final ConditionPayload conditionPayload = new ConditionPayload();
    payloadNotEmptyCondition.setCondition(conditionPayload);
    final Operator isEmpty = new IsEmpty();
    conditionPayload.setOperator(isEmpty);

    // Metadata deleted == false
    ConditionMetadata conditionMetadata = new ConditionMetadata();
    final Equals equalsOp = new Equals();
    equalsOp.setValue("False");
    conditionMetadata.setOperator(equalsOp);
    conditionMetadata.setMetadataKey("deleted");

    // Final and condition
    ConditionAnd andCondition = new ConditionAnd();
    andCondition.setConditions(Arrays.asList(payloadNotEmptyCondition, conditionMetadata));
    return andCondition;
  }

  @After
  protected void tearDown() throws Exception {
  }

  protected SplitJoinService createServiceForTests(Condition aggregatorCondition, boolean retainExceptionMsgs) {
    SplitJoinService sut = new SplitJoinService();
    XpathMetadataService metadataService = new XpathMetadataService();
    metadataService.setXpathQueries(new ArrayList<XpathQuery>(Arrays.asList(new ConfiguredXpathQuery("deleted", DELETED_FLAG_XPATH)
    )));

    // Aggregator
    final IgnoreOriginalXmlDocumentAggregator xmlDocumentAggregator = new IgnoreOriginalXmlDocumentAggregator("<filtered-output/>", new InsertNode(XPATH_OUTPUT));
    xmlDocumentAggregator.setFilterCondition(aggregatorCondition);
    xmlDocumentAggregator.setRetainFilterExceptionsMessages(retainExceptionMsgs);

    sut.setService(asCollection(new NullService(), metadataService));
    sut.setSplitter(new XpathMessageSplitter(SPLIT_XPATH2, ENCODING_UTF8));
    sut.setAggregator(xmlDocumentAggregator);
    return sut;
  }

  public static void execute(Service s, AdaptrisMessage msg) throws CoreException {
    start(s);
    try {
      s.doService(msg);
    }
    finally {
      stop(s);
    }
  }

  @Test
  public void testSplitJoinService_WithFilter() throws Exception {
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(INPUT_FILE_KEY));
    SplitJoinService service = createServiceForTests(createFilterCondition(), false);
    execute(service, msg);

    XPath xpath = new XPath();
    final NodeList nodeList = xpath.selectNodeList(XmlHelper.createDocument(msg), SPLIT_XPATH2);
    assertEquals(13, nodeList.getLength());
  }

  @Test
  public void test_SplitJoin_discardExeptionMessages() throws Exception {
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(INPUT_FILE_KEY));
    SplitJoinService service = createServiceForTests(new MyCondition(), false);
    execute(service, msg);
    XPath xpath = new XPath();
    final NodeList nodeList = xpath.selectNodeList(XmlHelper.createDocument(msg), SPLIT_XPATH2);
    assertEquals(8, nodeList.getLength());
  }

  @Test
  public void test_SplitJoin_retrainExeptionMessages() throws Exception {
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(INPUT_FILE_KEY));
    SplitJoinService service = createServiceForTests(new MyCondition(), true);
    execute(service, msg);
    XPath xpath = new XPath();
    final NodeList nodeList = xpath.selectNodeList(XmlHelper.createDocument(msg), SPLIT_XPATH2);
    assertEquals(16, nodeList.getLength());
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    return createServiceForTests(createFilterCondition(), false);
  }

  // Have a condition that every other call causes an exception to be thrown
  private static int numberOfCalls = 0;
  private class MyCondition extends ConditionImpl {

    @Override
    public boolean evaluate(AdaptrisMessage message) throws CoreException {
      numberOfCalls++;
      if (numberOfCalls %2 == 1) throw new CoreException("for testing");
      return true;
    }

    public void close() {
      throw new RuntimeException();
    }
  }
}
