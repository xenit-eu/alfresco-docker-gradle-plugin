package eu.xenit.gradle.testrunner.versions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.gradle.util.GradleVersion;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GradleVersionSpec {
    public static final Comparator<GradleVersionSpec> BY_VERSION_NUMBER = Comparator.comparing(versionSpec -> GradleVersion.version(versionSpec.getVersion()));

    private String version;
    private boolean snapshot;
    private boolean broken;
    private String rcFor;

    public String getVersion() {
        return version;
    }

    public Set<String> getVersionSeries() {
        String[] parts = GradleVersion.version(version).getBaseVersion().getVersion().split("\\.");

        Set<String> series = new HashSet<>();

        for (int i = 0; i < parts.length; i++) {
            String serie = "";
            for (int j = 0; j <= i; j++) {
                serie+=parts[j]+".";
            }
            series.add(serie.substring(0, Math.max(serie.length()-1, 0)));
        }

        return Collections.unmodifiableSet(series);
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isSnapshot() {
        return snapshot;
    }

    public void setSnapshot(boolean snapshot) {
        this.snapshot = snapshot;
    }

    public boolean isBroken() {
        return broken;
    }

    public void setBroken(boolean broken) {
        this.broken = broken;
    }

    public String getRcFor() {
        return rcFor;
    }

    public void setRcFor(String rcFor) {
        this.rcFor = rcFor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GradleVersionSpec that = (GradleVersionSpec) o;
        return snapshot == that.snapshot && broken == that.broken && version.equals(that.version) && rcFor.equals(
                that.rcFor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, snapshot, broken, rcFor);
    }
}
