package com.adaptris.core;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.stubs.MockService;

import junit.framework.TestCase;

import static org.mockito.Mockito.verify;

import java.util.Arrays;

public class SharedServiceTest extends TestCase {
  
  private SharedService sharedService;
  
  private Service mockService;
  
  @Mock
  private Service mockitoService;
  
  private Adapter adapter;
  
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    sharedService = new SharedService();
    sharedService.setLookupName("mock-service-id");
    mockService = new MockService();
    mockService.setUniqueId("mock-service-id");

  }

  public void tearDown() throws Exception {

  }
  
  public void testCloneService() throws Exception {
    adapter = createAdapterForSharedService(mockService, sharedService);
        
    assertNull(sharedService.getClonedService());
    
    adapter.prepare();
    adapter.init();
    
    assertNotNull(sharedService.getCloneService());
    
    assertFalse(mockService == sharedService.getClonedService());
    assertEquals(mockService.getUniqueId(), sharedService.getClonedService().getUniqueId());
    
    adapter.stop();
    adapter.close();
  }
  
  public void testNoCloneService() throws Exception {
    adapter = createAdapterForSharedService(mockService, sharedService);
    
    sharedService.setCloneService(false);
    
    assertNull(sharedService.getClonedService());
    
    adapter.prepare();
    adapter.init();
    
    assertNotNull(sharedService.getCloneService());
    
    assertTrue(mockService == sharedService.getClonedService());
    assertEquals(mockService.getUniqueId(), sharedService.getClonedService().getUniqueId());
    
    adapter.stop();
    adapter.close();
  }

  public void testPrepare() throws Exception {
    sharedService.setClonedService(mockitoService);
    sharedService.prepare();
    
    verify(mockitoService).prepare();
  }
  
  public void testInit() throws Exception {
    sharedService.setClonedService(mockitoService);
    sharedService.init();
    
    verify(mockitoService).init();
  }
  
  public void testStart() throws Exception {
    sharedService.setClonedService(mockitoService);
    sharedService.start();
    
    verify(mockitoService).start();
  }
  
  public void testStop() throws Exception {
    sharedService.setClonedService(mockitoService);
    sharedService.stop();
    
    verify(mockitoService).stop();
  }
  
  public void testClose() throws Exception {
    sharedService.setClonedService(mockitoService);
    sharedService.close();
    
    verify(mockitoService).close();
  }
  
  public void testCreateName() {
    sharedService.setClonedService(mockitoService);
    sharedService.createName();
    
    verify(mockitoService).createName();
  }

  public void testCreateQualifier() {
    sharedService.setClonedService(mockitoService);
    sharedService.createQualifier();
    
    verify(mockitoService).createQualifier();
  }

  public void testIsTrackingEndpoint() {
    sharedService.setClonedService(mockitoService);
    sharedService.isTrackingEndpoint();
    
    verify(mockitoService).isTrackingEndpoint();
  }

  public void testIsConfirmation() {
    sharedService.setClonedService(mockitoService);
    sharedService.isConfirmation();
    
    verify(mockitoService).isConfirmation();
  }

  public void testRetrieveComponentState() {
    sharedService.setClonedService(mockitoService);
    sharedService.retrieveComponentState();
    
    verify(mockitoService).retrieveComponentState();
  }

  public void testChangeState() {
    sharedService.setClonedService(mockitoService);
    
    InitialisedState newState = InitialisedState.getInstance();
    sharedService.changeState(newState);
    
    verify(mockitoService).changeState(newState); 
  }

  public void testRequestInit() throws CoreException {
    sharedService.setClonedService(mockitoService);
    sharedService.requestInit();
    
    verify(mockitoService).requestInit();
  }

  public void testRequestStart() throws CoreException {
    sharedService.setClonedService(mockitoService);
    sharedService.requestStart();
    
    verify(mockitoService).requestStart();
  }

  public void testRequestStop() {
    sharedService.setClonedService(mockitoService);
    sharedService.requestStop();
    
    verify(mockitoService).requestStop();
  }

  public void testRequestClose() {
    sharedService.setClonedService(mockitoService);
    sharedService.requestClose();
    
    verify(mockitoService).requestClose();
  }

  public void testDoService() throws ServiceException {
    sharedService.setClonedService(mockitoService);
    
    AdaptrisMessage msg = DefaultMessageFactory.getDefaultInstance().newMessage();
    sharedService.doService(msg);
    
    verify(mockitoService).doService(msg);
  }

  public void testSetUniqueId() {
    sharedService.setClonedService(mockitoService);
    sharedService.setUniqueId("newUniqueId");
    
    verify(mockitoService).setUniqueId("newUniqueId");
  }

  public void testGetUniqueId() {
    sharedService.setClonedService(mockitoService);
    sharedService.getUniqueId();
    
    verify(mockitoService).getUniqueId();
  }

  public void testIsBranching() {
    sharedService.setClonedService(mockitoService);
    sharedService.isBranching();
    
    verify(mockitoService).isBranching();
  }

  public void testContinueOnFailure() {
    sharedService.setClonedService(mockitoService);
    sharedService.continueOnFailure();
    
    verify(mockitoService).continueOnFailure();
  }
  
  
  private Adapter createAdapterForSharedService(Service mockService, SharedService sharedService) throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId("adapterId");
    adapter.getSharedComponents().setServices(Arrays.asList(new Service[] {mockService}));

    adapter.getChannelList().add(new Channel());
    adapter.getChannelList().get(0).getWorkflowList().add(new StandardWorkflow());
    ((StandardWorkflow) adapter.getChannelList().get(0).getWorkflowList().get(0)).getServiceCollection().add(sharedService);
    
    return adapter;
  }
  
}
