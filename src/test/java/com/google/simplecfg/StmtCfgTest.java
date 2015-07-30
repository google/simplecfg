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

import com.google.simplecfg.ast.CfgNode;
import com.google.simplecfg.ast.CompilationUnit;
import com.google.simplecfg.ast.ExtendJFinding;
import com.google.simplecfg.ast.FileClassSource;
import com.google.simplecfg.ast.JavaParser;
import com.google.simplecfg.ast.Program;
import com.google.simplecfg.ast.SourceFolderPath;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/** Tests for simplified Control Flow Graphs built for methods/constructors/initializers.  */
@RunWith(JUnit4.class)
public class StmtCfgTest {

  /** Helper method to parse an ExtendJ compilation unit from a file.  */
  private static CompilationUnit parseFile(String filename) {
    String path = "testdata/" + filename + ".javax";
    try {
      JavaParser parser = new JavaParser() {
        @Override
        public CompilationUnit parse(java.io.InputStream is,
            String fileName) throws java.io.IOException,
            beaver.Parser.Exception {
          return new com.google.simplecfg.parser.JavaParser().parse(is, fileName);
        }
      };
      Program program = new Program();
      program.setTypeLookupFilter(Program.ANALYZER_TYPE_FILTER);
      CompilationUnit unit = parser.parse(new FileInputStream(path), path);
      // Attach the parsed unit to a program node so we have a healthy AST.
      program.addCompilationUnit(unit);
      // Ensure compilation unit is set to final. This is important to get
      // caching to work right in the AST.
      unit = program.getCompilationUnit(0);
      unit.setClassSource(new FileClassSource(new SourceFolderPath("testdata"), filename));
      return unit;
    } catch (Exception e) {
      e.printStackTrace();
      fail("failed to parse test input file: " + path);
    }
    // Failed.
    return null;
  }

  /** Helper to get the findings for a given file. */
  protected static Collection<String> findings(String filename) {
    CompilationUnit unit = StmtCfgTest.parseFile(filename);
    Collection<String> findings = new HashSet<String>();
    for (ExtendJFinding finding : unit.findings()) {
      findings.add(finding.toString());
    }
    return findings;
  }

  private static CfgNode parseCfg(String filename) {
    CompilationUnit unit = parseFile(filename);
    assertThat(unit.getTypeDeclList()).isNotEmpty();
    assertThat(unit.getTypeDecl(0).getBodyDeclList()).isNotEmpty();
    return unit.getTypeDecl(0).getBodyDecl(0).entry();
  }

  /**
   * Assert and return a single successor of the node.
   */
  private static CfgNode succ(CfgNode node, String successor) {
    assertThat(cfgNames(node.successors())).containsExactly(successor);
    return node.successors().iterator().next();
  }

  /**
   * Assert the successors of the node and return them in an array
   * using the same ordering as the input array.
   */
  private static CfgNode[] succ(CfgNode node, String... successors) {
    assertThat(cfgNames(node.successors())).containsExactly((Object[]) successors);

    // Ensure no duplicate successor names.
    Set<String> dups = new HashSet<String>();
    for (String succ : successors) {
      if (dups.contains(succ)) {
        fail("can not assert successors with duplicate names");
      }
      dups.add(succ);
    }

    Map<String, CfgNode> successorMap = new HashMap<>();
    for (CfgNode successor : node.successors()) {
      successorMap.put(successor.toString(), successor);
    }

    CfgNode[] result = new CfgNode[successors.length];
    for (int i = 0; i < successors.length; ++i) {
      result[i] = successorMap.get(successors[i]);
    }
    return result;
  }

  /** Convert a collection of CfgNodes to a collection of of the node names.  */
  private static Collection<String> cfgNames(Iterable<? extends CfgNode> cfgs) {
    // Use a linked list because we need to preserve duplicate names.
    // TODO(joqvist): Use Java 8 stream api to map CfgNode -> String.
    Collection<String> names = new LinkedList<>();
    for (CfgNode cfg : cfgs) {
      names.add(cfg.toString());
    }
    return names;
  }

  @Test public void ifStmt01() {
    CfgNode entry = parseCfg("IfStmt01");
    CfgNode call1 = succ(entry, "call1()");
    CfgNode branch = succ(call1, "if (call1())");
    CfgNode[] targets = succ(branch, "onTrue()", "exit");
    CfgNode thenEnd = succ(targets[0], "then-end");
    assertThat(succ(thenEnd, "exit")).isSameAs(targets[1]);
  }

