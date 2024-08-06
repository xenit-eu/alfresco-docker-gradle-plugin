package eu.xenit.gradle.testrunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
                {"8.9"},
                {"8.0.1"},
                {"7.6.4"},
                {"7.0.2"},
                {"6.9.3"},
                {"6.2.2"},
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
        return getGradleRunner(projectFolder, task, "--rerun-tasks");
    }

    protected GradleRunner getGradleRunner(Path projectFolder, String task, String... additionalArguments) throws IOException {
        File tempExample = getOrCreateTemporaryFolder(projectFolder.getFileName().toString());
        FileUtils.copyDirectory(projectFolder.toFile(), tempExample);
        File gitDir = tempExample.toPath().resolve("_git").toFile();
        if (gitDir.exists()) {
            FileUtils.moveDirectory(gitDir, tempExample.toPath().resolve(".git").toFile());
        }

        Set<String> arguments = new LinkedHashSet<>();
        arguments.add(task);
        arguments.add("--stacktrace");
        arguments.add("--info");
        arguments.addAll(Arrays.asList(additionalArguments));

        GradleRunner runner = GradleRunner.create()
                .withProjectDir(tempExample)
                .withArguments(new ArrayList<>(arguments))
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

    private File getOrCreateTemporaryFolder(final String name) throws IOException {
        Path folder = temporaryFolder.getRoot().toPath().resolve(name);
        if (Files.exists(folder)) {
            return folder.toFile();
        }
        return temporaryFolder.newFolder(name);
    }
}
