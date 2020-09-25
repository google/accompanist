#!/bin/sh

# Work around Dokka failing to link against external links generated from 'gfm' sources.
wget -O package-list-coil-base https://coil-kt.github.io/coil/api/coil-base/package-list
sed -i.bak 's/$dokka.linkExtension:md/$dokka.linkExtension:html/g' package-list-coil-base

# Clear out the old API docs
rm -r docs/api
# Build the docs with dokka
./gradlew clean dokkaGfm

rm package-list-coil-base

cp README.md docs/index.md
cp CONTRIBUTING.md docs/contributing.md
cp images/social.png docs/header.png

sed -i.bak 's/CONTRIBUTING.md/contributing/' docs/index.md
sed -i.bak 's/coil\/README.md/coil/' docs/index.md
sed -i.bak 's/images\/social.png/header.png/' docs/index.md

cp coil/README.md docs/coil.md
mkdir -p docs/coil
cp coil/images/crossfade.gif docs/coil/crossfade.gif
sed -i.bak 's/images\/crossfade.gif/crossfade.gif/' docs/coil.md

cp picasso/README.md docs/picasso.md
mkdir -p docs/picasso
cp picasso/images/crossfade.gif docs/picasso/crossfade.gif
sed -i.bak 's/images\/crossfade.gif/crossfade.gif/' docs/picasso.md

# Convert docs/xxx.md links to just xxx/
sed -i.bak 's/docs\/\([a-zA-Z-]*\).md/\1/' docs/index.md

#########################
# Tidy up Dokka output
#########################

# Remove all of the line breaks in the docs
find docs/api/ -name '*.md' -exec sed -i.bak 's/<br><br>//g' {} \;
# Remove the random androidJvm headers
find docs/api/ -name '*.md' -exec sed -i.bak 's/\[*androidJvm\]*//g' {} \;
# Remove the 'Brief description' headers
find docs/api/ -name '*.md' -exec sed -i.bak 's/Brief description//g' {} \;
