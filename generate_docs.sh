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

# Fail on any error
set -ex

DOCS_ROOT=docs-gen

[ -d $DOCS_ROOT ] && rm -r $DOCS_ROOT
mkdir $DOCS_ROOT

# Work around Dokka failing to link against external links generated from 'gfm' sources.
curl -o package-list-coil-base https://coil-kt.github.io/coil/api/coil-base/package-list
sed -i.bak 's/$dokka.linkExtension:md/$dokka.linkExtension:html/g' package-list-coil-base

# Clear out the old API docs
[ -d docs/api ] && rm -r docs/api
# Build the docs with dokka
./gradlew dokkaHtmlMultiModule

# Re-word the Dokka call out
find docs/api/ -type f -name '*.html' -exec sed -i -e 's/Sponsored and developed/Documentation generated/g' {} \;
# Remove the copyright declaration
find docs/api/ -type f -name '*.html' -exec sed -i -e 's/© [0-9]* Copyright//' {} \;

# Clean up the temp Coil package list
rm package-list-coil-base

# Create a copy of our docs at our $DOCS_ROOT
cp -a docs/* $DOCS_ROOT

cp README.md $DOCS_ROOT/index.md
cp CONTRIBUTING.md $DOCS_ROOT/contributing.md

sed -i.bak 's/CONTRIBUTING.md/contributing/' $DOCS_ROOT/index.md
sed -i.bak 's/README.md//' $DOCS_ROOT/index.md
sed -i.bak 's/docs\/header.png/header.png/' $DOCS_ROOT/index.md

# Convert docs/xxx.md links to just xxx/
sed -i.bak 's/docs\/\([a-zA-Z-]*\).md/\1/' $DOCS_ROOT/index.md

# Finally delete all of the backup files
find . -name '*.bak' -delete
