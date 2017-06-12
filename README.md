# Interlok [![Build Status](https://travis-ci.org/adaptris/interlok.svg?branch=develop)](https://travis-ci.org/adaptris/interlok) [![codecov](https://codecov.io/gh/adaptris/interlok/branch/develop/graph/badge.svg)](https://codecov.io/gh/adaptris/interlok)

This is the base repository for Interlok. It contains the base adapter pared of all 3rd party dependencies other than open source ones. There is a single dependency (commented out) on Microsoft SQL Server JDBC provider which is not required unless you explicitly want to test JDBC against SQL Server.

## Setting up your Development Environment ##

### Pre-requisites ###

* ANT (1.9+)
* JAVA 1.8 (it is compiled with target=1.7, but generating javadocs has some specific 1.8 only flags).

### Optional pre-reqs ###

* MySQL (for tests)
* SFTP Server (for tests)

## Doing your first build ##


```
#!shell

cd ./adapter
ant jar
# You will see if you have setup your environment properly.
ant test
# This will take about 10 minutes, and at the end there will be a ./adapter/target/testoutput/html directory that contains the test output.
```
### Fixing your build ###

Your first build will have errors!

* Have you installed the (JCE) Unlimited Strength Jurisdiction Policy Files files? (some of the unit tests rely on strong crypto...) - http://www.oracle.com/technetwork/java/javase/downloads/index.html
* Windows User? then you will have to *at least once* encrypt a file so that an EFS certificate is created (for com.adaptris.security.password)
* Windows User? then you may not have an echo.exe which TestPPP relies on (com.adaptris.transport) - Either install cygwin from http://www.cygwin.com and make sure c:\cygwin\bin is in the path or perhaps create an echo.bat somewhere in the path (batch file option untested).


### JDK work-arounds ###

* Windows User? With a 32bit JVM you might get some stupid __Could not reserve enough space for object heap/OutOfMemory__ errors when starting ant, executing the unit tests, or executing the junit test reports at the end.
    * If you are using cygwin, then in your ~/.ant/ant.conf file have ```ANT_OPTS="-Xmx1024m"``` which sets the "initial ant memory to have a max of 1GB"; this should resolve any "OutOfMemory" errors that you might get running the post test report transforms.
    * If you are using the batch file (i.e. you're using the command prompt), then either set an environment variable ANT_OPTS to the same values as above
    * Modify the build properties _execution.maxmem_ and _execution.maxpermsize_ (as discussed below) to suit your environment.

## Modifying your build ##

Everything relies on build.properties; in the previous section you didn't make any changes, so it skipped a bunch of tests that required the optional components. You might want to enable those tests, possibly because you're working on fixing those components or well whatever. So what you will need to do is to create your own copy of build.properties which is simply a standard "properties" file with key=value entries.

### Standard properties ###

Property Key | Default Value | Description | Notes
------------ | ------------- | ----------- | -----
run.coverage| not defined |Setting this to true means that you try add coverage support during _ant test_ giving you a report about code coverage|It does add some time to the tests, and you'll see a new directory appear in testoutput/coverage |
junit.test.classes|```**/*Test*.java```|Standard ant filter so that when you run _ant test_ it only tests what you want to test ||
junit.forkmode|perTest|This controls how the junit tests are forked internally by ant|_perTest_ is slow, but guarantees isolation, _once_ means we fork the JVM once
execution.maxmem|1024m|How much memory you want forked processes to have, translates directly into a jvmarg of -Xmx${execution.maxmem}||
execution.maxpermsize|128m|How much memory for permanent generation you want forked processes to have, translates directly into a jvmarg of -XX:MaxPermSize=${execution.maxpermsize}|
default.ftp.tests.enabled|false| Whether or not FTP and SFTP tests are executed | This overrides the setting in default-test.properties.template, you will probably have to define additional properties if you want to test FTP/SFTP
default.jdbc.storedproc.tests.enabled| false | Whether or not the stored procedure tests are executed|This overrides a setting in default-test.properties.template, you will probably have to define additional properties if you want to test JDBC Stored procedures


So for instance if I only wanted to test the metadata services, run jacoco over the tests to find out if the code coverage is truly awesome, and make the tests _run somewhat faster_ then my build.properties would look like


```
#!properties

run.coverage=true
junit.test.classes=**/services/metadata/*Test*.java
junit.forkmode=once
```

You will see some additional files in resources/tests that have a hostname suffix; these are dedicated property files for running tests on those machines (which basically correspond to the machines that run hudson slaves); unless you're changing the property templates you probably don't need to touch these. You can refer to them to see how you can control the environment for your own test environment.


## How to publish your changes ##

If you've made some changes, and you want to check that the downstream projects (like interlok-optional) are OK with your changes, then the easiest solution is to build the snapshot release from Jenkins. This will trigger all the downstream builds once successful (it might take a while) and you'll be able to see any impact your changes may have had.


And that's it.