  @Test public void ifStmt02() {
    CfgNode entry = parseCfg("IfStmt02");
    CfgNode call1 = succ(entry, "call1()");
    CfgNode branch = succ(call1, "if (call1())");
    CfgNode[] targets = succ(branch, "onTrue()", "onFalse()");
    CfgNode thenEnd = succ(targets[0], "then-end");
    CfgNode elseEnd = succ(targets[1], "else-end");
    assertThat(succ(thenEnd, "exit")).isSameAs(succ(elseEnd, "exit"));
  }

  @Test public void ifStmt03() {
    CfgNode entry = parseCfg("IfStmt03");
    CfgNode call1 = succ(entry, "call1()");
    CfgNode branch = succ(call1, "if (call1())");
    CfgNode[] targets = succ(branch, "onTrue()", "onFalse()");
    succ(succ(targets[0], "then-end"), "exit");
    succ(targets[1], "call2()");
  }

  @Test public void ifStmt04() {
    CfgNode entry = parseCfg("IfStmt04");
    CfgNode call1 = succ(entry, "call1()");
    CfgNode branch = succ(call1, "if (call1())");
    CfgNode[] targets = succ(branch, "onTrue()", "onFalse()");
    succ(targets[0], "call2()");
    succ(succ(targets[1], "else-end"), "exit");
  }

  @Test public void forStmt01() {
    CfgNode entry = parseCfg("ForStmt01");
    CfgNode call1 = succ(entry, "call1()");
    CfgNode branch = succ(call1, "for (call1())");
    CfgNode[] targets = succ(branch, "call1()", "exit");
    assertThat(targets[0]).isSameAs(call1);
  }

  @Test public void forStmt02() {
    CfgNode entry = parseCfg("ForStmt02");
    CfgNode call1 = succ(entry, "call1()");
    CfgNode branch = succ(call1, "for (call1())");
    CfgNode[] targets = succ(branch, "call2()", "exit");
    assertThat(succ(targets[0], "call1()")).isSameAs(call1);
  }

  @Test public void forStmt03() {
    CfgNode entry = parseCfg("ForStmt03");
    CfgNode init = succ(entry, "init()");
    CfgNode cond = succ(init, "cond()");
    CfgNode branch = succ(cond, "for (cond())");
    CfgNode[] targets = succ(branch, "stmt()", "exit");
    CfgNode stmt = targets[0];
    CfgNode update = succ(stmt, "update()");
    assertThat(succ(update, "cond()")).isSameAs(cond);
  }

  @Test public void forStmt04() {
    CfgNode entry = parseCfg("ForStmt04");
    CfgNode i = succ(entry, "i()");
    CfgNode j = succ(i, "j()");
    CfgNode cond = succ(j, "cond()");
    CfgNode branch = succ(cond, "for (cond())");
    CfgNode[] targets = succ(branch, "stmt()", "exit");
    CfgNode stmt = targets[0];
    CfgNode u1 = succ(stmt, "u1()");
    CfgNode u2 = succ(u1, "u2()");
    CfgNode u3 = succ(u2, "u3()");
    assertThat(succ(u3, "cond()")).isSameAs(cond);
  }

  @Test public void forStmt05() {
    CfgNode entry = parseCfg("ForStmt05");
    CfgNode i = succ(entry, "i()");
    CfgNode branch = succ(i, "for (false)");
    CfgNode y = succ(branch, "y()");
    succ(y, "exit");
  }

  @Test public void whileStmt01() {
    CfgNode entry = parseCfg("WhileStmt01");
    CfgNode branch = succ(entry, "while (true)");
    assertThat(succ(branch, "while (true)")).isSameAs(branch);
  }

  @Test public void whileStmt02() {
    CfgNode entry = parseCfg("WhileStmt02");
    CfgNode cond = succ(entry, "cond()");
    CfgNode branch = succ(cond, "while (cond())");
    CfgNode[] targets = succ(branch, "cond()", "exit");
    assertThat(targets[0]).isSameAs(cond);
  }

  @Test public void whileStmt03() {
    CfgNode entry = parseCfg("WhileStmt03");
    CfgNode whileBranch = succ(entry, "while (true)");
    CfgNode cond = succ(whileBranch, "cond()");
    CfgNode ifBranch = succ(cond, "if (cond())");
    CfgNode[] whileSucc = succ(ifBranch, "while (true)", "break");
    assertThat(whileSucc[0]).isSameAs(whileBranch);
    succ(succ(whileSucc[1], "tail()"), "exit");
  }

