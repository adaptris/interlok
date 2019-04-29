---
layout: page
title: 0001-add-messagelogger-interface
---
# Add MessageLogger interface; remove AdaptrisMessage toString()

* Status: Accepted
* Deciders: Aaron McGrath, Lewin Chan, (Matthew Warman)
* Date: 2019-02-25

## Context and Problem Statement

Often, integrators use a `RegexpMetadataService` and extract the whole payload and store it as metadata. The payload can span multiple lines and be very long; sometimes they forget to remove it after they're done with it. If the message is funnelled into another workflow (via JMS, or something that supports MIME encoding), then the metadata is transported with the message to the new workflow.

When the message enters the second workflow, it is logged optionally including the payload, but always with the metadata. In the event that we have extra long metadata values, then this can make logging unusable when attempting to debug various runtime operations.

This is recorded as [INTERLOK-2647](https://adaptris.atlassian.net/browse/INTERLOK-2647)

## Considered Options

* Add an AdaptrisMessage#toString(boolean, boolean, boolean) method.
* Add a MessageLogger interface that can be composed into existing services to provide appropriate logging.

### AdaptrisMessage.toString(boolean, boolean, boolean)

AdaptrisMessage already contains multiple toString() methods :`toString(includePayload), toString(includePayload, includeEvents)`.  We could add another `toString(includePayload, includeEvents, includeMetadata)` which could be used where appropriate.

This smells wrong as we are binding logging behaviour into the object itself.

### Add a MessageLogger interface

```
@FunctionalInterface
public interface MessageLogger {
  String toString(AdaptrisMessage m);
}
```

Deprecate the existing toString() methods in AdaptrisMessage (but don't remove them until something like 3.12 / 3.11), and add concrete implementations of MessageLogger so that can log all the available options :

* MinimalMessageLogger -> just logs the uniqueId
* DefaultMessageLogger -> logs uniqueId + metadata
* TrucatedMessageLogger -> logs uniqueId + truncated metadata.
* PayloadMessageLogger -> logs uniqueId + metadata + payload
* FullMessageLogger -> logs uid, metadata, payload, events
... more as required.

We will need to handle deprecation in WorkflowImp (include-payload) and LogMessageService (includeEvents/includePayload) and modify them to use the new interface.
This will also require a change to AdaptrisMessage to _promote_ the getPayloadForLogging() method into the interface (rather than being in AdaptrisMessageImp).

This means that we end up with configuration like

```
<standard-workflow>
   <message-logger class="message-logging-with-metadata"/>
</standard-workflow>
```
and
```
<log-message-service>
   <logging-format class="message-logging-with-payload"/>
</log-message-service>
```

## Decision

Add the MessageLogger interface

## Consequences

* There will be more "deprecated" warnings in the code and we should be changing all the references for AdaptrisMessage#toString(true) which is sometimes used as logging when a fatal error happens.
* At some point we will break things by removing the toString() methods.

