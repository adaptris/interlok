package com.adaptris.core.services.splitter;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("mock-exception-strategy-service")
@AdapterComponent
@ComponentProfile(summary = "A mock exception strategy service", tag = "service")
public class MockExceptionStrategyService extends ServiceImp {
    public static final String SERVICE_RESULT = "serviceResult";
    public enum MODE {MIXED, SUCCESS, ERROR, NEUTRAL}

    private MODE mode = MODE.MIXED;

    public MockExceptionStrategyService() {
        super();
    }

    public MockExceptionStrategyService(MODE mode) {
        this();
        setMode(mode);
    }

    @Override
    protected void initService() throws CoreException {
    }

    @Override
    protected void closeService() {
    }

    @Override
    public void doService(AdaptrisMessage msg) throws ServiceException {
        switch (this.mode) {
            case MIXED:
                mixed(msg);
                break;
            case SUCCESS:
                success(msg);
                break;
            case ERROR:
                throwException(msg);
                break;
            default:
                break;
        }
    }

    @Override
    public void prepare() throws CoreException {
    }

    private void mixed(AdaptrisMessage msg) throws ServiceException {
        int msgCount = Integer.parseInt(msg.getMetadataValue(MessageSplitterServiceImp.KEY_CURRENT_SPLIT_MESSAGE_COUNT));
        if (msgCount % 2 == 0)
            msg.addMetadata(SERVICE_RESULT, "true");
        else
            throw new ServiceException("Generated mock service exception");
    }

    private void throwException(AdaptrisMessage msg) throws ServiceException {
        throw new ServiceException("Generated mock service exception");
    }

    private void success(AdaptrisMessage msg) throws ServiceException {
        msg.addMetadata(SERVICE_RESULT, "true");
    }

    public void setMode(MODE mode) {
        this.mode = mode;
    }
}