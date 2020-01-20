package eu.xenit.gradle.docker.compose;

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

public class ReplayableComposeConventionImpl implements DockerComposeConvention {

    private final DockerComposeConvention convention;

    private final List<ReplayableChange> changes = new ArrayList<>();

    @FunctionalInterface
    private interface ReplayableChange {

        public void replayInto(DockerComposeConvention convention);
    }

    public ReplayableComposeConventionImpl(DockerComposeConvention convention) {
        this.convention = convention;
    }

    @Override
    public void fromBuildImage(String environmentVariable,
            TaskProvider<? extends DockerBuildImage> buildImageTaskProvider) {
        convention.fromBuildImage(environmentVariable, buildImageTaskProvider);
        changes.add(c -> c.fromBuildImage(environmentVariable, buildImageTaskProvider));
    }

    @Override
    public void fromBuildImage(String environmentVariable, DockerBuildImage buildImage) {
        convention.fromBuildImage(environmentVariable, buildImage);
        changes.add(c -> c.fromBuildImage(environmentVariable, buildImage));
    }

    @Override
    public void fromBuildImage(TaskProvider<? extends DockerBuildImage> buildImageTaskProvider) {
        convention.fromBuildImage(buildImageTaskProvider);
        changes.add(c -> c.fromBuildImage(buildImageTaskProvider));
    }

    @Override
    public void fromBuildImage(DockerBuildImage buildImage) {
        convention.fromBuildImage(buildImage);
        changes.add(c -> c.fromBuildImage(buildImage));
    }

    @Override
    public void fromProject(Project project) {
        convention.fromProject(project);
        changes.add(c -> c.fromProject(project));
    }

    @Override
    public void fromProject(String projectName) {
        convention.fromProject(projectName);
        changes.add(c -> c.fromProject(projectName));
    }

    public void replayChangesInto(DockerComposeConvention convention) {
        changes.forEach(replayableChange -> replayableChange.replayInto(convention));
    }
}
