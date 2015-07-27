#!/bin/bash

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
