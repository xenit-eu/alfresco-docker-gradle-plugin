# Alfresco Docker Gradle Plugins

[![CI](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/workflows/CI/badge.svg)](https://github.com/xenit-eu/alfresco-docker-gradle-plugin/actions?query=workflow%3ACI+branch%3Amaster)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/eu/xenit/docker-alfresco/eu.xenit.docker-alfresco.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=eu.xenit.docker-alfresco)](https://plugins.gradle.org/plugin/eu.xenit.docker-alfresco)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/eu/xenit/docker/eu.xenit.docker.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=eu.xenit.docker)](https://plugins.gradle.org/plugin/eu.xenit.docker)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/eu/xenit/docker-compose/eu.xenit.docker-compose.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=eu.xenit.docker-compose)](https://plugins.gradle.org/plugin/eu.xenit.docker-compose)

This projects contains some gradle plugins that are used within Xenit Projects.

There are 3 main plugins:

- [`eu.xenit.docker-alfresco`](#plugin-eu-xenit-docker-alfresco): Makes it possible to build Alfresco and Share wars with amps installed. A docker
image can be built with the alfresco installed. It is also possible to include Alfresco Dynamic Extensions, and Alfresco
 Simple Modules.
- [`eu.xenit.docker`](#plugin-eu-xenit-docker): Build a docker image, starting from a Dockerfile.
- [`eu.xenit.docker-compose.auto`](#plugin-eu-xenit-docker-compose-auto): Inject built docker images from all projects into docker-compose

2 extensions on top of the `eu.xenit.docker` plugin to enhance functionality:

- [`eu.xenit.docker.autotag`](#plugin-eu-xenit-docker-autotag): Add or rewrite tags to Docker images based on git branch, environment, ...
- [`eu.xenit.docker.label`](#plugin-eu-xenit-docker-label): Add labels to Docker images based on git commit, environment, ...

2 lower-level helper plugins that you can use when you only want the absolute basic configuration, without any conventions applied by the higher-level plugins.

- `eu.xenit.docker-config`: Helper plugin that is used to configure the docker environment from `gradle.properties` settings.
    This plugin is automatically applied when using `eu.xenit.docker` or `eu.xenit.docker-alfresco`
- [`eu.xenit.docker-compose`](#plugin-eu-xenit-docker-compose): Sets up `dockerCompose` configuration without automatically including all docker images built by other projects
    This plugin is automatically applied when using the `eu.xenit.docker-compose.auto` plugin

## Setup

First, install Docker on you local machine. It may listen on either a unix socket of a tcp socket.
Named pipes on Windows are supported. It will default to `npipe:////./pipe/docker_engine` if it exists,
or fall back to `tcp://localhost:2375` when it does not exist.
On Linux, you have to add your user to the `docker` group and do a logout and login.

After setting up docker, you can configure the location of the docker socket, if it is different from settings above.
When necessary, you can configure these settings in the global Gradle configuration file, `$HOME/.gradle/gradle.properties`.

<details>
<summary>Global configuration settings</summary>

```properties
# Docker socket (UNIX; default on Linux)
eu.xenit.docker.url=unix:///var/run/docker.sock
# Docker socket (npipe; default on Windows)
eu.xenit.docker.url=npipe:////./pipe/docker_engine
# Docker socket (TCP; fallback on Windows)
eu.xenit.docker.url=tcp://localhost:2375
# Path to your docker certificates, if you use TLS with the docker daemon.
# Make sure to respect the naming convention of the files inside (ca.pem, cert.pem, key.pem)
# Defaults to $DOCKER_CERT_PATH
eu.xenit.docker.certPath=
# The ip address that exposed ports should be bound to. (Sets the value of the DOCKER_IP environment variable)
# Defaults to the IP address extracted from the docker socket, or 127.0.0.1
eu.xenit.docker.expose.ip=127.0.0.1

# Registry credentials if you are using a private registry or need to push to docker hub.
# Use https://index.docker.io/v1/ for Docker Hub.
# On Linux, this is not required, as the native Docker credentials can be used.
# On Windows, it is necessary to specify these parameters because native Docker credentials are not supported.
eu.xenit.docker.registry.url=https://hub.xenit.eu/v2
eu.xenit.docker.registry.username=
eu.xenit.docker.registry.password=
```

</details>

## Usage

When you want to build a Docker image, you can choose between 2 plugins, depending on your usecase:

The `eu.xenit.docker` plugin builds Docker images from a Dockerfile that you provide. This plugin can be used to build any Docker image.

The `eu.xenit.docker-alfresco` plugin is specialized to build Docker images containing Alfresco and/or Share.It knows how to install AMPs, Simple Modules and Dynamic Extensions bundles, and can install the resulting application in a prepared Tomcat container.

<a name="plugin-eu-xenit-docker"></a>
### Plugin `eu.xenit.docker`: Build a Docker image from a Dockerfile

First, you need to apply the plugin to your `build.gradle`
```groovy
plugins {
    id "eu.xenit.docker" version "5.1.0" // See https://plugins.gradle.org/plugin/eu.xenit.docker for the latest version
}
```

#### Configuration

Then you can configure the `dockerBuild` configuration to set the Dockerfile, repositories and tags for the image that will be built.

If a `Dockerfile` exists in the root of your project, it will be used as the Dockerfile for the image build.
Else, the Dockerfile generated by `createDockerFile` will be used for the image build.

Repositories and tags are combined to produce the full image name that will be built.
Repositories and tags are optional. If none (or an empty list) are provided, the image will not have a name and can't be pushed to a repository.

```groovy
dockerBuild {
    dockerFile = file('Dockerfile') // or `createDockerFile.destFile`

    // Repositories to publish to.
    // Full image names will be constructed by cartesian product of repositories and tags
    repositories = ['xenit/example-docker-plugin', 'hub.xenit.eu/private/example-docker-plugin']
    tags = ['1','1.0','test']
}
```

#### Tasks

<a name="task-DockerfileWithCopyTask"></a>
##### `createDockerFile`: Programmatically create a Dockerfile

The `createDockerFile` task is of type [`DockerfileWithCopyTask`](src/main/java/eu/xenit/gradle/docker/tasks/DockerfileWithCopyTask.java).
It contains all methods of [the `Dockerfile` task type](https://bmuschko.github.io/gradle-docker-plugin/api/com/bmuschko/gradle/docker/tasks/image/Dockerfile.html), and adds a `smartCopy` method to easily add files.

You can build your whole Dockerfile programmatically with this task.
This comes in handy when you want to add build artifacts to the Dockerfile, or want to generate it dynamically based on some custom logic.

```groovy
createDockerFile {
    from("debian")
    runCommand("apt-get install default-jre")
    volume("/data")
    smartCopy(jar, "/opt/app/app.jar")
    workingDir("/opt/app/")
    defaultCommand("java", "-jar", "app.jar")
}
```

###### Using `smartCopy` to copy files

A `smartCopy` method is available to make it easier to copy any file in the project to the Docker image.

`smartCopy` works similar to `copyFile`, except that paths are relative to the project directory instead of relative to the Dockerfile.
There is no limitation to the location of files that are copied with `smartCopy`, files can originate from outside the project directory.

<details>
<summary><code>smartCopy</code> invocations</summary>

* single file or directory inputs:
    * `smartCopy(String source, String destination)`: Evaluated as `smartCopy(project.file(source), destination)`, `source` is resolved using [`Project#file()`](https://docs.gradle.org/current/javadoc/org/gradle/api/Project.html#file-java.lang.Object-).
    * `smartCopy(File source, String destination)`: Copies file or directory `source` to `destination` in the Docker image.
        * If `destination` does not exist, the `source` will be copied to that path
        * If `destination` is an already existing directory, `source` will be copied inside that directory. This is the same behavior as the Docker COPY instruction and the Unix cp command have.
    * `smartCopy(Provider<File> source, String destination)`
    * `smartCopy(Provider<File> source, Provider<String> destination)`
* file collections:
    * `smartCopy(FileCollection source, String destination)`: Copies the files in `source` to the directory `destination` in the Docker image. Contrary to the single-file copy, `destination` is forced to be a directory.
    * `smartCopy(FileCollection source, Provider<String> destination)`

</details>

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

##### `buildDockerImage`: Build the configured Docker image

The `buildDockerImage` task is of type [`DockerBuildImage`](https://bmuschko.github.io/gradle-docker-plugin/api/com/bmuschko/gradle/docker/tasks/image/DockerBuildImage.html).

It is automatically configured to use the Dockerfile that is configured in `dockerBuild.dockerFile` and to tag the images with names generated from `dockerBuild.repositories` and `dockerBuild.tags`.

#####  `pushDockerImage`: Push the built Docker image to its repository

The `pushDockerImage` task is of type [`DockerPushImage`](https://bmuschko.github.io/gradle-docker-plugin/api/com/bmuschko/gradle/docker/tasks/image/DockerPushImage.html).

It is automatically configured to push all the tagged images that are built by `buildDockerImage` to their respective repositories.


#### Example

An example for this plugin can be found in the [docker plugin example](src/integrationTest/examples/example-docker-plugin)

```bash
cd src/integrationTest/examples/example-docker-plugin
../../../../gradlew buildDockerImage
```


<a name="plugin-eu-xenit-docker-alfresco"></a>
### Plugin `eu.xenit.docker-alfresco`: Build an Alfresco Docker image

This plugin is built on top of the [`eu.xenit.docker` plugin](#plugin-eu-xenit-docker).

First, you need to apply the plugin to your `build.gradle`

```groovy
plugins {
    id "eu.xenit.docker-alfresco" version "5.1.0" // See https://plugins.gradle.org/plugin/eu.xenit.docker-alfresco for the latest version
}
```

#### Configuration

##### Installing Alfresco extensions

The Alfresco or Share war that will be used is specified in the `baseAlfrescoWar` and `baseShareWar` configurations.
Each of these configurations is restricted to contain only one artifact.

Extensions are specified in the same way as normal dependencies are specified in Gradle.
Like other Gradle dependencies, they can refer to other projects in the same build, or they can be downloaded as dependencies from the configured repositories.
Depending on the kind of the extension, they are added to a different configuration:

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

##### Build Alfresco Docker image

To build the Alfresco (or Share) Docker image, you have two options:
 * Use a skeleton image which does not contain a WAR. You can use [xenit/alfresco-repository-skeleton](https://hub.docker.com/r/xenit/alfresco-repository-skeleton) and [xenit/alfresco-share-skeleton](https://hub.docker.com/r/xenit/alfresco-share-skeleton), which contain all supporting files except for WARs themselves.
 * Use a full image which already contains a WAR. You can use [xenit/alfresco-repository-community](https://hub.docker.com/r/xenit/alfresco-repository-community) and [xenit/alfresco-share-community](https://hub.docker.com/r/xenit/alfresco-share-community), which contain the community WARs.
    (In case you want to use Enterprise images, you will need credentials for the Alfresco Nexus and you can build them using [docker-alfresco](https://github.com/xenit-eu/docker-alfresco) and [docker-share](https://github.com/xenit-eu/docker-share))

The only difference is in the `baseImage` property, which will point to a different Docker image.

<details>
<summary>Example Alfresco community docker build</summary>

```groovy
dockerBuild {
    alfresco {
        // Base image used in the FROM of the docker build. Should be a compatible image.
        baseImage = "xenit/alfresco-repository-community:6.0.7-ga"

        // Putting leanImage on true will only apply the custom modules to
        // image, and not the base war itself. The base war of the original
        // image is therefor not overwritten/removed. Speeds up build times.
        // Smaller last layer of the image. See documentation of lean image below.
        leanImage = true
    }

    // Repositories to publish to. Full image names will be constructed by cartesian product of repositories and tags
    repositories = ['xenit/my-example-alfresco', 'hub.xenit.eu/private/example-alfresco']
    tags = ['some', 'useful', 'tags']
}
```
</details>

###### Lean image

Lean image is an improvement that will only apply custom modules to the Docker image, and will apply them to the war present in the base image.

For this to work safely, the `baseAlfrescoWar` and the Alfresco war inside the image must be the same.

Using lean image has multiple advantages, by avoiding a copy of the Alfresco war:
 * the build will go faster
 * the Gradle build folder will remain smaller
 * Docker image layering will work optimal, by avoiding duplication of the same war across all Alfresco images you build.

|               | With Lean image | Without Lean image | 
| ------------- | ------------- | ------------- |
| Alfresco from war is unpacked over image|No | Yes |
| Base image must contain Alfresco   | Yes  | No |
| Base image can contain Alfresco | Yes | Yes, but it will be removed. |
| Build speed   | Fast  | Slow |
| Image layer size   | Small  | Big |
| Gradle build folder size   | Small  | Big |

```groovy
dockerBuild {
    alfresco {
        leanImage = true
    }
}
```

##### Publishing wars with extensions

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

##### Adding additional WARs to the automatically generated Dockerfile

You can modify the automatically generated `Dockerfile`. The plugin will configure this task to add Alfresco and Share with their extensions,
but you can make additional modifications to it if necessary.
The `createDockerFile` task is enhanced with the [`DockerfileWithWarsExtension`](src/main/java/eu/xenit/gradle/docker/alfresco/tasks/extension/DockerfileWithWarsExtension.java),
which provides extra functions to [the base task provided by `eu.xenit.docker`](#task-DockerfileWithCopyTask).

```groovy
configurations {
    rootWar
}

createDockerFile {
    /* These properties apply globally to all addWar invocations */
    // Change directory where WAR files are written to, if your tomcat has a different webapps directory
    targetDirectory = "/usr/local/tomcat/webapps/"

    // Flag to set if the original WAR should be removed before a new WAR with the same name is added.
    // This option is used by dockerAlfresco.leanImage to be able to overlay partial WAR files
    removeExistingWar = true

    // Disables checking for matching Alfresco version between base WAR and base image (when using dockerAlfresco.leanImage)
    // DANGER: disabling this version checking is an escape-hatch for exceptional situations. Using a mismatched base WAR and base image WILL cause hard to debug issues.
    checkAlfrescoVersion = false
    
    /* Wars are added in the place where addWar() is called, and can be interleaved with other Dockerfile instructions */
    // Add an extra WAR file from a configuration
    addWar("ROOT", rootWar)
    // Add an extra WAR file from a normal file
    addWar("ROOT", file("deps/ROOT.war"))

}
```

#### Tasks

The `eu.xenit.docker-alfresco` plugin makes use of some custom Gradle tasks and task types to install Alfresco extensions.
You can use these task types to build your own pipeline to modify WAR files, but this should not be necessary in almost all usecases.

> Please note: The descriptions below are the *conceptual* descriptions of the tasks, which is only valid when the tasks are used together as done by default by the plugin.
> For the more complex technical descriptions, please see the collapsible section below.

The Gradle tasks created by this plugin are executed in this order. (Ordering might not be entirely accurate, because there is no strict dependency ordering between all tasks.)

 * `stripAlfrescoWar` & `stripShareWar`: [`StripAlfrescoWarTask`](src/main/java/eu/xenit/gradle/docker/alfresco/tasks/StripAlfrescoWarTask.java) Downloads Alfresco/Share war and trims them down to the minimum needed to apply AMPs
 * `applyAlfrescoAmp` & `applyShareAmp`: [`InstallAmpsInWarTask`](src/main/java/eu/xenit/gradle/docker/alfresco/tasks/InstallAmpsInWarTask.java) Uses [Alfresco MMT](https://docs.alfresco.com/5.2/concepts/dev-extensions-modules-management-tool.html) to install AMPs in Alfresco/Share
 * `applyAlfrescoSM` & `applyShareSM`: [`InjectFilesInWarTask`](src/main/java/eu/xenit/gradle/docker/alfresco/tasks/InjectFilesInWarTask.java) Installs [Simple Modules](https://docs.alfresco.com/5.2/concepts/dev-extensions-packaging-techniques-jar-files.html) on the Alfresco/Share classpath
 * `applyAlfrescoDE`: [`InjectFilesInWarTask`](src/main/java/eu/xenit/gradle/docker/alfresco/tasks/InjectFilesInWarTask.java) Installs [Dynamic extensions](https://github.com/xenit-eu/dynamic-extensions-for-alfresco) in Alfresco
 * `prefixAlfrescoLog4j` & `prefixShareLog4j`: [`PrefixLog4JWarTask`](src/main/java/eu/xenit/gradle/docker/alfresco/tasks/PrefixLog4JWarTask.java) Configures a prefix for the application's `log4j.properties` file
 * `alfrescoWar` & `shareWar`: [`MergeWarsTask`](src/main/java/eu/xenit/gradle/docker/alfresco/tasks/MergeWarsTask.java) Delivers the finished Alfresco/Share war with all extensions installed.
 * `createDockerfile`: [`DockerfileWithWarsTask`](src/main/java/eu/xenit/gradle/docker/alfresco/tasks/DockerfileWithWarsTask.java) Creates a Dockerfile with instructions to add Alfresco and/or Share wars. Adds labels to identify extensions that have been installed.
 * `buildDockerImage`: [`DockerBuildImage`](https://bmuschko.github.io/gradle-docker-plugin/api/com/bmuschko/gradle/docker/tasks/image/DockerBuildImage.html) Builds a Docker image with the provided Dockerfile
 * `pushDockerImage`: [`DockerPushImage`](https://bmuschko.github.io/gradle-docker-plugin/api/com/bmuschko/gradle/docker/tasks/image/DockerPushImage.html) Pushes all tags of the Docker image to the remote repository

<details>
<summary>Technical description of tasks</summary>

In order to support `leanImage` to be able to reduce image size and to reduce the size of the Gradle build folder, most tasks do not operate upon the full Alfresco or Share war.

The first task of the chain, `stripAlfrescoWar`, will take a full war as input and will strip it down to a minimal version that only contains the necessary files to install AMPs with the Alfresco MMT.
Next, all the `apply*` and `prefix*Log4j` tasks can work in parallel to create a modified war.
Finally, the `alfrescoWar` task will take the output war files and overlays them on top of `baseAlfrescoWar`, creating a full war file again with all extensions installed.

The `createDockerfile` task will do more than only writing a `Dockerfile`.
It also handles copying and extracting war files to a staging directory, from where they are added to the docker image during `dockerBuildImage`.
Similar to how `alfrescoWar` overlays war files, `createDockerfile` also overlays the contents of the war files on top of each other. When `leanImage` is used, the `baseAlfrescoWar` is not added, since its contents are already present in the base docker image.


```
baseAlfrescoWar -+-------------------------------------------------------------------------------------> alfrescoWar[MergeWarsTask]
                 \--> stripAlfrescoWar[StripAlfrescoWarTask] -+-> applyAlfrescoAmp[InstallAmpsInWarTask] --^ ^ ^ ^
                                                              +--> applyAlfrescoDE[InjectFilesInWarTask] ----/ | |
                                                              +--> applyAlfrescoSM[InjectFilesInWarTask] ------/ |
                                                              \--> prefixAlfrescoLog4j[PrefixLog4JWarTask] ------/
```

</details>

#### Example

An example for the usage of this plugin can be found in the [applyamps example](src/integrationTest/examples/applyamps-example).
```bash
./gradlew buildDockerImage
```

When you check the labels of the docker image, you will notice that the base wars and the amps are listed here.
The contents of these labels are not meant to be consumed automatically, only as reference for a human who inspects the image.

```bash
docker inspect applyamps-example:some
[...]
    "Labels": {
        "eu.xenit.gradle-plugin.applyAlfrescoAmp": "javascript-console-repo-0.6.amp",
        "eu.xenit.gradle-plugin.applyAlfrescoDE": "",
        "eu.xenit.gradle-plugin.applyAlfrescoSM": "",
        "eu.xenit.gradle-plugin.applyShareAmp": "javascript-console-share-0.6.amp",
        "eu.xenit.gradle-plugin.applyShareSM": "",
        "eu.xenit.gradle-plugin.stripAlfrescoWar": "content-services-community-6.0.a.war",
        "eu.xenit.gradle-plugin.stripShareWar": "share-6.0.c.war"
    }
[...]
```
 
<a name="plugin-eu-xenit-docker-label"></a>
### Plugin `eu.xenit.docker.label`: Automatically label docker images

This plugin configures the `eu.xenit.docker` plugin, and is automatically applied by it.

This plugin adds an extension to the `dockerBuild` configuration:

```groovy
dockerBuild {
    // [...]
    label {
        // Configure Docker image labels here
        fromGit()
    }
}
```

#### Label contributors

 * **git**: Adds labels derived from git: current branch, origin repository, latest commit hash and author.
   This label contributor is enabled by default and can be disabled by calling `fromGit(false)`

<a name="plugin-eu-xenit-docker-autotag"></a>
### Plugin `eu.xenit.docker.autotag`: Add and rewrite Docker image tags

This plugin configures the `eu.xenit.docker` plugin, and is automatically applied by it.

This plugin adds functions to the `dockerBuild` configuration that can be used to add and change Docker image tags.

#### `autotag.legacyTags(List<String>)`

```groovy
dockerBuild {
    tags = autotag.legacyTags(["some", "useful", "tags"])
}
```

On Jenkins, the branch is set with the environment variable `BRANCH_NAME`. When it is set, all tags will be
prepended with `$BRANCH_NAME-`, otherwise with `local`. An extra tag, `$BRANCH_NAME` or `local`, will be added too.
When the branch is `master`, the branch name is not prepended and a tag `latest` is added.

If the environment variable `BUILD_NUMBER` is set, an extra tag is added: `build-yyyyMMddHHmm-$BUILD_NUMBER`, also prepended with the branch name.

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
    id "eu.xenit.docker-compose" version "5.1.0" // See https://plugins.gradle.org/plugin/eu.xenit.docker-compose for the latest version
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
>     id "eu.xenit.docker-compose" version "5.1.0" apply false // See https://plugins.gradle.org/plugin/eu.xenit.docker-compose for the latest version
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
    id "eu.xenit.docker-compose.auto" version "5.1.0" // See https://plugins.gradle.org/plugin/eu.xenit.docker-compose.auto for the latest version
}
```

# Plugin development

## Creating a release

Every git tag is automatically published to the gradle plugins repository by Travis-CI.

This plugin follows SemVer and tags are managed with [Reckon](https://github.com/ajoberstar/reckon).

To create a release from a commit, use `./gradlew reckonTagPush -Preckon.scope=patch -Preckon.stage=final` to create a new patch release.

Tests are required to pass before a new release can be tagged.

