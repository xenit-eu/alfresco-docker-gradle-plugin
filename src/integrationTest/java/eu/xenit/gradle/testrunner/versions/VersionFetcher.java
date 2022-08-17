package eu.xenit.gradle.testrunner.versions;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.gradle.util.GradleVersion;

public class VersionFetcher {
    private static List<GradleVersionSpec> fetchAllVersions() {
        ObjectMapper mapper = new ObjectMapper();

        try {
            CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, GradleVersionSpec.class);
            return mapper.readerFor(type).readValue(new URL("https://services.gradle.org/versions/all"));
        } catch (MalformedURLException | JsonMappingException | JsonParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean isRelease(GradleVersionSpec versionSpec) {
        GradleVersion gradleVersion = GradleVersion.version(versionSpec.getVersion());
        return !versionSpec.isSnapshot() && versionSpec.getRcFor().isEmpty() && Objects.equals(gradleVersion.getBaseVersion(), gradleVersion);
    }

    public static boolean isBroken(GradleVersionSpec versionSpec) {
        return versionSpec.isBroken();
    }

    public static Predicate<GradleVersionSpec> greaterThan(String version) {
        return versionSpec -> GradleVersion.version(versionSpec.getVersion()).compareTo(GradleVersion.version(version)) > 0;
    }

    public static boolean firstPointRelease(GradleVersionSpec versionSpec) {
        return GradleVersion.version(versionSpec.getVersion()).getBaseVersion().getVersion().endsWith(".0");
    }

    public static Stream<GradleVersionSpec> fetchVersions() {
        return fetchAllVersions()
                .stream()
                .filter(((Predicate<? super GradleVersionSpec>) GradleVersionSpec::isBroken).negate());
    }

}
