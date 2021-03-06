package eu.xenit.gradle.docker.alfresco;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.bmuschko.gradle.docker.tasks.image.DockerPushImage;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import eu.xenit.gradle.docker.alfresco.tasks.extension.DockerfileWithWarsExtension;
import eu.xenit.gradle.docker.internal.JenkinsUtil;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal.InternalState;
import org.gradle.api.internal.artifacts.configurations.DefaultConfiguration;
import org.gradle.api.internal.project.DefaultProject;
import org.gradle.internal.impldep.org.junit.Assert;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Created by thijs on 9/21/16.
 */
public class DockerAlfrescoPluginTest {

    private DefaultProject getDefaultProject() {
        DefaultProject project = (DefaultProject) ProjectBuilder.builder().build();
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
        DefaultProject project = getDefaultProject();
        project.getDependencies().add("baseAlfrescoWar",
                project.files(this.getClass().getClassLoader().getResource("test123.war").getFile()));
        project.getDependencies().add("alfrescoAmp",
                project.files(this.getClass().getClassLoader().getResource("test123.amp").getFile()));
        DockerAlfrescoExtension dockerAlfrescoExtension = (DockerAlfrescoExtension) project.getExtensions()
                .getByName("dockerAlfresco");
        dockerAlfrescoExtension.dockerBuild((dockerBuildExtension -> {
            dockerBuildExtension.getRepository().set("docker.io/xenit/docker-gradle-plugin-test");
            dockerBuildExtension.getTags().set(Arrays.asList("hello", "world"));
            dockerBuildExtension.getAutomaticTags().set(true);
        }));
        project.evaluate();
        checkTaskExists(project, "pushDockerImage");

        Set<String> images = project.getTasks().withType(DockerPushImage.class).getByName("pushDockerImage").getImages()
                .get();

        if (!"master".equals(JenkinsUtil.getBranch())) {
            assertEquals(new HashSet(
                            Arrays.asList("docker.io/xenit/docker-gradle-plugin-test:" + JenkinsUtil.getBranch(),
                                    "docker.io/xenit/docker-gradle-plugin-test:" + JenkinsUtil.getBranch() + "-hello",
                                    "docker.io/xenit/docker-gradle-plugin-test:" + JenkinsUtil.getBranch() + "-world")),
                    images);
        } else {
            assertEquals(new HashSet(Arrays.asList("docker.io/xenit/docker-gradle-plugin-test:hello",
                    "docker.io/xenit/docker-gradle-plugin-test:world")), images);
        }

    }

    @Test
    public void testAddTags() {
        DefaultProject project = getDefaultProject();
        project.getDependencies().add("baseAlfrescoWar",
                project.files(this.getClass().getClassLoader().getResource("test123.war").getFile()));
        project.getDependencies().add("alfrescoAmp",
                project.files(this.getClass().getClassLoader().getResource("test123.amp").getFile()));
        DockerAlfrescoExtension dockerAlfrescoExtension = (DockerAlfrescoExtension) project.getExtensions()
                .getByName("dockerAlfresco");
        dockerAlfrescoExtension.dockerBuild((dockerBuildExtension) -> {
            dockerBuildExtension.getRepository().set("docker.io/xenit/docker-gradle-plugin-test");
            dockerBuildExtension.getTags().set(Arrays.asList("hello", "world"));
        });
        project.evaluate();
        checkTaskExists(project, "pushDockerImage");

        Set<String> images = project.getTasks().withType(DockerPushImage.class).getByName("pushDockerImage").getImages()
                .get();

        assertEquals(new HashSet(Arrays.asList("docker.io/xenit/docker-gradle-plugin-test:hello",
                "docker.io/xenit/docker-gradle-plugin-test:world")), images);
    }

    @Test
    public void testLeanImage() {
        DefaultProject project = getDefaultProject();
        project.getDependencies().add("baseAlfrescoWar",
                project.files(this.getClass().getClassLoader().getResource("test123.war").getFile()));
        project.getDependencies().add("alfrescoAmp",
                project.files(this.getClass().getClassLoader().getResource("test123.amp").getFile()));
        DockerAlfrescoExtension dockerAlfrescoExtension = (DockerAlfrescoExtension) project.getExtensions()
                .getByName("dockerAlfresco");
        dockerAlfrescoExtension.getLeanImage().set(true);
        project.evaluate();

        Dockerfile dockerfileWithWarsTask = (Dockerfile) project.getTasks()
                .getAt("createDockerFile");
        DockerfileWithWarsExtension dockerfileWithWarsExtension = DockerfileWithWarsExtension
                .get(dockerfileWithWarsTask);
        assertFalse("webapps/${war} folders should not be removed",
                dockerfileWithWarsExtension.getRemoveExistingWar().get());
    }

