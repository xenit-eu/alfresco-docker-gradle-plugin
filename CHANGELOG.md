# Alfresco Docker Gradle Plugins - Changelog

## Unreleased

## Version 5.0.2 - 2020-01-17

### Added

 * [#95](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/95) - Nested dockerCompose configurations

### Fixed

 * [#96](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/96) - eu.xenit.docker-compose plugin breaks up-to-date for buildDockerImage
 * [#98](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/98) - Alfresco extensions are not applied on Gradle 6.1

## Version 5.0.1 - 2020-01-22

### Fixes

 * [#91](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/91) - Make pushDockerImage task depend on the buildDockerImage task
 * [#92](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/92) - Fix pushDockerImage does not work with plain credentials in .docker/config.json

### Changed

 * [#93](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/93) - Dependencies are shaded into the plugin

## Version 5.0.0 - 2020-01-20

### Fixes

 * [#86](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/86) - Configure docker-compose correctly when expose IP is set to something else than 0.0.0.0 or 127.0.0.1
 * [#72](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/72)/[#89](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/89) - Truezip IO exception during applyAlfrescoAmp task

### Changed

 * [#55](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/55) - Update com.bmushko.docker-remote-api plugin from 4.6.2 to 6.1.1
 * [#82](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/82) - Update com.avast.gradle.docker-compose plugin from 0.8.12 to 0.10.7
 * [#75](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/75) - Use Gradle lazy `Property<>` for all task and configuration properties
 * [#83](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/83) - Move task classes to better namespaced packages & move internal classes to internal packages

### Added

 * [#59](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/59) - Add docker-compose plugin that sets docker images from docker build tasks
 * [#65](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/65) - Support reading credentials from docker credentials store
 * [#71](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/71) - Support Gradle 6.0
 
### Removed

 * [#58](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/58) - Do not automatically apply docker-compose plugin when applying docker or docker-alfresco
 * [#78](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/78) - Remove deprecated functionality
    - [#48](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/48) - automaticTags defaults to `false` now
    - Remove `ResolveWarTask` task type
    - Remove `labelDockerFile` task
    - Remove `buildLabels` task
    - Remove `mergeAlfrescoWar` task
    - Remove `dockerAlfresco.baseImage` configuration setting
    - Remove `dockerAlfresco.dockerBuild.repositoryBase` configuration setting
    - Remove `dockerFile.dockerBuild.repositoryBase` configuration setting
    - Remove `DockerfileWithWarsTask#baseImage` task property
    - Remove `DockerfileWithWarsTask#alfrescoWar` task property
    - Remove `DockerfileWithWarsTask#shareWar` task property

## Version 4.1.2 - 2019-12-17

### Fixes

* [#72](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/72) - ApplyAlfrescoAmp cannot open module.properties
* [#68](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/68) - DOCKER_HOST envvar is only set when eu.xenit.docker.url is set

## Version 4.1.1 - 2019-09-26

### Fixes

 * [#66](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/66) - smartCopy has unexpected behaviour with copying folder

## Version 4.1.0 - 2019-09-24

### Added

 * [DEVEM-344](https://xenitsupport.jira.com/browse/DEVEM-344) - Make MergeWarTask extend the Zip task
    - Make it easier to publish WAR files with extensions applied
 * [#44](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/44) - Automatically apply amps in the correct order
 * [#42](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/42) - Improve experience to add additional files to a docker image
 
### Fixes

 * [#38](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/38) - DockerfileWithWarsTask is not threadsafe
 * [#33](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/33) - Warning messages when building with Java 11
 * [#45](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/45) - Handle initialized git repository without commits
 * [#36](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/36) - Dependencies are resolved during the configuration phase

## Version 4.0.3 - 2019-01-17

### Fixes

 * [DEVEM-342](https://xenitsupport.jira.com/browse/DEVEM-342) - Update dependency `com.avast.gradle:gradle-docker-compose-plugin`
to version 0.8.12 to solve failing `composeUp` when there is output on stderr
 * [DEVEM-351](https://xenitsupport.jira.com/browse/DEVEM-351) - Ensure that the push tasks pushes the correct tags.
 * [#25](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/25) - Info and debug logging instead of stacktrace when not able to 
 create URL for commit.
 * [DEVEM-352](https://xenitsupport.jira.com/browse/DEVEM-352) - Clear error message when base image is not set.

## Version 4.0.2 - 2018-10-17

# Fixes

* [#9](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/9) - Broken version published to the plugin portal

## Version 4.0.1 (YANKED) - 2018-10-17

### Fixes

 * [#5](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/5) - Alfresco repository required to resolve classpath artifacts
 * [#6](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/6) - `DockerException: Bind address needs a port: null` when using docker-compose
 
### Changes

 * Update avast docker-compose plugin to 0.8.8

## Version 4.0.0 - 2018-10-03

### Changes

 * [DEVEM-325](https://xenitsupport.jira.com/browse/DEVEM-325) - Rename gradle plugins
    - Gradle plugins have been renamed to eu.xenit.docker-alfresco and eu.xenit.docker
 * [DEVEM-322](https://xenitsupport.jira.com/browse/DEVEM-322) - Publish to gradle plugin portal
    - This makes it easier to use the plugins, without setting buildscript repositories and classpath
 * [DEVEM-324](https://xenitsupport.jira.com/browse/DEVEM-324) - Configure Travis CI builds

### Fixes

 * [DEVEM-147](https://xenitsupport.jira.com/browse/DEVEM-147) - Applying the xenit-dockerbuild gradle plugin without providing a config results in a NullPointerException

## Version 3.3.1 - 2018-08-24

### Fixes

 * [DEVEM-301](https://xenitsupport.jira.com/browse/DEVEM-301) - GStringImpl cannot be cast to String when using tags with variables

### Added

 * [DEVEM-298](https://xenitsupport.jira.com/browse/DEVEM-298) - Using leanImage=true enables check if the version of the war matches the version in the baseImage

## Version 3.3.0 - 2018-06-07

### Added

 * Smaller Alfresco builds
    * [DEVEM-289](https://xenitsupport.jira.com/browse/DEVEM-289) - Stripping the Alfresco war
        - Apply modules to a stripped Alfresco war, which only contains version information
    * [DEVEM-290](https://xenitsupport.jira.com/browse/DEVEM-290) - Merging stripped war with baseWar
        - Merge stripped Alfresco war together with the baseWar, creating a full Alfresco war with modules applied
    * [DEVEM-291](https://xenitsupport.jira.com/browse/DEVEM-291) - Change gradle project structure to use stripped war
        - Wrap the existing war enrichment tasks with a stripping and merging task
    * [DEVEM-293](https://xenitsupport.jira.com/browse/DEVEM-293) - Skip merging war files when creating a docker file
        - Immediately extract the stripped wars on top of the exploded basewar
    * [DEVEM-295](https://xenitsupport.jira.com/browse/DEVEM-295) - Support for lean images
        - Only adding the extensions on top of the baseImage.
 * [DEVEM-292](https://xenitsupport.jira.com/browse/DEVEM-293) - Add flag to disable automatic tagging behaviors

## Version 3.2.0

### Dependencies

* [DEVEM-274](https://xenitsupport.jira.com/browse/DEVEM-274) - Updated dependency `com.bmuschko:gradle-docker-plugin:3.2.0` -> `3.2.5`
    - When defining a `HEALTHCHECK` for a container, `./gradlew composeUp` now waits untill the container is healthy. Great use-case for integration-tests!
* [DEVEM-279](https://xenitsupport.jira.com/browse/DEVEM-279) - Updated dependency `com.avast.gradle:gradle-docker-compose-plugin:0.4.2` -> `0.7.1`
    - Supports [nested configurations](https://github.com/avast/gradle-docker-compose-plugin#nested-configurations), which allows multiple compose-configs sets

### Added

* [DEVEM-283](https://xenitsupport.jira.com/browse/DEVEM-283) - support `dockerBuild` options: `pull`, `noCache`, `remove`
