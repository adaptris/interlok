package com.adaptris.core.services.aggregator;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.MultiPayloadMessageFactory;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.splitter.MultiPayloadMessageSplitter;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MultiPayloadAggregatorTest extends AggregatingServiceExample
{
  private static final String BACON = "Bacon ipsum dolor amet meatloaf filet mignon bresaola, swine hamburger jowl biltong corned beef shank. Shoulder spare ribs ground round pancetta tenderloin chislic biltong cupim sirloin shankle t-bone sausage bresaola tongue burgdoggen. Pastrami filet mignon spare ribs shankle meatball, kevin cupim frankfurter ground round porchetta chislic alcatra. Brisket biltong buffalo short ribs frankfurter corned beef cow spare ribs pork loin pork chop short loin kielbasa venison meatloaf. Porchetta brisket tail filet mignon, shankle fatback ham pork belly leberkas strip steak cow pig short ribs salami." + System.lineSeparator();
  private static final String CUPCAKE = "Cupcake ipsum dolor sit amet caramels. Cheesecake jelly beans halvah biscuit candy canes gummies oat cake. Cupcake jelly-o sesame snaps gummi bears fruitcake. Dessert cupcake cookie oat cake. Biscuit jelly beans cupcake sweet gummies. Candy canes cotton candy tiramisu bonbon cake fruitcake. Chupa chups jelly beans chocolate bar bonbon powder. Brownie cheesecake caramels candy canes icing halvah chocolate tootsie roll. Cupcake chupa chups danish macaroon fruitcake. Jujubes topping gingerbread jelly beans. Biscuit bear claw gingerbread pudding." + System.lineSeparator();
  private static final String PIRATE = "Yar Pirate Ipsum measured fer yer chains lass gangplank Barbary Coast scallywag hardtack rutters topgallant black spot tack. Spike lanyard topgallant cutlass skysail plunder long clothes black jack matey tackle. Cog brig spanker American Main carouser Corsair scourge of the seven seas wench mizzenmast lee. Lugger Privateer main sheet jolly boat squiffy prow Barbary Coast hardtack long boat mizzen. Pieces of Eight barque topgallant draught cog strike colors Privateer brigantine mutiny scuppers. Chase scuppers ye walk the plank pink heave down brig port clap of thunder yardarm." + System.lineSeparator();

  private MultiPayloadAggregator service;
  private MultiPayloadAdaptrisMessage message;

  @Before
  public void setUp() throws Exception
  {
    service = new MultiPayloadAggregator();
    service.setAggregator(new AppendingMessageAggregator());
    message = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage();

    message.addContent("bacon", BACON);
    message.addContent("cupcake", CUPCAKE);
    message.addContent("pirate", PIRATE);
  }

  @Test
  public void testService() throws Exception
  {
    execute(service, message);
    assertEquals(1, message.getPayloadCount());
    assertTrue(message.getContent().contains(BACON));
    assertTrue(message.getContent().contains(CUPCAKE));
    assertTrue(message.getContent().contains(PIRATE));
  }

  @Test(expected = ServiceException.class)
  public void testServiceException() throws Exception
  {
    DefectiveMessageFactory messageFactory = new DefectiveMessageFactory(DefectiveMessageFactory.WhenToBreak.BOTH);
    message = (MultiPayloadAdaptrisMessage)messageFactory.newMultiPayloadMessage();
    execute(service, message);

    fail(); // expected exception
  }

  @Test(expected = CoreException.class)
  public void testWrongMessageType() throws Exception {
    execute(service, AdaptrisMessageFactory.getDefaultInstance().newMessage());
  }

  @Override
  protected List<Service> retrieveObjectsForSampleConfig()
  {
    return createExamples(new MultiPayloadMessageSplitter(), new MultiPayloadMessageAggregator());
  }

  @Override
  protected Object retrieveObjectForSampleConfig()
  {
    MultiPayloadAggregator service = new MultiPayloadAggregator();
    service.setAggregator(new AppendingMessageAggregator());
    return service;
  }
}
