#==============================================================================
# PROJECT       : Transport Framework
# TYPE          : Makefile
# $Workfile: $
# $Revision: 1.1.1.1 $
# $Author: lchan $
# $Date: 2002/05/13 12:32:07 $
#==============================================================================
# VPATH sets the directory seach, i.e. if it can't find x.java
# in the current directory off it goes down VPATH to look for the file
MODULE_PATH=com/adaptris/transport
VPATH=$(MODULE_PATH)
MODULE_NAME=$(MODULE)

# Take a listing of all the java files in this module
JAVA_SRC = $(shell ls $(MODULE_PATH)/*.java)
# Substitute .java with .class
JAVA_CLASS = $(subst .java,.class,$(JAVA_SRC))
# All .class files make up our list of targets
TARGETS = $(JAVA_CLASS)


# Work out the manifest file and use it
MANIFEST=$(wildcard $(MODULE_PATH)/Manifest.mf)
ifneq (,$(MANIFEST))
   JAR_OPT=-cfm $(MODULE_NAME).jar $(MANIFEST)
else
   JAR_OPT=-cf $(MODULE_NAME).jar
endif

# Create a real to compile java class files
%.class: %.java
	@echo compiling $^
	@$(JAVAC) $(JAVAC_OPT) $^

# The compile directive is dependent on each of the .class files being
# up-to-date.
# the $(subst) command simply substitues all the / in VPATH for .
# each command to be executed is prefixed by a <tab> character
compile: $(TARGETS)
	@echo package $(subst /,.,$(MODULE_PATH)) compilation successful

# Remove executables and object files
#
clean:
	@rm -f $(TARGETS)

#
# Create distribution object.
distr:
	jar $(JAR_OPT) $(MODULE_PATH)/*.class

.PHONY: compile clean
