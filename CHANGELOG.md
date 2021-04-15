# Alfresco Docker Gradle Plugins - Changelog

## Unreleased

**This release drops support for Gradle versions before 5.6**

### Fixed

* [#191](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/191) - dockerCompose fromBuildImage/fromProject silently fail when wrong argument type is provided
* [#192](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/192) - Deprecated non-annotated properties in prefixXXXLog4j tasks
* [#204](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/204) - Support Gradle 7.0

### Dependencies

* [#203](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/203) - Bump gradle-docker-compose-plugin from 0.14.2 to 0.14.3

## Version 5.2.0 - 2021-01-22

### Added

* [#170](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/170) - dockerCompose.fromProject() with
  user-configured environment variable
* [#181](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/181) - When a failure occurs during applyAmp, show the filename that failed to apply
* [#185](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/185) - Update com.bmuschko:gradle-docker-plugin to 6.7.0
    * This enables native docker credential store support on Windows

### Fixed

* [#187](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/187) - Fix deprecation warnings shown
  incorrectly

## Version 5.1.1 - 2020-11-24

### Added

 * [#176](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/176) - Workaround for Docker bug with 7 consecutive COPY instructions

### Fixed

 * [#173](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/173) - `autotag.legacyTags()` NPE when no arguments provided
 * [#175](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/175) - Remove empty commands before doing COPY consolidation


## Version 5.1.0 - 2020-10-20

### Added

 * Add ability to configure multiple repositories for one image build (See [UPGRADING-5.1](./UPGRADING-5.1.md))

### Changed

 * [#161](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/161) - Build `eu.xenit.docker-alfresco` plugin on top of `eu.xenit.docker` plugin
    - Consolidate docker configurations into one configuration block: `dockerBuild` (See [UPGRADING-5.1](./UPGRADING-5.1.md))
    - [#156](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/156) - Add `createDockerFile` task in `eu.xenit.docker` plugin
    - [#157](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/157) - Configure `eu.xenit.docker` with settings from `eu.xenit.docker-alfresco` plugin
    - [#155](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/155) - Make a convention extension out of `addWar()`
    - [#158](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/158) - Move DockerBuildBehavior into `eu.xenit.docker` plugin
 * Move configuration of `pull`, `noCache` & `remove` properties to `buildDockerImage` task (See [UPGRADING-5.1](./UPGRADING-5.1.md))
 * Move `automaticTags` to separate extension to generate tags (See [UPGRADING-5.1](./UPGRADING-5.1.md))

## Version 5.0.7 - 2020-08-11

### Fixed

 * [#149](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/149) - Docker configuration is not automatically applied with docker-compose plugin

### Changed

 * [#145](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/145) - Npipe support for docker on windows
 * [#140](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/140) - addWar commands now make use of smartCopy under the hood

## Version 5.0.6 - 2020-05-25

### Fixed

 * [#133](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/133) - Alfresco version check does not check `version.label`
 * [#128](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/128) - `createDockerFile#removeExistingWar` config modified after configuration phase
 * [#126](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/126) - Fix build failures with special characters in commit messages

### Changed

 * [#124](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/124), [#127](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/127), [#131](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/131), [#135](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/135) - Update gradle-docker-compose-plugin from 0.10.9 to 0.12.1
    - [Add `checkContainersRunning` option](https://github.com/avast/gradle-docker-compose-plugin/releases/tag/0.10.10): Early error when container does not start up
    - [Fix reconnecting for nested settings](https://github.com/avast/gradle-docker-compose-plugin/releases/tag/0.12.1)

## Version 5.0.5 - 2020-04-06

### Changed

 * [#116](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/116) - `pushDockerImage` now fails when no image-tags are configured
 * [#115](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/115) - Update com.bmushko:gradle-docker-plugin to 6.4.0, supports Docker credentials store
 * [#121](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/121) - Bump gradle-docker-compose-plugin from 0.10.7 to 0.10.9

## Version 5.0.4 - 2020-02-28

### Added

 * [#112](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/112) - Disable automatic pulling of images when Gradle runs in `--offline` mode
 * [#80](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/81) - A better error message when an AMP fails to apply

### Fixed

 * [#107](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/107) - Fix lean image
 * [#50](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/50) - Installing AMPs in a war that already has AMPs installed
 * [#111](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/pull/111) - log4j warning when running some tasks

## Version 5.0.3 - 2020-02-24

### Fixed

 * [#97](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/97) - Fix labelling image with applied AMPs, DEs and SMs
 * [#104](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/104) - composeUp task fails because buildDockerImage is up-to-date

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
