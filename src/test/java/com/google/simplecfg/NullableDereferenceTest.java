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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collection;

/** Integration tests for the nullable dereference checker. */
@RunWith(JUnit4.class)
public class NullableDereferenceTest {

  @Test public void test01() {
    Collection<String> findings = StmtCfgTest.findings("Nullable01");
    assertThat(findings).containsExactly(
        "Nullable01:39:25: Dereferencing p, which was declared @Nullable.",
        "Nullable01:46:12: Dereferencing p, which was declared @Nullable.",
        "Nullable01:59:12: Dereferencing p, which was declared @Nullable."
        );
  }

  @Test public void test02() {
    Collection<String> findings = StmtCfgTest.findings("Nullable02");
    assertThat(findings).containsExactly(
        "Nullable02:44:7: Dereferencing p, which was declared @Nullable.",
        "Nullable02:49:7: Dereferencing p, which was declared @Nullable."
        );
  }

  @Test public void test03() {
    Collection<String> findings = StmtCfgTest.findings("Nullable03");
    assertThat(findings).isEmpty();
  }
}
