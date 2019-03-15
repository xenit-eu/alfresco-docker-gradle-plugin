# Alfresco Docker Gradle Plugins

[![Build Status](https://travis-ci.org/xenit-eu/alfresco-docker-gradle-plugin.svg?branch=master)](https://travis-ci.org/xenit-eu/alfresco-docker-gradle-plugin)

This projects contains some gradle plugins that are used within Xenit Projects.

Currently there are 3 plugins:

- docker-alfresco: Makes it possible to build Alfresco and Share wars with amps installed. A docker
image can be built with the alfresco installed. It is also possible to include Alfresco Dynamic Extensions, and Alfresco
 Simple Modules.
- docker: Build a docker image, starting from a Dockerfile.
- docker-config: Helper plugin that is used to configure the docker environment.
This plugin is automatically applied when using the 2 plugins below.

## Setup

First, install Docker on you local machine. It may listen on either a unix socket of a tcp socket. On Windows, you should
do nothing extra for the tcp socket. On Ubuntu, you can follow this guide: https://docs.docker.com/engine/admin/#ubuntu 
(only when using the tcp socket). When using the unix socket, you need to add your user to the docker group and do a logout and a login.

Install a recent version of Gradle. For your projects, it is a good idea to use the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html).
The plugins packed in this project expect some configuration to be done at the level of gradle.

You can configure the docker plugin in the gradle.properties file in your gradle home folder:
```properties
# Docker socket (UNIX; default)
eu.xenit.docker.url=unix:///var/run/docker.sock
# Your docker socket (TCP)
eu.xenit.docker.url=tcp://localhost:2375
# Path to your docker-certs, if you use ssl, otherwise, comment out
# Make sure to respect the naming convention of the files inside (ca.pem, cert.pem, key.pem)
eu.xenit.docker.certPath=/home/?/docker-certs
# The ip address that exposed ports should be bound to. (Sets the value of the DOCKER_IP environment variable)
eu.xenit.docker.expose.ip=127.0.0.1
# Registry credentials if you are using a private registry
eu.xenit.docker.registry.url=https://hub.xenit.eu/v2
eu.xenit.docker.registry.username=
eu.xenit.docker.registry.password=
```

## Usage

### Alfresco docker

The following instructions apply to the build.gradle file in your project.

Activate the plugin:

```groovy
plugins {
    id "eu.xenit.docker-alfresco" version "4.0.0" // Have a look at the releases to find the latest one
}
```

Then define what base wars you want to use, and what amps to install:

```groovy
dependencies {
    baseAlfrescoWar "org.alfresco:content-services-community:6.0.a@war"
    alfrescoAmp "de.fmaul:javascript-console-repo:0.6@amp"
    alfrescoDE(group: 'eu.xenit', name: 'move2alf-backend-de-50', version: '2.9.2-24'){
            transitive=false
        }
    alfrescoSM("group:artifactId:version")
    baseShareWar(group: 'org.alfresco', name: 'share', version: '5.1.1.4', ext: 'war')
    shareAmp(group: 'org.sharextras', name: 'javascript-console-share', version: '0.6.0', ext: 'amp')
    shareSM("group:artifactId:version")
}
```

Configure the docker build:
```groovy
dockerAlfresco {
    // Base image used in the FROM of the docker build. Should be a compatible image.
    baseImage = "alfresco/alfresco-content-repository-community:6.0.7-ga"

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

This configuration adds some tasks to your gradle build:

- resolveAlfrescoWar
- applyAlfrescoAmp
- applyAlfrescoDE
- applyAlfrescoSM
- resolveShareWar
- applyShareAmp
- applyShareSM
- buildDockerImage
- pushDockerImage

Example:
```bash
gradle buildDockerImage
```

When you check the labels of you docker image, you will notice that the base wars and the amps are listed.

The code for this example can be found [here](src/test/examples/applyamps-example)

### Build Docker Image

In your build.gradle, apply the plugin:
```groovy
plugins {
    id "eu.xenit.docker" version "4.0.0" // Have a look at the releases to find the latest one
}
```

The configuration for the Docker build:
```groovy
dockerFile {
    dockerFile = file('Dockerfile')
    dockerBuild {
        // Repository to publish on.
        repository = 'example-docker-plugin'
        tags = ['1','1.0','test'] //optional
        // On Jenkins, branches other than master will be appended with -branch.
        // Local build will be appended with -local
        // You can disable this behavior with
        // automaticTags = false
    }
}
```

Example:
```bash
gradle buildDockerImage
```

The code for this example can be found [here](src/test/examples/example-docker-plugin)

### Lean image

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

## Publishing wars with extensions

Instead of creating a docker image with Alfresco and Share embedded, it is also possible to publish
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

## Tagging behavior

On Jenkins, the branch is set with the environment variable `BRANCH_NAME`. When it is set, all manually tags will be
prepended with `branch-`, otherwise with `local`. Also an extra tag `branch`, or `local` will be added.
When the branch is `master`, the tags are not prepended and a tag `latest` is used.

When an environment variable `BUILD_NUMBER` is set, an extra tag is added: `build-yyyyMMddHHmm-buildnumber` is added.
When the branch is not master, this is also prepended.

This tagging behavior can be disabled by adding `automaticTags = false` do the dockerBuild configuration.

# Plugin development

## Creating a release

Every git tag is automatically published to the gradle plugins repository by Travis-CI.

This plugin follows SemVer and tags are managed with [Reckon](https://github.com/ajoberstar/reckon).

To create a release from a commit, use `./gradlew reckonTagPush -Preckon.scope=patch -Preckon.stage=final` to create a new patch release.

Tests are required to pass before a new release can be tagged.

