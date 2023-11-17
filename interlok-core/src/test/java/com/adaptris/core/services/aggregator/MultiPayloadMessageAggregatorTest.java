package com.adaptris.core.services.aggregator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.MultiPayloadMessageFactory;
import com.adaptris.core.Service;
import com.adaptris.core.services.aggregator.MessageAggregatorTest.EvenOddCondition;
import com.adaptris.core.services.splitter.MultiPayloadMessageSplitter;

public class MultiPayloadMessageAggregatorTest extends AggregatorCase {
  private static final String BACON = "Bacon ipsum dolor amet meatloaf filet mignon bresaola, swine hamburger jowl biltong corned beef shank. Shoulder spare ribs ground round pancetta tenderloin chislic biltong cupim sirloin shankle t-bone sausage bresaola tongue burgdoggen. Pastrami filet mignon spare ribs shankle meatball, kevin cupim frankfurter ground round porchetta chislic alcatra. Brisket biltong buffalo short ribs frankfurter corned beef cow spare ribs pork loin pork chop short loin kielbasa venison meatloaf. Porchetta brisket tail filet mignon, shankle fatback ham pork belly leberkas strip steak cow pig short ribs salami.";
  private static final String CUPCAKE = "Cupcake ipsum dolor sit amet caramels. Cheesecake jelly beans halvah biscuit candy canes gummies oat cake. Cupcake jelly-o sesame snaps gummi bears fruitcake. Dessert cupcake cookie oat cake. Biscuit jelly beans cupcake sweet gummies. Candy canes cotton candy tiramisu bonbon cake fruitcake. Chupa chups jelly beans chocolate bar bonbon powder. Brownie cheesecake caramels candy canes icing halvah chocolate tootsie roll. Cupcake chupa chups danish macaroon fruitcake. Jujubes topping gingerbread jelly beans. Biscuit bear claw gingerbread pudding.";
  private static final String PIRATE = "Yar Pirate Ipsum measured fer yer chains lass gangplank Barbary Coast scallywag hardtack rutters topgallant black spot tack. Spike lanyard topgallant cutlass skysail plunder long clothes black jack matey tackle. Cog brig spanker American Main carouser Corsair scourge of the seven seas wench mizzenmast lee. Lugger Privateer main sheet jolly boat squiffy prow Barbary Coast hardtack long boat mizzen. Pieces of Eight barque topgallant draught cog strike colors Privateer brigantine mutiny scuppers. Chase scuppers ye walk the plank pink heave down brig port clap of thunder yardarm.";

  private MultiPayloadMessageAggregator aggregator;
  private MultiPayloadAdaptrisMessage message;
  private List<AdaptrisMessage> messages;

  @BeforeEach
  public void setUp() throws Exception {
    aggregator = createAggregatorForTests();
    message = createNewMessage();
    messages = new ArrayList<>();
    messages.add(DefaultMessageFactory.getDefaultInstance().newMessage(BACON));
    messages.add(DefaultMessageFactory.getDefaultInstance().newMessage(CUPCAKE));
  }

  @Test
  public void testAggregatorReplace() throws Exception {
    aggregator.setReplaceOriginalMessage(true);
    aggregator.joinMessage(message, messages);
    assertTrue(aggregator.getReplaceOriginalMessage());
    assertEquals(2, message.getPayloadCount());
  }

  @Test
  public void testAggregatorAppend() throws Exception {
    aggregator.setReplaceOriginalMessage(false);
    aggregator.joinMessage(message, messages);
    assertFalse(aggregator.getReplaceOriginalMessage());
    assertEquals(3, message.getPayloadCount());
  }

  @Test
  public void testWrongMessageType() throws Exception {
    Assertions.assertThrows(CoreException.class, () -> {
      aggregator.joinMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage(), messages);
    });
  }

  @Test
  public void testWithFilter() throws Exception {
    aggregator.setReplaceOriginalMessage(false);
    aggregator.setFilterCondition(new EvenOddCondition());
    aggregator.aggregate(message, messages);
    assertFalse(aggregator.getReplaceOriginalMessage());
    // 2 to merge, of which one will be discarded + the original.
    assertEquals(2, message.getPayloadCount());
  }

  @Override
  protected MultiPayloadMessageAggregator createAggregatorForTests() {
    return new MultiPayloadMessageAggregator();
  }

  @Override
  protected List<Service> retrieveObjectsForSampleConfig() {
    return createExamples(new MultiPayloadMessageSplitter(), new MultiPayloadMessageAggregator());
  }

  private MultiPayloadAdaptrisMessage createNewMessage() {
    return (MultiPayloadAdaptrisMessage) new MultiPayloadMessageFactory().newMessage("original", PIRATE, "UTF-8");
  }

}
