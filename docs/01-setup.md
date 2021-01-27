# Setup

## Install docker & docker-compose

<details>
<summary>Installing on Linux</summary>

1. You can usually find a package in your package manager to install docker and docker-compose.
   * On Debian and Ubuntu: `apt-get install docker-ce docker-compose`
2. Add your user to the `docker` group to access Docker without being root.
    `adduser $USER docker`
3. Logout and log in again in your desktop environment to activate your new group membership.

</details>

<details>
<summary>Installing on Windows/Mac</summary>

1. Install Docker Desktop: https://www.docker.com/products/docker-desktop
2. Make sure that Docker is configured to start when you login.

</details>

Access to the Docker daemon can be configured to listen in various ways.
The default settings work with the Gradle plugin, but may not work with other tools, so you might have to change it.
In any case, make sure that the Gradle configuration below matches the configuration of the Docker daemon.


## Configure Gradle

Configuration of the Docker Gradle Plugins is done in `gradle.properties`.

On Linux, you can create or find this file in `~/.gradle/gradle.properties`.

On Windows, you can find a hidden `.gradle` folder in your user directory or create it if it does not exist. You can create or edit `gradle.properties` inside this folder.

### Connection to the Docker daemon

The Gradle plugin supports connecting to the Docker daemon in various ways:

* unix socket (default on Linux: `unix:///var/run/docker.sock`)
* named pipes (default on Windows: `npipe:////./pipe/docker_engine`)
* plain TCP/http (automatic fallback on Windows: `tcp://localhost:2375`)
* TLS/https (`https://localhost:2376`)

If the Docker daemon is listening on something other than the defaults, it must be configured either with the environment variable `$DOCKER_HOST` or with `eu.xenit.docker.url` in `gradle.properties`.

When using a TLS connection to the Docker daemon, either the environment variable `$DOCKER_CERT_PATH` must be configured, or `eu.xenit.docker.certPath` should be set in `gradle.properties`.


### Expose containers started with docker-compose

Projects that use the `$DOCKER_IP` environment variable in their docker-compose files leave it up to the user to specify on which IP address their ports are exposed.

The value of `$DOCKER_IP` defaults to `127.0.0.1`, but this can be configured with the `eu.xenit.docker.expose.ip` property in `gradle.properties`. A common change is to set it to `0.0.0.0` so exposed services are publicly available (usually only to members on the same network, but this depends on network and firewall settings, so use with care)

### Authenticating to a Docker registry

On Linux and Windows, the Docker Gradle Plugins can automatically make use of the authentication that is done by `docker login`.

It is possible to configure authentication for one Docker registry in `gradle.properties` by configuring `eu.xenit.docker.registry.url`, `eu.xenit.docker.registry.username` and `eu.xenit.docker.registry.password`.
These will preferentially be used over credentials from `docker login` for that registry.

<details>
<summary>Example</summary>

```properties
eu.xenit.docker.registry.url=https://index.docker.io/v1/
eu.xenit.docker.registry.username=
eu.xenit.docker.registry.password=
```

</details>
