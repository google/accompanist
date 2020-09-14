#!/bin/sh

cp README.md docs/index.md
cp CONTRIBUTING.md docs/contributing.md
cp images/social.png docs/header.png

sed -i '' 's/CONTRIBUTING.md/\/contributing/' docs/index.md
sed -i '' 's/docs\/using-snapshot-version.md/using-snapshot-version/' docs/index.md

# Deploy to Github pages.
mkdocs gh-deploy