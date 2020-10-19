package eu.xenit.gradle.docker.autotag;

import eu.xenit.gradle.docker.autotag.transformer.DockerSafeTagTransformer;
import eu.xenit.gradle.docker.autotag.transformer.LegacyTagTransformer;
import java.util.Set;

public class DockerAutotagExtension {

    public Set<String> legacyTags(Set<String> tags) {
        return new DockerSafeTagTransformer(LegacyTagTransformer.getInstance()).transform(tags);
    }
}
