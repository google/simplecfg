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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.simplecfg.ast.CompilationUnit;
import com.google.simplecfg.ast.ExtendJFinding;
import com.google.simplecfg.ast.FileClassSource;
import com.google.simplecfg.ast.JavaParser;
import com.google.simplecfg.ast.Program;
import com.google.simplecfg.ast.SourceFolderPath;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/** Unit tests for the already-closed checker. */
@RunWith(JUnit4.class)
public class NullableDereferenceTest {

  /** Helper to get the findings for a given file. */
  private static Collection<String> findings(String filename) {
    String path = "testdata/" + filename + ".javax";
    try {
      JavaParser parser = new JavaParser() {
        @Override
        public CompilationUnit parse(java.io.InputStream is,
            String fileName) throws java.io.IOException,
            beaver.Parser.Exception {
          return new parser.JavaParser().parse(is, fileName);
        }
      };
      Program program = new Program();
      CompilationUnit unit = parser.parse(new FileInputStream(path), path);
      // Attach the parsed unit to a program node so we have a healthy AST.
      program.addCompilationUnit(unit);
      // Ensure compilation unit is set to final. This is important to get
      // caching to work right in the AST.
      unit = program.getCompilationUnit(0);
      unit.setClassSource(new FileClassSource(new SourceFolderPath("testdata"), filename));
      Collection<String> findings = new HashSet<String>();
      for (ExtendJFinding finding : unit.findings()) {
        findings.add(finding.toString());
      }
      return findings;
    } catch (Exception e) {
      e.printStackTrace();
      fail("failed to parse test input file: " + path);
    }
    // Failure.
    return Collections.emptySet();
  }


  @Test public void test01() {
    Collection<String> findings = findings("Nullable01");
    assertThat(findings).containsExactly(
        "Nullable01:39:25: Dereferencing p, which was declared @Nullable.",
        "Nullable01:46:12: Dereferencing p, which was declared @Nullable.",
        "Nullable01:59:12: Dereferencing p, which was declared @Nullable."
        );
  }

  @Test public void test02() {
    Collection<String> findings = findings("Nullable02");
    assertThat(findings).containsExactly(
        "Nullable02:44:7: Dereferencing p, which was declared @Nullable.",
        "Nullable02:49:7: Dereferencing p, which was declared @Nullable."
        );
  }

  @Test public void test03() {
    Collection<String> findings = findings("Nullable03");
    assertThat(findings).isEmpty();
  }
}
