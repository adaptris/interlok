package com.adaptris.core.services;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.MessageHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * This service sets the configured or generated message id on the message.
 * WARNING: It is generally not recommended to alter the message id but if it is necessary
 * this service should be used with care and only at the beginning of the workflow.
 */
@XStreamAlias("message-unique-id-service")
@AdapterComponent
@ComponentProfile(summary = "Sets the configured or generated message id on the message", tag = "service")
public class MessageUniqueIdService extends ServiceImp {

    /**
     * Set to true to generate a message unique id. This overrides the default behaviour of setting based
     * on <code>messageUniqueId</code>. Defaults to false.
     */
    @Getter
    @Setter
    @InputFieldDefault(value = "false")
    private boolean generate = false;

    /**
     * The message unique id to set on the message. Can be an expression.
     */
    @Getter
    @Setter
    @InputFieldHint(expression = true)
    private String messageUniqueId;

    @Override
    protected void initService() throws CoreException {

    }

    @Override
    protected void closeService() {

    }

    @Override
    public void doService(AdaptrisMessage msg) throws ServiceException {
        String newUniqueId = generate ? MessageHelper.generateNewMessageUniqueId(msg) : msg.resolve(messageUniqueId);
        msg.setUniqueId(newUniqueId);
    }

    @Override
    public void prepare() throws CoreException {
        if (!generate) Args.notBlank(messageUniqueId, "messageUniqueId");
    }
}
