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
package com.google.shipshape.extendj;

import com.google.common.collect.Lists;
import com.google.shipshape.proto.NotesProto.Location;
import com.google.shipshape.proto.NotesProto.Note;
import com.google.shipshape.proto.NotesProto.Fix;
import com.google.shipshape.proto.NotesProto.FixRange;
import com.google.shipshape.proto.NotesProto.FixRange.Position;
import com.google.shipshape.proto.NotesProto.Replacement;
import com.google.shipshape.proto.TextRangeProto.TextRange;
import com.google.shipshape.proto.ShipshapeContextProto.ShipshapeContext;
import com.google.simplecfg.ast.ExtendJFinding;
import com.google.simplecfg.ast.ExtendJFinding.ExtendJFix;

import java.util.Collection;

/**
 * Converts an ExtendJ finding into a Shipshape finding.
 * <p>
 * The ExtendJ-based static analyses are not aware of the Shipshape note proto since the analyses
 * are designed to be integrated into different frameworks, not only Shipshape.  ExtendJFinding is a
 * generic static analysis finding without the ShipshapeContext needed to build a Shipshape note.
 */
public class ShipshapeFindingAdapter {

  /**
   * Build a Shipshape note from an ExtendJ finding. To build a Note proto we take an
   * ExtendJFinding, a ShipshapeContext, and a path prefix. The path prefix is stripped from the
   * source path of the ExtendJFinding, to make the generated Note proto relative to the project
   * root for the project analyzed, rather than absolute in the local analyzers filesystem.
   *
   * @param finding The generic ExtendJ finding, containing finding message, category, location and
   *     suggested fixes.
   * @param context The ShipshapeContext, needed to build a Note proto.
   * @param pathPrefix The prefix that is stripped from the ExtendJ findings path to build a
   *     project-relative path.
   * @return A Shipshape finding built from the ExtendJ finding.
   */
  public static Note adapt(ExtendJFinding finding, ShipshapeContext context, String pathPrefix) {
    String path = finding.sourcePath.substring(pathPrefix.length());

    Note.Builder note = Note.newBuilder()
        .setLocation(createLocation(finding, context, path))
        .setDescription(finding.message)
        .setCategory(ExtendJAnalyzer.CATEGORY)
        .setSubcategory(finding.subcategory);
    for (ExtendJFix fix : finding.fixes) {
      note.addFix(createFix(fix, context, path));
    }
    return note.build();
  }

  /**
   * Copy an ExtendJFix to a NotesProto.Fix.
   *
   * @param fix The generic ExtendJFix to copy.
   * @param context ShipshapeContext needed for NotesProto.
   * @param path Path of the file where the fix applies.
   */
  private static Fix createFix(ExtendJFix fix, ShipshapeContext context, String path) {
    return Fix.newBuilder()
        .setDescription(fix.description)
        .addReplacement(Replacement.newBuilder()
            .setPath(path)
            .setRange(FixRange.newBuilder()
                .setStart(Position.newBuilder().setLine(fix.startLine))
                .setEnd(Position.newBuilder().setLine(fix.endLine)))
            .setNewContent(fix.newText))
        .build();
  }

  /**
   * Build a NotesProto.Location from an ExtendJFinding.
   *
   * @param finding The generic ExtendJFinding to copy.
   * @param sourceContext SourceContext needed for the NotesProto.
   * @param path The project-relative path to the file where the finding should be reported.
   */
  private static Location createLocation(ExtendJFinding finding, ShipshapeContext context,
      String path) {
    return Location.newBuilder()
        .setPath(path)
        .setRange(TextRange.newBuilder()
            .setStartLine(finding.startLine)
            .setStartColumn(finding.startColumn)
            .setEndLine(finding.endLine)
            .setEndColumn(finding.endColumn))
        .build();
  }

}
