package eu.xenit.gradle.testrunner;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by thijs on 1/25/17.
 */
public class ExampleRunner extends AbstractIntegrationTest {

    private static Path EXAMPLES = Paths.get("src", "integrationTest", "examples");

    @Test
    public void testAlfrescoWarOnly() throws IOException {
        testProjectFolder(EXAMPLES.resolve("alfresco-war-only"));
    }

    @Test
    public void testApplyAmpsExample() throws IOException {
        testProjectFolder(EXAMPLES.resolve("applyamps-example"));
    }

    @Test
    public void testExampleDockerPlugin() throws IOException {
        testProjectFolder(EXAMPLES.resolve("example-docker-plugin"));
    }

    @Test
    public void testComposeUp() throws IOException {
        testProjectFolder(EXAMPLES.resolve("example-docker-plugin"), ":composeUp");
    }


    @Test
    public void testFileDependencies() throws IOException {
        testProjectFolder(EXAMPLES.resolve("file-dependencies"));
    }

    @Test
    public void applyDeExample() throws IOException {
        testProjectFolder(EXAMPLES.resolve("apply-de-example"));
    }

    @Test
    public void applySmExample() throws IOException {
        testProjectFolder(EXAMPLES.resolve("apply-sm-example"));
    }

    @Test
    public void withoutPluginExample() throws IOException {
        testProjectFolder(EXAMPLES.resolve("without-plugin-example"), ":testbuildDockerImage");
    }

    @Test
    public void dependentSubProjectsExample() throws IOException {
        testProjectFolder(EXAMPLES.resolve("dependent-subprojects"), ":dependent:buildDockerImage");
    }

    @Test
    public void buildArgsExample() throws IOException {
        testProjectFolder(EXAMPLES.resolve("example-buildargs"));
    }

    @Test
    public void leanExample() throws IOException {
        testProjectFolder(EXAMPLES.resolve("lean-example"));
    }

    @Test
    public void publishWar() throws IOException {
        testProjectFolder(EXAMPLES.resolve("publish-war"), ":publishToMavenLocal");
    }

}
