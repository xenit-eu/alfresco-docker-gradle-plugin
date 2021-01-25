# Plugin `eu.xenit.docker.label`: Automatically label docker images

This plugin configures [the `eu.xenit.docker` plugin](./02-plugin-docker.md), and is automatically applied by it.

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

## Label contributors

 * **git**: Adds labels derived from git: current branch, origin repository, latest commit hash and author.
   This label contributor is enabled by default and can be disabled by calling `fromGit(false)`
