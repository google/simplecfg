/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.simplecfg;

import com.google.simplecfg.ast.BytecodeParser;
import com.google.simplecfg.ast.BytecodeReader;
import com.google.simplecfg.ast.CompilationUnit;
import com.google.simplecfg.ast.ExtendJFinding;
import com.google.simplecfg.ast.Frontend;
import com.google.simplecfg.ast.JavaParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Produces findings using analyzers implemented in the ExtendJ compiler.
 */
public class ExtendJAnalyzer extends Frontend {
  private final JavaParser javaParser;
  private final BytecodeParser bytecodeParser;

  /**
   * Entry point.
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    ExtendJAnalyzer checker = new ExtendJAnalyzer();
    int result = checker.run(args);
    if (result != 0) {
      System.exit(result);
    }
  }

  /** Create new analyzer instance.  */
  public ExtendJAnalyzer() {
    super("ExtendJ Analyzer", "v1.0");
    javaParser = new JavaParser() {
      @Override
      public CompilationUnit parse(java.io.InputStream is, String fileName)
        throws java.io.IOException, beaver.Parser.Exception {
        return new parser.JavaParser().parse(is, fileName);
      }
    };
    bytecodeParser = new BytecodeParser();
  }

  /**
   * Run the Java checker.
   * @param args command-line arguments
   * @return 0 on success, 1 on error, 2 on configuration error, 3 on system
   */
  public int run(String args[]) {
    return run(args, bytecodeParser, javaParser);
  }

  @Override
  protected int processCompilationUnit(CompilationUnit unit) {
    if (unit.fromSource()) {
      for (ExtendJFinding finding : unit.findings()) {
        System.out.println(finding);
      }
    }
    return EXIT_SUCCESS;
  }

  @Override
  public int run(String[] args, BytecodeReader reader, JavaParser parser) {
    program.resetStatistics();
    program.initBytecodeReader(reader);
    program.initJavaParser(parser);

    initOptions();
    int argResult = processArgs(args);
    if (argResult != 0) {
      return argResult;
    }

    Collection<String> files = program.options().files();
    if (program.options().hasOption("-version")) {
      printVersion();
      return EXIT_SUCCESS;
    }
    if (program.options().hasOption("-help") || files.isEmpty()) {
      printUsage();
      return EXIT_SUCCESS;
    }

    try {
      for (String file : files) {
        program.addSourceFile(file);
      }

      // Process source compilation units.
      int compileResult = EXIT_SUCCESS;

      Iterator<CompilationUnit> iter = program.compilationUnitIterator();
      while (iter.hasNext()) {
        CompilationUnit unit = iter.next();
        int result = processCompilationUnit(unit);
        if (result != EXIT_SUCCESS) {
          compileResult = result;
          if (compileResult == EXIT_UNHANDLED_ERROR) {
            // Stop immediately when an unhandled error is encountered.
            return compileResult;
          }
        }
      }

      if (compileResult != EXIT_SUCCESS) {
        return compileResult;
      }
    } catch (Throwable t) {
      System.err.println("Errors:");
      System.err.println("Fatal exception:");
      t.printStackTrace(System.err);
      return EXIT_UNHANDLED_ERROR;
    } finally {
      if (program.options().hasOption("-profile")) {
        program.printStatistics(System.out);
      }
    }
    return EXIT_SUCCESS;
  }
}
