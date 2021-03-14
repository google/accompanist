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

ENCRYPT_KEY=$1

if [[ ! -z "$ENCRYPT_KEY" ]]; then
  # Decrypt GnuPG keyring
  openssl aes-256-cbc -md sha256 -d -in release/secring.gpg.aes -out release/secring.gpg -k ${ENCRYPT_KEY}

  # Decrypt Play Store key
  openssl aes-256-cbc -md sha256 -d -in release/signing.properties.aes -out release/signing.properties -k ${ENCRYPT_KEY}

else
  echo "ENCRYPT_KEY is empty"
fi
