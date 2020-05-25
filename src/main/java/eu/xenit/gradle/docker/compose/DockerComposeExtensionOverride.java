package eu.xenit.gradle.docker.compose;

import com.avast.gradle.dockercompose.ComposeExtension;
import com.avast.gradle.dockercompose.ComposeSettings;
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage;
import java.util.ArrayList;
import java.util.HashMap;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

class DockerComposeExtensionOverride extends ComposeExtension implements DockerComposeConvention {

    private final ReplayableComposeConventionImpl dockerComposeConvention;

    public DockerComposeExtensionOverride(Project project) {
        super(project);
        dockerComposeConvention = new ReplayableComposeConventionImpl(new DockerComposeConventionImpl(this));
    }

    @Override
    public ComposeSettings createNested(String name) {
        DockerComposeSettingsOverride r = new DockerComposeSettingsOverride(getProject(), name, getNestedName());
        DockerComposeConvention convention = new DockerComposeConventionImpl(r);
        dockerComposeConvention.replayChangesInto(convention);
        r.setDockerComposeConvention(convention);

        // Keep in sync with {@link ComposeSettings#createNested(String)}
        r.setBuildBeforeUp(getBuildBeforeUp());
        r.setBuildBeforePull(getBuildBeforePull());

        r.setWaitForTcpPorts(getWaitForTcpPorts());
        r.setTcpPortsToIgnoreWhenWaiting(new ArrayList<>(this.getTcpPortsToIgnoreWhenWaiting()));
        r.setWaitAfterTcpProbeFailure(this.getWaitAfterTcpProbeFailure());
        r.setWaitForTcpPortsTimeout(this.getWaitForTcpPortsTimeout());
        r.setWaitForTcpPortsDisconnectionProbeTimeout(this.getWaitForTcpPortsDisconnectionProbeTimeout());
        r.setWaitAfterHealthyStateProbeFailure(this.getWaitAfterHealthyStateProbeFailure());
        r.setWaitForHealthyStateTimeout(this.getWaitForHealthyStateTimeout());
        r.setCheckContainersRunning(this.getCheckContainersRunning());

        r.setCaptureContainersOutput(this.isCaptureContainersOutput());

        r.setIncludeDependencies(this.isIncludeDependencies());

        r.setRemoveOrphans(this.isRemoveOrphans());
        r.setForceRecreate(this.isForceRecreate());
        r.setNoRecreate(this.isNoRecreate());
        r.setBuildAdditionalArgs(new ArrayList<>(this.getBuildAdditionalArgs()));
        r.setPullAdditionalArgs(new ArrayList<>(this.getPullAdditionalArgs()));
        r.setUpAdditionalArgs(new ArrayList<>(this.getUpAdditionalArgs()));
        r.setDownAdditionalArgs(new ArrayList<>(this.getDownAdditionalArgs()));

        r.setProjectNamePrefix(this.getProjectNamePrefix());

        r.setStopContainers(this.isStopContainers());
        r.setRemoveContainers(this.isRemoveContainers());
        r.setRemoveImages(this.getRemoveImages());
        r.setRemoveVolumes(this.isRemoveVolumes());

        r.setIgnorePullFailure(this.isIgnorePullFailure());
        r.setIgnorePushFailure(this.isIgnorePushFailure());

        r.setExecutable(this.getExecutable());
        r.setEnvironment(new HashMap<>(this.getEnvironment()));

        r.setDockerExecutable(this.getDockerExecutable());

        r.setDockerComposeWorkingDirectory(this.getDockerComposeWorkingDirectory());
        r.setDockerComposeStopTimeout(this.getDockerComposeStopTimeout());
        return r;
    }

    @Override
    public void fromBuildImage(String environmentVariable, TaskProvider<? extends DockerBuildImage> buildImageTaskProvider) {
        dockerComposeConvention.fromBuildImage(environmentVariable, buildImageTaskProvider);
    }

    @Override
    public void fromBuildImage(String environmentVariable, DockerBuildImage buildImage) {
        dockerComposeConvention.fromBuildImage(environmentVariable, buildImage);
    }

    @Override
    public void fromBuildImage(TaskProvider<? extends DockerBuildImage> buildImageTaskProvider) {
        dockerComposeConvention.fromBuildImage(buildImageTaskProvider);
    }

    @Override
    public void fromBuildImage(DockerBuildImage buildImage) {
        dockerComposeConvention.fromBuildImage(buildImage);

    }

    @Override
    public void fromProject(Project project) {
        dockerComposeConvention.fromProject(project);
    }

    @Override
    public void fromProject(String projectName) {
        dockerComposeConvention.fromProject(projectName);
    }

}
