#!/bin/sh -e

# Copyright 2015 Google Inc. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Packages ShipShape proto jars from the ShipShape GitHub repository.

TEMPDIR="/tmp/shipshape$$"

git clone --depth=1 git@github.com:google/shipshape.git "$TEMPDIR"
REV=$(cd "$TEMPDIR"; git rev-parse --short HEAD)
echo "Revision: $REV"
sed -i "s/Version: .*\$/Version: r${REV}/" README.google
(cd "$TEMPDIR"; ./setup_bazel.sh && bazel build //shipshape/java/com/google/shipshape/service:java_dispatcher_deploy.jar)

# Remove write-protected Jars before copying new Jars.
rm -f *.jar

cp "${TEMPDIR}/bazel-bin/shipshape/java/com/google/shipshape/service/java_dispatcher_deploy.jar" .

# Clean up.
rm -rf "$TEMPDIR"

