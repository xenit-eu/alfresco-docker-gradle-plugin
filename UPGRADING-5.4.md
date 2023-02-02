# Upgrade guide: 5.3 -> 5.4

1. In `dockerCompose` blocks you can no longer use the `+=` operator on `useComposeFiles`.

    <table>
    <tr>
    <th>Old</th>
    <th>New</th>
    </tr>
    <tr>
    <td>

    ```groovy
    dockerCompose {
           useComposeFiles += ["docker-compose-db.yml"]
    }
    ```
   
    </td>
    <td>
    
    ```groovy
    dockerCompose {
           useComposeFiles.add("docker-compose-db.yml")
    }
    ```
   
    </td>
    </tr>

</table>

2. The 5.4 release drops a lot of older Gradle versions. Make sure you upgrade your Gradle wrapper to at least version 6.2:
```bash
./gradlew wrapper --gradle-version=7.6
```