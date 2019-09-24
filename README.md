# Alfresco Docker Gradle Plugins

[![Build Status](https://travis-ci.org/xenit-eu/alfresco-docker-gradle-plugin.svg?branch=master)](https://travis-ci.org/xenit-eu/alfresco-docker-gradle-plugin)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/eu/xenit/docker-alfresco/eu.xenit.docker-alfresco.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=eu.xenit.docker-alfresco)](https://plugins.gradle.org/plugin/eu.xenit.docker-alfresco)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/eu/xenit/docker/eu.xenit.docker.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=eu.xenit.docker)](https://plugins.gradle.org/plugin/eu.xenit.docker)

This projects contains some gradle plugins that are used within Xenit Projects.

Currently there are 3 plugins:

- `eu.xenit.docker-alfresco`: Makes it possible to build Alfresco and Share wars with amps installed. A docker
image can be built with the alfresco installed. It is also possible to include Alfresco Dynamic Extensions, and Alfresco
 Simple Modules.
- `eu.xenit.docker`: Build a docker image, starting from a Dockerfile.
- `eu.xenit.docker-config`: Helper plugin that is used to configure the docker environment.
This plugin is automatically applied when using the two other plugins.

## Setup

First, install Docker on you local machine. It may listen on either a unix socket of a tcp socket. Named pipes on Windows are not supported.
On Windows, you should enable the _Expose daemon on tcp://localhost:2375 without TLS_ option in the docker settings.
On Linux, you have to add your user to the `docker` group and do a logout and login.

After setting up docker, you can configure the location of the docker socket, if it is different from settings above.
When necessary, you can configure these settings in the global Gradle configuration file, `$HOME/.gradle/gradle.properties`.

```properties
# Docker socket (UNIX; default on Linux)
eu.xenit.docker.url=unix:///var/run/docker.sock
# Docker socket (TCP; default on Windows)
eu.xenit.docker.url=tcp://localhost:2375
# Path to your docker certificates, if you use TLS with the docker daemon.
# Make sure to respect the naming convention of the files inside (ca.pem, cert.pem, key.pem)
# Defaults to $DOCKER_CERT_PATH
eu.xenit.docker.certPath=
# The ip address that exposed ports should be bound to. (Sets the value of the DOCKER_IP environment variable)
# Defaults to the IP address extracted from the docker socket, or 127.0.0.1
eu.xenit.docker.expose.ip=127.0.0.1

# Registry credentials if you are using a private registry or need to push to docker hub.
# Use https://index.docker.io/v1/ for Docker Hub
eu.xenit.docker.registry.url=https://hub.xenit.eu/v2
eu.xenit.docker.registry.username=
eu.xenit.docker.registry.password=
```

## Usage

When you want to build a Docker image, you can choose between 2 plugins, depending on your usecase:

The `eu.xenit.docker` plugin builds Docker images from a Dockerfile that you provide. This plugin can be used to build any Docker image.
The `eu.xenit.docker-alfresco` plugin is specialized to build Docker images containing Alfresco and/or Share. It knows how to install AMPs, Simple Modules and Dynamic Extensions bundles, and can install the resulting application in a prepared Tomcat container.

### Plugin `eu.xenit.docker-alfresco`: Build an Alfresco Docker image

First, you need to apply the plugin to your `build.gradle`

```groovy
plugins {
    id "eu.xenit.docker-alfresco" version "4.1.0" // See https://plugins.gradle.org/plugin/eu.xenit.docker-alfresco for the latest version
}
```

#### Installing Alfresco extensions

The Alfresco or Share war that will be used is specified in the `baseAlfrescoWar` and `baseShareWar` configurations.
Each of these configurations is restricted to contain only one artifact.

Extensions are specified in the same way like normal dependencies are specified in Gradle.
Like other Gradle dependencies, they can refer to other projects in the same build or they can be downloaded as dependencies from the configured repositories.
Depending on the type of the extension, they are added to a different configuration:

 * `alfrescoAmp`: AMP packages to apply to `baseAlfrescoWar`
 * `alfrescoSM`: Simple modules (JAR files) to add to `baseAlfrescoWar`
 * `alfrescoDE`: Dynamic Extension modules to add to `baseAlfrescoWar`
 * `shareAmp`: AMP packages to apply to `baseShareWar`
 * `shareSM`: Simple modules (JAR files) to add to `baseShareWar`
 
Note that all configurations will also pull in their transitive dependencies by default,
which may result in undesired additional dependencies being added to Alfresco.
To avoid undesired dependencies, you can [set `transitive = false` on a dependency or configure exclusion rules on the configuration](https://docs.gradle.org/current/userguide/managing_transitive_dependencies.html#sec:excluding_transitive_module_dependencies).

<details>
<summary>Example Alfresco extensions</summary>

```groovy
dependencies {
    baseAlfrescoWar "org.alfresco:content-services-community:6.0.a@war"
    alfrescoAmp "de.fmaul:javascript-console-repo:0.6@amp"
    alfrescoDE(group: 'eu.xenit', name: 'move2alf-backend-de-50', version: '2.9.2-24') {
        transitive = false
    }
    alfrescoSM("group:artifactId:version")
    baseShareWar(group: 'org.alfresco', name: 'share', version: '5.1.1.4', ext: 'war')
    shareAmp(group: 'org.sharextras', name: 'javascript-console-share', version: '0.6.0', ext: 'amp')
    shareSM("group:artifactId:version")
}
```
</details>

#### Build Alfresco Docker image

To build the Alfresco (or Share) Docker image, you have two options:
 * Use a skeleton image which does not contain a WAR. You can use [xeniteu/alfresco-repository-skeleton](https://hub.docker.com/r/xeniteu/alfresco-repository-skeleton) and [xeniteu/alfresco-share-skeleton](https://hub.docker.com/r/xeniteu/alfresco-share-skeleton), which contain all supporting files except for WARs themselves.
 * Use a full image which already contains a WAR. You can use [xeniteu/alfresco-repository-community](https://hub.docker.com/r/xeniteu/alfresco-repository-community) and [xeniteu/alfresco-share-community](https://hub.docker.com/r/xeniteu/alfresco-share-community), which contain the community WARs.
    (In case you want to use Enterprise images, you will need credentials for the Alfresco nexus and you can build them using [docker-alfresco](https://github.com/xenit-eu/docker-alfresco) and [docker-share](https://github.com/xenit-eu/docker-share))

For both cases, the only difference is in the `baseImage` property, which will point to an other Docker image to use as base.

<details>
<summary>Example Alfresco community docker build</summary>

```groovy
dockerAlfresco {
    // Base image used in the FROM of the docker build. Should be a compatible image.
    baseImage = "xeniteu/alfresco-repository-community:6.0.7-ga"

    // Putting leanImage on true will only apply the custom modules to
    // image, and not the base war itself. The base war of the original
    // image is therefor not overwritten/removed. Speeds up build times.
    // Smaller last layer of the image. See documentation of lean image below.
    leanImage = true

    dockerBuild {
        // Repository to publish to.
        repository = 'my-example-alfresco'
        tags = ['some', 'useful', 'tags']

        // On Jenkins, branches other than master will be appended with -branch.
        // Local build will be appended with -local
        // You can disable this behavior with
        automaticTags = false
    }
}
```
</details>

##### Lean image

Lean image is a flag to optimize the build. This will optimize the build speed, gradle build folder size and docker image size. The following table explains the difference.

|               | With Lean image | Without Lean image | 
| ------------- | ------------- | ------------- |
| Alfresco from war is unpacked over image|No | Yes |
| Base image must contain Alfresco   | Yes  | No |
| Base image can contain Alfresco | Yes | Yes, but it will be removed. |
| Build speed   | Fast  | Slow |
| Image layer size   | Small  | Big |
| Gradle build folder size   | Small  | Big |

```groovy
dockerAlfresco {
    leanImage = true
}
```

#### Tasks

The `eu.xenit.docker-alfresco` plugin makes use of some custom Gradle tasks and task types to install Alfresco extensions.
You can use these task types to build your own pipeline to modify WAR files, but this should not be necessary in almost all usecases.

> Please note: The descriptions below are the *conceptual* descriptions of the tasks, which is only valid when the tasks are used together as done by default by the plugin.
> For the more complex technical descriptions, please see the collapsible section below.

The Gradle tasks created by this plugin are executed in this order. (Ordering might not be entirely accurate, because there is no strict dependency ordering between all tasks.)

 * `resolveAlfrescoWar` & `resolveShareWar`: [`StripAlfrescoWarTask`](src/main/java/eu/xenit/gradle/tasks/StripAlfrescoWarTask.java) Downloads Alfresco/Share WAR
 * `applyAlfrescoAmp` & `applyShareAmp`: [`InstallAmpsInWarTask`](src/main/java/eu/xenit/gradle/tasks/InstallAmpsInWarTask.java) Uses [Alfresco MMT](https://docs.alfresco.com/5.2/concepts/dev-extensions-modules-management-tool.html) to install AMPs in Alfresco/Share
 * `applyAlfrescoSM` & `applyShareSM`: [`InjectFilesInWarTask`](src/main/java/eu/xenit/gradle/tasks/InjectFilesInWarTask.java) Installs [Simple Modules](https://docs.alfresco.com/5.2/concepts/dev-extensions-packaging-techniques-jar-files.html) on the Alfresco/Share classpath
 * `applyAlfrescoDE`: [`InjectFilesInWarTask`](src/main/java/eu/xenit/gradle/tasks/InjectFilesInWarTask.java) Installs [Dynamic extensions](https://github.com/xenit-eu/dynamic-extensions-for-alfresco) in Alfresco
 * `alfrescoWar` & `shareWar`: [`MergeWarsTask`](src/main/java/eu/xenit/gradle/tasks/MergeWarsTask.java) Delivers the finished Alfresco/Share war with all extensions installed.
 * `createDockerfile`: [`DockerfileWithWarsTask`](src/main/java/eu/xenit/gradle/tasks/DockerfileWithWarsTask.java) Creates a Dockerfile with instructions to add Alfresco and/or Share wars. Adds labels to identify extensions that have been installed.
 * `buildDockerImage`: [`DockerBuildImage`](https://bmuschko.github.io/gradle-docker-plugin/api/com/bmuschko/gradle/docker/tasks/image/DockerBuildImage.html) Builds a Docker image with the provided Dockerfile
 * `pushDockerImage`: Pushes all tags of the Docker image to the remote repository

<details>
<summary>Technical description of tasks</summary>

In order to support `leanImage` to be able to reduce image size and to reduce the size of the Gradle build folder, most tasks do not operate upon the full Alfresco or Share war.

The first task of the chain, `resolveAlfrescoWar`, will take a full WAR as input and will strip the WAR down to a minimal version that only contains the needed files to install AMPs with the Alfresco MMT.
Next, all the `apply*` tasks can work in parallel to add the files they need to add.
Finally, the `alfrescoWar` task will take the output WAR files the `apply*` tasks and overlays them on top of `baseAlfrescoWar`, creating a full WAR file again with all extensions installed.

The `createDockerfile` task will do more than only writing a `Dockerfile`. It also handles copying and extracting WAR files to a staging directory, from where they are added to the docker image during `dockerBuildImage`.
Similar to how `alfrescoWar` overlays WAR files, `createDockerfile` also overlays the contents of the WAR files on top of each other. In case `leanImage` is used, the `baseAlfrescoWar` is not added, since its contents are already present in the base docker image.


```
baseAlfrescoWar -+----------------------------------------------------------------------------------------> alfrescoWar[MergeWarsTask]
                 \--> resolveAlfrescoWar[StripAlfrescoWarTask] -+-> applyAlfrescoAmp[InstallAmpsInWarTask] --^ ^ ^
                                                                +--> applyAlfrescoDE[InjectFilesInWarTask] ----/ |
                                                                \--> applyAlfrescoSM[InjectFilesInWarTask] ------/
```

</details>

An example for the usage of this plugin can be found in the [applyamps example](src/integrationTest/examples/applyamps-example).
```bash
gradle buildDockerImage
```

When you check the labels of you docker image, you will notice that the base wars and the amps are listed.

#### Publishing wars with extensions

Instead of creating a docker image with Alfresco and Share, it is also possible to publish
the Alfresco or Share war file with all AMPs, Dynamic Extensions and Simple Modules applied to it.

The tasks `alfrescoWar` and `shareWar` create respectively an Alfresco and a Share war that can be published.

```gradle
publishing.publications {
    alfresco(MavenPublication) {
        artifactId "repo"
        artifact alfrescoWar
    }
    share(MavenPublication) {
        artifactId "share"
        artifact shareWar
    }
}
```

### Plugin `eu.xenit.docker`: Build a Docker image from a Dockerfile

First, you need to apply the plugin to your `build.gradle`
```groovy
plugins {
    id "eu.xenit.docker" version "4.1.0" // See https://plugins.gradle.org/plugin/eu.xenit.docker for the latest version
}
```

Similar to `eu.xenit.docker-alfresco`, you also have configure the `dockerBuild` here to set the repository and tags for the image that will be built from a `Dockerfile`

```groovy
dockerFile {
    dockerFile = file('Dockerfile')
    dockerBuild {
        // Repository to publish on.
        repository = 'example-docker-plugin'
        tags = ['1','1.0','test'] //optional
        // On Jenkins, branches other than master will be prepended with $BRANCH_NAME-.
        // Local build will be prepended with local-
        // You can disable this behavior with
        // automaticTags = false
    }
}
```

An example for this plugin can be found in the [docker plugin example](src/integrationTest/examples/example-docker-plugin)
```bash
gradle buildDockerImage
```

#### Tasks

The `eu.xenit.docker` plugin registers following Gradle tasks:

 * `buildDockerImage`: [`DockerBuildImage`](https://bmuschko.github.io/gradle-docker-plugin/api/com/bmuschko/gradle/docker/tasks/image/DockerBuildImage.html) Builds a Docker image with the provided Dockerfile
 * `pushDockerImage`: Pushes all tags of the Docker image to the remote repository

## Tagging behavior

On Jenkins, the branch is set with the environment variable `BRANCH_NAME`. When it is set, all manually tags will be
prepended with `$BRANCH_NAME-`, otherwise with `local`. Also an extra tag `$BRANCH_NAME`, or `local` will be added.
When the branch is `master`, the tags are not prepended and a tag `latest` is used.

When an environment variable `BUILD_NUMBER` is set, an extra tag is added: `build-yyyyMMddHHmm-$BUILD_NUMBER` is added.
When the branch is not master, this is also prepended.

This tagging behavior can be disabled by adding `automaticTags = false` to the dockerBuild configuration.

# Plugin development

## Creating a release

Every git tag is automatically published to the gradle plugins repository by Travis-CI.

This plugin follows SemVer and tags are managed with [Reckon](https://github.com/ajoberstar/reckon).

To create a release from a commit, use `./gradlew reckonTagPush -Preckon.scope=patch -Preckon.stage=final` to create a new patch release.

Tests are required to pass before a new release can be tagged.

