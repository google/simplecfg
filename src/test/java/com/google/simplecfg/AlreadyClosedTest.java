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

import com.google.simplecfg.ast.Program;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collection;

/** Integration tests for the already-closed checker. */
@RunWith(JUnit4.class)
public class AlreadyClosedTest {

  @Test public void test01() {
    Collection<String> findings = StmtCfgTest.findings("Close01", Program.NO_TYPE_FILTER);
    assertThat(findings).containsExactly(
        "testdata/Close01.javax:23:5: close() may have already been called on writer at this point");
  }

  /**
   * Test that an already-closed finding was generated on the correct line for a simple positive
   * test case.
   * 
   * <p>This test case effectively checks that the type analysis works because the type used is
   * java.io.Writer, and the analyzer will check if that type is a subtype of java.io.Closeable.
   */
  @Test public void writer01() {
    Collection<String> findings = StmtCfgTest.findings("AlreadyClosedWriter01",
        Program.NO_TYPE_FILTER);
    assertThat(findings).hasSize(1);
    assertThat(findings).containsExactly(
        "testdata/AlreadyClosedWriter01.javax:27:5: close() may have already been called on writer at this point");
  }

  @Test public void controlFlow01() {
    Collection<Integer> lines = StmtCfgTest.findingLines("AlreadyClosedControlFlow01",
        Program.NO_TYPE_FILTER);
    assertThat(lines).containsExactly(34, 60, 68, 79, 84, 103, 118);
  }

  @Test public void negativeFindings01() {
    Collection<String> findings = StmtCfgTest.findings("AlreadyClosedNegativeFindings01",
        Program.NO_TYPE_FILTER);
    assertThat(findings).isEmpty();
  }
}
