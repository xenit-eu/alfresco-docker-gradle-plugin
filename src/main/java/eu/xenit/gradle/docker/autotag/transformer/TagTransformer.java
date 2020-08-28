package eu.xenit.gradle.docker.autotag.transformer;

import java.util.List;
import java.util.Set;

public interface TagTransformer {

    Set<String> transform(Set<String> tags);
}
