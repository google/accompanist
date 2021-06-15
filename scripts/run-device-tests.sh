#!/bin/bash

# Copyright 2021 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Fail on error and print out commands
set -ex

# By default we don't shard
SHARD_COUNT=0
SHARD_INDEX=0
# By default we don't log
LOG_FILE=""
# By default we run all tests
TASK="connectedCheck"

# Parse parameters
for i in "$@"; do
  case $i in
    --shard-count=*)
    SHARD_COUNT="${i#*=}"
    shift
    ;;
    --shard-index=*)
    SHARD_INDEX="${i#*=}"
    shift
    ;;
    --log-file=*)
    LOG_FILE="${i#*=}"
    shift
    ;;
    --run-affected)
    RUN_AFFECTED=true
    shift
    ;;
    --run-flaky-tests)
    RUN_FLAKY=true
    shift
    ;;
    --affected-base-ref=*)
    BASE_REF="${i#*=}"
    shift
    ;;
    *)
    echo "Unknown option"
    exit 1
    ;;
  esac
done

# Start logcat if we have a file to log to
if [[ ! -z "$LOG_FILE" ]]; then
  adb logcat > $LOG_FILE &
fi

FILTER_OPTS=""
# Filter out flaky tests if we're not set to run them
if [[ -z "$RUN_FLAKY" ]]; then
  FILTER_OPTS="$FILTER_OPTS -Pandroid.testInstrumentationRunnerArguments.notAnnotation=androidx.test.filters.FlakyTest"
fi

# If we're set to only run affected test, update the Gradle task
if [[ ! -z "$RUN_AFFECTED" ]]; then
  TASK="runAffectedAndroidTests -Paffected_module_detector.enable"
  # If we have a base branch set, add the Gradle property
  if [[ ! -z "$BASE_REF" ]]; then
    TASK="$TASK -Paffected_base_ref=$BASE_REF"
  fi
fi

SHARD_OPTS=""
if [ "$SHARD_COUNT" -gt "0" ]; then
  # If we have a shard count value, create the necessary Gradle property args.
  # We assume that SHARD_INDEX has been set too
  SHARD_OPTS="--no-configuration-cache"
  SHARD_OPTS="$SHARD_OPTS -Pandroid.testInstrumentationRunnerArguments.numShards=$SHARD_COUNT"
  SHARD_OPTS="$SHARD_OPTS -Pandroid.testInstrumentationRunnerArguments.shardIndex=$SHARD_INDEX"
fi

./gradlew  --scan --continue $TASK $FILTER_OPTS $SHARD_OPTS
