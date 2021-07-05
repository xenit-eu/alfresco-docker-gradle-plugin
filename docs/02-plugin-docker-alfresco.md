# Plugin `eu.xenit.docker-alfresco`: Build an Alfresco Docker image

This plugin is built on top of the [`eu.xenit.docker` plugin](./02-plugin-docker.md).

First, you need to apply the plugin to your `build.gradle`

```groovy
plugins {
    id "eu.xenit.docker-alfresco" version "5.2.0" // See https://plugins.gradle.org/plugin/eu.xenit.docker-alfresco for the latest version
}
```

## Configuration

### Installing Alfresco extensions

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

### Build Alfresco Docker image

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

#### Lean image

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

### Publishing wars with extensions

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

### Adding additional WARs to the automatically generated Dockerfile

You can modify the automatically generated `Dockerfile`. The plugin will configure this task to add Alfresco and Share with their extensions,
but you can make additional modifications to it if necessary.
The `createDockerFile` task is enhanced with the [`DockerfileWithWarsExtension`](../src/main/java/eu/xenit/gradle/docker/alfresco/tasks/extension/DockerfileWithWarsExtension.java),
which provides extra functions to [the base task provided by `eu.xenit.docker`](./02-plugin-docker.md#tasks).

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

## Tasks

The `eu.xenit.docker-alfresco` plugin makes use of some custom Gradle tasks and task types to install Alfresco extensions.
You can use these task types to build your own pipeline to modify WAR files, but this should not be necessary in almost all usecases.

> Please note: The descriptions below are the *conceptual* descriptions of the tasks, which is only valid when the tasks are used together as done by default by the plugin.
> For the more complex technical descriptions, please see the collapsible section below.

The Gradle tasks created by this plugin are executed in this order. (Ordering might not be entirely accurate, because there is no strict dependency ordering between all tasks.)

 * `stripAlfrescoWar` & `stripShareWar`: [`StripAlfrescoWarTask`](../src/main/java/eu/xenit/gradle/docker/alfresco/tasks/StripAlfrescoWarTask.java) Downloads Alfresco/Share war and trims them down to the minimum needed to apply AMPs
 * `applyAlfrescoAmp` & `applyShareAmp`: [`InstallAmpsInWarTask`](../src/main/java/eu/xenit/gradle/docker/alfresco/tasks/InstallAmpsInWarTask.java) Uses [Alfresco MMT](https://docs.alfresco.com/5.2/concepts/dev-extensions-modules-management-tool.html) to install AMPs in Alfresco/Share
 * `applyAlfrescoSM` & `applyShareSM`: [`InjectFilesInWarTask`](../src/main/java/eu/xenit/gradle/docker/alfresco/tasks/InjectFilesInWarTask.java) Installs [Simple Modules](https://docs.alfresco.com/5.2/concepts/dev-extensions-packaging-techniques-jar-files.html) on the Alfresco/Share classpath
 * `applyAlfrescoDE`: [`InjectFilesInWarTask`](../src/main/java/eu/xenit/gradle/docker/alfresco/tasks/InjectFilesInWarTask.java) Installs [Dynamic extensions](https://github.com/xenit-eu/dynamic-extensions-for-alfresco) in Alfresco
 * `prefixAlfrescoLog4j` & `prefixShareLog4j`: [`PrefixLog4JWarTask`](../src/main/java/eu/xenit/gradle/docker/alfresco/tasks/PrefixLog4JWarTask.java) Configures a prefix for the application's `log4j.properties` file
 * `alfrescoWar` & `shareWar`: [`MergeWarsTask`](../src/main/java/eu/xenit/gradle/docker/alfresco/tasks/MergeWarsTask.java) Delivers the finished Alfresco/Share war with all extensions installed.
 * `createDockerfile`: [`DockerfileWithWarsTask`](../src/main/java/eu/xenit/gradle/docker/alfresco/tasks/DockerfileWithWarsTask.java) Creates a Dockerfile with instructions to add Alfresco and/or Share wars. Adds labels to identify extensions that have been installed.
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

### Disabling log4j modifications

It is possible to disable the `prefixAlfrescoLog4j` or the `prefixShareLog4j` tasks using standard Gradle task management features, like [`enabled`](https://docs.gradle.org/current/dsl/org.gradle.api.Task.html#org.gradle.api.Task:enabled) or [`onlyIf`](https://docs.gradle.org/current/dsl/org.gradle.api.Task.html#org.gradle.api.Task:onlyIf(groovy.lang.Closure)).

```groovy
prefixAlfrescoLog4j {
  enabled = false
}

prefixShareLog4j {
  onlyIf { myHatColor == "purple" }
}
```

> **Warning:** This feature is only supported on Gradle 6.2 and later.
> It is not possible to disable these tasks on earlier Gradle versions.

## Example

An example for the usage of this plugin can be found in the [applyamps example](../src/integrationTest/examples/applyamps-example).

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
