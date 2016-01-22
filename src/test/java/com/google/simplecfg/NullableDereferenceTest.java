/**
 * Copyright 2016 Google Inc. All Rights Reserved.
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

import com.google.simplecfg.ast.CompilationUnit;
import com.google.simplecfg.ast.ExtendJFinding;
import com.google.simplecfg.ast.Program;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collection;

/**
 * Integration tests for the nullable dereference checker.
 *
 * <p>Tests are grouped by the type of null guard used, and then split arbitrarily into separate
 * tests/files in order to not have too many positive/negative finding tests in a single test file.
 */
@RunWith(JUnit4.class)
public class NullableDereferenceTest {

  @Test public void suggestedFixEndsWithNewline() {
    CompilationUnit unit = StmtCfgTest.parseFile("NullableNullGuard01",
        Program.ANALYZER_TYPE_FILTER);
    Collection<ExtendJFinding> findings = unit.findings();
    assertThat(findings).isNotEmpty();
    ExtendJFinding finding = findings.iterator().next();
    assertThat(finding.fixes).hasSize(1);
    assertThat(finding.fixes.iterator().next().newText).endsWith("\n");
  }

  @Test public void nullGuards01() {
    Collection<String> findings = StmtCfgTest.findings("NullableNullGuard01");
    assertThat(findings).containsExactly(
        "testdata/NullableNullGuard01.javax:42:25: Dereferencing p, which was declared @Nullable.",
        "testdata/NullableNullGuard01.javax:49:12: Dereferencing p, which was declared @Nullable.",
        "testdata/NullableNullGuard01.javax:62:12: Dereferencing p, which was declared @Nullable.",
        "testdata/NullableNullGuard01.javax:93:12: Dereferencing q, which was declared @Nullable."
        );
  }

  @Test public void nullGuards02() {
    Collection<String> findings = StmtCfgTest.findings("NullableNullGuard02");
    assertThat(findings).containsExactly(
        "testdata/NullableNullGuard02.javax:49:7: Dereferencing p, which was declared @Nullable.",
        "testdata/NullableNullGuard02.javax:54:7: Dereferencing p, which was declared @Nullable."
        );
  }

  @Test public void nullGuards03() {
    Collection<Integer> lines = StmtCfgTest.findingLines("NullableNullGuard03",
        Program.ANALYZER_TYPE_FILTER);
    assertThat(lines).containsExactly(28, 34, 41, 48, 113, 119);
  }

  @Test public void methodNullGuard01() {
    Collection<String> findings = StmtCfgTest.findings("NullableMethodNullGuard01");
    assertThat(findings).isEmpty();
  }

  @Test public void dataflow01() {
    Collection<String> findings = StmtCfgTest.findings("NullableDataflow01");
    assertThat(findings).containsExactly(
        "testdata/NullableDataflow01.javax:27:7: Dereferencing p, which was declared @Nullable.",
        "testdata/NullableDataflow01.javax:35:7: Dereferencing p, which was declared @Nullable."
        );
  }

  @Test public void instanceOf() {
    Collection<String> findings = StmtCfgTest.findings("NullableInstanceOf");
    assertThat(findings).isEmpty();
  }

  @Test public void variableArity() {
    Collection<String> findings = StmtCfgTest.findings("NullableVariableArity");
    assertThat(findings).isEmpty();
  }

  @Test public void nullableDereference01() {
    Collection<String> findings = StmtCfgTest.findings("NullableDereference01");
    assertThat(findings).containsExactly(
        "testdata/NullableDereference01.javax:27:12: Dereferencing p, which was declared @Nullable.",
        "testdata/NullableDereference01.javax:31:12: Dereferencing p, which was declared @Nullable."
        );
  }

  /** Test false positive for GitHub issue #10. */
  @Test public void issue10() {
    Collection<String> findings = StmtCfgTest.findings("NullableDereferenceIssue10");
    assertThat(findings).containsExactly(
        "testdata/NullableDereferenceIssue10.javax:34:31: Dereferencing y, which was declared @Nullable."
        );
  }

  @Test public void issue11() {
    Collection<String> findings = StmtCfgTest.findings("NullableDereferenceIssue11");
    assertThat(findings).isEmpty();
  }

  @Test public void issue12() {
    Collection<String> findings = StmtCfgTest.findings("NullableDereferenceIssue12");
    assertThat(findings).isEmpty();
  }

  @Test public void eqExpr() {
    Collection<String> findings = StmtCfgTest.findings("NullableDereferenceEqExpr");
    assertThat(findings).isEmpty();
  }

  @Test public void neExpr() {
    Collection<String> findings = StmtCfgTest.findings("NullableDereferenceNeExpr");
    assertThat(findings).isEmpty();
  }

  @Test public void methodCall() {
    Collection<String> findings = StmtCfgTest.findings("NullableDereferenceMethodCall");
    assertThat(findings).containsExactly(
        "testdata/NullableDereferenceMethodCall.javax:41:16: Dereferencing p, which was declared @Nullable."
        );
  }
}