  @Test public void whileStmt04() {
    CfgNode entry = parseCfg("WhileStmt04");
    CfgNode whileBranch = succ(entry, "while (true)");
    CfgNode cond = succ(whileBranch, "cond()");
    CfgNode ifBranch = succ(cond, "if (cond())");
    CfgNode[] ifSucc = succ(ifBranch, "continue", "x()");
    assertThat(succ(ifSucc[0], "while (true)")).isSameAs(whileBranch);
    CfgNode y = succ(succ(ifSucc[1], "break"), "y()");
    succ(y, "exit");
  }

  @Test public void whileStmt05() {
    CfgNode entry = parseCfg("WhileStmt05");
    CfgNode branch = succ(entry, "while (false)");
    CfgNode y = succ(branch, "y()");
    succ(y, "exit");
  }

  @Test public void doStmt01() {
    CfgNode entry = parseCfg("DoStmt01");
    CfgNode x = succ(entry, "x()");
    CfgNode y = succ(x, "y()");
    CfgNode branch = succ(y, "do_while (y())");
    CfgNode[] targets = succ(branch, "z()", "x()");
    succ(targets[0], "exit");
    assertThat(succ(targets[1], "y()")).isSameAs(y);
  }

  @Test public void doStmt02() {
    CfgNode entry = parseCfg("DoStmt02");
    CfgNode x = succ(entry, "x()");
    CfgNode branch = succ(x, "do_while (false)");
    CfgNode y = succ(branch, "y()");
    succ(y, "exit");
  }

  @Test public void enhancedFor01() {
    CfgNode entry = parseCfg("EnhancedFor01");
    CfgNode aList = succ(entry, "aList()");
    CfgNode branch = succ(aList, "for (int a : aList())");
    CfgNode[] targets = succ(branch, "x()", "exit");
    assertThat(succ(targets[0], "aList()")).isSameAs(aList);
  }

  @Test public void methodAccess01() {
    CfgNode entry = parseCfg("MethodAccess01");
    CfgNode p1 = succ(entry, "p1()");
    CfgNode p2 = succ(p1, "p2()");
    CfgNode p3 = succ(p2, "p3()");
    CfgNode x = succ(p3, "x()");
    succ(x, "exit");
  }

  @Test public void conditionalExpr01() {
    CfgNode entry = parseCfg("ConditionalExpr01");
    CfgNode x = succ(entry, "x()");
    CfgNode branch = succ(x, "if (x())");
    CfgNode[] targets = succ(branch, "y()", "z()");
    CfgNode thenEnd = succ(targets[0], "then-end");
    CfgNode elseEnd = succ(targets[1], "else-end");
    // Assert that the branches converge on exit.
    assertThat(succ(thenEnd, "exit")).isSameAs(succ(elseEnd, "exit"));
  }

  @Test public void switchStmt01() {
    CfgNode entry = parseCfg("SwitchStmt01");
    CfgNode expr = succ(entry, "expr()");
    CfgNode branch = succ(expr, "switch (expr())");
    CfgNode[] targets = succ(branch, "x()", "y()", "z()", "d()");
    assertThat(succ(targets[0], "y()")).isSameAs(targets[1]);
    CfgNode exit = succ(succ(targets[1], "break"), "exit");
    assertThat(succ(targets[2], "d()")).isSameAs(targets[3]);
    assertThat(succ(targets[3], "exit")).isSameAs(exit);
  }

  @Test public void switchStmt02() {
    CfgNode entry = parseCfg("SwitchStmt02");
    CfgNode expr = succ(entry, "expr()");
    CfgNode branch = succ(expr, "switch (expr())");
    CfgNode[] targets = succ(branch, "x()", "y()", "z()", "exit");
    assertThat(succ(targets[0], "y()")).isSameAs(targets[1]);
    assertThat(succ(succ(targets[1], "break"), "exit")).isSameAs(targets[3]);
    assertThat(succ(targets[2], "exit")).isSameAs(targets[3]);
  }

