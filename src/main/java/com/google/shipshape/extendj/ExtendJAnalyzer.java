/*
 * Copyright 2015 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.shipshape.extendj;

import com.google.common.collect.ImmutableList;
import com.google.shipshape.proto.NotesProto.Location;
import com.google.shipshape.proto.NotesProto.Note;
import com.google.shipshape.proto.TextRangeProto.TextRange;
import com.google.shipshape.proto.ShipshapeContextProto.ShipshapeContext;
import com.google.shipshape.service.ShipshapeLogger;
import com.google.shipshape.service.AnalyzerException;
import com.google.shipshape.service.StatelessAnalyzer;
import com.google.simplecfg.ExtendJAnalyzerFrontend;
import com.google.simplecfg.ast.ExtendJFinding;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

/** Uses the ExtendJ analyzer to find issues in Java code.  */
public class ExtendJAnalyzer extends StatelessAnalyzer {

  private static final ShipshapeLogger logger = ShipshapeLogger.getLogger(ExtendJAnalyzer.class);

  public static final String CATEGORY = "ExtendJ";

  @Override
  public String getCategory() {
    return CATEGORY;
  }

  @Override
  public ImmutableList<Note> analyze(ShipshapeContext context) throws AnalyzerException {
    String root = "";
    if (context.hasRepoRoot()) {
      root = context.getRepoRoot();
      if (!root.endsWith("/")) {
        root = root + "/";
      }
    }
    Collection<Note> findings = new LinkedList<>();
    for (String path : context.getFilePathList()) {
      String sourcePath = root + path;
      try {
        File file = new File(sourcePath);
        if (file.isFile()) {
          findings.addAll(analyzeFile(context, sourcePath, root));
        } else if (file.isDirectory()) {
          logger.warning("Warning: skipping directory " + path, context, CATEGORY);
        }
      } catch (Throwable e) {
        throw new AnalyzerException(CATEGORY, context,
            String.format("Failed to analyze file %s: %s",
                path, e.getMessage()), e);
      }
    }
    return ImmutableList.copyOf(findings);
  }

  /**
   * Analyze a single file for findings. Returns the findings in a collection.
   *
   * @param sourcePath The path to the file to analyze in local storage.
   * @param pathPrefix The path corresponding to the root directory in local storage. This part
   * is stripped in the generated findings.
   */
  public static Collection<Note> analyzeFile(ShipshapeContext context,
      String sourcePath, String pathPrefix) throws AnalyzerException {

    logger.info("Checking file " + sourcePath, context, CATEGORY);
    Collection<Note> findings = Lists.newArrayList();
    for (ExtendJFinding finding : ExtendJAnalyzerFrontend.analyzeFile(sourcePath)) {
      findings.add(ShipshapeFindingAdapter.adapt(finding, context, pathPrefix));
    }
    return findings;
  }

}
