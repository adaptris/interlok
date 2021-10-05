package com.adaptris.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.ServiceLoader;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageImp;
import com.adaptris.core.MultiPayloadAdaptrisMessageImp;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.interlok.resolver.Resolver;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Convenience helper to see if configuration is considered an expression
 * <p>
 * If we have configuration that is something like {@code %payload} then this needs to be resolved
 * from the message object at runtime. In the event that the value is something like
 * {@code a-hard-coded-queue} then this does not need to be resolved on a per-message basis. For
 * efficiency if the configuration does not need to be resolved on a per-message basis then we could
 * pre-empt some activity for performance purposes.
 * </p>
 * <p>
 * For the purposes of determining expressions, things like "%sysprop" are considered expressions
 * since {@link ExternalResolver} also supports xpath/json resolution from the payload.
 * </p>
 * 
 * @see ExternalResolver
 * @see AdaptrisMessage#resolve(String)
 */
public class InputFieldExpression {

  // AdaptrisMessage regular expression matchers.
  private static final String[] REGEXPS =
      {AdaptrisMessageImp.METADATA_RESOLVE_REGEXP, AdaptrisMessageImp.OBJECT_RESOLVE_REGEXP,
          MultiPayloadAdaptrisMessageImp.EXPLICIT_PAYLOAD_REGEXP,
          MultiPayloadAdaptrisMessageImp.IMPLICIT_PAYLOAD_REGEXP};


  // Taken from AdaptrisMessageImp#Resolvers.
  private static final String[] KEY_MATCHES = {AdaptrisMessageImp.UID_RESOLVE_KEY,
      AdaptrisMessageImp.SIZE_RESOLVE_KEY, AdaptrisMessageImp.PAYLOAD_RESOLVE_KEY};

  @Getter(AccessLevel.PRIVATE)
  private Collection<IsExpression> expressionList = new ArrayList<>();

  private static final InputFieldExpression IMPL = new InputFieldExpression();

  private InputFieldExpression() {
    Iterable<Resolver> resolvers = ServiceLoader.load(Resolver.class);
    for (Resolver r : resolvers) {
      expressionList.add((v) -> r.canHandle(v));
    }
    for (String regex : REGEXPS) {
      expressionList.add((v) -> v.matches(regex));
    }
    for (String exact : KEY_MATCHES) {
      expressionList.add((v) -> v.equalsIgnoreCase(exact));
    }
  }

  /**
   * Check if the configured value is considered an expression.
   * <p>
   * If we have configuration that is something like {@code %payload} then this needs to be resolved
   * from the message object at runtime. In the event that the value is something like
   * {@code a-hard-coded-queue} then this does not need to be resolved on a per-message basis. For
   * efficiency if the configuration does not need to be resolved on a per-message basis then we
   * could pre-empt some activity for performance purposes.
   * </p>
   * 
   * @param configuredValue the configured value.
   * @return if the value is considered something that is an expression (within reason).
   */
  public static boolean isExpression(String configuredValue) {
    return IMPL.getExpressionList().stream().filter((resolver) -> resolver.matches(configuredValue)).findFirst()
        .isPresent();
  }

  @FunctionalInterface
  private interface IsExpression {
    boolean matches(String value);
  }
}