  @Test public void tryStmt01() {
    CfgNode entry = parseCfg("TryStmt01");
    CfgNode tryBranch = succ(entry, "try");
    CfgNode[] trySucc = succ(tryBranch, "cond()", "x()");
    CfgNode[] condSucc = succ(trySucc[0], "exception", "if (cond())");
    CfgNode x = succ(condSucc[0], "x()");
    assertThat(x).isSameAs(trySucc[1]);
    CfgNode exit = succ(succ(x, "exception"), "exit");
    CfgNode[] ifSucc = succ(condSucc[1], "a()", "return");
    CfgNode[] aSucc = succ(ifSucc[0], "exception", "x()");
    assertThat(succ(aSucc[0], "x()")).isSameAs(x);
    assertThat(aSucc[1]).isNotSameAs(x);
    assertThat(succ(succ(aSucc[1], "y()"), "exit")).isSameAs(exit);
    CfgNode x2 = succ(ifSucc[1], "x()");
    assertThat(x2).isNotSameAs(x);
    assertThat(succ(x2, "exit")).isSameAs(exit);
  }

  @Test public void tryStmt02() {
    CfgNode entry = parseCfg("TryStmt02");
    CfgNode tryBranch = succ(entry, "try");
    CfgNode[] tryTargets = succ(tryBranch, "c1()", "c2()", "exit");
    assertThat(succ(tryTargets[0], "exit")).isSameAs(tryTargets[2]);
    assertThat(succ(tryTargets[1], "exit")).isSameAs(tryTargets[2]);
  }

  @Test public void tryStmt03() {
    CfgNode entry = parseCfg("TryStmt03");
    CfgNode tryEntry = succ(entry, "try");
    CfgNode[] targets = succ(tryEntry, "if (condition)", "x()");

    CfgNode exception = succ(targets[1], "exception");
    CfgNode exit = succ(exception, "exit");

    CfgNode[] ifSucc = succ(targets[0], "return", "x()");
    assertThat(succ(succ(ifSucc[0], "x()"), "exit")).isSameAs(exit);
    assertThat(succ(succ(ifSucc[1], "y()"), "exit")).isSameAs(exit);
  }

  @Test public void filtering01() {
    CfgNode entry = parseCfg("Filtering01");
    CfgNode i = succ(entry, "i()");
    CfgNode j = succ(i, "j()");
    CfgNode cond = succ(j, "cond()");
    CfgNode forBranch = succ(cond, "for (cond() && c == 3)");
    CfgNode[] forSucc = succ(forBranch, "stmt()", "return");
    CfgNode stmt = forSucc[0];
    CfgNode u1 = succ(stmt, "u1()");
    CfgNode u2 = succ(u1, "u2()");
    CfgNode u3 = succ(u2, "u3()");
    assertThat(succ(u3, "cond()")).isSameAs(cond);
  }

  @Test public void throwStmt01() {
    CfgNode entry = parseCfg("ThrowStmt01");
    CfgNode x = succ(entry, "x()");
    CfgNode exception = succ(x, "exception");
    succ(exception, "exit");
  }

  // Note: tests should be designed so that there is no need to test duplicate
  // successors. Simply insert an extra call at the start of one of the
  // branches.

  // Generated tests below here.
  // Be extra careful when generating a test: you must manually verify that
  // it tests the graph correctly and that the generated graph matches your
  // expectations. Watch out for nodes that should be identical but appear to
  // be separate: in some cases this is okay but it could also indicate faulty
  // caching for NTAs.

  @Test public void genTryStmt01() {
    CfgNode entry = parseCfg("GenTryStmt01");
    CfgNode tryEntry = succ(entry, "try");
    CfgNode[] targets = succ(tryEntry, "cond()", "c1()", "c2()");
    CfgNode[] targets2 = succ(targets[0], "exception", "if (cond())");
    CfgNode exit = succ(targets[1], "exit");
    assertThat(succ(targets[2], "exit")).isSameAs(exit);
    CfgNode[] targets3 = succ(targets2[0], "c1()", "c2()");
    assertThat(targets3[0]).isSameAs(targets[1]);
    assertThat(targets3[1]).isSameAs(targets[2]);
    CfgNode[] targets4 = succ(targets2[1], "exception", "x()");
    CfgNode[] targets5 = succ(targets4[0], "c1()", "c2()");
    assertThat(targets5[1]).isSameAs(targets[2]);
    assertThat(targets5[0]).isSameAs(targets[1]);
    CfgNode[] targets6 = succ(targets4[1], "exception", "exit");
    assertThat(targets6[1]).isSameAs(exit);
    CfgNode[] targets7 = succ(targets6[0], "c1()", "c2()");
    assertThat(targets7[0]).isSameAs(targets[1]);
    assertThat(targets7[1]).isSameAs(targets[2]);
  }

