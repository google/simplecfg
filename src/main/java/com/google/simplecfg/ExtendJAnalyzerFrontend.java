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
import com.google.simplecfg.ast.Program;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Produces findings using analyzers implemented in the ExtendJ compiler.
 */
public class ExtendJAnalyzerFrontend extends Frontend {

  private final JavaParser javaParser;
  private final BytecodeReader bytecodeReader;
  private final Collection<ExtendJFinding> findings = new ArrayList<ExtendJFinding>();

  /** Create new analyzer instance.  */
  public ExtendJAnalyzerFrontend() {
    super("ExtendJ Analyzer", "v1.0");
    javaParser = new JavaParser() {
      @Override
      public CompilationUnit parse(InputStream is, String fileName)
          throws IOException, beaver.Parser.Exception {
        return new com.google.simplecfg.parser.JavaParser().parse(is, fileName);
      }
    };
    bytecodeReader = new BytecodeReader() {
      @Override
      public CompilationUnit read(InputStream is, String fullName, Program p)
          throws FileNotFoundException, IOException {
        return new BytecodeParser(is, fullName).parse(null, null, p);
      }
    };
  }

  /**
   * Returns the list of findings from the analyzed source files.
   *
   * <p>Used by ExtendJAnalyzerMain to print the generated findings on stdout.
   */
  Collection<ExtendJFinding> getFindings() {
    return findings;
  }

  /**
   * Analyze a single file for findings and return the findings in a collection.
   */
  public static Collection<ExtendJFinding> analyzeFile(final String path) throws Error {
    ExtendJAnalyzerFrontend checker = new ExtendJAnalyzerFrontend();
    int result = checker.run(new String[] {path});
    if (result != EXIT_SUCCESS) {
      throw new Error("exit code: " + result);
    }
    return checker.findings;
  }

  /**
   * Run the Java checker.
   * @param args command-line arguments
   * @return 0 on success, 1 on error, 2 on configuration error, 3 on system
   */
  public int run(String args[]) {
    return run(args, bytecodeReader, javaParser);
  }

  @Override
  protected int processCompilationUnit(CompilationUnit unit) {
    if (unit.fromSource()) {
      findings.addAll(unit.findings());
    }
    return EXIT_SUCCESS;
  }

  @Override
  public int run(String[] args, BytecodeReader reader, JavaParser parser) {
    program.resetStatistics();
    program.setTypeLookupFilter(Program.ANALYZER_TYPE_FILTER);
    program.initBytecodeReader(bytecodeReader);
    program.initJavaParser(javaParser);

    initOptions();
    int argResult = processArgs(args);
    if (argResult != 0) {
      return argResult;
    }

    if (program.options().hasOption("-version")) {
      printVersion();
      return EXIT_SUCCESS;
    }

    Collection<String> files = program.options().files();
    if (program.options().hasOption("-help") || files.isEmpty()) {
      printUsage();
      return EXIT_SUCCESS;
    }

    return run(files);
  }

  private int run(Collection<String> files) {
    try {
      for (String file : files) {
        // Calling addSourceFile will parse the file and add it to the program AST.
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
    } catch (IOException e) {
      throw new Error(e);
    } finally {
      if (program.options().hasOption("-profile")) {
        program.printStatistics(System.out);
      }
    }
    return EXIT_SUCCESS;
  }
}
