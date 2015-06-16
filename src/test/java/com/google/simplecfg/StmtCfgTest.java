/**
 * Copyright 2014 Google Inc. All Rights Reserved.
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
import com.google.simplecfg.ast.JavaParser;
import com.google.simplecfg.ast.CompilationUnit;
import com.google.simplecfg.ast.Program;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/** Tests for if statement CFGs. */
@RunWith(JUnit4.class)
public class StmtCfgTest {

  private static CfgNode parseFile(String filename) {
    String path = "testdata/" + filename + ".java";
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
      assertThat(unit.getTypeDeclList()).isNotEmpty();
      assertThat(unit.getTypeDecl(0).getBodyDeclList()).isNotEmpty();
      return unit.getTypeDecl(0).getBodyDecl(0).entry();
    } catch (Exception e) {
      e.printStackTrace();
      fail("failed to parse test input file: " + path);
    }
    // Failure.
    return null;
  }

  /**
   * Assert and return a single successor of the node.
   */
  private static CfgNode succ(CfgNode node, String successor) {
    assertThat(node.successors()).containsExactly(successor);
    return node.successors().iterator().next();
  }

  /**
   * Assert the successors of the node and return them in an array
   * using the same ordering as the input array.
   */
  private static CfgNode[] succ(CfgNode node, String... successors) {
    assertThat(node.successors()).containsExactly((Object[]) successors);

    // Ensure no duplicate successor names.
    boolean duplicates = false;
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

  @Test public void ifStmt01() {
    CfgNode entry = parseFile("IfStmt01");
    CfgNode call1 = succ(entry, "call1()");
    CfgNode branch = succ(call1, "if (call1())");
    CfgNode[] targets = succ(branch, "onTrue()", "exit");
    assertThat(succ(targets[0], "exit")).isSameAs(targets[1]);
  }

  @Test public void ifStmt02() {
    CfgNode entry = parseFile("IfStmt02");
    CfgNode call1 = succ(entry, "call1()");
    CfgNode branch = succ(call1, "if (call1())");
    CfgNode[] targets = succ(branch, "onTrue()", "onFalse()");
    assertThat(succ(targets[0], "exit")).isSameAs(succ(targets[1], "exit"));
  }

  @Test public void ifStmt03() {
    CfgNode entry = parseFile("IfStmt03");
    CfgNode call1 = succ(entry, "call1()");
    CfgNode branch = succ(call1, "if (call1())");
    CfgNode[] targets = succ(branch, "onTrue()", "onFalse()");
    succ(targets[0], "exit");
    succ(targets[1], "call2()");
  }

  @Test public void ifStmt04() {
    CfgNode entry = parseFile("IfStmt04");
    CfgNode call1 = succ(entry, "call1()");
    CfgNode branch = succ(call1, "if (call1())");
    CfgNode[] targets = succ(branch, "onTrue()", "onFalse()");
    succ(targets[0], "call2()");
    succ(targets[1], "exit");
  }

  @Test public void forStmt01() {
    CfgNode entry = parseFile("ForStmt01");
    CfgNode call1 = succ(entry, "call1()");
    CfgNode branch = succ(call1, "for (call1())");
    CfgNode[] targets = succ(branch, "call1()", "exit");
    assertThat(targets[0]).isSameAs(call1);
  }

  @Test public void forStmt02() {
    CfgNode entry = parseFile("ForStmt02");
    CfgNode call1 = succ(entry, "call1()");
    CfgNode branch = succ(call1, "for (call1())");
    CfgNode[] targets = succ(branch, "call2()", "exit");
    assertThat(succ(targets[0], "call1()")).isSameAs(call1);
  }

  @Test public void forStmt03() {
    CfgNode entry = parseFile("ForStmt03");
    CfgNode init = succ(entry, "init()");
    CfgNode cond = succ(init, "cond()");
    CfgNode branch = succ(cond, "for (cond())");
    CfgNode[] targets = succ(branch, "stmt()", "exit");
    CfgNode stmt = targets[0];
    CfgNode update = succ(stmt, "update()");
    assertThat(succ(update, "cond()")).isSameAs(cond);
  }

  @Test public void forStmt04() {
    CfgNode entry = parseFile("ForStmt04");
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
    CfgNode entry = parseFile("ForStmt05");
    CfgNode i = succ(entry, "i()");
    CfgNode branch = succ(i, "for (false)");
    CfgNode y = succ(branch, "y()");
    succ(y, "exit");
  }

  @Test public void whileStmt01() {
    CfgNode entry = parseFile("WhileStmt01");
    CfgNode branch = succ(entry, "while (true)");
    assertThat(succ(branch, "while (true)")).isSameAs(branch);
  }

  @Test public void whileStmt02() {
    CfgNode entry = parseFile("WhileStmt02");
    CfgNode cond = succ(entry, "cond()");
    CfgNode branch = succ(cond, "while (cond())");
    CfgNode[] targets = succ(branch, "cond()", "exit");
    assertThat(targets[0]).isSameAs(cond);
  }

  @Test public void whileStmt03() {
    CfgNode entry = parseFile("WhileStmt03");
    CfgNode branch = succ(entry, "while (true)");
    CfgNode cond = succ(branch, "cond()");
    CfgNode branch2 = succ(cond, "if (cond())");
    CfgNode[] targets = succ(branch2, "while (true)", "tail()");
    assertThat(targets[0]).isSameAs(branch);
    succ(targets[1], "exit");
  }

  @Test public void whileStmt04() {
    CfgNode entry = parseFile("WhileStmt04");
    CfgNode branch = succ(entry, "while (true)");
    CfgNode cond = succ(branch, "cond()");
    CfgNode branch2 = succ(cond, "if (cond())");
    CfgNode[] targets = succ(branch2, "while (true)", "x()");
    assertThat(targets[0]).isSameAs(branch);
    CfgNode y = succ(targets[1], "y()");
    succ(y, "exit");
  }

  @Test public void whileStmt05() {
    CfgNode entry = parseFile("WhileStmt05");
    CfgNode branch = succ(entry, "while (false)");
    CfgNode y = succ(branch, "y()");
    succ(y, "exit");
  }

  @Test public void doStmt01() {
    CfgNode entry = parseFile("DoStmt01");
    CfgNode x = succ(entry, "x()");
    CfgNode y = succ(x, "y()");
    CfgNode branch = succ(y, "do_while (y())");
    CfgNode[] targets = succ(branch, "z()", "x()");
    succ(targets[0], "exit");
    assertThat(succ(targets[1], "y()")).isSameAs(y);
  }

  @Test public void doStmt02() {
    CfgNode entry = parseFile("DoStmt02");
    CfgNode x = succ(entry, "x()");
    CfgNode branch = succ(x, "do_while (false)");
    CfgNode y = succ(branch, "y()");
    succ(y, "exit");
  }

  @Test public void enhancedFor01() {
    CfgNode entry = parseFile("EnhancedFor01");
    CfgNode aList = succ(entry, "aList()");
    CfgNode branch = succ(aList, "for (int a : aList())");
    CfgNode[] targets = succ(branch, "x()", "exit");
    assertThat(succ(targets[0], "aList()")).isSameAs(aList);
  }

  @Test public void methodAccess01() {
    CfgNode entry = parseFile("MethodAccess01");
    CfgNode p1 = succ(entry, "p1()");
    CfgNode p2 = succ(p1, "p2()");
    CfgNode p3 = succ(p2, "p3()");
    CfgNode x = succ(p3, "x()");
    succ(x, "exit");
  }

  @Test public void conditionalExpr01() {
    CfgNode entry = parseFile("ConditionalExpr01");
    CfgNode x = succ(entry, "x()");
    CfgNode branch = succ(x, "if (x())");
    CfgNode[] targets = succ(branch, "y()", "z()");
    // If branches converge on exit.
    assertThat(succ(targets[0], "exit")).isSameAs(succ(targets[1], "exit"));
  }

  @Test public void switchStmt01() {
    CfgNode entry = parseFile("SwitchStmt01");
    CfgNode expr = succ(entry, "expr()");
    CfgNode branch = succ(expr, "switch (expr())");
    CfgNode[] targets = succ(branch, "x()", "y()", "z()", "d()");
    assertThat(succ(targets[0], "y()")).isSameAs(targets[1]);
    CfgNode exit = succ(targets[1], "exit");
    assertThat(succ(targets[2], "d()")).isSameAs(targets[3]);
    assertThat(succ(targets[3], "exit")).isSameAs(exit);
  }

  @Test public void switchStmt02() {
    CfgNode entry = parseFile("SwitchStmt02");
    CfgNode expr = succ(entry, "expr()");
    CfgNode branch = succ(expr, "switch (expr())");
    CfgNode[] targets = succ(branch, "x()", "y()", "z()", "exit");
    assertThat(succ(targets[0], "y()")).isSameAs(targets[1]);
    assertThat(succ(targets[1], "exit")).isSameAs(targets[3]);
    assertThat(succ(targets[2], "exit")).isSameAs(targets[3]);
  }

  @Test public void tryStmt01() {
    CfgNode entry = parseFile("TryStmt01");
    CfgNode tryBranch = succ(entry, "try");
    CfgNode[] trySucc = succ(tryBranch, "cond()", "x()");
    CfgNode[] condSucc = succ(trySucc[0], "exception", "if (cond())");
    CfgNode x = succ(condSucc[0], "x()");
    assertThat(x).isSameAs(trySucc[1]);
    CfgNode exit = succ(succ(x, "exception"), "exit");
    CfgNode[] ifSucc = succ(condSucc[1], "a()", "x()");
    CfgNode[] aSucc = succ(ifSucc[0], "exception", "x()");
    assertThat(succ(aSucc[0], "x()")).isSameAs(x);
    assertThat(aSucc[1]).isNotSameAs(x);
    assertThat(succ(succ(aSucc[1], "y()"), "exit")).isSameAs(exit);
    assertThat(succ(ifSucc[1], "exit")).isSameAs(exit);
  }

  @Test public void tryStmt02() {
    CfgNode entry = parseFile("TryStmt02");
    CfgNode tryBranch = succ(entry, "try");
    CfgNode[] tryTargets = succ(tryBranch, "c1()", "c2()", "exit");
    assertThat(succ(tryTargets[0], "exit")).isSameAs(tryTargets[2]);
    assertThat(succ(tryTargets[1], "exit")).isSameAs(tryTargets[2]);
  }

  @Test public void tryStmt03() {
    CfgNode entry = parseFile("TryStmt03");
    CfgNode tryEntry = succ(entry, "try");
    CfgNode[] targets = succ(tryEntry, "if (condition)", "x()");
    assertThat(targets[0].successors()).containsExactly("x()", "x()");

    CfgNode exception = succ(targets[1], "exception");
    CfgNode exit = succ(exception, "exit");

    // Here we need some logic to distingquish two successors with
    // the same name.
    Iterator<? extends CfgNode> succ = targets[0].successors().iterator();
    CfgNode x1 = succ.next();
    CfgNode x2 = succ.next();
    // One of the branches has y() as follow and the other has exit.
    if (x1.successors().iterator().next().toString().equals("exit")) {
      // Swap so that x1 is the one with y() as successor.
      CfgNode temp = x1;
      x1 = x2;
      x2 = temp;
    }
    assertThat(succ(succ(x1, "y()"), "exit")).isSameAs(exit);
    assertThat(succ(x2, "exit")).isSameAs(exit);
  }

  @Test public void filtering01() {
    CfgNode entry = parseFile("Filtering01");
    CfgNode i = succ(entry, "i()");
    CfgNode j = succ(i, "j()");
    CfgNode cond = succ(j, "cond()");
    CfgNode branch = succ(cond, "for (cond() && c == 3)");
    CfgNode[] targets = succ(branch, "stmt()", "exit");
    CfgNode stmt = targets[0];
    CfgNode u1 = succ(stmt, "u1()");
    CfgNode u2 = succ(u1, "u2()");
    CfgNode u3 = succ(u2, "u3()");
    assertThat(succ(u3, "cond()")).isSameAs(cond);
  }

  @Test public void throwStmt01() {
    CfgNode entry = parseFile("ThrowStmt01");
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
    CfgNode entry = parseFile("GenTryStmt01");
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
    CfgNode entry = parseFile("GenTryStmt02");
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
    CfgNode entry = parseFile("GenTryStmt03");
    CfgNode tryEntry = succ(entry, "try");
    CfgNode[] targets = succ(tryEntry, "c1()", "x()");
    CfgNode f = succ(targets[0], "f()");
    CfgNode[] targets2 = succ(targets[1], "exception", "f()");
    assertThat(targets2[1]).isSameAs(f);
    CfgNode exit = succ(f, "exit");
    assertThat(succ(targets2[0], "c1()")).isSameAs(targets[0]);
  }

  @Test public void genTryStmt04() {
    CfgNode entry = parseFile("GenTryStmt04");
    CfgNode tryEntry = succ(entry, "try");
    CfgNode[] tryEntrySucc = succ(tryEntry, "x()", "if (condition)");
    CfgNode exception = succ(tryEntrySucc[0], "exception");
    CfgNode[] ifBranchSucc = succ(tryEntrySucc[1], "a()", "x()");
    CfgNode exit = succ(exception, "exit");
    CfgNode[] aSucc = succ(ifBranchSucc[0], "exception", "x()");
    CfgNode y = succ(ifBranchSucc[1], "y()");
    assertThat(succ(aSucc[0], "x()")).isSameAs(tryEntrySucc[0]);
    assertThat(succ(aSucc[1], "exit")).isSameAs(exit);
    assertThat(succ(y, "exit")).isSameAs(exit);
  }

  @Test public void genTryStmt05() {
    CfgNode entry = parseFile("GenTryStmt05");
    CfgNode tryEntry = succ(entry, "try");
    CfgNode[] targets = succ(tryEntry, "f2()", "try");
    CfgNode exception = succ(targets[0], "exception");
    CfgNode[] targets2 = succ(targets[1], "f1()", "if (condition)");
    CfgNode exit = succ(exception, "exit");
    CfgNode exception2 = succ(targets2[0], "exception");
    CfgNode[] targets3 = succ(targets2[1], "exception", "f1()");
    assertThat(succ(exception2, "f2()")).isSameAs(targets[0]);
    assertThat(succ(targets3[0], "f1()")).isSameAs(targets2[0]);
    CfgNode[] targets4 = succ(targets3[1], "exception", "a()");
    assertThat(succ(targets4[0], "f2()")).isSameAs(targets[0]);
    CfgNode[] targets5 = succ(targets4[1], "exception", "f2()");
    assertThat(succ(targets5[0], "f2()")).isSameAs(targets[0]);
    CfgNode b = succ(targets5[1], "b()");
    assertThat(succ(b, "exit")).isSameAs(exit);
  }

  @Test public void genTryStmt06() {
    CfgNode entry = parseFile("GenTryStmt06");
    CfgNode tryEntry = succ(entry, "try");
    CfgNode[] targets = succ(tryEntry, "try", "f2()");
    CfgNode[] targets2 = succ(targets[0], "f1()", "s()");
    CfgNode exception = succ(targets[1], "exception");
    CfgNode f2 = succ(targets2[0], "f2()");
    CfgNode[] targets3 = succ(targets2[1], "exception", "f1()");
    CfgNode exit = succ(exception, "exit");
    assertThat(succ(f2, "exit")).isSameAs(exit);
    assertThat(succ(targets3[0], "f1()")).isSameAs(targets2[0]);
    CfgNode[] targets4 = succ(targets3[1], "exception", "f2()");
    assertThat(succ(targets4[0], "f2()")).isSameAs(targets[1]);
    assertThat(succ(targets4[1], "exit")).isSameAs(exit);
  }

  @Test public void genTryStmt07() {
    CfgNode entry = parseFile("GenTryStmt07");
    CfgNode tryEntry = succ(entry, "try");
    CfgNode[] targets = succ(tryEntry, "c1()", "x()");
    CfgNode f = succ(targets[0], "f()");
    CfgNode[] targets2 = succ(targets[1], "exception", "f()");
    CfgNode exit = succ(f, "exit");
    assertThat(succ(targets2[0], "c1()")).isSameAs(targets[0]);
    CfgNode y = succ(targets2[1], "y()");
    assertThat(succ(y, "exit")).isSameAs(exit);
  }

  @Test public void genForStmt01() {
    CfgNode entry = parseFile("GenForStmt01");
    CfgNode forBranch = succ(entry, "for (i < 100)");
    CfgNode[] forBranchSucc = succ(forBranch, "c()", "fin()");
    CfgNode whileBranch = succ(forBranchSucc[0], "while (c())");
    CfgNode exit = succ(forBranchSucc[1], "exit");
    CfgNode[] whileBranchSucc = succ(whileBranch, "if (i >= 40)", "u()");
    CfgNode[] ifBranchSucc = succ(whileBranchSucc[0], "fin()", "c()");
    assertThat(ifBranchSucc[0]).isSameAs(forBranchSucc[1]);
    assertThat(ifBranchSucc[1]).isSameAs(forBranchSucc[0]);
    assertThat(succ(whileBranchSucc[1], "for (i < 100)")).isSameAs(forBranch);
  }

  @Test public void genForStmt02() {
    CfgNode entry = parseFile("GenForStmt02");
    CfgNode forBranch = succ(entry, "for (i < 100)");
    CfgNode[] forBranchSucc = succ(forBranch, "c()", "fin()");
    CfgNode whileBranch = succ(forBranchSucc[0], "while (c())");
    CfgNode exit = succ(forBranchSucc[1], "exit");
    CfgNode[] whileBranchSucc = succ(whileBranch, "if (i >= 40)", "u()");
    CfgNode[] ifBranchSucc = succ(whileBranchSucc[0], "for (i < 100)", "c()");
    assertThat(ifBranchSucc[0]).isSameAs(forBranch);
    assertThat(ifBranchSucc[1]).isSameAs(forBranchSucc[0]);
    assertThat(succ(whileBranchSucc[1], "for (i < 100)")).isSameAs(forBranch);
  }

  @Test public void genClassInstance01() {
    CfgNode entry = parseFile("GenClassInstance01");
    CfgNode p1 = succ(entry, "p1()");
    CfgNode p2 = succ(p1, "p2()");
    CfgNode p3 = succ(p2, "p3()");
    CfgNode p4 = succ(p3, "p4()");
    CfgNode exit = succ(p4, "exit");
  }

  @Test public void genSwitchStmt01() {
    CfgNode entry = parseFile("GenSwitchStmt01");
    CfgNode whileBranch = succ(entry, "while (x + y == 400 - z)");
    CfgNode[] whileBranchSucc = succ(whileBranch, "switch (x)", "exit");
    CfgNode[] switchBranchSucc = succ(whileBranchSucc[0], "c1()", "c6()", "while (x + y == 400 - z)", "c4()", "c5()", "c2()", "c3()");
    assertThat(switchBranchSucc[2]).isSameAs(whileBranch);
    assertThat(succ(switchBranchSucc[0], "while (x + y == 400 - z)")).isSameAs(whileBranch);
    assertThat(succ(switchBranchSucc[1], "while (x + y == 400 - z)")).isSameAs(whileBranch);
    assertThat(succ(switchBranchSucc[3], "while (x + y == 400 - z)")).isSameAs(whileBranch);
    assertThat(succ(switchBranchSucc[4], "exit")).isSameAs(whileBranchSucc[1]);
    assertThat(succ(switchBranchSucc[5], "while (x + y == 400 - z)")).isSameAs(whileBranch);
    assertThat(succ(switchBranchSucc[6], "c2()")).isSameAs(switchBranchSucc[5]);
  }

  @Test public void genClassInstance02() {
    CfgNode entry = parseFile("GenClassInstance02");
    CfgNode toString = succ(entry, "toString()");
    CfgNode exit = succ(toString, "exit");
  }

  @Test public void genTryWithResources01() {
    CfgNode entry = parseFile("GenTryWithResources01");
    CfgNode openStream = succ(entry, "openStream()");
    CfgNode tryEntry = succ(openStream, "try");
    CfgNode[] tryEntrySucc = succ(tryEntry, "c()", "f()", "stmt()");
    CfgNode f = succ(tryEntrySucc[0], "f()");
    CfgNode exception = succ(tryEntrySucc[1], "exception");
    CfgNode[] stmtSucc = succ(tryEntrySucc[2], "exception", "f()");
    assertThat(stmtSucc[1]).isSameAs(f);
    CfgNode exit = succ(f, "exit");
    assertThat(succ(exception, "exit")).isSameAs(exit);
    CfgNode[] exceptionSucc = succ(stmtSucc[0], "c()", "f()");
    assertThat(exceptionSucc[1]).isSameAs(tryEntrySucc[1]);
    assertThat(exceptionSucc[0]).isSameAs(tryEntrySucc[0]);
  }

}