  @Test public void genTryStmt02() {
    CfgNode entry = parseCfg("GenTryStmt02");
    CfgNode tryEntry = succ(entry, "try");
    CfgNode[] targets = succ(tryEntry, "tryBlock()", "c2()", "c1()");
    CfgNode[] targets2 = succ(targets[0], "exception", "exit");
    assertThat(succ(targets[1], "exit")).isSameAs(targets2[1]);
    assertThat(succ(targets[2], "exit")).isSameAs(targets2[1]);
    CfgNode[] targets3 = succ(targets2[0], "c2()", "c1()");
    assertThat(targets3[0]).isSameAs(targets[1]);
    assertThat(targets3[1]).isSameAs(targets[2]);
  }

  @Test public void genTryStmt03() {
    CfgNode entry = parseCfg("GenTryStmt03");
    CfgNode tryEntry = succ(entry, "try");
    CfgNode[] targets = succ(tryEntry, "c1()", "x()");
    CfgNode f = succ(targets[0], "f()");
    CfgNode[] targets2 = succ(targets[1], "exception", "f()");
    assertThat(targets2[1]).isSameAs(f);
    CfgNode exit = succ(f, "exit");
    assertThat(succ(targets2[0], "c1()")).isSameAs(targets[0]);
  }


  @Test public void genTryStmt04() {
    CfgNode entry = parseCfg("GenTryStmt04");
    CfgNode tryEntry = succ(entry, "try");
    CfgNode[] tryEntrySucc = succ(tryEntry, "if (condition)", "x()");
    CfgNode[] ifBranchSucc = succ(tryEntrySucc[0], "a()", "x()");
    CfgNode exception = succ(tryEntrySucc[1], "exception");
    CfgNode[] aSucc = succ(ifBranchSucc[0], "exception", "return");
    CfgNode y = succ(ifBranchSucc[1], "y()");
    CfgNode exit = succ(exception, "exit");
    assertThat(succ(aSucc[0], "x()")).isSameAs(tryEntrySucc[1]);
    CfgNode x2 = succ(aSucc[1], "x()");
    assertThat(succ(y, "exit")).isSameAs(exit);
    assertThat(succ(x2, "exit")).isSameAs(exit);
  }

  @Test public void genTryStmt05() {
    CfgNode entry = parseCfg("GenTryStmt05");
    CfgNode tryEntry = succ(entry, "try");
    CfgNode[] tryEntrySucc = succ(tryEntry, "f2()", "try");
    CfgNode exception = succ(tryEntrySucc[0], "exception");
    CfgNode[] tryEntrySucc2 = succ(tryEntrySucc[1], "if (condition)", "f1()");
    CfgNode exit = succ(exception, "exit");
    CfgNode[] ifBranchSucc = succ(tryEntrySucc2[0], "exception", "f1()");
    CfgNode[] f1Succ = succ(tryEntrySucc2[1], "exception", "return");
    assertThat(succ(ifBranchSucc[0], "f1()")).isSameAs(tryEntrySucc2[1]);
    CfgNode[] f1Succ2 = succ(ifBranchSucc[1], "exception", "return");
    assertThat(succ(f1Succ[0], "f2()")).isSameAs(tryEntrySucc[0]);
    CfgNode f22 = succ(f1Succ[1], "f2()");
    assertThat(succ(f1Succ2[0], "f2()")).isSameAs(tryEntrySucc[0]);
    CfgNode f24 = succ(f1Succ2[1], "f2()");
    assertThat(succ(f22, "exit")).isSameAs(exit);
    assertThat(succ(f24, "exit")).isSameAs(exit);
  }

