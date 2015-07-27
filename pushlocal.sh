#!/bin/bash

# Push the extendj_shipshape/extendj image to a local Docer registry running on
# localhost:5000

docker tag extendj_shipshape/extendj localhost:5000/extendj_shipshape/extendj
docker push localhost:5000/extendj_shipshape/extendj
