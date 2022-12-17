#!/bin/bash

echo "Module user Starting"
sh "$ANOUNCY_PROJECT_PATH/ops/image-build.sh" "user" "$ANOUNCY_DOCKER_HOST/anouncy-user:$(uuidgen)"
echo "Module announce Starting"
sh "$ANOUNCY_PROJECT_PATH/ops/image-build.sh" "announce" "$ANOUNCY_DOCKER_HOST/anouncy-announce:$(uuidgen)" "skip-common-build"
echo "Module listing Starting"
sh "$ANOUNCY_PROJECT_PATH/ops/image-build.sh" "listing" "$ANOUNCY_DOCKER_HOST/anouncy-listing:$(uuidgen)" "skip-common-build"
echo "Module vote Starting"
sh "$ANOUNCY_PROJECT_PATH/ops/image-build.sh" "vote" "$ANOUNCY_DOCKER_HOST/anouncy-vote:$(uuidgen)" "skip-common-build"
echo "Module region Starting"
sh "$ANOUNCY_PROJECT_PATH/ops/image-build.sh" "region" "$ANOUNCY_DOCKER_HOST/anouncy-region:$(uuidgen)" "skip-common-build"
echo "Module persist Starting"
sh "$ANOUNCY_PROJECT_PATH/ops/image-build.sh" "persist" "$ANOUNCY_DOCKER_HOST/anouncy-persist:$(uuidgen)" "skip-common-build"
echo "Module location Starting"
sh "$ANOUNCY_PROJECT_PATH/ops/image-build.sh" "location" "$ANOUNCY_DOCKER_HOST/anouncy-location:$(uuidgen)" "skip-common-build"