  @Test public void genTryStmt06() {
    CfgNode entry = parseCfg("GenTryStmt06");
    CfgNode tryEntry = succ(entry, "try");
    CfgNode[] tryEntrySucc = succ(tryEntry, "f2()", "try");
    CfgNode exception = succ(tryEntrySucc[0], "exception");
    CfgNode[] tryEntrySucc2 = succ(tryEntrySucc[1], "s()", "f1()");
    CfgNode exit = succ(exception, "exit");
    CfgNode[] sSucc = succ(tryEntrySucc2[0], "exception", "f1()");
    CfgNode[] f1Succ = succ(tryEntrySucc2[1], "exception", "return");
    assertThat(succ(sSucc[0], "f1()")).isSameAs(tryEntrySucc2[1]);
    CfgNode[] f1Succ2 = succ(sSucc[1], "exception", "return");
    assertThat(succ(f1Succ[0], "f2()")).isSameAs(tryEntrySucc[0]);
    CfgNode f22 = succ(f1Succ[1], "f2()");
    assertThat(succ(f1Succ2[0], "f2()")).isSameAs(tryEntrySucc[0]);
    CfgNode f24 = succ(f1Succ2[1], "f2()");
    assertThat(succ(f22, "exit")).isSameAs(exit);
    assertThat(succ(f24, "exit")).isSameAs(exit);
  }

  @Test public void genTryStmt07() {
    CfgNode entry = parseCfg("GenTryStmt07");
    CfgNode tryEntry = succ(entry, "try");
    CfgNode[] tryEntrySucc = succ(tryEntry, "c1()", "x()");
    CfgNode returnMarker = succ(tryEntrySucc[0], "return");
    CfgNode[] xSucc = succ(tryEntrySucc[1], "exception", "f()");
    CfgNode f = succ(returnMarker, "f()");
    assertThat(succ(xSucc[0], "c1()")).isSameAs(tryEntrySucc[0]);
    CfgNode y = succ(xSucc[1], "y()");
    CfgNode exit = succ(f, "exit");
    assertThat(succ(y, "exit")).isSameAs(exit);
  }

  @Test public void genForStmt01() {
    CfgNode entry = parseCfg("GenForStmt01");
    CfgNode forBranch = succ(entry, "for (i < 100)");
    CfgNode[] forBranchSucc = succ(forBranch, "c()", "fin()");
    CfgNode whileBranch = succ(forBranchSucc[0], "while (c())");
    CfgNode exit = succ(forBranchSucc[1], "exit");
    CfgNode[] whileBranchSucc = succ(whileBranch, "if (i >= 40)", "u()");
    CfgNode[] ifBranchSucc = succ(whileBranchSucc[0], "break", "c()");
    assertThat(ifBranchSucc[1]).isSameAs(forBranchSucc[0]);
    assertThat(succ(whileBranchSucc[1], "for (i < 100)")).isSameAs(forBranch);
    assertThat(succ(ifBranchSucc[0], "fin()")).isSameAs(forBranchSucc[1]);
  }

  @Test public void genForStmt02() {
    CfgNode entry = parseCfg("GenForStmt02");
    CfgNode forBranch = succ(entry, "for (i < 100)");
    CfgNode[] forBranchSucc = succ(forBranch, "c()", "fin()");
    CfgNode whileBranch = succ(forBranchSucc[0], "while (c())");
    CfgNode exit = succ(forBranchSucc[1], "exit");
    CfgNode[] whileBranchSucc = succ(whileBranch, "if (i >= 40)", "u()");
    CfgNode[] ifBranchSucc = succ(whileBranchSucc[0], "continue", "c()");
    assertThat(ifBranchSucc[1]).isSameAs(forBranchSucc[0]);
    assertThat(succ(whileBranchSucc[1], "for (i < 100)")).isSameAs(forBranch);
    assertThat(succ(ifBranchSucc[0], "for (i < 100)")).isSameAs(forBranch);
  }

  @Test public void genClassInstance01() {
    CfgNode entry = parseCfg("GenClassInstance01");
    CfgNode p1 = succ(entry, "p1()");
    CfgNode p2 = succ(p1, "p2()");
    CfgNode p3 = succ(p2, "p3()");
    CfgNode p4 = succ(p3, "p4()");
    CfgNode exit = succ(p4, "exit");
  }

