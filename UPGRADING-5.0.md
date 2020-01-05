# Upgrade guide: 4.x -> 5.0

1. The [`com.avast.gradle.docker-compose`](https://github.com/avast/gradle-docker-compose-plugin) plugin is no longer automatically applied.
   You have to manually apply it when you need `composeUp` and `composeDown` tasks.
   
   <table>
    <tr>
    <th>Old</th>
    <th>New</th>
    </tr>
    <tr>
    <td>
    
    ```groovy
    plugins {
        id "eu.xenit.docker-alfresco" version "4.1.2"
    }
    ```
   
    </td>
    <td>
    
    ```groovy
   plugins {
       id "eu.xenit.docker-alfresco" version "5.0.0"
       id "com.avast.gradle.docker-compose" version "0.10.7"
   }
    ```
   
    </td>
    </tr>
    <tr>
    <td>
    
    ```groovy
    plugins {
        id "eu.xenit.docker" version "4.1.2"
    }
    ```
   
    </td>
    <td>
    
    ```groovy
   plugins {
       id "eu.xenit.docker" version "5.0.0"
       id "com.avast.gradle.docker-compose" version "0.10.7"
   }
    ```
   
    </td>
    </tr>
   </table>

2. Configuration and tasks now use Gradle [`Property<>`](https://docs.gradle.org/current/javadoc/org/gradle/api/provider/Property.html).
   This usually does not affect your `build.gradle`, but if you explicitly call getters instead of accessing as a property, keep in mind that the return type has changed.
   Because `Property<>` is now used, setters have been removed. Instead of using `setFoo(bla)`, you should now use `getFoo().set(bla)`. 

3. Automatic tagging has been disabled by default.
   You can re-enable automatic tagging based on branch name and build number by using:
   ```groovy
   dockerAlfresco {
       dockerBuild {
           automaticTags = true
       }
   }
   ```

4. `resolveAlfrescoWar` and `resolveShareWar` tasks have been renamed to `stripAlfrescoWar` and `stripShareWar`.
   
5. [`gradle-docker-plugin`](https://bmuschko.github.io/gradle-docker-plugin/#change_log) has been upgraded from 4.6.2 to 6.1.1.
    This should not affect basic usage, but may break more advanced usecases. See the breaking changes in the changelog there.

6. The `pushTag*` tasks that allow to push individual tags have been removed. Use `pushDockerImage` to push all tags of the docker image.
