Simple CFG ExtendJ Module
=========================

This is a Control Flow Graph (CFG) module for the ExtendJ compiler for building
simplified CFGs. This module builds CFGs for Java methods where only branches
and method calls have are included.  These simple CFGs provide enough
information to perform intraprocedural static analyses on Java code.

This repository also includes a sample analyzer which checks if there are
additional calls to a java.io.Reader/java.io.Writer after `close()` was called
on the same instance.

Disclaimer
----------

This is not an official Google product (experimental or otherwise), it is just
code that happens to be owned by Google.

Dependencies
------------

To build the Simplified CFG generator you need the following dependencies:

* Git
* Gradle 2.4
* ExtendJ

This repository has a submodule for the ExtendJ compiler. If you did not clone
this repository with the `--recursive` flag you will have to run `git submodule
init` followed by `git submodule update`, this will clone a specific commit from
the ExtendJ repository into the `third_party/extendj/` directory.

Building
--------

Build the Simplified CFG generator Jar file by running the following Gradle command:

    gradle jar

Testing
-------

The tests may be run by issuing the following command:

    gradle test

Most tests check that a well-formed SimpleCFG is built for each Java file in the
testdata directory. The tests are structured so that they test the successors of
each node in the resulting CFG for the single block/method in each of the Java files.

You can generate images for the CFGs in each test file by running the `graph.sh`
shell script.
