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

import com.google.simplecfg.ast.Program;
import com.google.simplecfg.ast.Frontend;
import com.google.simplecfg.ast.CompilationUnit;
import com.google.simplecfg.ast.BytecodeParser;
import com.google.simplecfg.ast.JavaParser;
import com.google.simplecfg.ast.BodyDecl;
import com.google.simplecfg.ast.TypeDecl;

import java.io.FileInputStream;

/** Generate test cases for the first CFG of each input class. */
class TestGenerator {

  private final JavaParser javaParser;

  public TestGenerator() {
    javaParser = new JavaParser() {
      @Override
      public CompilationUnit parse(java.io.InputStream is, String fileName)
          throws java.io.IOException, beaver.Parser.Exception {
        return new com.google.simplecfg.parser.JavaParser().parse(is, fileName);
      }
    };
  }

  public static void main(String args[]) {
    int exitCode = new TestGenerator().run(args);
    if (exitCode != 0) {
      System.exit(exitCode);
    }
  }

  private int run(String args[]) {
    for (String path : args) {
      try {
        Program program = new Program();
        CompilationUnit unit = javaParser.parse(new FileInputStream(path), path);
        // Attach the parsed unit to a program node so we have a healthy AST.
        program.addCompilationUnit(unit);
        // Ensure compilation unit is set to final. This is important to get
        // caching to work right in the AST.
        unit = program.getCompilationUnit(0);
        if (unit.getNumTypeDecl() < 1) {
          System.err.println("Error: no classes declared in file " + path);
          return 1;
        }
        TypeDecl type = unit.getTypeDecl(0);
        if (type.getNumBodyDecl() < 1) {
          System.err.println("Error: first class has no body decls in file " + path);
          return 1;
        }
        BodyDecl bd = type.getBodyDecl(0);
        bd.printCfgTest();
      } catch (Exception e) {
        System.err.println("Failed to parse input file: " + path);
        e.printStackTrace();
        return 1;
      }
    }
    return 0;
  }
}
