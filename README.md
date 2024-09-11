# 1001 Albums Generator Widget  
  
# Git flow / Pull Requests
Before creating a pull request, make sure that your branch is up to date with main. Either rebasing or merges are fine.

After approval, the PR should be **SQUASHED AND MERGED**. Main should remain as clean as possible. This also helps when generating release notes.

## Releasing
To release the app we want to have at least one release candidate running for a few days, while observing for new crashes on Sentry.
When we're confident that the release is stable, we create a release.
The tag of the release dictates the version name of the app.

For the following sections we want to release app **1.3**

## Release candidate
Create a branch from main in the following format: `release/1.3` -> Push  
[Draft a new release](https://github.com/Clausr/1001albums/releases/new)  
1. Create a new tag: 1.3.0-rc01
2. Target `release/1.3` branch
3. Generate release notes
4. Choose Set as a pre-release
5. Publish release
This will start building the production app and push it to internal testers on Google Play

## Release
If any changes between release candidate and this release, they should be merged into the newly created release branch.
[Draft a new release](https://github.com/Clausr/1001albums/releases/new)  
1. Create a new tag: 1.3.0
2. Target `release/1.3` branch
3. Generate release notes
4. Choose **Set as the latest release**
5. Publish release

After Github Actions is done running, go to [Play Console](https://play.google.com/console/u/0/developers/7571329545281378386/app/4976014396259861064/tracks/internal-testing) and promote the build to production
