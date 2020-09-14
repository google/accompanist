#!/bin/sh

# First generate the docs
generate_docs.sh

# Deploy to Github pages.
mkdocs gh-deploy
