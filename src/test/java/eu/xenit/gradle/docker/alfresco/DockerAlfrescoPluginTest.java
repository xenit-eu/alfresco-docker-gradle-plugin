package eu.xenit.gradle.docker.alfresco;

import com.bmuschko.gradle.docker.tasks.image.DockerPushImage;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import eu.xenit.gradle.docker.alfresco.tasks.extension.DockerfileWithWarsExtension;
import eu.xenit.gradle.docker.internal.JenkinsUtil;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.Objects;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Created by thijs on 9/21/16.
 */
public class DockerAlfrescoPluginTest {

    private ProjectInternal getDefaultProject() {
        // Cast to ProjectInternal is required in tests to manually trigger .evaluate()
        ProjectInternal project = (ProjectInternal) ProjectBuilder.builder().build();
        project.getPluginManager().apply(DockerAlfrescoPlugin.class);
        return project;
    }

    @Rule
    public final TemporaryFolder testProjectFolder = new TemporaryFolder();

    @Test
    public void testApplyAlfrescoAmps() {
        testApplyAmps("Alfresco");
    }

    @Test
    public void testApplyShareAmps() {
        testApplyAmps("Share");
    }

    @Test
    public void testAddTagsAutomatic() {
        ProjectInternal project = getDefaultProject();
        project.getDependencies().add("baseAlfrescoWar",
                project.files(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test123.war")).getFile()));
        project.getDependencies().add("alfrescoAmp",
                project.files(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test123.amp")).getFile()));

        var dockerAlfrescoExtension = (DockerAlfrescoExtension) project.getExtensions()
                .getByName("dockerAlfresco");

        dockerAlfrescoExtension.dockerBuild(dockerBuildExtension -> {
            dockerBuildExtension.getRepository().set("docker.io/xenit/docker-gradle-plugin-test");
            dockerBuildExtension.getTags().set(Set.of("hello", "world"));
            dockerBuildExtension.getAutomaticTags().set(true);
        });

        project.evaluate();
        checkTaskExists(project, "pushDockerImage");

        Set<String> images = project.getTasks()
                .withType(DockerPushImage.class)
                .getByName("pushDockerImage")
                .getImages()
                .get();

        if (!"master".equals(JenkinsUtil.getBranch())) {
            assertEquals(Set.of(
                    "docker.io/xenit/docker-gradle-plugin-test:" + JenkinsUtil.getBranch(),
                    "docker.io/xenit/docker-gradle-plugin-test:" + JenkinsUtil.getBranch() + "-hello",
                    "docker.io/xenit/docker-gradle-plugin-test:" + JenkinsUtil.getBranch() + "-world"
            ), images);
        } else {
            assertEquals(Set.of(
                    "docker.io/xenit/docker-gradle-plugin-test:hello",
                    "docker.io/xenit/docker-gradle-plugin-test:world"
            ), images);
        }
    }

    @Test
    public void testAddTags() {
        ProjectInternal project = getDefaultProject();
        project.getDependencies().add("baseAlfrescoWar",
                project.files(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test123.war")).getFile()));
        project.getDependencies().add("alfrescoAmp",
                project.files(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test123.amp")).getFile()));

        var dockerAlfrescoExtension = (DockerAlfrescoExtension) project.getExtensions()
                .getByName("dockerAlfresco");

        dockerAlfrescoExtension.dockerBuild(dockerBuildExtension -> {
            dockerBuildExtension.getRepository().set("docker.io/xenit/docker-gradle-plugin-test");
            dockerBuildExtension.getTags().set(Set.of("hello", "world"));
        });

        project.evaluate();
        checkTaskExists(project, "pushDockerImage");

        Set<String> images = project.getTasks()
                .withType(DockerPushImage.class)
                .getByName("pushDockerImage")
                .getImages()
                .get();

        assertEquals(Set.of(
                "docker.io/xenit/docker-gradle-plugin-test:hello",
                "docker.io/xenit/docker-gradle-plugin-test:world"
        ), images);
    }

    @Test
    public void testLeanImage() {
        ProjectInternal project = getDefaultProject();
        project.getDependencies().add("baseAlfrescoWar",
                project.files(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test123.war")).getFile()));
        project.getDependencies().add("alfrescoAmp",
                project.files(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test123.amp")).getFile()));

        var dockerAlfrescoExtension = (DockerAlfrescoExtension) project.getExtensions()
                .getByName("dockerAlfresco");
        dockerAlfrescoExtension.getLeanImage().set(true);

        project.evaluate();

        var dockerfileWithWarsTask = (Dockerfile) project.getTasks().getByName("createDockerFile");
        var dockerfileWithWarsExtension = DockerfileWithWarsExtension.get(dockerfileWithWarsTask);

        assertFalse("webapps/${war} folders should not be removed",
                dockerfileWithWarsExtension.getRemoveExistingWar().get());
    }

    @Test
    public void testLeanImage_removeExistingWarExplicitlyOverwritten() {
        ProjectInternal project = getDefaultProject();
        project.getDependencies().add("baseAlfrescoWar",
                project.files(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test123.war")).getFile()));
        project.getDependencies().add("alfrescoAmp",
                project.files(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test123.amp")).getFile()));

        var dockerAlfrescoExtension = (DockerAlfrescoExtension) project.getExtensions()
                .getByName("dockerAlfresco");
        var dockerfileWithWarsTask = (Dockerfile) project.getTasks().getByName("createDockerFile");

        dockerAlfrescoExtension.getLeanImage().set(false);
        var dockerfileWithWarsExtension = DockerfileWithWarsExtension.get(dockerfileWithWarsTask);
        dockerfileWithWarsExtension.getRemoveExistingWar().set(false);

        project.evaluate();

        assertFalse("webapps/${war} folders should not be removed",
                dockerfileWithWarsExtension.getRemoveExistingWar().get());
    }

    @Test
    public void testApplySm() {
        ProjectInternal project = getDefaultProject();
        project.getDependencies().add("baseAlfrescoWar",
                project.files(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test123.war")).getFile()));
        project.getDependencies().add("baseShareWar",
                project.files(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test123.war")).getFile()));
        project.getDependencies().add("alfrescoSM",
                project.files(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test123.jar")).getFile()));
        project.getDependencies().add("shareSM",
                project.files(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test123.jar")).getFile()));

        project.evaluate();

        checkTaskExists(project, "applyAlfrescoSM");
        checkTaskExists(project, "applyShareSM");
    }

    @Test
    public void testLazyResolve() {
        ProjectInternal project = getDefaultProject();
        project.getDependencies().add("baseAlfrescoWar",
                project.files(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test123.war")).getFile()));
        project.getDependencies().add("baseShareWar",
                project.files(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test123.war")).getFile()));

        project.evaluate();

        Configuration alfrescoConfiguration = project.getConfigurations().getByName("baseAlfrescoWar");
        assertEquals(Configuration.State.UNRESOLVED, alfrescoConfiguration.getState());

        Configuration shareConfiguration = project.getConfigurations().getByName("baseShareWar");
        assertEquals(Configuration.State.UNRESOLVED, shareConfiguration.getState());
    }

    private void testApplyAmps(String warName) {
        ProjectInternal project = getDefaultProject();
        project.getDependencies().add("base" + warName + "War",
                project.files(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test123.war")).getFile()));
        project.getDependencies().add(warName.toLowerCase() + "Amp",
                project.files(Objects.requireNonNull(this.getClass().getClassLoader().getResource("test123.amp")).getFile()));

        project.evaluate();

        checkTaskExists(project, "apply" + warName + "Amp");
    }

    private void checkTaskExists(Project project, String taskName) {
        try {
            project.getTasks().getByName(taskName);
        } catch (UnknownTaskException e) {
            fail("Task " + taskName + " not found");
        }
    }

    private void checkTaskNotExists(Project project, String taskName) {
        try {
            project.getTasks().getByName(taskName);
            fail("There should be no " + taskName + " task");
        } catch (UnknownTaskException e) {
            assert true;
        }
    }


}
