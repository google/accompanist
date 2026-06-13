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

set -e

RESULT_FILE="$1"

if [ -f "$RESULT_FILE" ]; then
  rm "$RESULT_FILE"
fi
touch "$RESULT_FILE"

checksum_file() {
  # Use SHA-256 for better security and collision resistance compared to MD5.
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$1" | awk '{print $1}'
  else
    openssl dgst -sha256 -r "$1" | awk '{print $1}'
  fi
}

FILES=()
while read -r -d '' FILE; do
	FILES+=("$FILE")
done < <(find . -type f \( -name "build.gradle*" -o -name "*.versions.toml" -o -name "gradle-wrapper.properties" \) -print0)

# Loop through files and append SHA-256 to result file
for FILE in "${FILES[@]}"; do
	echo "$(checksum_file "$FILE")" >> "$RESULT_FILE"
done
# Now sort the file so that it is idempotent
sort "$RESULT_FILE" -o "$RESULT_FILE"
