---
layout: page
title: 0006-workflow-callback
---
# Add a new method to AdaptrisMessageListener

* Status: ACCEPTED
* Deciders: Aaron McGrath, Lewin Chan, Gerco Dries, Matt Warman, Sebastien Belin, (Paul Higginson)
* Date: 2019-11-06

## Context and Problem Statement

When you enable [Dead Letter Queues](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-dead-letter-queues.html) in SQS; the contract is that messages read from a source queue are failed over to the dead letter queue once the redrive policy is fired (i.e. after a max number of attempts to deliver the message). From testing, this is predicated on the fact that the message is not deleted. Of course, our sqs-polling-consumer deletes the message after submitting it to the workflow.

Since we can't rely on the JMS semantics either (since onMessage() traditionally doesn't throw exceptions, and RuntimeExceptions aren't propagated by Interlok) we then have to think about some way of having an asynchronous callback.


## Considered Options

* Do Nothing
* Modify AdaptrisMessageListener to have callbacks.

## Decision Outcome

Chosen option: Modify AdaptrisMessageListener to have callbacks.

## Pros and Cons of the Options

### Do Nothing


* Good, because no change to code.
* Bad, because we can't use SQS DLC behaviours.
* Bad, because we might not preserve all message attributes (depending on configuration).
* Neutral, error handling is still fully within the purview of Interlok (which is predictable)

### Modify AdaptrisMessageListener to have callbacks.

If we change AdaptrisMessageListener to be this :

```
default void onAdaptrisMessage(AdaptrisMessage msg) {
  onAdaptrisMessage(msg, (s)-> {}, (f)->());
}

void onAdaptrisMessage(AdaptrisMessage msg, java.util.function.Consumer<AdaptrisMessage> success, Consumer<AdaptrisMessage> failure);
```

Then we can effectively make our SQS polling consumer this :
```
for (Message message : messages) {
  try {
    AdaptrisMessage adpMsg = AdaptrisMessageFactory.defaultIfNull(getMessageFactory()).newMessage(message.getBody());
    // stuff skipped for brevity.
    final String handle = message.getReceiptHandle();
    // on success we delete the message, on failure we leave it in situ.
    // Might need to make that configurable, since we don't want poison messages
    retrieveAdaptrisMessageListener().onAdaptrisMessage(adpMsg, (s) -> {
      sqs.deleteMessage(new DeleteMessageRequest(queueUrl, handle))
    }, (f) -> {});
    if (!continueProcessingMessages(++count)) {
      break messageCountLoop;
    }
  }
  catch (Exception e){
    log.error("Error processing message id: " + message.getMessageId(), e);
  }
}

```

* Good, because this makes the behavour controllable from the consumers perspective (e.g. fs-consumer could wait until the workflow completed before deleting the file...)
* Good, because this will have the side effect of enabling callbacks for all consumers if they want it; which means we can get rid of the object monitors and things that we do Jetty + pooling workflow...
* Bad, because it's a callback, and it happens at some point in the future... is the session/queue whatever still valid.
* Bad, because threadsafe is hard.
* Neutral, all workflows have to change (and message listener stub implementations).

### Note 2020-02-12

The API change is going to be `void onAdaptrisMessage(AdaptrisMessage msg, Consumer<AdaptrisMessage> success);` since the semantics of how the failure will be handled (or where it should be fired) isn't clear at the moment. As of the next major release (v4); we are proposing a different way of handling things like this, since asynchronous callbacks are becoming ever more popular.