package com.adaptris.core.services.splitter;

import com.adaptris.core.*;
import com.adaptris.util.GuidGenerator;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MultiPayloadMessageSplitterTest extends SplitterCase
{
	private static final String ID_1 = "bacon";
	private static final String ID_2 = "cupcake";
	private static final String PAYLOAD_1 = "Bacon ipsum dolor amet shoulder sirloin chuck pig pork loin. Shoulder capicola cupim, brisket jerky prosciutto pig sirloin hamburger bresaola t-bone short loin ribeye beef. Ham short loin chicken drumstick burgdoggen shankle. Cupim drumstick shankle, t-bone swine pancetta tail burgdoggen sausage meatloaf rump pastrami pork chop pork belly picanha. Kielbasa tail chicken chislic sirloin, capicola pancetta jowl buffalo cupim.";
	private static final String PAYLOAD_2 = "Cupcake ipsum dolor sit amet sesame snaps biscuit gummies. Donut cotton candy lemon drops wafer jelly sweet roll ice cream jelly. Cheesecake brownie ice cream chocolate biscuit. Cotton candy pastry sweet roll dessert donut lollipop lollipop jelly. Danish sesame snaps sweet roll fruitcake tootsie roll biscuit pudding. Cotton candy lollipop caramels icing macaroon jelly-o. Cake sweet cheesecake.";

	private MultiPayloadMessageSplitter splitter;
	private MultiPayloadAdaptrisMessage message;

	public MultiPayloadMessageSplitterTest(String testName)
	{
		super (testName);
	}

	@Override
	@Before
	protected void setUp() throws Exception
	{
		splitter = createSplitterForTests();
		message = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage(ID_1, PAYLOAD_1, "UTF-8");
		message.addContent(ID_2, PAYLOAD_2);
	}

	@Test
	public void testMessageSplitter()
	{
		List<AdaptrisMessage> response = (List<AdaptrisMessage>)splitter.splitMessage(message);
		assertEquals(2, response.size());
		for (AdaptrisMessage message : response)
		{
			String content = message.getContent();
			if (!PAYLOAD_1.equals(content) && !PAYLOAD_2.equals(content))
			{
				fail();
			}
		}
	}

	@Test
	public void testWrongMessageType()
	{
		AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
		List<AdaptrisMessage> response = (List<AdaptrisMessage>)splitter.splitMessage(message);
		assertEquals(1, response.size());
	}

	@Test
	public void testNonCloneableMessage()
	{
		List<AdaptrisMessage> response = (List<AdaptrisMessage>)splitter.splitMessage(new MultiPayloadAdaptrisMessageImp("bacon", new GuidGenerator(), DefaultMessageFactory.getDefaultInstance(), PAYLOAD_1.getBytes()) {
			@Override
			public MessageLifecycleEvent getMessageLifecycleEvent()
			{
				return new MessageLifecycleEvent()
				{
					@Override
					public List<MleMarker> getMleMarkers()
					{
						List<MleMarker> list = new ArrayList<>();
						list.add(new MleMarker()
						{
							@Override
							public Object clone() throws CloneNotSupportedException
							{
								throw new CloneNotSupportedException();
							}
						});
						return list;
					}
				};
			}
		});
		assertEquals(0, response.size());
	}


	@Override
	protected MultiPayloadMessageSplitter createSplitterForTests()
	{
		return new MultiPayloadMessageSplitter();
	}

	@Override
	protected String createBaseFileName(Object object)
	{
		return super.createBaseFileName(object) + "-MultiPayloadMessageSplitter";
	}

	@Override
	protected Object retrieveObjectForSampleConfig()
	{
		return null; // override retrieveObjectsForSampleConfig below instead
	}

	@Override
	protected List retrieveObjectsForSampleConfig()
	{
		return createExamples(createSplitterForTests());
	}
}
