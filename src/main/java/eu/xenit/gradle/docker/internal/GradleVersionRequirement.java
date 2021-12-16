package eu.xenit.gradle.docker.internal;

import java.util.function.Supplier;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.util.GradleVersion;

public final class GradleVersionRequirement {
    private static final Logger LOGGER  = Logging.getLogger(GradleVersionRequirement.class);

    private GradleVersionRequirement() {
    }

    static class UnsupportedGradleVersion extends UnsupportedOperationException {
        private static String formatRequirement(GradleVersion requiredVersion, GradleVersion currentVersion, String feature) {
            return requiredVersion.toString() + " or later is required" + (feature == null ? ""
                    : " to " + feature) + ". You are currently using " + currentVersion.toString();
        }

        private UnsupportedGradleVersion(GradleVersion requiredVersion, GradleVersion currentVersion, String feature) {
            super(formatRequirement(requiredVersion, currentVersion, feature));
        }
    }

    public static <T> T ifAtLeast(String gradleVersion, Supplier<T> ifVersion, Supplier<T> ifNotVersion) {
        return ifAtLeast(GradleVersion.version(gradleVersion), ifVersion, ifNotVersion);
    }

    public static <T> T ifAtLeast(GradleVersion gradleVersion, Supplier<T> ifVersion, Supplier<T> ifNotVersion) {
        if(isAtLeast(gradleVersion, null, true)) {
            return ifVersion.get();
        } else {
            return ifNotVersion.get();
        }

    }

    public static boolean isAtLeast(GradleVersion gradleVersion, String feature) {
        return isAtLeast(gradleVersion, feature, false);
    }

    public static boolean isAtLeast(GradleVersion gradleVersion, String feature, boolean silent) {
        if (GradleVersion.current().compareTo(gradleVersion) < 0) {
            if(!silent) {
                LOGGER.warn("{}", UnsupportedGradleVersion.formatRequirement(gradleVersion, GradleVersion.current(), feature));
            }
            return false;
        }
        return true;
    }

    public static boolean isAtLeast(String gradleVersion, String feature) {
        return isAtLeast(GradleVersion.version(gradleVersion), feature);
    }

    public static void atLeast(GradleVersion gradleVersion, String feature) {
        if (!isAtLeast(gradleVersion, feature, true)) {
            throw new UnsupportedGradleVersion(gradleVersion, GradleVersion.current(), feature);
        }
    }

    public static void atLeast(String gradleVersion, String feature) {
        atLeast(GradleVersion.version(gradleVersion), feature);
    }

    public static <T> T atLeast(String gradleVersion, String feature, Supplier<T> callable) {
        atLeast(gradleVersion, feature);
        return callable.get();
    }
}
