#!/bin/sh

# Work around Dokka failing to link against external links generated from 'gfm' sources.
wget -O package-list-coil-base https://coil-kt.github.io/coil/api/coil-base/package-list
sed -i '' 's/$dokka.linkExtension:md/$dokka.linkExtension:html/g' package-list-coil-base

# Build the coil-base docs.
./gradlew clean dokka

rm package-list-coil-base

cp README.md docs/index.md
cp CONTRIBUTING.md docs/contributing.md
cp images/social.png docs/header.png

sed -i '' 's/CONTRIBUTING.md/contributing/' docs/index.md
sed -i '' 's/coil\/README.md/coil/' docs/index.md
sed -i '' 's/images\/social.png/header.png/' docs/index.md

cp coil/README.md docs/coil.md
mkdir -p docs/coil
cp coil/images/crossfade.gif docs/coil/crossfade.gif

sed -i '' 's/images\/crossfade.gif/crossfade.gif/' docs/coil.md

# Convert docs/xxx.md links to just xxx/
sed -i '' 's/docs\/\([a-zA-Z-]*\).md/\1/' docs/index.md
