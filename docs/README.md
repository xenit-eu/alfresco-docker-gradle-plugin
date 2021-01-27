# (Alfresco) Docker Gradle Plugins

A set of Gradle plugins to build Docker images and run Docker containers.

## Documentation

If this is the first time that you are going to use the Docker Gradle Plugins, [follow the setup guide to get started](./01-setup.md).

### Building a Docker image

To build a Docker image, you will use the core [`eu.xenit.docker`](./02-plugin-docker.md) plugin.

When using this plugin, some extension modules that add additional functionality are also activated:

* [`eu.xenit.docker.autotag`](./02-plugin-autotag.md): Add or rewrite tags to Docker images based on git branch, environment, ...
* [`eu.xenit.docker.label`](./02-plugin-label.md): Add labels to Docker images based on git commit, environment, ...

To build Alfresco and/or Share Docker images with AMPs, Dynamic Extensions and Simple Modules, you can use the  [`eu.xenit.docker-alfresco`](./02-plugin-docker-alfresco.md) plugin, which is built on top of the `eu.xenit.docker` plugin.

### Running Docker containers

To run Docker containers, you can specify them in [a `docker-compose.yml` file](https://docs.docker.com/compose/compose-file/).

The [`eu.xenit.docker-compose`](./02-plugin-docker-compose.md) plugin allows you to run `docker-compose` commands from Gradle. It can make use of Docker images built in the same or in different projects.

To automatically inject references to all Docker images built across all projects, you can use the [`eu.xenit.docker-compose.auto](./02-plugin-docker-compose-auto.md) plugin.
