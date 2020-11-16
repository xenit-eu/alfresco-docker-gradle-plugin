package eu.xenit.gradle.docker.autotag;

import eu.xenit.gradle.docker.autotag.transformer.DockerSafeTagTransformer;
import eu.xenit.gradle.docker.autotag.transformer.LegacyTagTransformer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DockerAutotagExtension {

    public Set<String> legacyTags() {
        return legacyTags(Collections.emptySet());
    }

    public Set<String> legacyTags(List<String> tags) {
        return legacyTags(new HashSet<>(tags));
    }

    public Set<String> legacyTags(Set<String> tags) {
        return new DockerSafeTagTransformer(LegacyTagTransformer.getInstance()).transform(tags);
    }
}
