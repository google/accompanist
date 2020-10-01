#!/bin/sh

DOCS_ROOT=docs-gen

mkdir $DOCS_ROOT

# Work around Dokka failing to link against external links generated from 'gfm' sources.
wget -O package-list-coil-base https://coil-kt.github.io/coil/api/coil-base/package-list
sed -i.bak 's/$dokka.linkExtension:md/$dokka.linkExtension:html/g' package-list-coil-base

# Clear out the old API docs
[ -d docs/api ] && rm -r docs/api
# Build the docs with dokka
./gradlew clean dokkaGfm

# Clean up the temp Coil package list
rm package-list-coil-base

# Copy over any static + API docs to our $DOCS_ROOT
cp -r docs/ $DOCS_ROOT/

cp README.md $DOCS_ROOT/index.md
cp CONTRIBUTING.md $DOCS_ROOT/contributing.md
cp images/social.png $DOCS_ROOT/header.png

sed -i.bak 's/CONTRIBUTING.md/contributing/' $DOCS_ROOT/index.md
sed -i.bak 's/coil\/README.md/coil/' $DOCS_ROOT/index.md
sed -i.bak 's/images\/social.png/header.png/' $DOCS_ROOT/index.md

cp coil/README.md $DOCS_ROOT/coil.md
mkdir -p $DOCS_ROOT/coil
cp coil/images/crossfade.gif $DOCS_ROOT/coil/crossfade.gif
sed -i.bak 's/images\/crossfade.gif/crossfade.gif/' $DOCS_ROOT/coil.md

cp picasso/README.md $DOCS_ROOT/picasso.md
mkdir -p $DOCS_ROOT/picasso
cp picasso/images/crossfade.gif $DOCS_ROOT/picasso/crossfade.gif
sed -i.bak 's/images\/crossfade.gif/crossfade.gif/' $DOCS_ROOT/picasso.md

# Convert docs/xxx.md links to just xxx/
sed -i.bak 's/docs\/\([a-zA-Z-]*\).md/\1/' $DOCS_ROOT/index.md

#########################
# Tidy up Dokka output
#########################

# Remove all of the line breaks in the docs
find $DOCS_ROOT/api/ -name '*.md' -exec sed -i.bak 's/<br><br>//g' {} \;
# Remove the random androidJvm headers
find $DOCS_ROOT/api/ -name '*.md' -exec sed -i.bak 's/\[*androidJvm\]*//g' {} \;
# Remove the 'Brief description' headers
find $DOCS_ROOT/api/ -name '*.md' -exec sed -i.bak 's/Brief description//g' {} \;
