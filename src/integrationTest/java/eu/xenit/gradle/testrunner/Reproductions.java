package eu.xenit.gradle.testrunner;

import static org.junit.Assert.assertTrue;

import eu.xenit.gradle.alfresco.tasks.DockerfileWithWarsTask;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.gradle.testkit.runner.BuildResult;
import org.junit.Test;

/**
 * Created by thijs on 1/25/17.
 */
public class Reproductions extends AbstractIntegrationTest {

    private static Path REPRODUCTIONS = Paths.get("src", "integrationTest", "reproductions");

    @Test
    public void testalfrescDeDoubleTask() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("alfrescoDE-double-task"), ":createDockerFile");
    }

    @Test
    public void testTagging() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("tagfailing"), ":buildDockerImage");
    }


    @Test
    public void testGitCommitWithQuoteTag() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("gitCommitWithQuoteTag"), ":buildDockerImage");
    }

    @Test
    public void testModifyDockerfile() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("modifyDockerfile"), ":createDockerFile");
    }

    @Test
    public void testGitWithoutCommits() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("git-without-commits"), ":buildDockerImage");
    }

    @Test
    public void testApplyAmpsSubproject() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("applyamps-subproject"), ":applyAlfrescoAmp");
    }

    @Test
    public void testVersionPropertiesNotOverwritten() throws IOException {
        testProjectFolderThatShouldFail(
                REPRODUCTIONS.resolve("different-version-properties-between-war-and-image-should-fail"),
                ":buildDockerImage");
    }

    @Test
    public void testVersionPropertiesOverwritten() throws IOException {
        testProjectFolder(
                REPRODUCTIONS.resolve("same-version-properties-between-war-and-image-should-succeed"),
                ":buildDockerImage");
    }

    @Test
    public void testDockerPluginWithoutConfiguration() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("docker-plugin-without-config"), ":buildDockerImage");
    }

    @Test
    public void testDockerAlfrescoPluginWithoutConfiguration() throws IOException {
        BuildResult buildResult = getGradleRunner(REPRODUCTIONS.resolve("docker-alfresco-plugin-without-config"), ":buildDockerImage").buildAndFail();
        assertTrue(buildResult.getOutput().contains(DockerfileWithWarsTask.MESSAGE_BASE_IMAGE_NOT_SET) || buildResult
                .getOutput().contains("No value has been specified for property 'baseImage'"));
    }

    @Test
    public void testAlfrescoAmpDependencyOrder() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("alfresco-amp-dependency-order"), ":applyAlfrescoAmp");

    }
}
