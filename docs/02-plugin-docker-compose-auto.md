# Plugin `eu.xenit.docker-compose.auto`: Automatically inject built docker images into docker-compose

This plugin is an extension of [`eu.xenit.docker-compose`](./02-plugin-docker-compose.md) that automatically uses `dockerCompose.fromProject()` for all projects in your Gradle build.

It does not require you to explicitly list which projects you want to depend on.

```groovy
plugins {
    id "eu.xenit.docker-compose.auto" version "5.3.2" // See https://plugins.gradle.org/plugin/eu.xenit.docker-compose.auto for the latest version
}
```
