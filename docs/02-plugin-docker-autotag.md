# Plugin `eu.xenit.docker.autotag`: Add and rewrite Docker image tags

This plugin configures [the `eu.xenit.docker` plugin](./02-plugin-docker.md), and is automatically applied by it.

This plugin adds functions to the `dockerBuild` configuration that can be used to add and change Docker image tags.

## `autotag.legacyTags(List<String>)`

```groovy
dockerBuild {
    tags = autotag.legacyTags(["some", "useful", "tags"])
}
```

On Jenkins, the branch is set with the environment variable `BRANCH_NAME`. When it is set, all tags will be
prepended with `$BRANCH_NAME-`, otherwise with `local`. An extra tag, `$BRANCH_NAME` or `local`, will be added too.
When the branch is `master`, the branch name is not prepended and a tag `latest` is added.

If the environment variable `$BUILD_NUMBER` is set, an extra tag is added: `build-yyyyMMddHHmm-$BUILD_NUMBER`, also prepended with the branch name.
