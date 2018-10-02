package eu.xenit.gradle.testrunner;

import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Created by thijs on 1/25/17.
 */
public class Reproductions extends AbstractIntegrationTest {

    private static Path REPRODUCTIONS = Paths.get("src", "integration-test", "reproductions");

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
        testProjectFolder(REPRODUCTIONS.resolve("gitCommitWithQuoteTag"), ":labelDockerFile");
    }

    @Test
    public void testModifyDockerfile() throws IOException {
        testProjectFolder(REPRODUCTIONS.resolve("modifyDockerfile"), ":createDockerFile");
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
}
