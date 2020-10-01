package com.adaptris.core.services.conditional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MessageLifecycleEvent;
import com.adaptris.core.MleMarker;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.MultiPayloadAdaptrisMessageImp;
import com.adaptris.core.MultiPayloadMessageFactory;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.util.GuidGenerator;

public class ForEachTest extends ConditionalServiceExample
{
	private static final byte[] PAYLOAD_1 = "Bacon ipsum dolor amet ball tip pancetta chuck boudin leberkas, alcatra shoulder pig meatball ground round cupim frankfurter chicken andouille. Landjaeger alcatra pork belly rump drumstick beef ribs shoulder. Corned beef venison meatloaf ham hock doner pig salami burgdoggen cow tongue. Swine pork loin ham, kevin flank pig salami biltong shankle ball tip alcatra short ribs hamburger tongue. Buffalo meatloaf capicola, leberkas jowl biltong kevin drumstick pastrami andouille.".getBytes();
	private static final byte[] PAYLOAD_2 = "Cupcake ipsum dolor sit amet danish fruitcake candy. Icing jujubes powder sweet. Pie gingerbread bonbon dragee lollipop sesame snaps. Bonbon gummi bears danish caramels cupcake powder. Gingerbread bonbon croissant tootsie roll jelly pastry. Candy canes powder sweet biscuit fruitcake. Dragee biscuit brownie biscuit pudding jelly. Muffin tootsie roll tiramisu ice cream macaroon cupcake. Sugar plum powder gummi bears dessert wafer. Cheesecake lollipop tootsie roll dessert lollipop icing cheesecake.".getBytes();

	private ForEach forEach;
	private MultiPayloadAdaptrisMessage message;
	private ThenService then;
	@Mock
	private Service mock;

	@Before
	public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks(this);
		forEach = new ForEach();
		then = new ThenService();
		then.setService(mock);
		forEach.setThen(then);
		message = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage("bacon", PAYLOAD_1);
		message.addPayload("cupcake", PAYLOAD_2);
	}

	@Test
	public void testForEach() throws Exception
	{
		forEach.doService(message);
		assertEquals(new Integer(1), forEach.getThreadCount());
		verify(mock, times(2)).doService(any(AdaptrisMessage.class));
	}

	@Test
	public void testWrongMessage() throws Exception
	{
		AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
		forEach.doService(message);
		verify(mock, times(1)).doService(message);
	}

	@Test
	public void testParallelForEach() throws Exception
	{
		forEach.setThreadCount(0);
		assertEquals(new Integer(0), forEach.getThreadCount());
		forEach.doService(message);
		verify(mock, times(2)).doService(any(AdaptrisMessage.class));
	}

	@Test
	public void testBadThreadCount()
	{
		forEach.setThreadCount(-1);
		assertEquals(new Integer(0), forEach.getThreadCount());
	}

	@Test
	public void testBadThenService() throws Exception
	{
		doThrow(new ServiceException()).when(mock).doService(any(AdaptrisMessage.class));
		forEach.doService(message);
		verify(mock, times(2)).doService(any(AdaptrisMessage.class));
	}

	@Test
  @SuppressWarnings("serial")
	public void testNonCloneableMessage() throws Exception
	{
		forEach.doService(new MultiPayloadAdaptrisMessageImp("bacon", new GuidGenerator(), DefaultMessageFactory.getDefaultInstance(), PAYLOAD_1) {
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
		verify(mock, never()).doService(any(AdaptrisMessage.class));
	}

	@Override
	protected Object retrieveObjectForSampleConfig()
	{
		ForEach forEach = new ForEach();
		ThenService then = new ThenService();
		then.setService(new LogMessageService());
		forEach.setThen(then);
		return forEach;
	}

}
