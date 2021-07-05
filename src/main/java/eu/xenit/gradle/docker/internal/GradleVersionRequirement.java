package eu.xenit.gradle.docker.internal;

import java.util.function.Supplier;
import org.gradle.util.GradleVersion;

public final class GradleVersionRequirement {

    private GradleVersionRequirement() {
    }

    static class UnsupportedGradleVersion extends UnsupportedOperationException {

        private UnsupportedGradleVersion(GradleVersion requiredVersion, GradleVersion currentVersion, String feature) {
            super(requiredVersion.toString() + " or later is required" + (feature == null ? ""
                    : " to " + feature) + ". You are currently using " + currentVersion.toString());
        }
    }

    public static void atLeast(GradleVersion gradleVersion, String feature) {
        if (GradleVersion.current().compareTo(gradleVersion) < 0) {
            throw new UnsupportedGradleVersion(gradleVersion, GradleVersion.current(), feature);
        }
    }

    public static void atLeast(String gradleVersion, String feature) {
        atLeast(GradleVersion.version(gradleVersion), feature);
    }

    public static <T> T atLeast(GradleVersion gradleVersion, String feature, Supplier<T> callable) {
        atLeast(gradleVersion, feature);
        return callable.get();
    }

    public static <T> T atLeast(String gradleVersion, String feature, Supplier<T> callable) {
        atLeast(gradleVersion, feature);
        return callable.get();
    }
}
