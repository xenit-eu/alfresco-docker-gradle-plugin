package eu.xenit.gradle.testrunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Created by thijs on 3/2/17.
 */
@RunWith(Parameterized.class)
public abstract class AbstractIntegrationTest {

    @Parameter(0)
    public String gradleVersion;

    @Parameters(name = "Gradle v{0}")
    public static Collection<Object[]> testData() {
        String forceGradleVersion = System.getProperty("eu.xenit.gradle.integration.useGradleVersion");
        if (forceGradleVersion != null) {
            return Arrays.asList(new Object[][]{
                    {forceGradleVersion},
            });
        }
        return Arrays.asList(new Object[][]{
                {"5.6.4"},
                {"5.5.1"},
                {"5.4.1"},
                {"5.3.1"},
                {"5.2.1"},
        });
    }


    protected void testProjectFolderThatShouldFail(Path projectFolder, String task) throws IOException {
        testProjectFolder(projectFolder, task, true);
    }

    protected void testProjectFolder(Path projectFolder, String task) throws IOException {
        testProjectFolder(projectFolder, task, false);
    }

    protected void testProjectFolder(Path projectFolder, String task, boolean expectsException) throws IOException {
        if (expectsException) {
            testProjectFolderExpectFailure(projectFolder, task, null);
        } else {
            testProjectFolderExpectSucces(projectFolder, task, null);
        }
    }

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    protected void testProjectFolderExpectSucces(Path projectFolder, String task, String message) throws IOException {

        BuildResult buildResult = getGradleRunner(projectFolder, task).build();
        assertEquals(TaskOutcome.SUCCESS, buildResult.task(task).getOutcome());
        if (message != null) {
            assertTrue(buildResult.getOutput().contains(message));
        }
    }

    protected void testProjectFolderExpectFailure(Path projectFolder, String task, String message) throws IOException {
        BuildResult buildResult = getGradleRunner(projectFolder, task).buildAndFail();
        if (message != null) {
            assertTrue(buildResult.getOutput().contains(message));
        }

    }

    protected GradleRunner getGradleRunner(Path projectFolder, String task) throws IOException {
        File tempExample = temporaryFolder.newFolder(projectFolder.getFileName().toString());
        FileUtils.copyDirectory(projectFolder.toFile(), tempExample);
        File gitDir = tempExample.toPath().resolve("_git").toFile();
        if (gitDir.exists()) {
            FileUtils.moveDirectory(gitDir, tempExample.toPath().resolve(".git").toFile());
        }

        GradleRunner runner =  GradleRunner.create()
                .withProjectDir(tempExample)
                .withArguments(task, "--stacktrace", "--rerun-tasks", "--info")
                .withPluginClasspath()
                .forwardOutput();

        if(System.getProperty("eu.xenit.gradle.integration.useGradleVersion") == null) {
            return runner.withGradleVersion(gradleVersion);
        }

        return runner;
    }

    protected void testProjectFolder(Path projectFolder) throws IOException {
        testProjectFolder(projectFolder, ":buildDockerImage");
    }
}
