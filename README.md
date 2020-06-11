# YouTube Connector

## Connector Description

This connector allows you to crawl metadata for YouTube videos in a cahnnel

## Quick start

1. Build using
```
./gradlew clean assemblePlugin
```

2. This produces one zip file, `build/libs/youtube-connector.zip`. This artifact can be uploaded directly to Fusion as a connector plugin.

3. Configure in the Fusion UI.

## Potential improvements

* Use checkpoints to do incremental crawling so you don't have to refetch all videos every time.
* Improve OAuth configuration to use client id/secret + refresh tokens, rather than plain access tokens