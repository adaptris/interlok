package com.adaptris.core;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import java.util.*;

import static com.adaptris.core.CoreConstants.OBJ_METADATA_EXCEPTION;
import static com.adaptris.core.CoreConstants.OBJ_METADATA_EXCEPTION_CAUSE;

/**
 * <p>ConfigurableExceptionHandler is an exception handling component that allows
 * for configurable behavior based on specific error conditions. It supports
 * matching exceptions against rules using regular expressions and executing
 * corresponding services for handling those exceptions.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>Configurable rules for matching exceptions using {@link RegexExceptionMatcher}.</li>
 *   <li>Support for handling exceptions based on fields like exception type,
 *       message, cause, or stack trace.</li>
 *   <li>Fallback to a default exception processing service if no rules match.</li>
 * </ul>
 *
 * @see RegexExceptionMatcher
 */
@XStreamAlias("configurable-exception-handler")
@AdapterComponent
@ComponentProfile(summary = "An exception handling component configurable for specific errors", tag = "error-handling,base")
public class ConfigurableExceptionHandler extends RootProcessingExceptionHandler implements EventHandlerAware {

    @Getter
    @Setter
    @Valid
    private Service defaultExceptionProcessingService;
    @Valid
    @AutoPopulated
    @XStreamImplicit
    @Getter
    @Setter
    private List<Rule> rules;
    @Getter
    private transient Map<String, Workflow> workflows;
    @Getter
    private transient EventHandler eventHandler;

    public ConfigurableExceptionHandler() {
        super();
        workflows = new HashMap<>();
        rules = new LinkedList<>();
    }

    /**
     * Handles an exception by applying configured rules to determine
     * the appropriate action. If no rules match, the default processing exception
     * service is invoked if configured.
     *
     * @param msg the {@link AdaptrisMessage} containing the exception details
     */
    @Override
    public void handleProcessingException(AdaptrisMessage msg) {
        msg.getObjectHeaders().put(CoreConstants.OBJ_METADATA_MESSAGE_FAILED, true);

        final ExceptionDetails exceptionDetails = extractExceptionDetailsFromObjectMetadata(msg);

        try {
            boolean matchedRule = false;

            for (Rule rule : rules) {
                if (!matchedRule) {
                    matchedRule = applyRuleIfMatched(msg, rule, exceptionDetails);
                }
            }

            if (!matchedRule && getDefaultExceptionProcessingService() != null) {
                getDefaultExceptionProcessingService().doService(msg);
            }
        } catch (Exception ex) {
            log.error("Exception handling error msg [{}]",
                    MessageLoggerImpl.LAST_RESORT_LOGGER.toString(msg), ex);
        }

        notifyParent(msg);
    }

    @Override
    public void init() throws CoreException {
        LifecycleHelper.registerEventHandler(getDefaultExceptionProcessingService(), eventHandler);
        LifecycleHelper.init(getDefaultExceptionProcessingService());
    }

    @Override
    public void start() throws CoreException {
        LifecycleHelper.start(getDefaultExceptionProcessingService());
    }

    @Override
    public void stop() {
        LifecycleHelper.stop(getDefaultExceptionProcessingService());
    }

    @Override
    public void close() {
        LifecycleHelper.close(getDefaultExceptionProcessingService());
    }

    @Override
    public void prepare() throws CoreException {
        LifecycleHelper.prepare(getDefaultExceptionProcessingService());
    }

    @Override
    public void registerWorkflow(Workflow w) {
        workflows.put(w.obtainWorkflowId(), w);
    }

    @Override
    public void registerEventHandler(EventHandler eh) {
        eventHandler = eh;
    }

    @Override
    public boolean hasConfiguredBehaviour() {
        return getDefaultExceptionProcessingService() != null;
    }

    /**
     * Applies a rule to the given message and exception details to determine if the rule matches.
     * If the rule matches, the associated service is executed.
     *
     * @param msg the {@link AdaptrisMessage} containing the message to process
     * @param rule the {@link Rule} to apply
     * @param exceptionDetails the {@link ExceptionDetails} extracted from the message
     * @return {@code true} if the rule matches and the service is executed, {@code false} otherwise
     * @throws CoreException if an error occurs while preparing or executing the service
     */
    boolean applyRuleIfMatched(AdaptrisMessage msg, Rule rule, ExceptionDetails exceptionDetails) throws CoreException {
        if (rule.getMatcher() == null) {
            return false;
        }

        final Service ruleProcessingExceptionService = rule.getExceptionProcessingService();
        final RegexExceptionMatcher matcher = rule.getMatcher();

        if (ruleProcessingExceptionService == null) {
            return false;
        }

        ruleProcessingExceptionService.prepare();

        if (matcher.matches(matcher.getMatchAgainstField().getSource(exceptionDetails))) {
            ruleProcessingExceptionService.doService(msg);
            return true;
        }
        return false;
    }

    private ExceptionDetails extractExceptionDetailsFromObjectMetadata(AdaptrisMessage msg) {
        final Exception exception = (Exception) msg.getObjectHeaders().getOrDefault(OBJ_METADATA_EXCEPTION, null);
        final String cause = (String) msg.getObjectHeaders().getOrDefault(OBJ_METADATA_EXCEPTION_CAUSE, null);

        return ExceptionDetails.from(exception, cause);
    }

    /**
     * Represents a rule for handling specific exceptions. Each rule consists of a
     * {@link RegexExceptionMatcher} to match exception details and a
     * {@link Service} to process the exception if the rule matches.
     *
     * @see RegexExceptionMatcher
     * @see Service
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @AdapterComponent
    @XStreamAlias("exception-rule")
    @ComponentProfile(summary = "A rule for handling a specific error", tag = "error-handling")
    @DisplayOrder(order = {"matcher", "exceptionProcessingService"})
    public static class Rule {
        private RegexExceptionMatcher matcher;
        private Service exceptionProcessingService;
    }

}
