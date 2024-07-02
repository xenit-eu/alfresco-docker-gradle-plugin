# Upgrade guide: 5.4 -> 5.5

This version uses docker compose v2 by default. If you still want to keeping using docker-compose v1 instead, put:

```groovy
dockerCompose {
     useDockerComposeV2 = false
}
```
   