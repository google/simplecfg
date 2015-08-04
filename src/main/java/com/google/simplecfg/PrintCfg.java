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
import com.google.simplecfg.ast.BodyDecl;
import com.google.simplecfg.ast.TypeDecl;
import com.google.simplecfg.parser.JavaParser;

import java.io.FileInputStream;
import java.util.Set;
import java.util.HashSet;

/**
 * Prints a Simplified Control Flow Graph for the first method in a Java program.
 */
public class PrintCfg {

  public static void main(String args[]) {
    int exitCode = new PrintCfg().run(args);
    if (exitCode != 0) {
      System.exit(exitCode);
    }
  }

  private int run(String args[]) {
    Set<String> argSet = new HashSet<>();
    for (String arg : args) {
      argSet.add(arg);
    }
    boolean reverse = argSet.contains("-reverse");
    for (String path : args) {
      if (!path.equals("-reverse")) {
        try {
          Program program = new Program();
          program.setTypeLookupFilter(Program.BASE_LIBRARY_FILTER);
          CompilationUnit unit = new JavaParser().parse(new FileInputStream(path), path);
          // Attach the parsed unit to a program node so we have a healthy AST.
          program.addCompilationUnit(unit);
          // Ensure compilation unit is set to final. This is important to get
          // caching to work right in the AST.
          unit = program.getCompilationUnit(0);
          for (TypeDecl type : unit.getTypeDeclList()) {
            for (BodyDecl bd : type.getBodyDeclList()) {
              if (reverse) {
                bd.printReverseCfg();
              } else {
                bd.printCfg();
              }
            }
          }
        } catch (Exception e) {
          System.err.println("Failed to parse input file: " + path);
          e.printStackTrace();
          return 1;
        }
      }
    }
    return 0;
  }
}
