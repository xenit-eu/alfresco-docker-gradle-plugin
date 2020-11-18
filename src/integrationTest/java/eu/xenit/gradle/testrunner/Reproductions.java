package eu.xenit.gradle.testrunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import eu.xenit.gradle.docker.alfresco.tasks.extension.internal.DockerfileWithWarsExtensionImpl;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.TaskOutcome;
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
        BuildResult buildResult = getGradleRunner(REPRODUCTIONS.resolve("docker-alfresco-plugin-without-config"),
                ":buildDockerImage").buildAndFail();
        assertTrue(buildResult.getOutput().contains(DockerfileWithWarsExtensionImpl.MESSAGE_BASE_IMAGE_NOT_SET));
    }

    @Test
    public void testAlfrescoAmpDependencyOrder() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("alfresco-amp-dependency-order"), ":applyAlfrescoAmp");

    }

    @Test
    public void testConfigureRegistryCredentials() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("configure-registry-credentials"), ":buildDockerImage");

    }

    @Test
    public void testIssue98() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("issue-98"), ":functionalTest");
    }

    @Test
    public void testIssue97() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("issue-97"), ":functionalTest");
    }

    @Test
    public void testIssue96() throws IOException {
        final String folder = "issue-96";
        final String task = ":buildDockerImage";

        testProjectFolder(REPRODUCTIONS.resolve(folder), ":buildDockerImage");

        BuildResult buildResult = getGradleRunner(REPRODUCTIONS.resolve(folder), task, "--info")
                .build();
        assertEquals("buildDockerImage should be UP-TO-DATE",
                TaskOutcome.UP_TO_DATE, Objects.requireNonNull(buildResult.task(task)).getOutcome());
    }

    @Test
    public void testIssue104() throws IOException {
        final String folder = "issue-104";
        final String task = ":functionalTest";

        testProjectFolder(REPRODUCTIONS.resolve(folder), task);

        // Check if second time it does complete succesfully too
        getGradleRunner(REPRODUCTIONS.resolve(folder), task, "--info").build();
    }

    @Test
    public void testIssue107() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("issue-107"), ":functionalTest");
    }

    @Test
    public void testIssue50() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("issue-50"), ":alfrescoWar");
    }

    @Test
    public void testIssue114() throws IOException {
        BuildResult result = getGradleRunner(REPRODUCTIONS.resolve("issue-114"), ":pushDockerImage").buildAndFail();

        BuildTask pushDockerImage = result.task(":pushDockerImage");
        assertNotNull(pushDockerImage);
        assertEquals(TaskOutcome.FAILED, pushDockerImage.getOutcome());
    }

    @Test
    public void testIssue133Extension() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("issue-133-extension"), ":buildDockerImage", true);
    }

    @Test
    public void testIssue133Label() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("issue-133-label"), ":buildDockerImage", true);
    }

    @Test
    public void testIssue173Empty() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("issue-173-empty"), ":buildDockerImage");
    }

    @Test
    public void testIssue173Array() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("issue-173-array"), ":buildDockerImage");
    }

    @Test
    public void testRemoveEmptyCommands() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("remove-empty-commands"), ":createDockerFile");
    }

    @Test
    public void testIssue176Mitigation() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("issue-176-mitigation"), ":createDockerFile");
    }
}
