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
 */
public class ShipshapeFindingAdapter {
  /** Builds a Shipshape note from an ExtendJ finding.  */
  public static Note adapt(ExtendJFinding finding,
      ShipshapeContext context, String pathPrefix) {
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

  private static Fix createFix(ExtendJFix fix, ShipshapeContext context, String path) {
    return Fix.newBuilder()
        .setDescription(fix.description)
        .addReplacement(
            Replacement.newBuilder()
                .setPath(path)
                .setRange(
                    FixRange.newBuilder()
                        .setStart(Position.newBuilder().setLine(fix.startLine))
                        .setEnd(Position.newBuilder().setLine(fix.endLine)))
                .setNewContent(fix.newText))
        .build();
  }

  private static Location createLocation(ExtendJFinding finding, ShipshapeContext context,
      String path) {
    TextRange.Builder textRange = TextRange.newBuilder()
      .setStartLine(finding.startLine)
      .setStartColumn(finding.startColumn)
      .setEndLine(finding.endLine)
      .setEndColumn(finding.endColumn);

    return Location.newBuilder()
      .setPath(path)
      .setRange(textRange)
      .build();
  }

}
