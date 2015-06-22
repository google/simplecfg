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
/**
 * This is test data, not real source code!
 * StmtCfgTest builds and tests the CFG for the first block/method in this class.
 */
class GenTryStmt04 {
  void m() {
    // Test that finally handlers are generated in separate CFG branches.
    // Non-identical CFG nodes with the same name are expected in this graph.
    try {
      if (condition) {
        a();
        return;
      }
    } finally {
      x();
    }
    y();
  }
}
