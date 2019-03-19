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
        return Arrays.asList(new Object[][]{
                {"5.1.1"},
                {"5.0"},
                {"4.10.3"},
                {"4.9"},
                {"4.8.1"},
                {"4.7"},
                {"4.6"},
                {"4.5.1"},
                {"4.4.1"},
                {"4.3.1"},
                {"4.2"},
                {"4.1"},
                {"4.0.2"},
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

        return GradleRunner.create()
                .withProjectDir(tempExample)
                .withArguments(task, "--stacktrace", "--rerun-tasks")
                .withGradleVersion(gradleVersion)
                .withPluginClasspath()
                .withDebug(true)
                .forwardOutput();
    }

    protected void testProjectFolder(Path projectFolder) throws IOException {
        testProjectFolder(projectFolder, ":buildDockerImage");
    }
}