  @Test public void genSwitchStmt01() {
    CfgNode entry = parseCfg("GenSwitchStmt01");
    CfgNode whileBranch = succ(entry, "while (x + y == 400 - z)");
    CfgNode[] whileBranchSucc = succ(whileBranch, "switch (x)", "exit");
    CfgNode[] switchBranchSucc = succ(whileBranchSucc[0], "c3()", "c6()", "while (x + y == 400 - z)", "c1()", "c5()", "break", "c2()", "c4()");
    assertThat(switchBranchSucc[2]).isSameAs(whileBranch);
    assertThat(succ(switchBranchSucc[0], "c2()")).isSameAs(switchBranchSucc[6]);
    CfgNode breakMarker = succ(switchBranchSucc[1], "break");
    CfgNode breakMarker2 = succ(switchBranchSucc[3], "break");
    CfgNode returnMarker = succ(switchBranchSucc[4], "return");
    assertThat(succ(switchBranchSucc[5], "while (x + y == 400 - z)")).isSameAs(whileBranch);
    CfgNode continueMarker = succ(switchBranchSucc[6], "continue");
    CfgNode breakMarker3 = succ(switchBranchSucc[7], "break");
    assertThat(succ(breakMarker, "while (x + y == 400 - z)")).isSameAs(whileBranch);
    assertThat(succ(breakMarker2, "while (x + y == 400 - z)")).isSameAs(whileBranch);
    assertThat(succ(returnMarker, "exit")).isSameAs(whileBranchSucc[1]);
    assertThat(succ(continueMarker, "while (x + y == 400 - z)")).isSameAs(whileBranch);
    assertThat(succ(breakMarker3, "while (x + y == 400 - z)")).isSameAs(whileBranch);
  }

  @Test public void genClassInstance02() {
    CfgNode entry = parseCfg("GenClassInstance02");
    CfgNode toString = succ(entry, "toString()");
    CfgNode exit = succ(toString, "exit");
  }

  @Test public void genTryWithResources01() {
    CfgNode entry = parseCfg("GenTryWithResources01");
    CfgNode openStream = succ(entry, "openStream()");
    CfgNode[] openStreamSucc = succ(openStream, "exception", "try");
    CfgNode[] exceptionSucc = succ(openStreamSucc[0], "c()", "f()");
    CfgNode[] tryEntrySucc = succ(openStreamSucc[1], "c()", "stmt()", "f()");
    assertThat(tryEntrySucc[2]).isSameAs(exceptionSucc[1]);
    assertThat(tryEntrySucc[0]).isSameAs(exceptionSucc[0]);
    CfgNode f = succ(exceptionSucc[0], "f()");
    CfgNode exception = succ(exceptionSucc[1], "exception");
    CfgNode[] stmtSucc = succ(tryEntrySucc[1], "exception", "f()");
    assertThat(stmtSucc[1]).isSameAs(f);
    CfgNode exit = succ(f, "exit");
    assertThat(succ(exception, "exit")).isSameAs(exit);
    CfgNode[] exceptionSucc2 = succ(stmtSucc[0], "c()", "f()");
    assertThat(exceptionSucc2[0]).isSameAs(exceptionSucc[0]);
    assertThat(exceptionSucc2[1]).isSameAs(exceptionSucc[1]);
  }

  @Test public void genTryWithResources02() {
    CfgNode entry = parseCfg("GenTryWithResources02");
    CfgNode o1 = succ(entry, "o1()");
    CfgNode[] o1Succ = succ(o1, "exception", "o2()");
    CfgNode[] exceptionSucc = succ(o1Succ[0], "c()", "f()");
    CfgNode[] o2Succ = succ(o1Succ[1], "exception", "try");
    CfgNode f = succ(exceptionSucc[0], "f()");
    CfgNode exception = succ(exceptionSucc[1], "exception");
    CfgNode[] exceptionSucc2 = succ(o2Succ[0], "c()", "f()");
    assertThat(exceptionSucc2[0]).isSameAs(exceptionSucc[0]);
    assertThat(exceptionSucc2[1]).isSameAs(exceptionSucc[1]);
    CfgNode[] tryEntrySucc = succ(o2Succ[1], "c()", "stmt()", "f()");
    assertThat(tryEntrySucc[2]).isSameAs(exceptionSucc[1]);
    assertThat(tryEntrySucc[0]).isSameAs(exceptionSucc[0]);
    CfgNode exit = succ(f, "exit");
    assertThat(succ(exception, "exit")).isSameAs(exit);
    CfgNode[] stmtSucc = succ(tryEntrySucc[1], "exception", "f()");
    assertThat(stmtSucc[1]).isSameAs(f);
    CfgNode[] exceptionSucc3 = succ(stmtSucc[0], "c()", "f()");
    assertThat(exceptionSucc3[1]).isSameAs(exceptionSucc[1]);
    assertThat(exceptionSucc3[0]).isSameAs(exceptionSucc[0]);
  }

}