    @Test
    public void testLeanImage_removeExistingWarExplicitlyOverwritten() {
        DefaultProject project = getDefaultProject();
        project.getDependencies().add("baseAlfrescoWar",
                project.files(this.getClass().getClassLoader().getResource("test123.war").getFile()));
        project.getDependencies().add("alfrescoAmp",
                project.files(this.getClass().getClassLoader().getResource("test123.amp").getFile()));
        DockerAlfrescoExtension dockerAlfrescoExtension = (DockerAlfrescoExtension) project.getExtensions()
                .getByName("dockerAlfresco");
        Dockerfile dockerfileWithWarsTask = (Dockerfile) project.getTasks()
                .getByName("createDockerFile");
        dockerAlfrescoExtension.getLeanImage().set(false);
        DockerfileWithWarsExtension dockerfileWithWarsExtension = DockerfileWithWarsExtension
                .get(dockerfileWithWarsTask);
        dockerfileWithWarsExtension.getRemoveExistingWar().set(false);

        project.evaluate();

        assertFalse("webapps/${war} folders should not be removed",
                dockerfileWithWarsExtension.getRemoveExistingWar().get());
    }

    @Test
    public void testApplySm() {
        DefaultProject project = getDefaultProject();
        project.getDependencies().add("baseAlfrescoWar",
                project.files(this.getClass().getClassLoader().getResource("test123.war").getFile()));
        project.getDependencies().add("baseShareWar",
                project.files(this.getClass().getClassLoader().getResource("test123.war").getFile()));
        project.getDependencies().add("alfrescoSM",
                project.files(this.getClass().getClassLoader().getResource("test123.jar").getFile()));
        project.getDependencies()
                .add("shareSM", project.files(this.getClass().getClassLoader().getResource("test123.jar").getFile()));
        project.evaluate();
        checkTaskExists(project, "applyAlfrescoSM");
        checkTaskExists(project, "applyShareSM");
    }

    @Test
    public void testLazyResolve() {
        DefaultProject project = getDefaultProject();
        project.getDependencies().add("baseAlfrescoWar",
                project.files(this.getClass().getClassLoader().getResource("test123.war").getFile()));
        project.getDependencies().add("baseShareWar",
                project.files(this.getClass().getClassLoader().getResource("test123.war").getFile()));

        project.evaluate();

        DefaultConfiguration alfrescoConfiguration = (DefaultConfiguration) project.getConfigurations()
                .getByName("baseAlfrescoWar");
        assertEquals(InternalState.UNRESOLVED, alfrescoConfiguration.getResolvedState());
        DefaultConfiguration shareConfiguration = (DefaultConfiguration) project.getConfigurations()
                .getByName("baseShareWar");
        assertEquals(InternalState.UNRESOLVED, shareConfiguration.getResolvedState());
    }

//    @Test
//    public void testBuildNumberTag(){
//        DefaultProject project = getDefaultProject();
//        project.getDependencies().add("baseAlfrescoWar", project.files(this.getClass().getClassLoader().getResource("test123.war").getFile()));
//        //setEnvironmentVariable("BUILD_NUMBER", "987");
//        System.setProperty("BUILD_NUMBER", "987");
//        project.evaluate();
//        checkTaskExists(project, "getImagebuild-987");
//    }

    private void testApplyAmps(String warName) {
        DefaultProject project = getDefaultProject();
        project.getDependencies().add("base" + warName + "War",
                project.files(this.getClass().getClassLoader().getResource("test123.war").getFile()));
        project.getDependencies().add(warName.toLowerCase() + "Amp",
                project.files(this.getClass().getClassLoader().getResource("test123.amp").getFile()));
        project.evaluate();

        checkTaskExists(project, "apply" + warName + "Amp");
    }

    private void checkTaskExists(DefaultProject project, String taskName) {
        try {
            project.getTasks().getAt(taskName);
        } catch (UnknownTaskException e) {
            Assert.fail("Task " + taskName + " not found");
        }
    }

    private void checkTaskNotExists(DefaultProject project, String taskName) {
        try {
            project.getTasks().getAt(taskName);
            Assert.fail("There should be no " + taskName + " task");
        } catch (UnknownTaskException e) {
            assert true;
        }
    }


}
