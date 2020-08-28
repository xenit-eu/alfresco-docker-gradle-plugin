package eu.xenit.gradle.docker.core;

import java.util.ArrayList;
import java.util.List;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;

public abstract class DockerExtension implements ExtensionAware {

    public abstract RegularFileProperty getDockerFile();

    public abstract ListProperty<String> getRepositories();

    public abstract ListProperty<String> getTags();

    Provider<List<String>> getImages() {
        return getRepositories().flatMap(repositories -> getTags().map(tags -> {
            List<String> images = new ArrayList<>(repositories.size() * tags.size());
            for (String repository : repositories) {
                for (String tag : tags) {
                    images.add(repository + ":" + tag);
                }
            }
            return images;
        }));
    }
}
