package eu.xenit.gradle.testrunner.versions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Comparator;
import org.gradle.util.GradleVersion;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GradleVersionSpec {
    private String version;
    private boolean snapshot;
    private boolean broken;
    private String rcFor;

    public String getVersion() {
        return version;
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
}
