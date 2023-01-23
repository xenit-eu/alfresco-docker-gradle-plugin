package eu.xenit.gradle.docker.compose;

import com.avast.gradle.dockercompose.ComposeExtension;
import com.avast.gradle.dockercompose.ComposeSettings;
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage;
import groovy.lang.MissingMethodException;
import java.util.ArrayList;
import java.util.HashMap;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

abstract class DockerComposeExtensionOverride extends ComposeExtension implements DockerComposeConvention {

    private final ReplayableComposeConventionImpl dockerComposeConvention;

    public DockerComposeExtensionOverride(Project project) {
        super(project);
        dockerComposeConvention = new ReplayableComposeConventionImpl(new DockerComposeConventionImpl(this));
    }

    @Override
    public Object methodMissing(String name, Object args) {
        switch (name) {
            case "fromProject":
            case "fromBuildImage":
                throw new MissingMethodException(name, getClass(), (Object[]) args);
            default:
                return super.methodMissing(name, args);
        }
    }

    @Override
    protected ComposeSettings cloneAsNested(String name) {
        Project project = getTasksConfigurator().getProject();
        DockerComposeSettingsOverride r = project.getObjects().newInstance(
                DockerComposeSettingsOverride.class, project, name, getNestedName());
        DockerComposeConvention convention = new DockerComposeConventionImpl(r);
        dockerComposeConvention.replayChangesInto(convention);
        r.setDockerComposeConvention(convention);

        // Keep in sync with {@link ComposeSettings#cloneAsNested(String)}

        r.getIncludeDependencies().set(getIncludeDependencies().get());

        r.getBuildAdditionalArgs().set(new ArrayList<String>(this.getBuildAdditionalArgs().get()));
        r.getPullAdditionalArgs().set(new ArrayList<String>(this.getPullAdditionalArgs().get()));
        r.getUpAdditionalArgs().set(new ArrayList<String>(this.getUpAdditionalArgs().get()));
        r.getDownAdditionalArgs().set(new ArrayList<String>(this.getDownAdditionalArgs().get()));
        r.getComposeAdditionalArgs().set(new ArrayList<String>(this.getComposeAdditionalArgs().get()));

        r.getBuildBeforeUp().set(this.getBuildBeforeUp().get());
        r.getBuildBeforePull().set(this.getBuildBeforePull().get());

        r.getRemoveOrphans().set(this.getRemoveOrphans().get());
        r.getForceRecreate().set(this.getForceRecreate().get());
        r.getNoRecreate().set(this.getNoRecreate().get());

        r.getStopContainers().set(getStopContainers().get());
        r.getRemoveContainers().set(getRemoveContainers().get());
        r.getRetainContainersOnStartupFailure().set(getRetainContainersOnStartupFailure().get());
        r.getRemoveImages().set(getRemoveImages().get());
        r.getRemoveVolumes().set(getRemoveVolumes().get());

        r.getIgnorePullFailure().set(getIgnorePullFailure().get());
        r.getIgnorePushFailure().set(getIgnorePushFailure().get());

        r.getWaitForTcpPorts().set(this.getWaitForTcpPorts().get());
        r.getTcpPortsToIgnoreWhenWaiting().set(new ArrayList<Integer>(this.getTcpPortsToIgnoreWhenWaiting().get()));
        r.getWaitForHealthyStateTimeout().set(getWaitAfterTcpProbeFailure().get());
        r.getWaitForTcpPortsTimeout().set(getWaitForTcpPortsTimeout().get());
        r.getWaitForTcpPortsDisconnectionProbeTimeout().set(getWaitForTcpPortsDisconnectionProbeTimeout().get());
        r.getWaitAfterHealthyStateProbeFailure().set(getWaitAfterHealthyStateProbeFailure().get());
        r.getWaitForHealthyStateTimeout().set(getWaitForHealthyStateTimeout().get());
        r.getCheckContainersRunning().set(getCheckContainersRunning().get());

        r.getCaptureContainersOutput().set(getCaptureContainersOutput().get());

        r.setProjectNamePrefix(this.getProjectNamePrefix());

        r.getExecutable().set(this.getExecutable().get());
        r.getDockerExecutable().set(this.getDockerExecutable().get());
        r.getEnvironment().set(new HashMap<String, Object>(this.getEnvironment().get()));

        r.getDockerComposeWorkingDirectory().set(this.getDockerComposeWorkingDirectory().getOrNull());
        r.getDockerComposeStopTimeout().set(this.getDockerComposeStopTimeout().get());
        return r;
    }

    @Override
    public void fromBuildImage(String environmentVariable,
            TaskProvider<? extends DockerBuildImage> buildImageTaskProvider) {
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

    @Override
    public void fromProject(String environmentVariable, Project project) {
        dockerComposeConvention.fromProject(environmentVariable, project);
    }

    @Override
    public void fromProject(String environmentVariable, String projectName) {
        dockerComposeConvention.fromProject(environmentVariable, projectName);
    }

}
