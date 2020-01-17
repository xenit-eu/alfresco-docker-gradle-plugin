# Alfresco Docker Gradle Plugins

[![Build Status](https://travis-ci.org/xenit-eu/alfresco-docker-gradle-plugin.svg?branch=master)](https://travis-ci.org/xenit-eu/alfresco-docker-gradle-plugin)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/eu/xenit/docker-alfresco/eu.xenit.docker-alfresco.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=eu.xenit.docker-alfresco)](https://plugins.gradle.org/plugin/eu.xenit.docker-alfresco)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/eu/xenit/docker/eu.xenit.docker.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=eu.xenit.docker)](https://plugins.gradle.org/plugin/eu.xenit.docker)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/eu/xenit/docker-compose/eu.xenit.docker-compose.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=eu.xenit.docker-compose)](https://plugins.gradle.org/plugin/eu.xenit.docker-compose)

This projects contains some gradle plugins that are used within Xenit Projects.

There are 3 main plugins:

- [`eu.xenit.docker-alfresco`](#plugin-eu-xenit-docker-alfresco): Makes it possible to build Alfresco and Share wars with amps installed. A docker
image can be built with the alfresco installed. It is also possible to include Alfresco Dynamic Extensions, and Alfresco
 Simple Modules.
- [`eu.xenit.docker`](#plugin-eu-xenit-docker): Build a docker image, starting from a Dockerfile.
- [`eu.xenit.docker-compose.auto`](#plugin-eu-xenit-docker-compose-auto): Inject built docker images from all projects in docker-compose

2 lower-level helper plugins that you can use when you only want the absolute basic configuration, without any conventions applied by the higher-level plugins.

- `eu.xenit.docker-config`: Helper plugin that is used to configure the docker environment from `gradle.properties` settings.
    This plugin is automatically applied when using `eu.xenit.docker` or `eu.xenit.docker-alfresco`
- [`eu.xenit.docker-compose`](#plugin-eu-xenit-docker-compose): Sets up `dockerCompose` configuration without automatically including all docker images built by other projects
    Tis plugin is automatically applied when using the `eu.xenit.docker-compose.auto` plugin

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

<a name="plugin-eu-xenit-docker-alfresco"></a>
### Plugin `eu.xenit.docker-alfresco`: Build an Alfresco Docker image

First, you need to apply the plugin to your `build.gradle`

```groovy
plugins {
    id "eu.xenit.docker-alfresco" version "5.0.0" // See https://plugins.gradle.org/plugin/eu.xenit.docker-alfresco for the latest version
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

 * `stripAlfrescoWar` & `stripShareWar`: [`StripAlfrescoWarTask`](src/main/java/eu/xenit/gradle/tasks/StripAlfrescoWarTask.java) Downloads Alfresco/Share war and trims them down to the minimum needed to apply AMPs
 * `applyAlfrescoAmp` & `applyShareAmp`: [`InstallAmpsInWarTask`](src/main/java/eu/xenit/gradle/tasks/InstallAmpsInWarTask.java) Uses [Alfresco MMT](https://docs.alfresco.com/5.2/concepts/dev-extensions-modules-management-tool.html) to install AMPs in Alfresco/Share
 * `applyAlfrescoSM` & `applyShareSM`: [`InjectFilesInWarTask`](src/main/java/eu/xenit/gradle/tasks/InjectFilesInWarTask.java) Installs [Simple Modules](https://docs.alfresco.com/5.2/concepts/dev-extensions-packaging-techniques-jar-files.html) on the Alfresco/Share classpath
 * `applyAlfrescoDE`: [`InjectFilesInWarTask`](src/main/java/eu/xenit/gradle/tasks/InjectFilesInWarTask.java) Installs [Dynamic extensions](https://github.com/xenit-eu/dynamic-extensions-for-alfresco) in Alfresco
 * `alfrescoWar` & `shareWar`: [`MergeWarsTask`](src/main/java/eu/xenit/gradle/tasks/MergeWarsTask.java) Delivers the finished Alfresco/Share war with all extensions installed.
 * `createDockerfile`: [`DockerfileWithWarsTask`](src/main/java/eu/xenit/gradle/tasks/DockerfileWithWarsTask.java) Creates a Dockerfile with instructions to add Alfresco and/or Share wars. Adds labels to identify extensions that have been installed.
 * `buildDockerImage`: [`DockerBuildImage`](https://bmuschko.github.io/gradle-docker-plugin/api/com/bmuschko/gradle/docker/tasks/image/DockerBuildImage.html) Builds a Docker image with the provided Dockerfile
 * `pushDockerImage`: [`DockerPushImage`](https://bmuschko.github.io/gradle-docker-plugin/api/com/bmuschko/gradle/docker/tasks/image/DockerPushImage.html) Pushes all tags of the Docker image to the remote repository

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

```groovy
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

#### Adding additional WARs to the automatically generated Dockerfile

You can modify the automatically generated `Dockerfile`. The plugin will configure this task to add Alfresco and Share with their extensions,
but you can make additional modifications to it if necessary.
The `createDockerFile` task is of type [`DockerfileWithWarsTask`](src/main/java/eu/xenit/gradle/tasks/DockerfileWithWarsTask.java),
which provides extra functionality on top of the methods provided by the upstream [`Dockerfile`](https://bmuschko.github.io/gradle-docker-plugin/api/com/bmuschko/gradle/docker/tasks/image/Dockerfile.html) type.

```groovy
configurations {
    rootWar
}
// Extensions provided by DockerfileWithWarsTask
createDockerFile {
    // Change directory where WAR files are written to, if your tomcat has a different webapps directory
    targetDirectory = "/usr/local/tomcat/webapps/"

    // Add an extra WAR file from a configuration
    addWar("ROOT", rootWar)
    // Add an extra WAR file from a normal file
    addWar("ROOT", file("deps/ROOT.war"))

    // Flag to set if the original WAR should be removed before a new WAR with the same name is added.
    // This option is used by dockerAlfresco.leanImage to be able to overlay partial WAR files
    removeExistingWar = true

    // Disables checking for matching Alfresco version between base WAR and base image (when using dockerAlfresco.leanImage)
    // DANGER: disabling this version checking is an escape-hatch for exceptional situations. Using a mismatched base WAR and base image WILL cause hard to debug issues.
    checkAlfrescoVersion false
}
```

#### Adding files to the automatically generated Dockerfile

The `createDockerFile` task is of type [`DockerfileWithWarsTask`](src/main/java/eu/xenit/gradle/tasks/DockerfileWithWarsTask.java)
(extending [`DockerfileWithCopyTask`](src/main/java/eu/xenit/gradle/tasks/DockerfileWithCopyTask.java) and the upstream [`Dockerfile`](https://bmuschko.github.io/gradle-docker-plugin/api/com/bmuschko/gradle/docker/tasks/image/Dockerfile.html) types)

A `smartCopy` method is available to make it easier to copy any file in the project to the Docker image.

```groovy
createDockerFile {
    // Copy the contents of "directory123" in your project to /opt/directory123 in the Docker image
    smartCopy "directory123", "/opt/directory123"
    // Copy the file "file123" in your project to /opt/file123 in the Docker image
    // Note: when copying a file and /opt/file123 is an already existing directory, the file will be copied to /opt/file123/file123
    // This is standard behavior of both Docker COPY and the Unix cp command
    smartCopy "file123", "/opt/file123"

    // Copy all files of a FileCollection or Configuration into a folder
    smartCopy files("lib"), "/tmp/classes"
    smartCopy runtimeClasspath, "/tmp/classes"
}
```

<a name="plugin-eu-xenit-docker"></a>
### Plugin `eu.xenit.docker`: Build a Docker image from a Dockerfile

First, you need to apply the plugin to your `build.gradle`
```groovy
plugins {
    id "eu.xenit.docker" version "5.0.0" // See https://plugins.gradle.org/plugin/eu.xenit.docker for the latest version
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

<a name="plugin-eu-xenit-docker-compose"></a>
### Plugin `eu.xenit.docker-compose`: Inject built docker images into docker-compose

This plugin extends the [`com.avast.gradle.docker-compose`](https://github.com/avast/gradle-docker-compose-plugin) plugin
to use docker images built with the [`DockerBuildImage`](https://bmuschko.github.io/gradle-docker-plugin/api/com/bmuschko/gradle/docker/tasks/image/DockerBuildImage.html) tasks in your `docker-compose.yml` file

It extends the `dockerCompose` configuration block with two functions:

 * `fromBuildImage([environmentVariable,] task)`: Add a dependency on the specified `DockerBuildImage` task, and expose the image id as an environment variable.
    * `fromBuildImage(String environmentVariable, DockerBuildImage task)`, `fromBuildImage(String environmentVariable, TaskProvider<DockerBuildImage> taskProvider)`: Exposes the id of the docker image built by the task as the specified environment variable.
    * `fromBuildImage(DockerBuildImage task)`, `fromBuildImage(TaskProvider<DockerBuildImage> taskProvider)`: Exposes the id of the docker image built by the task as an environment variable based on the project and task name.
 * `fromProject(project)`: Use `fromBuildImage()` for each `DockerBuildImage` task in the project.
    When the `eu.xenit.docker` or the `eu.xenit.docker-alfresco` plugins are applied, the `buildDockerImage` task is exposed as an environment variable based on project name only.
    * `fromProject(String)`
    * `fromProject(Project)`

```groovy
plugins {
    id "eu.xenit.docker-compose" version "5.0.0" // See https://plugins.gradle.org/plugin/eu.xenit.docker-compose for the latest version
}

dockerCompose {
    fromProject(":some-other-project")
    fromBuildImage(project.tasks.named("buildXYZDockerImage"))
}

task buildXYZDockerImage(type: DockerBuildImage) {
    // ...
}
```

> **Important:** Because Gradle uses plugin classpath isolation, this plugin needs to be present on the root project
> (or on a common parent of the docker-compose subproject and the subprojects that build docker images)
> 
> You can add the plugin to the root project without applying it by using `apply false`.
>
> ```groovy
> plugins {
>     id "eu.xenit.docker-compose" version "5.0.0" apply false
> }
> ```
> 
> In the subprojects, you can then apply plugins without specifying versions.
>
> ```groovy
> plugins {
>     id "eu.xenit.docker-compose"
> }
> ```

#### Environment variable naming

To generate an environment name for a docker image of a task, we concatenate project name, `_`, task name, `_DOCKER_IMAGE`.
Non-alphanumeric characters are converted to underscores, and camelCased names are converted to uppercase SNAKE_CASE names.

e.g. task `:projectA:taskAbc` -> `PROJECT_A_TASK_ABC_DOCKER_IMAGE`

When a project uses the `eu.xenit.docker` or the `eu.xenit.docker-alfresco` plugins, the `buildDockerImage` task is also exposed as a shorter environment variable: project name, `_DOCKER_IMAGE`

e.g. project `:projectA` -> `PROJECT_A_DOCKER_IMAGE` and `PROJECT_A_BUILD_DOCKER_IMAGE_DOCKER_IMAGE`


<a name="plugin-eu-xenit-docker-compose-auto"></a>
### Plugin `eu.xenit.docker-compose.auto`: Automatically inject built docker images into docker-compose

This plugin is an extension of [`eu.xenit.docker-compose`](#plugin-eu-xenit-docker-compose) that automatically uses `dockerCompose.fromProject()` for all projects in your Gradle build.

It does not require you to explicitly list which projects you want to depend on.

```groovy
plugins {
    id "eu.xenit.docker-compose.auto" version "5.0.0" // See https://plugins.gradle.org/plugin/eu.xenit.docker-compose.auto for the latest version
}
```

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

