package eu.xenit.gradle.docker.compose;

import static org.junit.Assert.*;

import org.junit.Test;

public class UtilTest {

    @Test
    public void safeEnvironmentVariableName() {
        assertEquals("PROJECT_TASK_NAME", Util.safeEnvironmentVariableName("project:taskName"));
        assertEquals("PROJECT_SUBPROJECT_BUILD_DOCKER_IMAGE", Util.safeEnvironmentVariableName("project:subproject:buildDockerImage"));
        assertEquals("PROJECT_SUBPROJECT_1_BUILD_DOCKER_IMAGE", Util.safeEnvironmentVariableName("project:subproject-1:buildDockerImage"));
    }
}
