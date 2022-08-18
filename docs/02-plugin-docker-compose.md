# Plugin `eu.xenit.docker-compose`: Inject built docker images into docker-compose

This plugin extends the [`com.avast.gradle.docker-compose`](https://github.com/avast/gradle-docker-compose-plugin) plugin
to use docker images built with the [`DockerBuildImage`](https://bmuschko.github.io/gradle-docker-plugin/api/com/bmuschko/gradle/docker/tasks/image/DockerBuildImage.html) tasks in your `docker-compose.yml` file

```groovy
plugins {
    id "eu.xenit.docker-compose" version "5.3.2" // See https://plugins.gradle.org/plugin/eu.xenit.docker-compose for the latest version
}
```

## Configuration

This plugin extends the [`dockerCompose` configuration block](https://github.com/avast/gradle-docker-compose-plugin#usage) with two functions:

* `fromBuildImage([environmentVariable,] task)`: Add a dependency on the specified `DockerBuildImage` task, and expose
  the image id as an environment variable.
    * `fromBuildImage(String environmentVariable, DockerBuildImage task)`
      , `fromBuildImage(String environmentVariable, TaskProvider<DockerBuildImage> taskProvider)`: Exposes the id of the
      docker image built by the task as the specified environment variable.
    * `fromBuildImage(DockerBuildImage task)`, `fromBuildImage(TaskProvider<DockerBuildImage> taskProvider)`: Exposes
      the id of the docker image built by the task as an environment variable based on the project and task name.
* `fromProject([environmentVariable,] project)`: Use `fromBuildImage()` for each `DockerBuildImage` task in the project.
    * `fromProject(String environmentVariable, Project project)`
      , `fromProject(String environmentVariable, String project)`: Every docker image built by `DockerBuildImage` in the
      project is exposed in an environment variable that starts with the value of `environmentVariable`, `_` and the
      underscored and capitalized task name. When the `eu.xenit.docker` or the `eu.xenit.docker-alfresco` plugins are
      applied, the `buildDockerImage` task itself is exposed as an environment variable with the value
      of `environmentVariable`.
    * `fromProject(Project project)`, `fromProject(String project)`: The environment variable as which docker images are
      exposed is based on the project name and task name, as described
      in [Environment variable naming](#environment-variable-naming).

```groovy
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
>     id "eu.xenit.docker-compose" version "5.3.2" apply false // See https://plugins.gradle.org/plugin/eu.xenit.docker-compose for the latest version
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

## Environment variable naming

To generate an environment name for a BuildDockerImage task, we concatenate project name, `_`, task name, `_DOCKER_IMAGE`.
Non-alphanumeric characters are converted to underscores, and camelCased names are converted to uppercase SNAKE_CASE names.

e.g. task `:projectA:taskAbc` -> `PROJECT_A_TASK_ABC_DOCKER_IMAGE`

When a project uses the `eu.xenit.docker` or the `eu.xenit.docker-alfresco` plugins, the `buildDockerImage` task is also exposed as a shorter environment variable: project name, `_DOCKER_IMAGE`

e.g. project `:projectA` -> `PROJECT_A_DOCKER_IMAGE` and `PROJECT_A_BUILD_DOCKER_IMAGE_DOCKER_IMAGE`
