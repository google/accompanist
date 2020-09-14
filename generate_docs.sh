#!/bin/sh

cp README.md docs/index.md
cp CONTRIBUTING.md docs/contributing.md
cp images/social.png docs/header.png

sed -i '' 's/CONTRIBUTING.md/\/contributing/' docs/index.md
sed -i '' 's/images\/social.png/\/header.png/' docs/index.md

# Convert docs/xxx.md links to just xxx/
sed -i '' 's/docs\/\([a-zA-Z-]*\).md/\1/' docs/index.md
