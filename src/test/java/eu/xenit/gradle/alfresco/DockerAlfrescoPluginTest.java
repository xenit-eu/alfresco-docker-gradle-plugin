package eu.xenit.gradle.alfresco;

import static org.junit.Assert.assertFalse;

import eu.xenit.gradle.JenkinsUtil;
import eu.xenit.gradle.tasks.DockerfileWithWarsTask;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.internal.project.DefaultProject;
import org.gradle.internal.impldep.org.junit.Assert;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
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



    @Rule public final TemporaryFolder testProjectFolder = new TemporaryFolder();
    private File buildFile;

    @Before
    public void setup() throws IOException {
        buildFile = testProjectFolder.newFile("build.gradle");
    }


    @Test
    public void testLog4jRegex(){
        String base = "share";
        String log4j = "log4j.appender.Console.layout.ConversionPattern=%d{ISO8601} %x %-5p [%c{3}] [%t] %m%n ";
        log4j = log4j.replaceAll("log4j\\.appender\\.Console\\.layout\\.ConversionPattern=\\%d\\{ISO8601\\}", "log4j\\.appender\\.Console\\.layout\\.ConversionPattern=\\[" + base.toUpperCase() + "\\]%%d\\ \\{ISO8601\\}");
        System.out.println(log4j);
    }

    private void writeFile(File destination, String content) throws IOException {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(destination));
            output.write(content);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    @Test
    public void testApplyAlfrescoAmps(){
        testApplyAmps("Alfresco");
    }

    @Test
    public void testApplyShareAmps(){
        testApplyAmps("Share");
    }

    @Test
    public void testAddTags(){
        DefaultProject project = getDefaultProject();
        project.getDependencies().add("baseAlfrescoWar", project.files(this.getClass().getClassLoader().getResource("test123.war").getFile()));
        project.getDependencies().add("alfrescoAmp", project.files(this.getClass().getClassLoader().getResource("test123.amp").getFile()));
        DockerAlfrescoExtension dockerAlfrescoExtension = (DockerAlfrescoExtension) project.getExtensions().getByName("dockerAlfresco");
        dockerAlfrescoExtension.dockerBuild((dockerBuildExtension -> {
            dockerBuildExtension.setTags(Arrays.asList("hello", "world"));
        }));
        project.evaluate();

        if(!"master".equals(JenkinsUtil.getBranch())){
            checkTaskExists(project, "pushTag"+JenkinsUtil.getBranch()+"-hello");
            checkTaskExists(project, "pushTag"+JenkinsUtil.getBranch()+"-world");
        } else {
            checkTaskExists(project, "pushTaghello");
            checkTaskExists(project, "pushTagworld");
        }

    }

    @Test
    public void testAddTagsPlain(){
        DefaultProject project = getDefaultProject();
        project.getDependencies().add("baseAlfrescoWar", project.files(this.getClass().getClassLoader().getResource("test123.war").getFile()));
        project.getDependencies().add("alfrescoAmp", project.files(this.getClass().getClassLoader().getResource("test123.amp").getFile()));
        DockerAlfrescoExtension dockerAlfrescoExtension = (DockerAlfrescoExtension) project.getExtensions().getByName("dockerAlfresco");
        dockerAlfrescoExtension.dockerBuild((dockerBuildExtension) -> {
            dockerBuildExtension.setTags(Arrays.asList("hello", "world"));
            dockerBuildExtension.setAutomaticTags(false);
        });
        project.evaluate();

        checkTaskExists(project, "pushTaghello");
        checkTaskExists(project, "pushTagworld");
        checkTaskNotExists(project, "pushTaglatest");
    }

    @Test
    public void testLeanImage(){
        DefaultProject project = getDefaultProject();
        project.getDependencies().add("baseAlfrescoWar", project.files(this.getClass().getClassLoader().getResource("test123.war").getFile()));
        project.getDependencies().add("alfrescoAmp", project.files(this.getClass().getClassLoader().getResource("test123.amp").getFile()));
        DockerAlfrescoExtension dockerAlfrescoExtension = (DockerAlfrescoExtension) project.getExtensions().getByName("dockerAlfresco");
        dockerAlfrescoExtension.setLeanImage(true);
        project.evaluate();

        DockerfileWithWarsTask dockerfileWithWarsTask = (DockerfileWithWarsTask) project.getTasks().getAt("createDockerFile");
        assertFalse("webapps/${war} folders should not be removed", dockerfileWithWarsTask.getRemoveExistingWar());
    }

    @Test
    public void testApplySm(){
        DefaultProject project = getDefaultProject();
        project.getDependencies().add("baseAlfrescoWar", project.files(this.getClass().getClassLoader().getResource("test123.war").getFile()));
        project.getDependencies().add("baseShareWar", project.files(this.getClass().getClassLoader().getResource("test123.war").getFile()));
        project.getDependencies().add("alfrescoSM", project.files(this.getClass().getClassLoader().getResource("test123.jar").getFile()));
        project.getDependencies().add("shareSM", project.files(this.getClass().getClassLoader().getResource("test123.jar").getFile()));
        project.evaluate();
        checkTaskExists(project, "applyAlfrescoSM");
        checkTaskExists(project, "applyShareSM");
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

    private void testApplyAmps(String warName){
        DefaultProject project = getDefaultProject();
        project.getDependencies().add("base"+warName+"War", project.files(this.getClass().getClassLoader().getResource("test123.war").getFile()));
        project.getDependencies().add(warName.toLowerCase()+"Amp", project.files(this.getClass().getClassLoader().getResource("test123.amp").getFile()));
        project.evaluate();

        checkTaskExists(project, "apply"+warName+"Amp");
    }

    private void checkTaskExists(DefaultProject project, String taskName) {
        try {
            project.getTasks().getAt(taskName);
        } catch (UnknownTaskException e){
            Assert.fail("Task "+taskName+" not found");
        }
    }

    private void checkTaskNotExists(DefaultProject project, String taskName) {
        try {
            project.getTasks().getAt(taskName);
            Assert.fail("There should be no "+taskName+" task");
        } catch (UnknownTaskException e){
            assert true;
        }
    }


}
