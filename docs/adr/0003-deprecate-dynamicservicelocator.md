---
layout: page
title: 0003-deprecate-dynamicservicelocator
---
# Deprecate DynamicServiceLocator in favour of DynamicServicExecutor

* Status: Accepted
* Deciders: Aaron McGrath, Lewin Chan
* Date: 2019-03-21

## Context and Problem Statement

With the standard document trading network, there is a move towards centralised processing such that companies need only define their "mapping specification" and key information, and everything is handled centrally.

Traditionally, this was done via DynamicServiceLocator with files located on the fileystem named in the form : `SRC-DST-MSG_TYPE.xml`; recently, this has been done in a dedicated application which is now deprecated.

The way that DynamicServiceLocator works contains a lot of extraneous configuration that only have a single implementation; it was designed for extensibility, but it's over complicated in terms of XML and coupling. It needs to be simplified so that it's more understandable in the UI.

## Decision

Deprecate DynamicServiceLocator, leave it available but marked as deprecated. Improve DynamicServiceExcutor so that it can be used instead.

### Enhancing DynamicServiceExecutor.

What this means is to implement additional ServiceExtractor implementations that allow the user to extract the _`_service-to-execute_ from an external location; currently the two supplied implementations simply use the _AdaptrisMessage_ object to extract the services.

The additions required are :
* Extract from a URL such that you can configure `http://my.server.com/%message{source}/%message{destination}/%message{messageType}.xml`. This would replace the existing DynamicServiceLocator functionality from _RemoteServiceStore_ and _LocalServiceStore_.
* Extract from Database -> such that you can configure something like `SELECT dynamicService FROM services WHERE src='%message{source}' AND dest='%message{destination}' AND msgType='%message{messageType}'`
  * The above would be quite "open" to SQL injection style attacks; so people should probably use _JdbcDataQueryService_ and then handle it from metadata.
* Extract from a Cache -> similar to doing a RetrieveFromCache
* Extract it from metadata.

## Consequences

DynamicServiceLocator and associated classes (such as TradingRelationship etc) are now marked as deprecated since 3.8.4, with a Removal notice for 3.11.0. This will show up in 3.8.4 releases on the UI page.
