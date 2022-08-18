package eu.xenit.gradle.testrunner.versions;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GradleVersionSeries {
    private final String series;
    private final Set<GradleVersionSpec> versionSpecs;

    public GradleVersionSeries(String series, Set<GradleVersionSpec> versionSpecs) {
        this.series = series;
        this.versionSpecs = versionSpecs;
    }

    public static GradleVersionSeries extract(String series, Collection<GradleVersionSpec> versionSpecs) {
        Set<GradleVersionSpec> extractedVersions = versionSpecs.stream()
                .filter(spec -> spec.getVersionSeries().contains(series))
                .collect(Collectors.toSet());
        return new GradleVersionSeries(series, extractedVersions);
    }

    public int getSeriesPriority() {
        if(series.isEmpty()) {
            return 0;
        }
        return series.split("\\.").length;
    }

    public GradleVersionSpec getLowestVersion() {
        return versionSpecs.stream()
                .min(GradleVersionSpec.BY_VERSION_NUMBER)
                .orElse(null);
    }

    public GradleVersionSpec getHighestVersion() {
        return versionSpecs.stream()
                .max(GradleVersionSpec.BY_VERSION_NUMBER)
                .orElse(null);
    }

    public GradleVersionSeries filter(Predicate<GradleVersionSpec> predicate) {
        return new GradleVersionSeries(series, versionSpecs.stream().filter(predicate).collect(Collectors.toSet()));
    }

    public boolean isEmpty() {
        return versionSpecs.isEmpty();
    }

}
