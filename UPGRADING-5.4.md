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