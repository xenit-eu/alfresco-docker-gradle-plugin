package eu.xenit.gradle.docker.autotag.transformer;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.gradle.api.GradleException;

public class DockerSafeTagTransformer implements TagTransformer {

    private TagTransformer parent;

    private static final int DOCKER_TAG_LENGTH_CONSTRAINT = 128;
    private static final String DOCKER_TAG_LENGTH_CONSTRAINT_ERRORMSG =
            "Automatic tags will violate tag length constraint of " + DOCKER_TAG_LENGTH_CONSTRAINT
                    + ", due to usage of branch name in tag. Modify branch name or disable automatic tags.";

    public DockerSafeTagTransformer(TagTransformer parent) {
        this.parent = parent;
    }

    @Override
    public Set<String> transform(Set<String> tags) {
        return parent.transform(tags).stream()
                .map(DockerSafeTagTransformer::cleanForDockerTag)
                .peek(DockerSafeTagTransformer::validateDockerTagLength)
                .collect(Collectors.toSet());
    }

    /* Remove illegal characters from tags through encoding*/
    private static String cleanForDockerTag(String tag) {
        Pattern illegalCharacters = Pattern.compile("[/\\\\:<>\"?*|]");
        Matcher matcher = illegalCharacters.matcher(tag);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, "_");
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static void validateDockerTagLength(String tag) {
        if (tag.length() > DOCKER_TAG_LENGTH_CONSTRAINT) {
            throw new GradleException(DOCKER_TAG_LENGTH_CONSTRAINT_ERRORMSG);
        }
    }
}
