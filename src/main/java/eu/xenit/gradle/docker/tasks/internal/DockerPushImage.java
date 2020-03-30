package eu.xenit.gradle.docker.tasks.internal;

import java.util.Set;
import org.gradle.api.Action;
import org.gradle.api.Task;

public class DockerPushImage extends com.bmuschko.gradle.docker.tasks.image.DockerPushImage {

    public DockerPushImage() {
        prependParallelSafeAction(new CheckDockerTagsAction());
    }

    private final class CheckDockerTagsAction implements Action<Task> {

        @Override
        public void execute(Task task) {
            boolean isEmpty = DockerPushImage.this.getImages()
                    .map(Set::isEmpty)
                    .getOrElse(true);
            if (isEmpty) {
                throw new NoImagesConfiguredException();
            }
        }
    }

}
