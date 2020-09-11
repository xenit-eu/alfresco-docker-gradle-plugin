# Upgrade guide: 5.0 -> 5.1

1. The `dockerFile` and `dockerAlfresco` configuration blocks have been deprecated and have been consolidated to one configuration block.
   
   <table>
    <tr>
    <th>Old</th>
    <th>New</th>
    </tr>
    <tr>
    <td>
    
    ```groovy
    dockerFile {
       dockerBuild {
           [...]
       }
    }
    ```
   
    </td>
    <td>
    
    ```groovy
    dockerBuild {
       [...]
    }
    ```
   
    </td>
    </tr>
    <tr>
    <td>
    
    ```groovy
    dockerAlfresco {
       baseImage = "..."
       leanImage = true
       dockerBuild {
           [...]
       }
    }
    ```
   
    </td>
    <td>
    
    ```groovy
    dockerBuild {
       [...]
       alfresco {
           baseImage = "..."
           leanImage = true
       }       
    }
    ```
   
    </td>
    </tr>
   </table>
   
2. `dockerBuild.repository` has been renamed to `dockerBuild.repositories` and supports tagging an image with multiple repositories at once.
   
   <table>
    <tr>
    <th>Old</th>
    <th>New</th>
    </tr>
    <tr>
    <td>
    
       ```groovy
       dockerFile {
           dockerBuild {
               repository = "some-repository/image-name"
               tags = ["some", "tags"]
           }
       }
       ```
       
    </td>
    <td>
        
        ```groovy
        dockerBuild {
            repositories = ["some-repository/image-name"]
            tags = ["some", "tags"]
        }
        ```
    
    </td>
    </tr>
   </table>

3. `pull`, `noCache` and `remove` properties on the `dockerBuild` extension are deprecated.
    They can be set directly on the `buildDockerImage` task.

   <table>
    <tr>
    <th>Old</th>
    <th>New</th>
    </tr>
    <tr>
    <td>
    
       ```groovy
       dockerFile {
           dockerBuild {
                pull = false
                noCache = true
                remove = false
           }
       }
       ```
       
    </td>
    <td>
        
        ```groovy
        buildDockerImage {
            pull = false
            noCache = true
            remove = false
        }
        ```
    
    </td>
    </tr>
   </table>


4. Automatic tagging been deprecated. You can use `autotag.legacyTags()` to keep using same tagging functionality.

   <table>
    <tr>
    <th>Old</th>
    <th>New</th>
    </tr>
    <tr>
    <td>
    
       ```groovy
       dockerAlfresco {
           dockerBuild {
               automaticTags = true
               tags = ["some", "tags"]
           }
       }
       ```
       
    </td>
    <td>
        
        ```groovy
        dockerBuild {
            tags = autotag.legacyTags(["some", "tags"])
        }
        ```
    
    </td>
    </tr>
   </table>

5. The `eu.xenit.docker` plugin now creates a `createDockerFile` task, so you don't have to create one manually anymore.
   As long as no `Dockerfile` is present in the project directory, it will automatically be used by `buildDockerImage`.

   <table>
    <tr>
    <th>Old</th>
    <th>New</th>
    </tr>
    <tr>
    <td>
    
       ```groovy
       task createDockerFile(type: DockerfileWithCopyTask) {
            [...]
       }
       ```
       
    </td>
    <td>
        
        ```groovy
        createDockerFile {
            [...]
        }
        ```
    
    </td>
    </tr>
   </table>


