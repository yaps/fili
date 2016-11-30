#!/bin/bash
##
# Make sure we're on the right branch / commit for the build
##

# TODO: Update this with the rest of the build flows
echo "[INSTALL] Branch: $(git branch -v | grep \*)"
if [[ "${TRAVIS_PULL_REQUEST}" == "false" ]]; then
  git checkout ${TRAVIS_BRANCH}
fi
