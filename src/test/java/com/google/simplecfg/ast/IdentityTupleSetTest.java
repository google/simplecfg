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
package com.google.simplecfg.ast;

import static com.google.common.truth.Truth.assertThat;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link IdentityTupleSet}. */
@RunWith(JUnit4.class)
public class IdentityTupleSetTest {

  // Two unique objects used for testing.
  private Object a = new Object();
  private Object b = new Object();

  @Test public void testSize() {
    assertThat(new IdentityTupleSet<>(null, null)).hasSize(1);
    assertThat(new IdentityTupleSet<>(a, a)).hasSize(1);
    assertThat(new IdentityTupleSet<>(a, b)).hasSize(2);
    assertThat(new IdentityTupleSet<>(null, b)).hasSize(2);
  }

  @Test public void testIsEmpty() {
    assertThat(new IdentityTupleSet<>(null, null)).isNotEmpty();
    assertThat(new IdentityTupleSet<>(a, a)).isNotEmpty();
    assertThat(new IdentityTupleSet<>(a, b)).isNotEmpty();
    assertThat(new IdentityTupleSet<>(null, b)).isNotEmpty();
  }

  @Test public void testDuplicateContains() {
    assertThat(new IdentityTupleSet<>(null, null)).containsExactly((Object) null);
    assertThat(new IdentityTupleSet<>(a, a)).containsExactly(a);
  }

  @Test public void testContains() {
    assertThat(new IdentityTupleSet<>(a, b)).containsExactly(a, b);
  }
}
