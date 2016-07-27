Simple CFG module for ExtendJ
=============================

This is a Control Flow Graph (CFG) module for the ExtendJ compiler for building
simplified CFGs. This module builds CFGs for Java methods where only branches
and method calls have are included.  These simple CFGs provide enough
information to perform intraprocedural static analyses on Java code.

This repository also includes two sample Java static analyzers based on the this
CFG module.  One analyzer checks for additional calls to a
java.io.Reader/java.io.Writer after `close()` was called on the same instance.
The other analyzer checks for potential `null` dereferences on paramters
annotated with javax.annotation.Nullable.

Disclaimer
----------

This is not an official Google product (experimental or otherwise), it is just
code that happens to be owned by Google.

Shipshape Module
----------------

The demo analyzers can be plugged into the [Shipshape][1] pipeline. The
Shipshape integration is currently experimental.

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

Note that you must have the Git submodule `third_party/extendj/git` in
order to build SimpleCFG. To download the submodule, use the following commands:

    git submodule init
    git submodule update


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

[1]: https://github.com/google/shipshape
