package eu.xenit.gradle.docker.label;

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage;
import eu.xenit.gradle.docker.core.DockerExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskCollection;

public class DockerLabelPlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = "eu.xenit.docker.label";

    @Override
    public void apply(Project project) {
        DockerExtension dockerExtension = project.getExtensions().getByType(DockerExtension.class);
        final TaskCollection<DockerBuildImage> dockerBuildImages = project.getTasks().withType(DockerBuildImage.class);
        dockerExtension.getExtensions().create("label", DockerLabelExtension.class, dockerBuildImages);
    }
}
