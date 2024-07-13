#!/bin/bash

nearestTag=$(git describe --tags --abbrev=0 | tr -d '\n')

branch="$CI_SOURCEBRANCHNAME"
hash=${CI_SOURCEVERSION:0:6}
buildId="$CI_BUILDID"

if [[ $branch == *"merge" ]]; then
    branch="pullrequest-$CI_PULLREQUESTNUMBER"
fi

branch=$(echo "$branch" | sed 's/[^a-zA-Z0-9]/-/g')
# Semver meta only allows 0-9 a-z and -

tag=$(git -c 'versionsort.suffix=-' tag -l --points-at HEAD --sort=v:refname | tail -1)

if [ -z "$tag" ]; then
    versionName="${nearestTag}+${branch}.g${hash}.b${buildId}"
else
    versionName="$tag"
fi
versionCode=$((GITHUB_RUN_NUMBER + 100))

echo "versionName: $versionName"
echo "versionCode: $versionCode"

echo "ORG_GRADLE_PROJECT_OAG_VERSION_CODE=$versionCode" >>$GITHUB_ENV
echo "ORG_GRADLE_PROJECT_OAG_VERSION_NAME=$versionName" >>$GITHUB_ENV
echo "OAG_VERSION_CODE=$versionCode" >>$GITHUB_ENV
echo "OAG_VERSION_NAME=$versionName" >>$GITHUB_ENV
echo "CI_BUILD=true" >>$GITHUB_ENV
