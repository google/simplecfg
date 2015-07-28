#!/bin/bash

# Copyright 2015 Google Inc. All Rights Reserved.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Run shipshape with the SimpleCFG based analyzers.
# This script needs shipshape to live in a sibling to the SimpleCFG directory.

SHIPSHAPE="../shipshape"
if [ ! -d "$SHIPSHAPE" ]; then
  echo "Error: shipshape directory not found: $SHIPSHAPE"
  exit 1
fi

# Remove potentially already running container.
docker rm -f extendj_0

declare -xr TEST_DIR=$(realpath $(dirname "$0"))

echo "Test dir: $TEST_DIR"

set -e

gradle docker

cd $SHIPSHAPE
./setup_bazel.sh
bazel build //shipshape/cli/...
bazel build //shipshape/docker:service

docker tag -f gcr.io/shipshape_releases/service:latest gcr.io/shipshape_releases/service:local

# Run shipshape on itself.
# TODO(joqvist): make a more interesting demo.
bazel-bin/shipshape/cli/shipshape --categories="ExtendJ" \
  --analyzer_images="extendj_shipshape/extendj" \
  --tag=local \
  --stderrthreshold=INFO \
  ${SHIPSHAPE}
