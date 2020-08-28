package eu.xenit.gradle.docker.autotag.transformer;

import eu.xenit.gradle.docker.internal.JenkinsUtil;
import java.util.Set;
import java.util.stream.Collectors;

public class LegacyTagTransformer implements TagTransformer {

    private static final LegacyTagTransformer INSTANCE = new LegacyTagTransformer();

    public static LegacyTagTransformer getInstance() {
        return INSTANCE;
    }

    private LegacyTagTransformer() {

    }

    @Override
    public Set<String> transform(Set<String> tags) {
        String jenkinsBranch = JenkinsUtil.getBranch();
        boolean isMaster = "master".equals(jenkinsBranch);
        Set<String> newTags = tags.stream().map(tag -> {
            if (isMaster) {
                return tag;
            } else {
                return jenkinsBranch + "-" + tag;
            }
        }).collect(Collectors.toSet());

        if (JenkinsUtil.getBuildId() != null) {
            if (isMaster) {
                newTags.add("build-" + JenkinsUtil.getBuildId());
            } else {
                newTags.add(jenkinsBranch + "-build-" + JenkinsUtil.getBuildId());
            }
        }

        if (isMaster) {
            newTags.add("latest");
        } else {
            newTags.add(jenkinsBranch);
        }
        return newTags;
    }
}
