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

import com.google.simplecfg.ast.ExtendJFinding;

/**
 * Produces findings using analyzers implemented in the ExtendJ compiler.
 */
public class ExtendJAnalyzerMain {

  /**
   * Entry point.
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    ExtendJAnalyzerFrontend checker = new ExtendJAnalyzerFrontend();
    int result = checker.run(args);
    if (result != 0) {
      System.exit(result);
    }
    System.out.println("Found " + checker.getFindings().size() + " findings.");
    for (ExtendJFinding finding : checker.getFindings()) {
      System.out.println(finding);
    }
  }
}
