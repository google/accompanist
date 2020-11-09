#!/bin/sh

DOCS_ROOT=docs-gen

[ -d $DOCS_ROOT ] && rm -r $DOCS_ROOT
mkdir $DOCS_ROOT

# Work around Dokka failing to link against external links generated from 'gfm' sources.
wget -O package-list-coil-base https://coil-kt.github.io/coil/api/coil-base/package-list
sed -i.bak 's/$dokka.linkExtension:md/$dokka.linkExtension:html/g' package-list-coil-base

# Clear out the old API docs
[ -d docs/api ] && rm -r docs/api
# Build the docs with dokka
./gradlew clean dokkaHtml

# Clean up the temp Coil package list
rm package-list-coil-base

# Copy over any static + API docs to our $DOCS_ROOT
cp -R docs/* $DOCS_ROOT

cp README.md $DOCS_ROOT/index.md
cp CONTRIBUTING.md $DOCS_ROOT/contributing.md
cp images/social.png $DOCS_ROOT/header.png

sed -i.bak 's/CONTRIBUTING.md/contributing/' $DOCS_ROOT/index.md
sed -i.bak 's/README.md//' $DOCS_ROOT/index.md
sed -i.bak 's/images\/social.png/header.png/' $DOCS_ROOT/index.md

# Convert docs/xxx.md links to just xxx/
sed -i.bak 's/docs\/\([a-zA-Z-]*\).md/\1/' $DOCS_ROOT/index.md

cp coil/README.md $DOCS_ROOT/coil.md
mkdir -p $DOCS_ROOT/coil
cp -r coil/images $DOCS_ROOT/coil

cp picasso/README.md $DOCS_ROOT/picasso.md
mkdir -p $DOCS_ROOT/picasso
cp -r picasso/images $DOCS_ROOT/picasso

cp glide/README.md $DOCS_ROOT/glide.md
mkdir -p $DOCS_ROOT/glide
cp -r glide/images $DOCS_ROOT/glide

cp insets/README.md $DOCS_ROOT/insets.md
mkdir -p $DOCS_ROOT/insets
cp -r insets/images $DOCS_ROOT/insets
